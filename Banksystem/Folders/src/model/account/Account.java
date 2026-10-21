package model.account;

import enums.AccountFeeConstants;
import enums.AccountStatus;
import enums.AccountType;
import enums.TransactionStatus;
import enums.TransactionType;
import exception.ClosedAccountHasBalanceException;
import exception.ClosedAccountHasUnpaidFeeException;
import exception.ClosedCheckingAccountOverdrawnException;
import interfaces.Transferable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import model.customer.Customer;
import model.transaction.PausedTransaction;
import model.transaction.Transaction;
import service.GlobalData;
import util.DateTimeManager;

public class Account implements Transferable, java.io.Serializable {

    private static int accountCounter = 0;

    protected final String accountNumber;
    private final Customer accountOwner;
    private final String openingDate;
    private final AccountType accountType;
    private final LocalDateTime timestamp;

    protected AccountStatus accountStatus;
    protected double balance;

    protected final ArrayList<Transaction> transactionHistory;
    protected final ArrayList<Transaction> pausedTransactions;

    public Account(Customer accountOwner, AccountType accountType, LocalDateTime timestamp) {
        this.accountOwner = accountOwner;
        this.accountType = accountType;
        this.balance = 0.0;
        this.accountStatus = AccountStatus.ACTIVE;

        String numericId = accountOwner.getCustomerIdString().replaceAll("\\D", "");
        this.accountNumber = "ACC" + accountCounter + numericId;

        this.openingDate = DateTimeManager.dateToString(LocalDate.from(timestamp).atStartOfDay());
        this.timestamp = timestamp;

        this.transactionHistory = new ArrayList<>();
        this.pausedTransactions = new ArrayList<>();

        GlobalData.addAccount(this);

        accountCounter++;
        GlobalData.recordAccountOpened();
    }

    public void activateAccount() {
        if (accountStatus == AccountStatus.ACTIVE) {
            System.out.println("account all ready Active ");
        }
        if (accountStatus == AccountStatus.FROZEN) {
            accountStatus = AccountStatus.ACTIVE;
            completeAllPausedTransactions();
        }

    }

    public void activateByDeposit(double paymentAmount, double totalFee) {
        if (accountStatus != AccountStatus.FROZEN) {
            System.out.println("Error: Only a frozen account can be activated by deposit.");
            return;
        }

        if (!Double.isFinite(paymentAmount) || paymentAmount <= 0) {
            System.out.println("Error: Activation payment must be greater than 0.");
            return;
        }

        if (Math.abs(paymentAmount - totalFee) > 0.000001) {
            System.out.printf("Error: Exact activation payment required: $%.2f%n", totalFee);
            return;
        }

        double activationFee = AccountFeeConstants.ACCOUNT_ACTIVATION_FEE.getValue();
        Transaction activationTransaction = new Transaction(this, TransactionType.ActivateAccountFee, activationFee);
        activationTransaction.markAsCompleted(balance);
        transactionHistory.add(activationTransaction);
        GlobalData.addFeeMoney(activationFee);

        if (this instanceof CheckingAccount checking && checking.getUnpaidFeeAmount() > 0) {
            double unpaidFees = checking.getUnpaidFeeAmount();
            Transaction unpaidFeeTransaction = new Transaction(this, TransactionType.UnpaidFeePayment, unpaidFees);
            unpaidFeeTransaction.markAsCompleted(balance);
            transactionHistory.add(unpaidFeeTransaction);
            GlobalData.addFeeMoney(unpaidFees);
            checking.setUnpaidFeeAmount(0.0);
        }

        activateAccount();
        System.out.printf("Account %s activated successfully. Activation payment: $%.2f%n",
                accountNumber, paymentAmount);
    }

    public void activateFromTransfer(Account sourceAccount, double totalFee) {
        if (!canActivateFromTransfer(sourceAccount, totalFee)) return;

        sourceAccount.balance -= totalFee;
        GlobalData.addFeeMoney(totalFee);

        Transaction activationTransaction = new Transaction(
                sourceAccount,
                TransactionType.ActivateAccountFee,
                AccountFeeConstants.ACCOUNT_ACTIVATION_FEE.getValue());
        activationTransaction.setDestinationAccount(this);
        activationTransaction.markAsCompleted(sourceAccount.balance);
        sourceAccount.transactionHistory.add(activationTransaction);

        if (this instanceof CheckingAccount checking && checking.getUnpaidFeeAmount() > 0) {
            double unpaidFees = checking.getUnpaidFeeAmount();
            Transaction unpaidFeeTransaction = new Transaction(sourceAccount, TransactionType.UnpaidFeePayment, unpaidFees);
            unpaidFeeTransaction.setDestinationAccount(this);
            unpaidFeeTransaction.markAsCompleted(sourceAccount.balance);
            sourceAccount.transactionHistory.add(unpaidFeeTransaction);
            checking.setUnpaidFeeAmount(0.0);
        }

        activateAccount();
        System.out.printf("Account %s activated successfully. $%.2f paid from %s.%n",
                accountNumber, totalFee, sourceAccount.getAccountNumber());
    }

    public void completeAllPausedTransactions() {
        if (pausedTransactions.isEmpty()) {
            return;
        }

        for (Transaction tx : new ArrayList<>(pausedTransactions)) {
            if (tx.getDestinationAccount() == this && tx.getTransactionStatus() == enums.TransactionStatus.PAUSED) {
                completePausedTransferIn(tx);
            }
        }
    }

    public void completePausedTransferIn(Transaction pausedTransaction) {
        if (pausedTransaction == null) {
            return;
        }
        if (accountStatus != AccountStatus.ACTIVE) {
            System.out.println("Error: Destination account must be ACTIVE before completing a paused transaction.");
            return;
        }
        Account source = pausedTransaction.getSourceAccount();
        if (source == null) {
            System.out.println("Error: Source account is missing.");
            return;
        }

        balance += pausedTransaction.getAmount();
        pausedTransaction.markAsCompleted(balance);
        pausedTransactions.remove(pausedTransaction);
        source.pausedTransactions.remove(pausedTransaction);

        Account originalDestination = pausedTransaction.getDestinationAccount();
        if (originalDestination != null && originalDestination != this) {
            originalDestination.pausedTransactions.remove(pausedTransaction);
        }

        PausedTransaction.remove(PausedTransaction.findPausedTransactionById(pausedTransaction.getTransactionId()));
        System.out.println("Paused transaction " + pausedTransaction.getTransactionId()
                + " completed successfully. Amount credited: $" + String.format("%.2f", pausedTransaction.getAmount()));
    }

    public void deposit(double amount) {

    }

    public void freezeAccount() {

        if (accountStatus == AccountStatus.FROZEN) {
            System.out.println("account all ready frozen ");
        }
        if (accountStatus == AccountStatus.ACTIVE) {
            accountStatus = AccountStatus.FROZEN;
        }
    }

    public void pauseTransfer(Transaction transaction, Account destinationAccount) {
        balance -= transaction.getAmount();
        GlobalData.addWithdrawMoney(transaction.getAmount());
        transaction.markAsPaused(balance, this);
        transactionHistory.add(transaction);
        pausedTransactions.add(transaction);
        destinationAccount.pausedTransactions.add(transaction);

        PausedTransaction pausedTx = new PausedTransaction(
                transaction.getTransactionId(),
                transaction.getTransactionType(),
                transaction.getTransactionTime(),
                transaction.getAmount(),
                transaction.getTransactionStatus(),
                this,
                transaction);
        PausedTransaction.add(pausedTx);

        System.out.println("Transfer paused: destination account is not ACTIVE.");
    }

    public void processEndOfMonth() {

    }

    public void transferIn(double amount, Account sourceAccount) {

    }

    public void transferIn(double amount, Account sourceAccount, String description) {
        transferIn(amount, sourceAccount);
    }

    public void transferOut(double amount, Account destinationAccount) {

    }

    public void transferOut(double amount, Account destinationAccount, String description) {
        transferOut(amount, destinationAccount);
    }

    public void validateCloseAccount() throws ClosedAccountHasUnpaidFeeException, ClosedAccountHasBalanceException, ClosedCheckingAccountOverdrawnException {

        if (accountStatus != AccountStatus.ACTIVE) {
            throw new ClosedAccountHasBalanceException("Only ACTIVE accounts can be closed.");
        }
        if (this instanceof CheckingAccount) {
            CheckingAccount checking = (CheckingAccount) this;
            if (checking.getUnpaidFeeAmount() > 0.000001) {
                throw new ClosedAccountHasUnpaidFeeException("Checking account has unpaid fees. Clear all fees before closing.");
            }
            if (checking.getBalance() < -0.000001) {
                throw new ClosedCheckingAccountOverdrawnException("Checking account is overdrawn. Clear the debt before closing.");
            }
        }
        if (balance > 0.000001) {
            throw new ClosedAccountHasBalanceException("Account still has funds. Withdraw or transfer out the remaining balance before closing. Current balance: $" + String.format("%.2f", balance));
        }

    }

    public void withdraw(double amount) {

    }

    private boolean canActivateFromTransfer(Account sourceAccount, double totalFEE) {
        if (sourceAccount == null) {
            return false;
        }

        if (sourceAccount.getAccountType() == AccountType.FIXED_DEPOSIT) {
            System.out.println("Error: Cannot activate from Fixed Deposit account.");
            return false;
        }

        if (sourceAccount.getBalance() < totalFEE) {
            System.out.println("Error: Insufficient balance to pay activation fee of " + totalFEE);
            return false;
        }

        if (sourceAccount instanceof SavingsAccount
                && sourceAccount.getBalance() - totalFEE < AccountFeeConstants.SAVING_ACCOUNT_MINIMUM_BALANCE.getValue()) {
            System.out.println("Error: Activation payment would reduce the Savings balance below its minimum.");
            return false;
        }

        if (sourceAccount instanceof CheckingAccount
                && sourceAccount.getBalance() - totalFEE < AccountFeeConstants.CHECKING_ACCOUNT_OVERDRAFT_LIMIT.getValue()) {
            System.out.println("Error: Activation payment would exceed the Checking overdraft limit.");
            return false;
        }

        if (sourceAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            System.out.println("Error: Source account must be ACTIVE.");
            return false;
        }

        return true;
    }

    public static int getAccountCounter() {
        return accountCounter;
    }

    public static void setAccountCounter(int counter) {
        accountCounter = Math.max(0, counter);
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Customer getAccountOwner() {
        return accountOwner;
    }

    public String getAccountOwnerId() {
        return accountOwner.getCustomerIdString();
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public double getBalance() {
        return balance;
    }

    public String getOpeningDate() {
        return openingDate;
    }

    public int getTransactionCount() {
        return transactionHistory.size();
    }

    public ArrayList<Transaction> getTransactionsByDateRange(String startDate, String endDate) {
        ArrayList<Transaction> filtered = new ArrayList<>();

        if (startDate == null || endDate == null) {
            System.out.println("Start date and end date are required.");
            return filtered;
        }

        LocalDate start;
        LocalDate end;

        try {
            start = DateTimeManager.stringToDate(startDate);
            end = DateTimeManager.stringToDate(endDate);
        } catch (RuntimeException ex) {
            System.out.println("Invalid date. Use YYYY-MM-DD.");
            return filtered;
        }

        if (end.isBefore(start)) {
            System.out.println("End date cannot be before start date.");
            return filtered;
        }

        for (Transaction tx : transactionHistory) {
            try {
                LocalDate txDate = DateTimeManager.stringToDate(tx.getTransactionTime().substring(0, 10));
                if (!txDate.isBefore(start) && !txDate.isAfter(end)) {
                    filtered.add(tx);
                }
            } catch (RuntimeException ignored) {
            }
        }

        return filtered;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    protected void setBalance(double balance) {
        if (!Double.isFinite(balance)) {
            throw new IllegalArgumentException("Balance must be a finite number");
        }
        if (balance < 0 && this.accountType != AccountType.CHECKING) {
            throw new IllegalArgumentException(
                    "Negative balance not allowed for " + this.accountType);
        }
        this.balance = balance;
    }

    public void displayAccountInfo() {
        System.out.println("\n===== Account Information =====");
        System.out.println("Account Number: " + getAccountNumber());
        System.out.println("Owner ID: " + getAccountOwnerId());
        System.out.println("Opening Date: " + getOpeningDate());
        System.out.println("Status: " + getAccountStatus());
        System.out.println("Type: " + getAccountType());
        System.out.println("Balance: $" + String.format("%.2f", getBalance()));
        System.out.println("================================\n");
    }

    public void displayPausedTransactionsForAccount() {
        if (pausedTransactions.isEmpty()) {
            System.out.println("No transactions found for this account.");
            return;
        }

        System.out.println("\n===== Transaction  for " + accountNumber + " =====");
        for (Transaction transaction : pausedTransactions) {
            System.out.println("TransactionId " + transaction.getTransactionId() + "TransactionType" + transaction.getTransactionType() + "amount" + transaction.getAmount());

        }
    }

    public void displayTransactionHistory() {
        if (transactionHistory.isEmpty()) {
            System.out.println("No transactions found for this account.");
            return;
        }

        System.out.println("\n===== Transaction History for " + accountNumber + " =====");
        for (Transaction transaction : transactionHistory) {
            transaction.transactionInfo();
        }
        System.out.println("=====================================================\n");
    }

    public void displayTransactionsByDateRange(String startDate, String endDate) {
        ArrayList<Transaction> transactions = getTransactionsByDateRange(startDate, endDate);

        if (transactions.isEmpty()) {
            System.out.println("No transactions found between " + startDate + " and " + endDate);
            return;
        }

        double openingBalance = 0;
        Transaction lastBeforeStart = null;

        for (Transaction tx : transactionHistory) {
            String txDate = tx.getTransactionTime().substring(0, 10);

            if (txDate.compareTo(startDate) < 0 && tx.getTransactionStatus() == TransactionStatus.COMPLETED) {
                lastBeforeStart = tx;
            }
        }

        if (lastBeforeStart != null) {
            openingBalance = lastBeforeStart.getResultingBalance();
        }

        double closingBalance = openingBalance;
        if (!transactions.isEmpty()) {
            closingBalance = transactions.get(transactions.size() - 1).getResultingBalance();
        }

        System.out.println("\n===== Transactions: " + startDate + " to " + endDate + " =====");
        System.out.println("Account: " + accountNumber + " (" + accountType + ")");
        System.out.println("Opening Balance (before " + startDate + "): $" + String.format("%.2f", openingBalance));
        System.out.println("-----------------------------------------------------------");

        for (Transaction tx : transactions) {
            System.out.printf("%-19s | %-30s | $%10.2f | %s%n",
                    tx.getTransactionTime().substring(0, 19),
                    tx.getTransactionType(),
                    tx.getAmount(),
                    tx.getTransactionStatus());
        }

        System.out.println("-----------------------------------------------------------");
        System.out.println("Closing Balance (after " + endDate + "): $" + String.format("%.2f", closingBalance));
        System.out.println("Net Change: $" + String.format("%.2f", (closingBalance - openingBalance)));
        System.out.println("Total Transactions: " + transactions.size());
        System.out.println("=====================================================================\n");
    }

    @Override
    public String toString() {
        return getAccountType() + "  •  " + getAccountStatus() + "  •  "
                + String.format("$%,.2f", getBalance());
    }
}
