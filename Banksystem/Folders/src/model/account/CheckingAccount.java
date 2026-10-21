package model.account;

import enums.AccountFeeConstants;
import enums.AccountStatus;
import enums.AccountType;
import enums.TransactionType;
import exception.CheckingAccountFeeNotPaidException;
import exception.LinkedAccountInactiveException;
import exception.OverdraftLimitException;
import exception.UnsupportedAccountOperationException;
import interfaces.FeeCharging;
import java.time.LocalDateTime;
import model.customer.Customer;
import model.transaction.Transaction;
import service.GlobalData;
import validation.TransactionValidator;

public class CheckingAccount extends Account implements FeeCharging {

    private double unpaidFeeAmount;

    public CheckingAccount(Customer accountOwner, AccountType accountType, double initialBalance, LocalDateTime timestamp) {
        super(accountOwner, accountType, timestamp);
        if (initialBalance > 0)
        {
            this.setBalance(initialBalance);

        }

        this.unpaidFeeAmount = 0.0;
        GlobalData.getCheckingAccounts().add(this);
    }

    @Override
    public void applyFee() {
        double feeAmount = AccountFeeConstants.CHECKING_ACCOUNT_MONTHLY_FEE.getValue();
        Transaction transaction = new Transaction(this, TransactionType.feeCharge, feeAmount);

        if (accountStatus == AccountStatus.FROZEN) {
            transaction.markAsFailed(balance);
            transactionHistory.add(transaction);
            unpaidFeeAmount += feeAmount;
            System.out.println("Fee charged to frozen account. " +
                    "Fee amount: $" + String.format("%.2f", feeAmount) +
                    " | Total unpaid fees: $" + String.format("%.2f", unpaidFeeAmount));
            return;
        }

        try {
            TransactionValidator.validateCheckingAccountTransactions(
                    TransactionType.feeCharge, this, feeAmount, null);

            balance -= feeAmount;
            GlobalData.addFeeMoney(feeAmount);
            transaction.markAsCompleted(balance);
            transactionHistory.add(transaction);

        } catch (UnsupportedAccountOperationException e) {
            System.out.println(e.getMessage());
            transaction.markAsFailed(balance);
            transactionHistory.add(transaction);
            unpaidFeeAmount += feeAmount;

        } catch (CheckingAccountFeeNotPaidException e) {
            System.out.println(e.getMessage());
            transaction.markAsFailed(balance);
            transactionHistory.add(transaction);

            freezeAccount();
            unpaidFeeAmount += feeAmount;
        }
    }

    @Override
    public void deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Error: Deposit amount must be greater than 0");
            return;
        }

        Transaction transaction = new Transaction(this, TransactionType.Deposit, amount);

        try {
            TransactionValidator.validateCheckingAccountTransactions(
                    TransactionType.Deposit, this, amount, null);

            balance += amount;
            GlobalData.addDepositMoney(amount);
            transaction.markAsCompleted(balance);
            transactionHistory.add(transaction);

        } catch (UnsupportedAccountOperationException e) {
            System.out.println(e.getMessage());
            transaction.markAsFailed(balance);
            transactionHistory.add(transaction);
        }
    }

    @Override
    public void transferIn(double amount, Account sourceAccount) {
        transferIn(amount, sourceAccount, "");
    }

    public void transferIn(double amount, Account sourceAccount, String description) {
        Transaction transaction = new Transaction(this, TransactionType.transfer_in, amount, description);
        balance += amount;
        transaction.setSourceAccount(sourceAccount);
        transaction.markAsCompleted(balance);
        transactionHistory.add(transaction);
    }

    public void transferOut(double amount, Account destinationAccount) {
        transferOut(amount, destinationAccount, "");
    }

    @Override
    public void transferOut(double amount, Account destinationAccount, String description) {
        if (amount <= 0 || !Double.isFinite(amount)) {
            System.out.println("Error: Transfer amount must be greater than 0.");
            return;
        }

        if (destinationAccount == null) {
            System.out.println("Error: Destination account cannot be null.");
            return;
        }

        if (destinationAccount == this) {
            System.out.println("Error: Source and destination accounts must be different.");
            return;
        }

        Transaction transaction = new Transaction(this, TransactionType.transfer_out, amount, description);
        transaction.setDestinationAccount(destinationAccount);

        try {
            TransactionValidator.validateCheckingAccountTransactions(
                    TransactionType.transfer_out, this, amount, destinationAccount);

            balance -= amount;
            transaction.markAsCompleted(balance);
            transactionHistory.add(transaction);

            destinationAccount.transferIn(amount, this, description);

        } catch (LinkedAccountInactiveException ex) {
            pauseTransfer(transaction, destinationAccount);
        } catch (UnsupportedAccountOperationException ex) {

            System.out.println(ex.getMessage());
            transaction.markAsFailed(balance);
            transactionHistory.add(transaction);

        } catch (OverdraftLimitException ex) {
            System.out.println(ex.getMessage());
            transaction.markAsFailed(balance);
            transactionHistory.add(transaction);
        }
    }

    @Override
    public void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Error: Withdrawal amount must be greater than 0");
            return;
        }

        Transaction transaction = new Transaction(this, TransactionType.Withdrawal, amount);

        try {
            TransactionValidator.validateCheckingAccountTransactions(
                    TransactionType.Withdrawal, this, amount, null);

            balance -= amount;
            GlobalData.addWithdrawMoney(amount);
            transaction.markAsCompleted(balance);
            transactionHistory.add(transaction);

        } catch (OverdraftLimitException e) {
            System.out.println(e.getMessage());
            transaction.markAsFailed(balance);
            transactionHistory.add(transaction);

        } catch (UnsupportedAccountOperationException e) {
            System.out.println(e.getMessage());
            transaction.markAsFailed(balance);
            transactionHistory.add(transaction);
        }
    }

    public void processEndOfMonth() {
        System.out.println("Processing Checking Account: " + this.accountNumber);
        applyFee();
    }

    public double getUnpaidFeeAmount() {
        return unpaidFeeAmount;
    }

    public void setUnpaidFeeAmount(double amount) {
        this.unpaidFeeAmount = amount;
    }
}
