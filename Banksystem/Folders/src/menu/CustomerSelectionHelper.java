package menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import model.customer.Customer;
import service.GlobalData;

public class CustomerSelectionHelper {

    private final InputHelper input;

    public CustomerSelectionHelper(Scanner scanner) {
        input = new InputHelper(scanner);
    }

    public Customer selectCustomer() {
        List<Customer> customers = activeCustomers();
        if (customers.isEmpty()) {
            System.out.println("No active customers are available.");
            return null;
        }

        MainMenu.header("SELECT CUSTOMER");
        System.out.println("1. View All Customers");
        System.out.println("2. Find by Customer ID");
        System.out.println("3. Find by Name");
        System.out.println("4. Find by Email");
        System.out.println("5. Find by Phone");
        System.out.println("0. Back");

        return switch (input.integer("Select an option: ")) {
            case 1 -> choose(customers, "customer");
            case 2 -> searchById(customers);
            case 3 -> search(customers, "name", Customer::getFullName);
            case 4 -> search(customers, "email", Customer::getEmail);
            case 5 -> search(customers, "phone", Customer::getPhoneNumber);
            case 0 -> null;
            default -> invalidCustomerChoice();
        };
    }

    public Customer choose(List<Customer> customers) {
        return choose(customers, "customer");
    }

    private Customer searchById(List<Customer> customers) {
        String id = input.text("Enter Customer ID to search: ");
        Customer match = IdBinarySearch.find(customers, id, Customer::getCustomerIdString);
        if (match != null) {
            return choose(List.of(match), "matching customer");
        }

        Customer removedMatch = GlobalData.getAllCustomers().stream()
                .filter(customer -> customer.getCustomerIdString().equalsIgnoreCase(id.trim()))
                .filter(Customer::isRemoved)
                .findFirst()
                .orElse(null);
        if (removedMatch != null) {
            System.out.println("That customer has been removed and cannot be selected.");
            return null;
        }

        System.out.println("No exact customer ID match. Searching for similar IDs...");
        List<Customer> suggestions = customers.stream()
                .filter(customer -> SearchMatcher.matches(customer.getCustomerIdString(), id))
                .toList();
        if (suggestions.isEmpty()) {
            System.out.println("No customer matched that ID or a similar ID.");
            return null;
        }
        return choose(suggestions, "suggested customer");
    }

    private Customer search(List<Customer> customers, String label, ValueGetter getter) {
        String query = input.text("Enter " + label + " to search: ");
        List<Customer> matches = customers.stream()
                .filter(customer -> SearchMatcher.matches(getter.value(customer), query))
                .toList();

        if (matches.isEmpty()) {
            System.out.println("No customers matched your search.");
            return null;
        }
        return choose(matches, "matching customer");
    }

    private Customer choose(List<Customer> customers, String label) {
        if (customers == null || customers.isEmpty()) {
            System.out.println("No " + label + "s are available.");
            return null;
        }

        System.out.println("\nSelect " + label + ":");
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            System.out.printf("%d. %s | ID: %s | Email: %s | Phone: %s | Open accounts: %d%n",
                    i + 1,
                    customer.getFullName(),
                    customer.getCustomerIdString(),
                    display(customer.getEmail()),
                    display(customer.getPhoneNumber()),
                    customer.getAccountCount());
        }

        int choice = input.integer("Select customer: ");
        if (choice < 1 || choice > customers.size()) {
            System.out.println("ERROR: Invalid customer selection.");
            return null;
        }
        return customers.get(choice - 1);
    }

    private List<Customer> activeCustomers() {
        return new ArrayList<>(GlobalData.getAllCustomers().stream()
                .filter(customer -> !customer.isRemoved())
                .toList());
    }

    private String display(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private Customer invalidCustomerChoice() {
        System.out.println("ERROR: Choose a valid customer option.");
        return null;
    }

    @FunctionalInterface
    private interface ValueGetter {
        String value(Customer customer);
    }
}
