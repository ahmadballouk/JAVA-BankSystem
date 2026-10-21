package service;

import java.time.YearMonth;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.account.Account;
import model.account.CheckingAccount;
import model.account.FixedDepositAccount;
import model.account.SavingsAccount;
import model.bank.MonthlyReport;
import model.customer.Customer;
import model.transaction.Transaction;
import model.transaction.PausedTransaction;
import util.DateTimeManager;

public class GlobalData {

    private static final List<Customer> allCustomers = new ArrayList<>();
    private static final List<Account> allAccounts = new ArrayList<>();
    private static final List<Transaction> allTransactions = new ArrayList<>();
    private static final List<SavingsAccount> totalSavingsAccounts = new ArrayList<>();
    private static final List<CheckingAccount> totalCheckingAccounts = new ArrayList<>();
    private static final List<FixedDepositAccount> totalFixedDepositAccounts = new ArrayList<>();
    private static final Map<YearMonth, MonthlyReport> monthlyReports = new HashMap<>();

    private static double totalInterestPaid;
    private static double totalFeeCharged;
    private static double totalDepositsAmount;
    private static double totalWithdrawalsAmount;

    private static int monthlyCustomersAdded;
    private static int monthlyAccountsOpened;
    private static int monthlyTransactionsCount;
    private static double monthlyDepositsAmount;
    private static double monthlyWithdrawalsAmount;
    private static double monthlyFeesCharged;
    private static double monthlyInterestPaid;

    public static void addDepositMoney(double amount) {
        monthlyDepositsAmount += amount;
        totalDepositsAmount += amount;
    }

    public static void addFeeMoney(double amount) {
        monthlyFeesCharged += amount;
        totalFeeCharged += amount;
    }

    public static void addInterestMoney(double amount) {
        monthlyInterestPaid += amount;
        totalInterestPaid += amount;
    }

    public static void addWithdrawMoney(double amount) {
        monthlyWithdrawalsAmount += amount;
        totalWithdrawalsAmount += amount;
    }

    public static void recordAccountOpened() {
        monthlyAccountsOpened++;
    }

    public static void recordTransactionCreated() {
        monthlyTransactionsCount++;
    }

    public static void resetMonthlyData() {
        monthlyCustomersAdded = 0;
        monthlyAccountsOpened = 0;
        monthlyTransactionsCount = 0;
        monthlyDepositsAmount = 0;
        monthlyWithdrawalsAmount = 0;
        monthlyFeesCharged = 0;
        monthlyInterestPaid = 0;
    }

    public static int totalCheckingAccounts() {
        return totalCheckingAccounts.size();
    }

    public static int totalFixedDepositAccounts() {
        return totalFixedDepositAccounts.size();
    }

    public static int totalSavingsAccounts() {
        return totalSavingsAccounts.size();
    }

    public static void recordCustomerCreated() {
        monthlyCustomersAdded++;
    }

    public static void addCustomer(Customer customer) {
        if (customer == null || allCustomers.contains(customer)) return;
        allCustomers.add(customer);
        allCustomers.sort(Comparator.comparing(Customer::getCustomerIdString, String.CASE_INSENSITIVE_ORDER));
    }

    public static void addAccount(Account account) {
        if (account == null || allAccounts.contains(account)) return;
        allAccounts.add(account);
        allAccounts.sort(Comparator.comparing(Account::getAccountNumber, String.CASE_INSENSITIVE_ORDER));
    }

    public static void addTransaction(Transaction transaction) {
        if (transaction == null || allTransactions.contains(transaction)) return;
        allTransactions.add(transaction);
        allTransactions.sort(Comparator.comparing(Transaction::getTransactionId, String.CASE_INSENSITIVE_ORDER));
    }

    public static List<Account> getAllAccounts() {
        return allAccounts;
    }

    public static List<Customer> getAllCustomers() {
        return allCustomers;
    }

    public static List<Transaction> getAllTransactions() {
        return allTransactions;
    }

    public static List<CheckingAccount> getCheckingAccounts() {
        return totalCheckingAccounts;
    }

    public static List<FixedDepositAccount> getFixedDepositAccounts() {
        return totalFixedDepositAccounts;
    }

    public static int getMonthlyAccountsOpened() {
        return monthlyAccountsOpened;
    }

    public static int getMonthlyCustomersAdded() {
        return monthlyCustomersAdded;
    }

    public static double getMonthlyDepositsAmount() {
        return monthlyDepositsAmount;
    }

    public static double getMonthlyFeesCharged() {
        return monthlyFeesCharged;
    }

    public static double getMonthlyInterestPaid() {
        return monthlyInterestPaid;
    }

    public static Map<YearMonth, MonthlyReport> getMonthlyReports() {
        return monthlyReports;
    }

    public static int getMonthlyTransactionsCount() {
        return monthlyTransactionsCount;
    }

    public static double getMonthlyWithdrawalsAmount() {
        return monthlyWithdrawalsAmount;
    }

    public static List<SavingsAccount> getSavingsAccounts() {
        return totalSavingsAccounts;
    }

    public static int getTotalAccounts() {
        return allAccounts.size();
    }

    public static int getTotalAccountsCreated() {
        return Account.getAccountCounter();
    }

    public static int getTotalCustomersCreated() {
        return Customer.getCustomerCounter();
    }

    public static int getTotalCustomers() {
        return allCustomers.size();
    }

    public static double getTotalDepositsAmount() {
        return totalDepositsAmount;
    }

    public static double getTotalFeesCharged() {
        return totalFeeCharged;
    }

    public static double getTotalInterestPaid() {
        return totalInterestPaid;
    }

    public static int getTotalTransactions() {
        return allTransactions.size();
    }

    public static int getTotalTransactionsCreated() {
        return Math.max(0, Transaction.getTransactionCounter() - 1);
    }

    public static double getTotalWithdrawalsAmount() {
        return totalWithdrawalsAmount;
    }

    public static void showBankAssets() {
        double checkingTotal = 0.0;
        for (CheckingAccount account : totalCheckingAccounts) {
            if (account != null) {
                checkingTotal += account.getBalance();
            }
        }

        double savingsTotal = 0.0;
        for (SavingsAccount account : totalSavingsAccounts) {
            if (account != null) {
                savingsTotal += account.getBalance();
            }
        }

        double fixedDepositTotal = 0.0;
        for (FixedDepositAccount account : totalFixedDepositAccounts) {
            if (account != null) {
                fixedDepositTotal += account.getBalance();
            }
        }

        double grandTotalAssets = checkingTotal + savingsTotal + fixedDepositTotal;

        System.out.println("\n==================================================");
        System.out.println("               CURRENT BANK ASSETS                ");
        System.out.println("==================================================");
        System.out.println(" Active Customers          : " + allCustomers.size());
        System.out.println(" Active Accounts           : " + allAccounts.size());
        System.out.println("   ├─ Checking Accounts    : " + totalCheckingAccounts.size()
                + "  (Total: $" + String.format("%.2f", checkingTotal) + ")");
        System.out.println("   ├─ Savings Accounts     : " + totalSavingsAccounts.size()
                + "  (Total: $" + String.format("%.2f", savingsTotal) + ")");
        System.out.println("   └─ Fixed Deposit Accs   : " + totalFixedDepositAccounts.size()
                + "  (Total: $" + String.format("%.2f", fixedDepositTotal) + ")");
        System.out.println("--------------------------------------------------");
        System.out.println(" TOTAL ASSETS UNDER MGMT   : $" + String.format("%.2f", grandTotalAssets));
        System.out.println("==================================================\n");
    }

    public static Snapshot createSnapshot() {
        Snapshot snapshot = new Snapshot();
        snapshot.customers = new ArrayList<>(allCustomers);
        snapshot.accounts = new ArrayList<>(allAccounts);
        snapshot.transactions = new ArrayList<>(allTransactions);
        snapshot.savingsAccounts = new ArrayList<>(totalSavingsAccounts);
        snapshot.checkingAccounts = new ArrayList<>(totalCheckingAccounts);
        snapshot.fixedDepositAccounts = new ArrayList<>(totalFixedDepositAccounts);
        snapshot.monthlyReports = new HashMap<>(monthlyReports);
        snapshot.totalInterestPaid = totalInterestPaid;
        snapshot.totalFeeCharged = totalFeeCharged;
        snapshot.totalDepositsAmount = totalDepositsAmount;
        snapshot.totalWithdrawalsAmount = totalWithdrawalsAmount;
        snapshot.monthlyCustomersAdded = monthlyCustomersAdded;
        snapshot.monthlyAccountsOpened = monthlyAccountsOpened;
        snapshot.monthlyTransactionsCount = monthlyTransactionsCount;
        snapshot.monthlyDepositsAmount = monthlyDepositsAmount;
        snapshot.monthlyWithdrawalsAmount = monthlyWithdrawalsAmount;
        snapshot.monthlyFeesCharged = monthlyFeesCharged;
        snapshot.monthlyInterestPaid = monthlyInterestPaid;
        snapshot.customerCounter = Customer.getCustomerCounter();
        snapshot.accountCounter = Account.getAccountCounter();
        snapshot.transactionCounter = Transaction.getTransactionCounter();
        snapshot.pausedTransactions = new ArrayList<>(PausedTransaction.getPausedList());
        snapshot.currentDate = DateTimeManager.getCurrentDate();
        return snapshot;
    }

    public static void restoreSnapshot(Snapshot snapshot) {
        allCustomers.clear(); allCustomers.addAll(snapshot.customers);
        allAccounts.clear(); allAccounts.addAll(snapshot.accounts);
        allTransactions.clear(); allTransactions.addAll(snapshot.transactions);
        totalSavingsAccounts.clear(); totalSavingsAccounts.addAll(snapshot.savingsAccounts);
        totalCheckingAccounts.clear(); totalCheckingAccounts.addAll(snapshot.checkingAccounts);
        totalFixedDepositAccounts.clear(); totalFixedDepositAccounts.addAll(snapshot.fixedDepositAccounts);
        monthlyReports.clear(); monthlyReports.putAll(snapshot.monthlyReports);
        totalInterestPaid = snapshot.totalInterestPaid;
        totalFeeCharged = snapshot.totalFeeCharged;
        totalDepositsAmount = snapshot.totalDepositsAmount;
        totalWithdrawalsAmount = snapshot.totalWithdrawalsAmount;
        monthlyCustomersAdded = snapshot.monthlyCustomersAdded;
        monthlyAccountsOpened = snapshot.monthlyAccountsOpened;
        monthlyTransactionsCount = snapshot.monthlyTransactionsCount;
        monthlyDepositsAmount = snapshot.monthlyDepositsAmount;
        monthlyWithdrawalsAmount = snapshot.monthlyWithdrawalsAmount;
        monthlyFeesCharged = snapshot.monthlyFeesCharged;
        monthlyInterestPaid = snapshot.monthlyInterestPaid;
        Customer.setCustomerCounter(snapshot.customerCounter);
        Account.setAccountCounter(snapshot.accountCounter);
        Transaction.setTransactionCounter(snapshot.transactionCounter);
        PausedTransaction.restorePausedList(snapshot.pausedTransactions);
        DateTimeManager.setCurrentDate(snapshot.currentDate);
    }

    public static class Snapshot implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private List<Customer> customers;
        private List<Account> accounts;
        private List<Transaction> transactions;
        private List<SavingsAccount> savingsAccounts;
        private List<CheckingAccount> checkingAccounts;
        private List<FixedDepositAccount> fixedDepositAccounts;
        private Map<YearMonth, MonthlyReport> monthlyReports;
        private double totalInterestPaid, totalFeeCharged, totalDepositsAmount, totalWithdrawalsAmount;
        private int monthlyCustomersAdded, monthlyAccountsOpened, monthlyTransactionsCount;
        private double monthlyDepositsAmount, monthlyWithdrawalsAmount, monthlyFeesCharged, monthlyInterestPaid;
        private int customerCounter, accountCounter, transactionCounter;
        private List<PausedTransaction> pausedTransactions;
        private LocalDate currentDate;
    }

}
