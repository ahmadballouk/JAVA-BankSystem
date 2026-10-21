package menu;

import enums.AccountType;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import model.account.Account;
import model.customer.Customer;
import service.GlobalData;

public class AccountSelectionHelper {

    private final InputHelper input;
    private final CustomerSelectionHelper customers;

    public AccountSelectionHelper(Scanner scanner) {
        input = new InputHelper(scanner);
        customers = new CustomerSelectionHelper(scanner);
    }

    public Account selectAccount() {
        return selectAccount(GlobalData.getAllAccounts(), "account");
    }

    public Account selectAccount(List<Account> candidates, String label) {
        List<Account> accounts = selectAccounts(candidates, label);
        return choose(accounts, label);
    }

    public List<Account> selectAccounts(List<Account> candidates, String label) {
        if (candidates == null || candidates.isEmpty()) {
            System.out.println("No " + label + "s are available.");
            return List.of();
        }

        MainMenu.header("SELECT " + label.toUpperCase());
        System.out.println("1. View All Accounts");
        System.out.println("2. Filter by Account Type");
        System.out.println("3. Select from Customer Accounts");
        System.out.println("4. Find by Account ID");
        System.out.println("0. Back");

        return switch (input.integer("Select an option: ")) {
            case 1 -> new ArrayList<>(candidates);
            case 2 -> filterByType(candidates);
            case 3 -> byCustomer(candidates);
            case 4 -> findById(candidates);
            case 0 -> List.of();
            default -> {
                System.out.println("ERROR: Choose a valid account option.");
                yield List.of();
            }
        };
    }

    public Account selectForCustomer(Customer customer, List<Account> candidates, String label) {
        if (customer == null) return null;
        return choose(accountsForCustomer(customer, candidates), label);
    }

    private List<Account> filterByType(List<Account> candidates) {
        AccountType[] types = AccountType.values();
        System.out.println("\nSelect account type:");
        for (int i = 0; i < types.length; i++) System.out.println((i + 1) + ". " + types[i]);

        int choice = input.integer("Account type: ");
        if (choice < 1 || choice > types.length) {
            System.out.println("ERROR: Invalid account type.");
            return List.of();
        }
        AccountType type = types[choice - 1];
        return candidates.stream().filter(account -> account.getAccountType() == type).toList();
    }

    private List<Account> byCustomer(List<Account> candidates) {
        Customer customer = customers.selectCustomer();
        return customer == null ? List.of() : accountsForCustomer(customer, candidates);
    }

    private List<Account> findById(List<Account> candidates) {
        String id = input.text("Account ID: ");
        Account match = IdBinarySearch.find(candidates, id, Account::getAccountNumber);
        if (match != null) {
            return List.of(match);
        }

        System.out.println("No exact account ID match. Searching for similar IDs...");
        List<Account> suggestions = candidates.stream()
                .filter(account -> SearchMatcher.matches(account.getAccountNumber(), id))
                .toList();
        if (suggestions.isEmpty()) {
            System.out.println("No account matched that ID or a similar ID.");
            return List.of();
        }
        return suggestions;
    }

    private List<Account> accountsForCustomer(Customer customer, List<Account> candidates) {
        return candidates.stream().filter(customer.getAllAccounts()::contains).toList();
    }

    private Account choose(List<Account> accounts, String label) {
        if (accounts == null || accounts.isEmpty()) {
            System.out.println("No " + label + "s matched the selection.");
            return null;
        }

        System.out.println("\nSelect " + label + ":");
        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);
            System.out.printf("%d. %s | Type: %s | Owner: %s | Status: %s | Balance: $%.2f%n",
                    i + 1,
                    account.getAccountNumber(),
                    account.getAccountType(),
                    account.getAccountOwner().getFullName(),
                    account.getAccountStatus(),
                    account.getBalance());
        }

        int choice = input.integer("Select account: ");
        if (choice < 1 || choice > accounts.size()) {
            System.out.println("ERROR: Invalid account selection.");
            return null;
        }
        return accounts.get(choice - 1);
    }
}
