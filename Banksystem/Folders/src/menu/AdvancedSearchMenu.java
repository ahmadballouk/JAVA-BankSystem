package menu;

import persistence.BankFileManager;
import java.util.Scanner;
import model.account.Account;
import model.customer.Customer;
import model.transaction.Transaction;

public class AdvancedSearchMenu {

    private final InputHelper input;
    private final CustomerSelectionHelper customers;
    private final AccountSelectionHelper accounts;
    private final TransactionSelectionHelper transactions;

    public AdvancedSearchMenu(Scanner scanner) {
        input = new InputHelper(scanner);
        customers = new CustomerSelectionHelper(scanner);
        accounts = new AccountSelectionHelper(scanner);
        transactions = new TransactionSelectionHelper(scanner);
    }

    public void start() {
        boolean back = false;
        while (!back) {
            MainMenu.header("ADVANCED SEARCH");
            System.out.println("1. Search Customer");
            System.out.println("2. Search Account");
            System.out.println("3. Search Transaction");
            System.out.println("0. Back");

            switch (input.integer("Select an option: ")) {
                case 1 -> showCustomer();
                case 2 -> showAccount();
                case 3 -> showTransaction();
                case 0 -> back = true;
                default -> System.out.println("ERROR: Choose one of the displayed options.");
            }
            BankFileManager.save();
            if (!back) input.pause();
        }
    }

    private void showCustomer() {
        Customer customer = customers.selectCustomer();
        if (customer != null) customer.displayCustomerInfo();
    }

    private void showAccount() {
        Account account = accounts.selectAccount();
        if (account != null) account.displayAccountInfo();
    }

    private void showTransaction() {
        Transaction transaction = transactions.selectTransaction();
        transactions.display(transaction);
    }
}
