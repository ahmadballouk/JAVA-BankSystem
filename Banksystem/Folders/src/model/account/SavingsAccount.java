package model.account;

import enums.AccountFeeConstants;
import enums.AccountType;
import enums.TransactionType;
import exception.InsufficientFundsException;
import exception.LinkedAccountInactiveException;
import exception.MinimumBalanceException;
import exception.UnsupportedAccountOperationException;
import interfaces.InterestBearing;
import java.time.LocalDateTime;
import model.customer.Customer;
import model.transaction.Transaction;
import service.GlobalData;
import validation.TransactionValidator;

public class SavingsAccount extends Account implements InterestBearing {

    private static final double MONTHLY_INTEREST_RATE =
            AccountFeeConstants.SAVING_ACCOUNT_MONTHLY_INTEREST_RATE.getValue();

    public SavingsAccount(Customer accountOwner, AccountType accountType, LocalDateTime timestamp, double initialBalance) {
        super(accountOwner, accountType, timestamp);
        if (initialBalance > 0)
        {
            this.setBalance(initialBalance);
        }
        GlobalData.getSavingsAccounts().add(this);
    }

    @Override
    public void applyInterest() {
        double interestEarned = calculateMonthlyInterest();
        Transaction transaction = new Transaction(this, TransactionType.interestPayout, interestEarned);

        try {
            TransactionValidator.validateSavingsAccountTransactions(
                    TransactionType.interestPayout, this, interestEarned, null);

            balance += interestEarned;
            transaction.markAsCompleted(balance);
            transactionHistory.add(transaction);
            GlobalData.addInterestMoney(interestEarned);

            System.out.printf("Interest applied: $%.2f | New balance: $%.2f%n", interestEarned, balance);

        } catch (UnsupportedAccountOperationException e) {
            System.out.println(e.getMessage());
            transaction.markAsFailed(balance);
            transactionHistory.add(transaction);
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
            TransactionValidator.validateSavingsAccountTransactions(
                    TransactionType.Deposit, this, amount, null);

            balance += amount;
            transaction.markAsCompleted(balance);
            transactionHistory.add(transaction);
            GlobalData.addDepositMoney(amount);

            System.out.printf("Deposit successful: $%.2f | New balance: $%.2f%n", amount, balance);

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
            TransactionValidator.validateSavingsAccountTransactions(
                    TransactionType.transfer_out, this, amount, destinationAccount);

            balance -= amount;
            transaction.markAsCompleted(balance);
            transactionHistory.add(transaction);

            destinationAccount.transferIn(amount, this, description);

        } catch (LinkedAccountInactiveException ex) {
            pauseTransfer(transaction, destinationAccount);
        } catch (UnsupportedAccountOperationException ex) {
            if (destinationAccount.getAccountStatus() == enums.AccountStatus.FROZEN) {
                pauseTransfer(transaction, destinationAccount);
            } else {
                System.out.println(ex.getMessage());
                transaction.markAsFailed(balance);
                transactionHistory.add(transaction);
            }
        } catch (MinimumBalanceException | InsufficientFundsException ex) {
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
            TransactionValidator.validateSavingsAccountTransactions(
                    TransactionType.Withdrawal, this, amount, null);

            balance -= amount;
            transaction.markAsCompleted(balance);
            transactionHistory.add(transaction);
            GlobalData.addWithdrawMoney(amount);

            System.out.printf("Withdrawal successful: $%.2f | Remaining balance: $%.2f%n", amount, balance);

        } catch (MinimumBalanceException e) {
            System.out.println(e.getMessage());
            transaction.markAsFailed(balance);
            transactionHistory.add(transaction);

        } catch (InsufficientFundsException e) {
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
        System.out.println("Processing Saving Account: " + this.accountNumber);
        applyInterest();
    }

    private double calculateMonthlyInterest() {
        return balance * (MONTHLY_INTEREST_RATE / 12);
    }
}
