import enums.AccountType;
import enums.AccountStatus;
import enums.FixedDepositAccountType;
import enums.TransactionType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import model.account.CheckingAccount;
import model.account.FixedDepositAccount;
import model.account.SavingsAccount;
import model.bank.MonthlyReport;
import model.customer.Customer;
import model.transaction.Transaction;
import persistence.BankFileManager;
import service.GlobalData;
import util.DateTimeManager;

public class PersistenceTestHarness {
    private static int passed;
    private static int failed;
    private static final Path DATA = Path.of("data");

    private static void test(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS: " + name);
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }

    private static void deleteTree(Path path) throws IOException {
        if (!Files.exists(path)) return;
        try (var paths = Files.walk(path)) {
            for (Path item : paths.sorted((a, b) -> b.getNameCount() - a.getNameCount()).toList()) {
                Files.deleteIfExists(item);
            }
        }
    }

    private static void clearInMemoryState() {
        GlobalData.getAllCustomers().clear();
        GlobalData.getAllAccounts().clear();
        GlobalData.getAllTransactions().clear();
        GlobalData.getSavingsAccounts().clear();
        GlobalData.getCheckingAccounts().clear();
        GlobalData.getFixedDepositAccounts().clear();
        GlobalData.getMonthlyReports().clear();
        model.transaction.PausedTransaction.restorePausedList(new ArrayList<>());
        Customer.setCustomerCounter(0);
        model.account.Account.setAccountCounter(0);
        Transaction.setTransactionCounter(1);
    }

    public static void main(String[] args) throws Exception {
        deleteTree(DATA);

        DateTimeManager.setCurrentDate(LocalDate.of(2001, 1, 1));
        BankFileManager.load();
        test("first startup uses computer date", DateTimeManager.getCurrentDate().equals(LocalDate.now()));
        test("first startup creates data directory", Files.isDirectory(DATA));
        test("first startup creates state file", Files.isRegularFile(DATA.resolve("bank_state.dat")));
        test("first startup creates counters text file", Files.isRegularFile(DATA.resolve("counters.txt")));
        test("first startup creates monthly reports file", Files.isRegularFile(DATA.resolve("monthly_reports.txt")));
        test("first startup creates error log", Files.isRegularFile(DATA.resolve("errors.log")));
        test("initial counters file has expected header values",
                Files.readString(DATA.resolve("counters.txt")).contains("customerCounter=0")
                        && Files.readString(DATA.resolve("counters.txt")).contains("transactionCounter=1"));
        test("save removes temporary file after successful move", !Files.exists(DATA.resolve("bank_state.tmp")));

        LocalDate savedDate = LocalDate.of(2024, 2, 29);
        DateTimeManager.setCurrentDate(savedDate);
        Customer customer = new Customer("Persistence User", "123456789", "persist@example.com", LocalDateTime.now());
        SavingsAccount account = new SavingsAccount(customer, AccountType.SAVINGS, LocalDateTime.now(), 250.75);
        customer.addAccount(account);
        Transaction transaction = new Transaction(account, TransactionType.Deposit, 10.0, "Persistence test");
        transaction.markAsCompleted(account.getBalance());
        Transaction pausedTransaction = new Transaction(account, TransactionType.transfer_out, 7.0, "Paused persistence test");
        pausedTransaction.markAsPaused(account.getBalance(), account);
        model.transaction.PausedTransaction.add(new model.transaction.PausedTransaction(
                pausedTransaction.getTransactionId(), pausedTransaction.getTransactionType(),
                pausedTransaction.getTransactionTime(), pausedTransaction.getAmount(),
                pausedTransaction.getTransactionStatus(), account, pausedTransaction));
        new MonthlyReport(1, 1, 2, 10.0, 0.0, 0.0, 0.0, customer);
        int savedCustomerCounter = Customer.getCustomerCounter();
        int savedAccountCounter = model.account.Account.getAccountCounter();
        int savedTransactionCounter = Transaction.getTransactionCounter();
        BankFileManager.save();
        test("save writes counters text", Files.readString(DATA.resolve("counters.txt")).contains("customerCounter=" + savedCustomerCounter));
        test("save writes monthly report text", Files.readString(DATA.resolve("monthly_reports.txt")).contains(YearMonth.from(savedDate).toString()));
        test("save leaves no temporary file", !Files.exists(DATA.resolve("bank_state.tmp")));

        clearInMemoryState();
        DateTimeManager.setCurrentDate(LocalDate.now());
        BankFileManager.load();
        test("load restores saved banking date", DateTimeManager.getCurrentDate().equals(savedDate));
        test("load restores customer", GlobalData.getAllCustomers().size() == 1);
        test("load restores account", GlobalData.getAllAccounts().size() == 1);
        test("load restores all transactions", GlobalData.getAllTransactions().size() == 2);
        test("load restores global paused transaction list", model.transaction.PausedTransaction.getPausedList().size() == 1);
        test("paused transaction keeps its original transaction reference", model.transaction.PausedTransaction.getPausedList().get(0).getOriginalTransaction() == GlobalData.getAllTransactions().get(1));
        test("paused transaction keeps its source account reference", model.transaction.PausedTransaction.getPausedList().get(0).getSourceAccount() == GlobalData.getAllAccounts().get(0));
        test("load restores monthly report map", GlobalData.getMonthlyReports().containsKey(YearMonth.from(savedDate)));
        Customer loadedCustomer = GlobalData.getAllCustomers().get(0);
        SavingsAccount loadedAccount = (SavingsAccount) GlobalData.getAllAccounts().get(0);
        test("serialization preserves owner-account object relationship", loadedAccount.getAccountOwner() == loadedCustomer);
        test("serialization preserves customer-account list relationship", loadedCustomer.getAccounts().contains(loadedAccount));
        test("serialization preserves account balance", Math.abs(loadedAccount.getBalance() - 250.75) < 0.000001);
        test("serialization preserves transaction description", GlobalData.getAllTransactions().get(0).getDescription().equals("Persistence test"));
        test("load restores customer counter", Customer.getCustomerCounter() == savedCustomerCounter);
        test("load restores account counter", model.account.Account.getAccountCounter() == savedAccountCounter);
        test("load restores transaction counter", Transaction.getTransactionCounter() == savedTransactionCounter);

        Files.delete(DATA.resolve("counters.txt"));
        Files.delete(DATA.resolve("monthly_reports.txt"));
        Files.delete(DATA.resolve("errors.log"));
        BankFileManager.load();
        test("load recreates missing counters file", Files.isRegularFile(DATA.resolve("counters.txt")));
        test("load recreates missing monthly reports file", Files.isRegularFile(DATA.resolve("monthly_reports.txt")));
        test("load recreates missing error log", Files.isRegularFile(DATA.resolve("errors.log")));
        test("load regenerates readable reports from saved state", Files.readString(DATA.resolve("monthly_reports.txt")).contains(YearMonth.from(savedDate).toString()));

        ((SavingsAccount) GlobalData.getAllAccounts().get(0)).deposit(5.0);
        BankFileManager.save();
        clearInMemoryState();
        BankFileManager.load();
        test("repeated save overwrites state with latest balance", Math.abs(GlobalData.getAllAccounts().get(0).getBalance() - 255.75) < 0.000001);
        test("repeated save does not duplicate customers", GlobalData.getAllCustomers().size() == 1);

        Customer multiTypeOwner = GlobalData.getAllCustomers().get(0);
        SavingsAccount linkedSavings = (SavingsAccount) GlobalData.getAllAccounts().get(0);
        CheckingAccount checking = new CheckingAccount(multiTypeOwner, AccountType.CHECKING, 100.0, LocalDateTime.now());
        multiTypeOwner.addAccount(checking);
        FixedDepositAccount fixedDeposit = new FixedDepositAccount(multiTypeOwner, AccountType.FIXED_DEPOSIT,
                500.0, 3, linkedSavings, FixedDepositAccountType.AUTO_RENEW_PRINCIPAL_AND_INTEREST, LocalDateTime.now());
        multiTypeOwner.addAccount(fixedDeposit);
        checking.setAccountStatus(AccountStatus.CLOSED);
        multiTypeOwner.moveToClosedAccounts(checking);
        test("closed account is retained in customer's all-account history before save",
                multiTypeOwner.getAllAccounts().contains(checking) && !multiTypeOwner.getAccounts().contains(checking));
        test("save supports all three account subtypes", BankFileManager.save());
        clearInMemoryState();
        BankFileManager.load();
        Customer restoredOwner = GlobalData.getAllCustomers().get(0);
        test("load restores checking account subtype", GlobalData.getAllAccounts().stream().anyMatch(a -> a instanceof CheckingAccount));
        test("load restores fixed-deposit account subtype", GlobalData.getAllAccounts().stream().anyMatch(a -> a instanceof FixedDepositAccount));
        test("load restores linked fixed-deposit account reference", ((FixedDepositAccount) GlobalData.getAllAccounts().stream()
                .filter(a -> a instanceof FixedDepositAccount).findFirst().orElseThrow()).getAccountOwner() == restoredOwner);
        test("load restores closed-account history", restoredOwner.getAllAccounts().stream()
                .anyMatch(a -> a instanceof CheckingAccount && a.getAccountStatus() == AccountStatus.CLOSED));
        test("load restores account-type indexes", GlobalData.getSavingsAccounts().size() == 1
                && GlobalData.getCheckingAccounts().size() == 1 && GlobalData.getFixedDepositAccounts().size() == 1);

        Files.writeString(DATA.resolve("bank_state.tmp"), "stale temporary data", StandardCharsets.UTF_8);
        test("save safely replaces a stale temporary file", BankFileManager.save() && !Files.exists(DATA.resolve("bank_state.tmp")));

        byte[] validSavedState = Files.readAllBytes(DATA.resolve("bank_state.dat"));
        Files.write(DATA.resolve("bank_state.tmp"), validSavedState);
        Files.delete(DATA.resolve("bank_state.dat"));
        BankFileManager.load();
        test("startup recovers a valid temporary state when the main state is missing",
                Files.isRegularFile(DATA.resolve("bank_state.dat"))
                        && !Files.exists(DATA.resolve("bank_state.tmp"))
                        && GlobalData.getAllCustomers().size() == 1);
        validSavedState = Files.readAllBytes(DATA.resolve("bank_state.dat"));
        Files.delete(DATA.resolve("bank_state.dat"));
        boolean missingStateRejected = false;
        try { BankFileManager.load(); } catch (IllegalStateException expected) { missingStateRejected = true; }
        test("missing main state is rejected when saved metadata remains", missingStateRejected);
        test("missing state does not silently create a new bank snapshot", !Files.exists(DATA.resolve("bank_state.dat")));
        test("missing state scenario is logged", Files.readString(DATA.resolve("errors.log")).contains("main bank state file is missing"));
        Files.write(DATA.resolve("bank_state.dat"), validSavedState);
        BankFileManager.load();

        Files.writeString(DATA.resolve("bank_state.dat"), "not a serialized object", StandardCharsets.UTF_8);
        boolean corruptRejected = false;
        try { BankFileManager.load(); } catch (IllegalStateException expected) { corruptRejected = true; }
        test("corrupt state file is rejected instead of silently reset", corruptRejected);
        test("corrupt state failure is logged", Files.readString(DATA.resolve("errors.log")).contains("Startup load failed"));

        Files.write(DATA.resolve("bank_state.dat"), new byte[0]);
        boolean emptyRejected = false;
        try { BankFileManager.load(); } catch (IllegalStateException expected) { emptyRejected = true; }
        test("empty state file is rejected", emptyRejected);

        try (var output = new java.io.ObjectOutputStream(Files.newOutputStream(DATA.resolve("bank_state.dat")))) {
            output.writeObject("not a Snapshot");
        }
        boolean wrongTypeRejected = false;
        try { BankFileManager.load(); } catch (IllegalStateException expected) { wrongTypeRejected = true; }
        test("valid serialized object of wrong type is rejected", wrongTypeRejected);
        test("repeated load errors append to log", Files.readString(DATA.resolve("errors.log")).lines().filter(line -> line.contains("Startup load failed")).count() >= 3);

        deleteTree(DATA);
        Files.writeString(DATA, "this path is a file, not a directory", StandardCharsets.UTF_8);
        boolean badDirectoryRejected = false;
        try { BankFileManager.load(); } catch (IllegalStateException expected) { badDirectoryRejected = true; }
        test("invalid data-folder path fails safely on load", badDirectoryRejected);
        test("save reports failure when data folder path is not a directory", !BankFileManager.save());
        Files.deleteIfExists(DATA);

        System.out.println("RESULT: passed=" + passed + " failed=" + failed);
        if (failed > 0) System.exit(1);
    }
}
