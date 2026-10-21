package menu;

import enums.AccountStatus;
import java.util.List;
import java.util.Scanner;
import model.account.Account;
import model.transaction.PausedTransaction;
import model.transaction.Transaction;
import repository.AccountRepository;
import service.GlobalData;

public class TransactionRepository {

    private final InputHelper input;
    private final AccountRepository accountRepository;
    private final AccountSelectionHelper accounts;
    private final TransactionSelectionHelper transactions;

    public TransactionRepository(Scanner scanner) {
        input = new InputHelper(scanner);
        accountRepository = new AccountRepository(scanner);
        accounts = new AccountSelectionHelper(scanner);
        transactions = new TransactionSelectionHelper(scanner);
    }

    void deposit() {
        Account account = accounts.selectAccount();
        if (account != null) account.deposit(input.positiveAmount("Deposit amount: "));
    }

    void withdraw() {
        Account account = accounts.selectAccount();
        if (account != null) account.withdraw(input.positiveAmount("Withdrawal amount: "));
    }

    void transfer() {
        Account source = accounts.selectAccount();
        if (source == null) return;

        Account destination = accounts.selectAccount();
        if (destination == null || source == destination) {
            if (destination == source) System.out.println("ERROR: Source and destination must be different.");
            return;
        }

        double amount = input.positiveAmount("Transfer amount: ");
        String description = input.optionalText("Transfer description (optional): ");
        if (input.yesNo("Confirm transfer of $" + String.format("%.2f", amount) + "? [Y/N]: ")) {
            source.transferOut(amount, destination, description);
        } else {
            System.out.println("Transfer cancelled.");
        }
    }

    void viewTransactionInfo() {
        transactions.display(transactions.selectTransaction());
    }

    void viewAccountTransactionHistory() {
        Account account = accounts.selectAccount();
        if (account != null) account.displayTransactionHistory();
    }

    void displayAllTransactions() {
        display(transactions.filterAllTransactions(), "TRANSACTIONS");
    }

    void viewFilteredTransactions() {
        System.out.println("\n1. All Transactions");
        System.out.println("2. Transactions for an Account");

        switch (input.integer("Select a scope: ")) {
            case 1 -> display(transactions.filterAllTransactions(), "FILTERED TRANSACTIONS");
            case 2 -> {
                Account account = accounts.selectAccount();
                if (account != null) display(transactions.filterForAccount(account), "FILTERED ACCOUNT TRANSACTIONS");
            }
            default -> System.out.println("ERROR: Choose 1 or 2.");
        }
    }

    void pausedTransactionsMenu() {
        System.out.println("\n1. View Global Paused Transactions");
        System.out.println("2. View Paused Transactions for an Account");

        switch (input.integer("Select an option: ")) {
            case 1 -> displayPaused(PausedTransaction.getPausedList());
            case 2 -> {
                Account account = accounts.selectAccount();
                if (account != null) account.displayPausedTransactionsForAccount();
            }
            default -> System.out.println("ERROR: Choose 1 or 2.");
        }
    }

    void resumePausedTransaction() {
        List<PausedTransaction> paused = PausedTransaction.getPausedList();
        if (paused.isEmpty()) {
            System.out.println("No paused transactions are waiting.");
            return;
        }

        PausedTransaction selected = selectPaused(paused);
        if (selected == null) return;

        Account destination = accounts.selectAccount(activeAccounts(), "active destination account");
        if (destination != null) accountRepository.completePausedTransaction(
                selected, destination, destination.getAccountOwner());
    }

    void resolveOrphanedPausedTransactions() {
        List<PausedTransaction> orphaned = PausedTransaction.getPausedList().stream()
                .filter(PausedTransaction::isOrphaned)
                .toList();
        if (orphaned.isEmpty()) {
            System.out.println("No orphaned paused transactions were found.");
            return;
        }

        displayPaused(orphaned);
        PausedTransaction selected = selectPaused(orphaned);
        if (selected == null) return;

        Account destination = accounts.selectAccount(activeAccounts(), "new destination account");
        if (destination == null) return;

        System.out.printf("Held amount: $%.2f%n", selected.getAmount());
        System.out.printf("New destination: %s | Owner: %s%n",
                destination.getAccountNumber(), destination.getAccountOwner().getFullName());

        if (input.yesNo("Confirm resolution? [Y/N]: ")) {
            accountRepository.completePausedTransaction(selected, destination, destination.getAccountOwner());
        } else {
            System.out.println("Resolution cancelled.");
        }
    }

    private List<Account> activeAccounts() {
        return GlobalData.getAllAccounts().stream()
                .filter(account -> account.getAccountStatus() == AccountStatus.ACTIVE)
                .toList();
    }

    private void display(List<Transaction> transactions, String title) {
        if (transactions == null || transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }
        MainMenu.header(title);
        transactions.forEach(this::displayTransaction);
    }

    private void displayTransaction(Transaction transaction) {
        String owner = GlobalData.getAllAccounts().stream()
                .filter(account -> account.getAccountNumber().equalsIgnoreCase(transaction.getAccountNumber()))
                .map(account -> account.getAccountOwner().getFullName())
                .findFirst().orElse("Unknown");
        System.out.printf("%s | Account: %s | Owner: %s | Type: %s | Amount: $%.2f | Status: %s | Description: %s%n",
                transaction.getTransactionId(),
                transaction.getAccountNumber(),
                owner,
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getTransactionStatus(),
                transaction.getDescription().isBlank() ? "-" : transaction.getDescription());
    }

    private void displayPaused(List<PausedTransaction> paused) {
        if (paused == null || paused.isEmpty()) {
            System.out.println("No paused transactions found.");
            return;
        }
        MainMenu.header("PAUSED TRANSACTIONS");
        for (PausedTransaction transaction : paused) {
            Account source = transaction.getSourceAccount();
            Account destination = transaction.getDestinationAccount();
            System.out.printf("%s | Type: %s | Amount: $%.2f | Source: %s | Destination: %s%n",
                    transaction.getTransactionId(),
                    transaction.getTransactionType(),
                    transaction.getAmount(),
                    accountLabel(source),
                    destination == null ? "NONE / ORPHANED" : accountLabel(destination));
        }
    }

    private String accountLabel(Account account) {
        return account.getAccountNumber() + " / " + account.getAccountOwner().getFullName();
    }

    private PausedTransaction selectPaused(List<PausedTransaction> paused) {
        for (int i = 0; i < paused.size(); i++) {
            PausedTransaction transaction = paused.get(i);
            System.out.printf("%d. %s | %s | $%.2f%n",
                    i + 1, transaction.getTransactionId(), transaction.getTransactionType(), transaction.getAmount());
        }
        int choice = input.integer("Select paused transaction: ");
        if (choice < 1 || choice > paused.size()) {
            System.out.println("ERROR: Invalid paused transaction selection.");
            return null;
        }
        return paused.get(choice - 1);
    }
}
