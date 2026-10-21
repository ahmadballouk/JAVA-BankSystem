package menu;

import enums.TransactionStatus;
import enums.TransactionType;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import model.account.Account;
import model.transaction.Transaction;
import service.GlobalData;
import util.DateTimeManager;

public class TransactionSelectionHelper {

    private final InputHelper input;
    private final AccountSelectionHelper accounts;

    public TransactionSelectionHelper(Scanner scanner) {
        input = new InputHelper(scanner);
        accounts = new AccountSelectionHelper(scanner);
    }

    public Transaction selectTransaction() {
        MainMenu.header("SELECT TRANSACTION");
        System.out.println("1. Search All Transactions");
        System.out.println("2. Search by Account");
        System.out.println("3. Find by Transaction ID");
        System.out.println("0. Back");

        return switch (input.integer("Select an option: ")) {
            case 1 -> selectFromAll();
            case 2 -> selectFromAccount();
            case 3 -> selectById(GlobalData.getAllTransactions());
            case 0 -> null;
            default -> invalid();
        };
    }

    public List<Transaction> filterAllTransactions() {
        System.out.println("\n1. View All Transactions");
        System.out.println("2. Filter Transactions");
        return switch (input.integer("Select an option: ")) {
            case 1 -> GlobalData.getAllTransactions();
            case 2 -> filter(GlobalData.getAllTransactions());
            default -> {
                System.out.println("ERROR: Choose 1 or 2.");
                yield List.of();
            }
        };
    }

    public List<Transaction> filterForAccount(Account account) {
        return filter(accountTransactions(account));
    }

    public List<Transaction> accountTransactions(Account account) {
        return GlobalData.getAllTransactions().stream()
                .filter(transaction -> transaction.getSourceAccount() == account
                        || transaction.getDestinationAccount() == account
                        || account.getAccountNumber().equalsIgnoreCase(transaction.getAccountNumber()))
                .toList();
    }

    public List<Transaction> filter(List<Transaction> transactions) {
        System.out.println("\n1. By Status");
        System.out.println("2. By Transaction Type");
        System.out.println("3. By Date Range");
        System.out.println("4. By Amount Range");

        return switch (input.integer("Select a filter: ")) {
            case 1 -> byStatus(transactions);
            case 2 -> byType(transactions);
            case 3 -> byDate(transactions);
            case 4 -> byAmount(transactions);
            default -> {
                System.out.println("ERROR: Choose a valid filter.");
                yield List.of();
            }
        };
    }

    private Transaction selectFromAll() {
        List<Transaction> transactions = filterAllTransactions();
        return choose(transactions);
    }

    private Transaction selectFromAccount() {
        Account account = accounts.selectAccount();
        if (account == null) return null;

        List<Transaction> transactions = accountTransactions(account);
        System.out.println("\n1. View All Account Transactions");
        System.out.println("2. Filter Account Transactions");
        return switch (input.integer("Select an option: ")) {
            case 1 -> choose(transactions);
            case 2 -> choose(filter(transactions));
            default -> invalid();
        };
    }

    private Transaction selectById(List<Transaction> transactions) {
        String id = input.text("Transaction ID: ");
        Transaction match = IdBinarySearch.find(transactions, id, Transaction::getTransactionId);
        if (match != null) {
            return choose(List.of(match));
        }

        System.out.println("No exact transaction ID match. Searching for similar IDs...");
        List<Transaction> suggestions = transactions.stream()
                .filter(transaction -> SearchMatcher.matches(transaction.getTransactionId(), id))
                .toList();
        if (suggestions.isEmpty()) {
            System.out.println("No transaction matched that ID or a similar ID.");
            return null;
        }
        return choose(suggestions);
    }

    private Transaction choose(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            System.out.println("No transactions matched your search.");
            return null;
        }

        for (int i = 0; i < transactions.size(); i++) displayChoice(i + 1, transactions.get(i));
        int choice = input.integer("Select transaction: ");
        if (choice < 1 || choice > transactions.size()) {
            System.out.println("ERROR: Invalid transaction selection.");
            return null;
        }
        return transactions.get(choice - 1);
    }

    public void display(Transaction transaction) {
        if (transaction == null) return;
        Account account = findAccount(transaction);
        String owner = account == null ? "Unknown" : account.getAccountOwner().getFullName();
        String accountId = transaction.getAccountNumber();
        System.out.println("\n================ TRANSACTION INFORMATION ================");
        System.out.println("Transaction ID : " + transaction.getTransactionId());
        System.out.println("Account ID     : " + accountId);
        System.out.println("Owner          : " + owner);
        System.out.println("Type           : " + transaction.getTransactionType());
        System.out.printf("Amount         : $%.2f%n", transaction.getAmount());
        System.out.println("Status         : " + transaction.getTransactionStatus());
        System.out.println("Description    : " + display(transaction.getDescription()));
        System.out.println("Time           : " + transaction.getTransactionTime());
        System.out.printf("Resulting Bal. : $%.2f%n", transaction.getResultingBalance());
        System.out.println("==========================================================");
    }

    private List<Transaction> byStatus(List<Transaction> transactions) {
        TransactionStatus[] values = TransactionStatus.values();
        for (int i = 0; i < values.length; i++) System.out.println((i + 1) + ". " + values[i]);
        int choice = input.integer("Status: ");
        if (choice < 1 || choice > values.length) return List.of();
        return transactions.stream().filter(t -> t.getTransactionStatus() == values[choice - 1]).toList();
    }

    private List<Transaction> byType(List<Transaction> transactions) {
        TransactionType[] values = TransactionType.values();
        for (int i = 0; i < values.length; i++) System.out.println((i + 1) + ". " + values[i]);
        int choice = input.integer("Transaction type: ");
        if (choice < 1 || choice > values.length) return List.of();
        return transactions.stream().filter(t -> t.getTransactionType() == values[choice - 1]).toList();
    }

    private List<Transaction> byDate(List<Transaction> transactions) {
        LocalDate start = date("Start date (YYYY-MM-DD): ");
        LocalDate end = date("End date (YYYY-MM-DD): ");
        if (start == null || end == null || end.isBefore(start)) {
            System.out.println("ERROR: Invalid date range.");
            return List.of();
        }
        return transactions.stream().filter(t -> {
            try {
                LocalDate date = DateTimeManager.stringToDate(t.getTransactionTime().substring(0, 10));
                return !date.isBefore(start) && !date.isAfter(end);
            } catch (RuntimeException e) {
                return false;
            }
        }).toList();
    }

    private List<Transaction> byAmount(List<Transaction> transactions) {
        System.out.println("1. Between two amounts");
        System.out.println("2. Greater than or equal to");
        System.out.println("3. Less than or equal to");
        return switch (input.integer("Select amount filter: ")) {
            case 1 -> {
                double min = input.amount("Minimum amount: ");
                double max = input.amount("Maximum amount: ");
                yield max < min ? List.of() : transactions.stream()
                        .filter(t -> t.getAmount() >= min && t.getAmount() <= max).toList();
            }
            case 2 -> {
                double min = input.amount("Minimum amount: ");
                yield transactions.stream().filter(t -> t.getAmount() >= min).toList();
            }
            case 3 -> {
                double max = input.amount("Maximum amount: ");
                yield transactions.stream().filter(t -> t.getAmount() <= max).toList();
            }
            default -> List.of();
        };
    }

    private LocalDate date(String prompt) {
        try {
            return DateTimeManager.stringToDate(input.text(prompt));
        } catch (RuntimeException e) {
            System.out.println("ERROR: Use YYYY-MM-DD.");
            return null;
        }
    }

    private Account findAccount(Transaction transaction) {
        return GlobalData.getAllAccounts().stream()
                .filter(account -> account.getAccountNumber().equalsIgnoreCase(transaction.getAccountNumber()))
                .findFirst().orElse(transaction.getSourceAccount());
    }

    private void displayChoice(int number, Transaction transaction) {
        Account account = findAccount(transaction);
        String owner = account == null ? "Unknown" : account.getAccountOwner().getFullName();
        System.out.printf("%d. %s | Account: %s | Owner: %s | Type: %s | $%.2f | %s | %s | %s%n",
                number,
                transaction.getTransactionId(),
                transaction.getAccountNumber(),
                owner,
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getTransactionStatus(),
                transaction.getDescription(),
                transaction.getTransactionTime());
    }

    private String display(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private Transaction invalid() {
        System.out.println("ERROR: Choose a valid transaction option.");
        return null;
    }
}
