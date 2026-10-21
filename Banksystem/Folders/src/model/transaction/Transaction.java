package model.transaction;

import enums.TransactionStatus;
import enums.TransactionType;
import model.account.Account;
import service.GlobalData;
import static util.DateTimeManager.getFormattedDateTimeString;

public class Transaction implements java.io.Serializable {

    private static int transactionCounter = 1;

    private final String transactionId;
    private final String accountNumber;
    private final TransactionType transactionType;
    private final double amount;
    private String description;

    private final String transactionTime;
    private double resultingBalance;
    private TransactionStatus transactionStatus;
    private Account sourceAccount;
    private Account destinationAccount;

    public Transaction(Account account, TransactionType transactionType, double amount) {
        this(account, transactionType, amount, "");
    }

    public Transaction(Account account, TransactionType transactionType, double amount, String description) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }

        if (transactionType == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }

        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException("Transaction amount must be a finite non-negative number");
        }

        this.transactionId = String.format("TXN%06d", transactionCounter);
        transactionCounter++;

        this.accountNumber = account.getAccountNumber();
        this.transactionType = transactionType;
        this.transactionTime = getFormattedDateTimeString();
        this.amount = amount;
        this.description = description == null ? "" : description.trim();
        this.transactionStatus = null;
        GlobalData.addTransaction(this);
        GlobalData.recordTransactionCreated();
    }

    public void markAsCompleted(double balance) {
        this.transactionStatus = TransactionStatus.COMPLETED;
        this.resultingBalance = balance;
    }

    public void markAsFailed(double balance) {
        this.transactionStatus = TransactionStatus.FAILED;
        this.resultingBalance = balance;
    }

    public void markAsPaused(double balance, Account sourceAccount) {
        this.sourceAccount = sourceAccount;
        this.transactionStatus = TransactionStatus.PAUSED;
        this.resultingBalance = balance;
    }

    public void transactionInfo() {
        System.out.printf(
                "ID: %s | Type: %s | Amount: $%.2f | Status: %s | Balance: $%.2f | Time: %s%n",
                getTransactionId(),
                getTransactionType(),
                getAmount(),
                getTransactionStatus(),
                getResultingBalance(),
                getTransactionTime());
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description.trim();
    }

    public Account getDestinationAccount() {
        return destinationAccount;
    }

    public double getResultingBalance() {
        return resultingBalance;
    }

    public Account getSourceAccount() {
        return sourceAccount;
    }

    public static int getTransactionCounter() {
        return transactionCounter;
    }

    public static void setTransactionCounter(int counter) {
        transactionCounter = Math.max(1, counter);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public String getTransactionTime() {
        return transactionTime;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setDestinationAccount(Account destinationAccount) {
        this.destinationAccount = destinationAccount;
    }

    public void setSourceAccount(Account sourceAccount) {
        this.sourceAccount = sourceAccount;
    }
}
