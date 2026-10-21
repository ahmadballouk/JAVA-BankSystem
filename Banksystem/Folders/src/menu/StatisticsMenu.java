package menu;

import persistence.BankFileManager;
import java.util.List;
import java.util.Scanner;
import model.account.Account;
import model.customer.Customer;
import service.GlobalData;

public class StatisticsMenu {

    private final InputHelper input;

    public StatisticsMenu(Scanner scanner) {
        this.input = new InputHelper(scanner);
    }

    public void start() {
        boolean back = false;

        while (!back) {
            MainMenu.header("BANK STATISTICS");
            System.out.println("1. Bank Dashboard");
            System.out.println("2. Asset Summary");
            System.out.println("3. Account Counts");
            System.out.println("4. Customer Balances");
            System.out.println("5. All Accounts");
            System.out.println("6. All Customers");
            System.out.println("0. Back");

            switch (input.integer("Select an option: ")) {
                case 1 -> dashboard();
                case 2 -> GlobalData.showBankAssets();
                case 3 -> accountCounts();
                case 4 -> customerBalances();
                case 5 -> allAccounts();
                case 6 -> allCustomers();
                case 0 -> back = true;
                default -> System.out.println("ERROR: Choose one of the displayed options.");
            }
            BankFileManager.save();

            if (!back) {
                input.pause();
            }
        }
    }

    private void accountCounts() {
        System.out.println("\n===== ACCOUNT COUNTS =====");
        System.out.printf("Checking       : %d%n", GlobalData.totalCheckingAccounts());
        System.out.printf("Savings        : %d%n", GlobalData.totalSavingsAccounts());
        System.out.printf("Fixed Deposit  : %d%n", GlobalData.totalFixedDepositAccounts());
        System.out.println("===========================");
    }

    private void allAccounts() {
        List<Account> accounts = GlobalData.getAllAccounts();

        if (accounts.isEmpty()) {
            System.out.println("No accounts are open.");
            return;
        }

        System.out.println("\n===== ALL ACCOUNTS =====");
        for (Account account : accounts) {
            System.out.printf("%s | Owner: %-20s | %-14s | %-8s | $%.2f%n",
                    account.getAccountNumber(),
                    account.getAccountOwner().getFullName(),
                    account.getAccountType(),
                    account.getAccountStatus(),
                    account.getBalance());
        }
        System.out.println("=========================");
    }

    private void allCustomers() {
        List<Customer> customers = GlobalData.getAllCustomers();

        if (customers.isEmpty()) {
            System.out.println("No customers are registered.");
            return;
        }

        System.out.println("\n===== ALL CUSTOMERS =====");
        for (Customer customer : customers) {
            System.out.printf("%s | %-25s | %d open account(s) | %s%n",
                    customer.getCustomerIdString(),
                    customer.getFullName(),
                    customer.getAccountCount(),
                    customer.isRemoved() ? "REMOVED" : "ACTIVE");
        }
        System.out.println("==========================");
    }

    private void customerBalances() {
        List<Customer> customers = GlobalData.getAllCustomers();

        if (customers.isEmpty()) {
            System.out.println("No customers are registered.");
            return;
        }

        System.out.println("\n===== CUSTOMER BALANCES =====");
        for (Customer customer : customers) {
            System.out.printf("%s | %-25s | $%.2f%n",
                    customer.getCustomerIdString(),
                    customer.getFullName(),
                    customer.getTotalBalance());
        }
        System.out.println("==============================");
    }

    private void dashboard() {
        double assets = GlobalData.getAllAccounts().stream()
                .mapToDouble(Account::getBalance)
                .sum();

        System.out.println("\n===== BANK DASHBOARD =====");
        System.out.println("Customers       : " + GlobalData.getTotalCustomers());
        System.out.println("Accounts        : " + GlobalData.getTotalAccounts());
        System.out.println("Transactions    : " + GlobalData.getTotalTransactions());
        System.out.printf("Current Assets  : $%.2f%n", assets);
        System.out.printf("Total Deposits  : $%.2f%n", GlobalData.getTotalDepositsAmount());
        System.out.printf("Total Withdrawals: $%.2f%n", GlobalData.getTotalWithdrawalsAmount());
        System.out.printf("Total Fees      : $%.2f%n", GlobalData.getTotalFeesCharged());
        System.out.printf("Total Interest  : $%.2f%n", GlobalData.getTotalInterestPaid());
    }
}
