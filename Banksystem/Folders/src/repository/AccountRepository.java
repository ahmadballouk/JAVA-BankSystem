package repository;

import enums.AccountFeeConstants;
import enums.AccountStatus;
import enums.AccountType;
import enums.FixedDepositAccountType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import menu.InputHelper;
import menu.AccountSelectionHelper;
import menu.CustomerSelectionHelper;
import model.account.Account;
import model.account.CheckingAccount;
import model.account.FixedDepositAccount;
import model.account.SavingsAccount;
import model.customer.Customer;
import model.transaction.PausedTransaction;
import model.transaction.Transaction;
import service.GlobalData;

public class AccountRepository {

    private final InputHelper input;
    private final AccountSelectionHelper accounts;
    private final CustomerSelectionHelper customers;

    public AccountRepository(Scanner scanner) {
        input = new InputHelper(scanner);
        accounts = new AccountSelectionHelper(scanner);
        customers = new CustomerSelectionHelper(scanner);
    }

    public void activateByDeposit() {

        Account account = accounts.selectAccount(frozenAccounts(), "frozen account");
        if (account == null) return;

        double activationFee = AccountFeeConstants.ACCOUNT_ACTIVATION_FEE.getValue();
        double unpaidFees = getUnpaidFees(account);
        double totalFees = activationFee + unpaidFees;
        displayActivationFees(account, activationFee, totalFees);
        double amount = input.positiveAmount("Enter the exact activation payment: ");
        if (Math.abs(amount - totalFees) > 0.000001) {
            System.out.printf("ERROR: Exact activation payment required: $%.2f%n", totalFees);
            return;
        }
        if (!input.yesNo("Confirm activation and fee payment? [Y/N]: ")) {
            System.out.println("Activation cancelled.");
            return;
        }

        account.activateByDeposit(amount, totalFees);
    }

    public void activateByTransfer() {
        Account frozenAccount = accounts.selectAccount(frozenAccounts(), "frozen account");
        if (frozenAccount == null) return;

        List<Account> sourceCandidates = GlobalData.getAllAccounts().stream()
                .filter(a -> a.getAccountStatus() == AccountStatus.ACTIVE && a != frozenAccount)
                .filter(a -> a.getAccountType() != AccountType.FIXED_DEPOSIT)
                .toList();

        Account sourceAccount = accounts.selectAccount(sourceCandidates, "source account");
        if (sourceAccount == null) return;

        double activationFee = AccountFeeConstants.ACCOUNT_ACTIVATION_FEE.getValue();
        double totalFees = activationFee + getUnpaidFees(frozenAccount);

        displayActivationFees(frozenAccount, activationFee, totalFees);
        System.out.printf("Total required from source account: $%.2f%n", totalFees);
        System.out.printf("Source balance: $%.2f%n", sourceAccount.getBalance());

        if (!input.yesNo("Confirm activation and fee payment from this source account? [Y/N]: ")) {
            System.out.println("Activation cancelled.");
            return;
        }
        frozenAccount.activateFromTransfer(sourceAccount,totalFees);
    }

    public void closeAccount() {
        Account account = accounts.selectAccount(GlobalData.getAllAccounts(), "account");
        if (account == null) return;
        System.out.printf("Account: %s | Owner: %s%n", account.getAccountNumber(), account.getAccountOwner().getFullName());
        if (input.yesNo("Close this account? [Y/N]: ")) account.getAccountOwner().closeAccount(account);
    }

    public void completePausedTransaction(PausedTransaction pausedTransaction, Account account, Customer customer) {
        if (pausedTransaction == null || account == null || customer == null) {
            System.out.println("Error: Paused Transaction, Account, or Customer cannot be null");
            return;
        }

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            System.out.println("Error: Selected account must be ACTIVE before resuming the transaction.");
            return;
        }

        Transaction original = pausedTransaction.getOriginalTransaction();
        if (original == null) {
            System.out.println("Error: Original transaction is missing.");
            return;
        }

        Account originalDestination = original.getDestinationAccount();
        if (originalDestination == null) {
            original.setDestinationAccount(account);
        } else if (originalDestination != account) {
            if (originalDestination.getAccountStatus() == AccountStatus.CLOSED) {
                original.setDestinationAccount(account);
            } else {
                System.out.println("Error: Selected account is not the destination of this paused transaction.");
                return;
            }
        }

        account.completePausedTransferIn(original);
    }

    public void fixedDepositDetails() {
        Account account = accounts.selectAccount(findByAccountType(AccountType.FIXED_DEPOSIT), "fixed deposit account");
        if (account instanceof FixedDepositAccount fixedDeposit) fixedDeposit.displayFixedAccountInfo();
    }

    public void freezeAccount() {

        List<Account> accounts = GlobalData.getAllAccounts().stream()
                .filter(a -> a.getAccountStatus() != AccountStatus.CLOSED)
                .toList();

        Account account = this.accounts.selectAccount(accounts, "account");
        if (account != null) account.freezeAccount();
    }

    public void openAccount() {
        Customer customer = customers.selectCustomer();
        if (customer == null) return;

        System.out.println("1. Checking");
        System.out.println("2. Saving");
        System.out.println("3. Fixed Deposit");
        int type = input.integer("Account type: ");
        double initialBalance;

        switch (type) {
            case 1 -> {
                initialBalance = input.amount("Initial balance: ");
                showCreatedAccount(createCheckingAccount(customer, LocalDateTime.now(), initialBalance));
            }
            case 2 -> {
                initialBalance = input.amount("Initial balance: ");
                showCreatedAccount(createSavingsAccount(customer, LocalDateTime.now(), initialBalance));
            }
            case 3 -> openFixedDeposit(customer);
            default -> System.out.println("ERROR: Choose a valid account type.");
        }
    }

    public void viewAccountDetails() {
        Account account = accounts.selectAccount(GlobalData.getAllAccounts(), "account");
        if (account == null) return;
        account.displayAccountInfo();
        if (account instanceof FixedDepositAccount fixedDeposit) fixedDeposit.displayFixedAccountInfo();
    }

    public void viewAllAccounts() {
        List<Account> selectedAccounts = this.accounts.selectAccounts(GlobalData.getAllAccounts(), "account");
        if (selectedAccounts.isEmpty()) {
            System.out.println("No accounts found for the selected filter.");
            return;
        }
        System.out.println("\n===== ALL SYSTEM ACCOUNTS =====");
        for (Account account : selectedAccounts) account.displayAccountInfo();
        System.out.println("===============================\n");
    }

    private Account createCheckingAccount(Customer customer, LocalDateTime timestamp, double initialBalance) {
        if (customer == null) {
            System.out.println("Error: Customer cannot be null");
            return null;
        }

        if (!customer.canCreateAccountType(AccountType.CHECKING)) {
            System.out.println("Error: Customer already has a Checking Account");
            return null;
        }

        double balance = Math.max(0, initialBalance);
        Account account = new CheckingAccount(customer, AccountType.CHECKING, balance, timestamp);
        customer.addAccount(account);
        return account;
    }

    private Account createFixedDepositAccount(Customer customer, LocalDateTime timestamp, double initialBalance,
            int termMonth, Account linkedAccount, FixedDepositAccountType depositType) {
        if (customer == null) {
            System.out.println("Error: Customer cannot be null");
            return null;
        }

        if (!customer.canCreateAccountType(AccountType.FIXED_DEPOSIT)) {
            System.out.println("Error: Customer already has a Fixed Deposit Account");
            return null;
        }

        Account account = new FixedDepositAccount(customer, AccountType.FIXED_DEPOSIT, initialBalance,
                termMonth, linkedAccount, depositType, timestamp);
        customer.addAccount(account);

        return account;
    }

    private Account createSavingsAccount(Customer customer, LocalDateTime timestamp, double initialBalance) {
        if (customer == null) {
            System.out.println("Error: Customer cannot be null");
            return null;
        }

        if (!customer.canCreateAccountType(AccountType.SAVINGS)) {
            System.out.println("Error: Customer already has a Savings Account");
            return null;
        }

        double balance = Math.max(0, initialBalance);
        Account account = new SavingsAccount(customer, AccountType.SAVINGS, timestamp, balance);
        customer.addAccount(account);
        return account;
    }

    private List<Account> frozenAccounts() {
        return findByStatus(AccountStatus.FROZEN).stream()
                .filter(account -> account.getAccountType() != AccountType.FIXED_DEPOSIT)
                .toList();
    }

    private double getUnpaidFees(Account account) {
        return account instanceof CheckingAccount checking ? checking.getUnpaidFeeAmount() : 0.0;
    }

    private void displayActivationFees(Account account, double activationFee, double totalFees) {
        System.out.printf("Activation Fee: $%.2f%n", activationFee);
        if (account instanceof CheckingAccount) {
            System.out.printf("Unpaid/Pending Fees: $%.2f%n", getUnpaidFees(account));
        }
        System.out.printf("Total fees required to activate: $%.2f%n", totalFees);
    }

    private List<Account> findByAccountType(AccountType type) {
        if (type == null) return List.of();
        return GlobalData.getAllAccounts().stream()
                .filter(account -> account.getAccountType() == type)
                .toList();
    }

    private List<Account> findByStatus(AccountStatus status) {
        if (status == null) return List.of();
        return GlobalData.getAllAccounts().stream()
                .filter(account -> account.getAccountStatus() == status)
                .toList();
    }

    private void openFixedDeposit(Customer customer) {
        double initialBalance = input.positiveAmount("Initial deposit: ");
        int termMonths = input.positiveInteger("Term in months: ");

        System.out.println("1. Auto Renew Principal + Interest");
        System.out.println("2. Auto Renew Principal Only");
        System.out.println("3. Close & Payout");

        FixedDepositAccountType depositType = switch (input.integer("Maturity option: ")) {
            case 1 -> FixedDepositAccountType.AUTO_RENEW_PRINCIPAL_AND_INTEREST;
            case 2 -> FixedDepositAccountType.AUTO_RENEW_PRINCIPAL_ONLY;
            case 3 -> FixedDepositAccountType.CLOSE_AND_PAYOUT;
            default -> null;

        };

        if (depositType == null) {
            System.out.println("ERROR: Choose a valid maturity option.");
            return;
        }

        Account linkedAccount = null;
        if (depositType != FixedDepositAccountType.AUTO_RENEW_PRINCIPAL_AND_INTEREST) {
            linkedAccount = selectCustomerNonFixedAccount(customer);
            if (linkedAccount == null) {
                System.out.println("ERROR: A linked Checking or Savings account is required for this maturity option.");
                return;
            }
        }

        showCreatedAccount(createFixedDepositAccount(
                customer, LocalDateTime.now(), initialBalance, termMonths, linkedAccount, depositType));
    }

    private Account selectCustomerNonFixedAccount(Customer customer) {
        List<Account> accounts = customer.getAccounts().stream()
                .filter(a -> a.getAccountType() != AccountType.FIXED_DEPOSIT && a.getAccountStatus() != AccountStatus.CLOSED)
                .toList();
        return this.accounts.selectForCustomer(customer, accounts, "linked account");
    }

    private void showCreatedAccount(Account account) {
        if (account != null) {
            System.out.println("Account opened successfully.");
            System.out.println("Account Number: " + account.getAccountNumber());
            System.out.println("Owner: " + account.getAccountOwner().getFullName());
        }
    }
}
