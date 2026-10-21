package menu;

import persistence.BankFileManager;
import java.util.Scanner;
import model.customer.Customer;
import repository.CustomerRepository;

public class CustomerMenu {

    private final InputHelper input;
    private final CustomerRepository repository;
    private final CustomerSelectionHelper selector;

    public CustomerMenu(Scanner scanner) {
        input = new InputHelper(scanner);
        repository = new CustomerRepository();
        selector = new CustomerSelectionHelper(scanner);
    }

    public void start() {
        boolean back = false;
        while (!back) {
            MainMenu.header("CUSTOMER MANAGEMENT");
            System.out.println("1. Create Customer");
            System.out.println("2. View All Customers");
            System.out.println("3. View Customer Details");
            System.out.println("4. Remove Customer");
            System.out.println("5. View Removed Customers");
            System.out.println("0. Back");

            switch (input.integer("Select an option: ")) {
                case 1 -> createCustomer();
                case 2 -> repository.displayAllCustomers();
                case 3 -> viewCustomerDetails();
                case 4 -> removeCustomer();
                case 5 -> repository.displayAllRemovedCustomers();
                case 0 -> back = true;
                default -> System.out.println("ERROR: Choose one of the displayed options.");
            }
            BankFileManager.save();
            if (!back) input.pause();
        }
    }

    private void createCustomer() {
        String name = input.name("Full name: ");
        String phone = input.optionalPhoneNumber("Phone number (optional): ");
        String email = input.optionalEmail("Email address (optional): ");

        Customer customer = repository.createCustomer(
                name,
                phone.isBlank() ? null : phone,
                email.isBlank() ? null : email);

        if (customer != null) {
            System.out.println("SUCCESS: Customer created successfully.");
            System.out.println("Customer ID: " + customer.getCustomerIdString());
        }
    }

    private void removeCustomer() {
        Customer customer = selector.selectCustomer();
        if (customer == null) return;

        customer.displayCustomerInfo();
        if (input.yesNo("Remove this customer? [Y/N]: ")) repository.removeCustomer(customer);
    }

    private void viewCustomerDetails() {
        Customer customer = selector.selectCustomer();
        if (customer == null) return;

        customer.displayCustomerInfo();
        customer.displayAccountsWithTotals();
        customer.displayDeletedAccounts();
    }
}
