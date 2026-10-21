package model.customer;

import enums.AccountStatus;
import enums.AccountType;
import exception.ClosedAccountHasBalanceException;
import exception.ClosedAccountHasUnpaidFeeException;
import exception.ClosedCheckingAccountOverdrawnException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import model.account.Account;
import model.account.FixedDepositAccount;
import service.GlobalData;
import util.DateTimeManager;

public class Customer implements java.io.Serializable {

    private static int customerCounter = 0;

    private final String customerId;
    private final String fullName;
    private final String registrationDate;

    private final String phoneNumber;
    private final String email;

    private final ArrayList<Account> accounts;
    private final ArrayList<Account> closedAccounts;
    private boolean removed = false;

    public Customer(String fullName, String phoneNumber, String email, LocalDateTime timestamp) {
        this.customerId = String.format("C%03d", customerCounter);
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.registrationDate = DateTimeManager.dateToString(timestamp);
        this.accounts = new ArrayList<>();
        this.closedAccounts = new ArrayList<>();

        customerCounter++;
        GlobalData.addCustomer(this);
        GlobalData.recordCustomerCreated();
    }

    public void addAccount(Account account) {
        if (account != null && account.getAccountOwner() == this && !accounts.contains(account)) {
            accounts.add(account);
        }
    }

    public void addClosedAccount(Account account) {
        if (account != null && !closedAccounts.contains(account)) {
            closedAccounts.add(account);
        }
    }

    public void moveToClosedAccounts(Account account) {
        if (account == null || account.getAccountOwner() != this) return;
        accounts.remove(account);
        addClosedAccount(account);
    }

    public void closeAccount(Account account) {
        if (account == null) {
            System.out.println("Error: Account cannot be null");
            return;
        }

        if (!accounts.contains(account)) {
            System.out.println("Error: This account does not belong to the customer.");
            return;
        }

        if (account instanceof FixedDepositAccount) {
            System.out.println("ERROR: A Fixed Deposit can only be closed through its maturity process.");
            return;
        }
        try {
            account.validateCloseAccount();
            account.setAccountStatus(AccountStatus.CLOSED);

            accounts.remove(account);
            closedAccounts.add(account);

            System.out.println("Account " + account.getAccountNumber() + " closed successfully.");
        } catch (ClosedAccountHasBalanceException | ClosedAccountHasUnpaidFeeException
                | ClosedCheckingAccountOverdrawnException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    public void processEndOfMonth() {
        for (Account account : new ArrayList<>(accounts)) {
            account.processEndOfMonth();
        }
    }

    public boolean canCreateAccountType(AccountType accountType) {
        if (accountType == null) {
            return false;
        }
        for (Account account : accounts) {
            if (account.getAccountType() == accountType) {
                return false;
            }
        }
        return true;
    }

    public boolean canRemoveCustomer() {
        return !removed && !hasOpenAccounts();
    }

    public int getAccountCount() {
        return accounts.size();
    }

    public ArrayList<Account> getAccounts() {
        return new ArrayList<>(accounts);
    }

    public ArrayList<Account> getAllAccounts() {
        ArrayList<Account> all = new ArrayList<>(accounts);
        all.addAll(closedAccounts);
        return all;
    }

    public static int getCustomerCounter() {
        return customerCounter;
    }

    public static void setCustomerCounter(int counter) {
        customerCounter = Math.max(0, counter);
    }

    public String getCustomerIdString() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public double getTotalBalance() {
        double total = 0;
        for (Account account : accounts) {
            total += account.getBalance();
        }
        return total;
    }

    public boolean hasOpenAccounts() {
        return !accounts.isEmpty();
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public void displayAccountsWithTotals() {
        System.out.println("\n===== Account Summary for " + fullName + " =====");
        System.out.println("Customer ID: " + customerId);
        System.out.println("---------------------------------------------------");

        double total = 0;
        for (Account account : accounts) {
            double balance = account.getBalance();
            System.out.printf("%-20s | %-15s | $%12.2f%n",
                    account.getAccountType(),
                    account.getAccountNumber(),
                    balance);
            total += balance;
        }

        System.out.println("---------------------------------------------------");
        System.out.printf("%-20s | %-15s | $%12.2f%n",
                "TOTAL", "", total);
        System.out.println("===================================================\n");
    }

    public void displayCustomerInfo() {
        System.out.println("\n===== Customer Information =====");
        System.out.println("Full Name: " + fullName);
        System.out.println("Customer ID: " + customerId);

        if (phoneNumber != null) {
            System.out.println("Phone Number: " + phoneNumber);
        }

        if (email != null) {
            System.out.println("Email: " + email);
        }

        System.out.println("Registration Date: " + registrationDate);
        System.out.println("Total Accounts: " + getAccountCount());
        System.out.println("================================\n");
    }

    public void displayDeletedAccounts() {
        System.out.println("\n===== Account Summary for " + fullName + " =====");
        System.out.println("Customer ID: " + customerId);
        System.out.println("---------------------------------------------------");

        for (Account account : closedAccounts) {
            double balance = account.getBalance();
            System.out.printf("%-20s | %-15s | $%12.2f%n",
                    account.getAccountType(),
                    account.getAccountNumber(),
                    balance);
        }

        System.out.println("---------------------------------------------------");

    }

}
