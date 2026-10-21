package model.account;

import enums.AccountFeeConstants;
import enums.AccountStatus;
import enums.AccountType;
import enums.FixedDepositAccountType;
import enums.TransactionType;
import exception.EarlyWithdrawalException;
import exception.InsufficientFundsException;
import exception.UnsupportedAccountOperationException;
import interfaces.FeeCharging;
import interfaces.InterestBearing;
import java.time.LocalDateTime;
import model.customer.Customer;
import model.term.Term;
import model.transaction.PausedTransaction;
import model.transaction.Transaction;
import service.GlobalData;
import validation.TransactionValidator;

public class FixedDepositAccount extends Account implements InterestBearing, FeeCharging {

    private double annualInterestRate;
    private double penaltyFee;
    private boolean interestDeposited;
    private double maturityInterestAmount;
    private final Account linkedAccount;
    private final FixedDepositAccountType depositType;
    private boolean allowTransferMode;
    private final Term term;

    public FixedDepositAccount(
            Customer accountOwner,
            AccountType accountType,
            double initialBalance,
            int termMonths,
            Account linkedAccount,
            FixedDepositAccountType depositType,LocalDateTime timestamp) {

        super(accountOwner, accountType,timestamp);
        this.term = new Term(termMonths);
        this.setBalance(initialBalance);
        this.linkedAccount = linkedAccount;
        this.depositType = depositType;
        this.interestDeposited = false;
        this.maturityInterestAmount = 0.0;
        this.allowTransferMode = false;

        calculateInterestRateAndPenalty(termMonths);

        GlobalData.getFixedDepositAccounts().add(this);
    }

    @Override
    public void applyFee() {
        Transaction transaction = new Transaction(this, TransactionType.earlyWithdrawalPenalty, penaltyFee);

        try {
            TransactionValidator.validateFixedDepositAccountTransactions(
                    TransactionType.feeCharge, this, penaltyFee, null);

            double newBalance = this.balance - penaltyFee;
            this.setBalance(newBalance);
            GlobalData.addFeeMoney(penaltyFee);
            transaction.markAsCompleted(balance);
            transactionHistory.add(transaction);

        } catch (UnsupportedAccountOperationException e) {
            System.out.println(e.getMessage());
            transaction.markAsFailed(balance);
            transactionHistory.add(transaction);
        }
    }

    @Override
    public void applyInterest() {
        double interestEarned = calculateMaturityInterest();
        Transaction transaction = new Transaction(this, TransactionType.interestPayout, interestEarned);

        try {
            TransactionValidator.validateFixedDepositAccountTransactions(
                    TransactionType.interestPayout, this, interestEarned, null);

            double newBalance = this.balance + interestEarned;
            this.setBalance(newBalance);
            GlobalData.addInterestMoney(interestEarned);

            this.maturityInterestAmount = interestEarned;
            this.interestDeposited = true;
            transaction.markAsCompleted(balance);
            transactionHistory.add(transaction);

            System.out.printf("Interest applied: $%.2f | New balance: $%.2f%n", interestEarned, balance);

        } catch (UnsupportedAccountOperationException e) {
            System.out.println(e.getMessage());
            transaction.markAsFailed(balance);
            transactionHistory.add(transaction);
        }
    }

    @Override
    public void deposit(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            System.out.println("Error: Deposit amount must be greater than 0.");
            return;
        }

        Transaction transaction = new Transaction(this, TransactionType.Deposit, amount);

        try {
            TransactionValidator.validateFixedDepositAccountTransactions(
                    TransactionType.Deposit, this, amount, null);

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

        System.out.println("Error: Fixed Deposit accounts cannot receive transfers. " +
                "Principal is locked for the contract duration.");
    }

    public void transferOut(double amount, Account destinationAccount) {
        transferOut(amount, destinationAccount, "");
    }

    @Override
    public void transferOut(double amount, Account destinationAccount, String description) {
        if (!Double.isFinite(amount) || amount <= 0) {
            System.out.println("Error: Fixed Deposit payout amount must be greater than 0.");
            return;
        }

        Transaction transaction = new Transaction(this, TransactionType.FixedDepositAccountPayout, amount);
        transaction.setDestinationAccount(destinationAccount);
        if (accountStatus != AccountStatus.ACTIVE) {
            transaction.markAsFailed(balance);
            transactionHistory.add(transaction);
            System.out.println("Error: Fixed Deposit must be ACTIVE for a payout.");
            return;
        }
        if (!allowTransferMode) {
            transaction.markAsFailed(balance);
            transactionHistory.add(transaction);
            System.out.println("Error: Fixed Deposit payout is only allowed during maturity processing.");
            return;
        }
        if (amount <= 0 || amount > balance + 0.000001) {
            transaction.markAsFailed(balance);
            transactionHistory.add(transaction);
            System.out.println("Error: Invalid Fixed Deposit payout amount.");
            return;
        }
        boolean destinationExists = destinationAccount != null && GlobalData.getAllAccounts().contains(destinationAccount);
        if (!destinationExists) {
            balance -= amount;
            if (Math.abs(balance) < 0.000001) balance = 0.0;
            GlobalData.addWithdrawMoney(amount);
            transaction.setDestinationAccount(null);
            transaction.markAsPaused(balance, this);
            transactionHistory.add(transaction);
            pausedTransactions.add(transaction);
            PausedTransaction.add(new PausedTransaction(
                    transaction.getTransactionId(), transaction.getTransactionType(), transaction.getTransactionTime(),
                    transaction.getAmount(), transaction.getTransactionStatus(), this, transaction));
            if (depositType == FixedDepositAccountType.CLOSE_AND_PAYOUT) {
                accountStatus = AccountStatus.CLOSED;
                getAccountOwner().moveToClosedAccounts(this);
                allowTransferMode = false;

                System.out.println(
                        "Payout is held as an orphaned paused transaction because the linked account no longer exists. Fixed Deposit closed.");

            } else {

                System.out.println("Payout is held as an orphaned paused transaction because the linked account no longer exists.");
            }
            return;
        }
        if (destinationAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            balance -= amount;
            if (Math.abs(balance) < 0.000001) balance = 0.0;
            GlobalData.addWithdrawMoney(amount);
            transaction.markAsPaused(balance, this);
            transactionHistory.add(transaction);
            pausedTransactions.add(transaction);
            destinationAccount.pausedTransactions.add(transaction);
            PausedTransaction.add(new PausedTransaction(
                    transaction.getTransactionId(), transaction.getTransactionType(), transaction.getTransactionTime(),
                    transaction.getAmount(), transaction.getTransactionStatus(), this, transaction));
            if (depositType == FixedDepositAccountType.CLOSE_AND_PAYOUT) {
                accountStatus = AccountStatus.CLOSED;
                getAccountOwner().moveToClosedAccounts(this);
                allowTransferMode = false;

            }
            System.out.println("Payout paused: linked account is not ACTIVE. The funds have already been deducted from the Fixed Deposit.");
            return;
        }
        destinationAccount.transferIn(amount, this, description);
        balance -= amount;
        if (Math.abs(balance) < 0.000001) balance = 0.0;
        GlobalData.addWithdrawMoney(amount);
        transaction.markAsCompleted(balance);
        transactionHistory.add(transaction);
        if (depositType == FixedDepositAccountType.CLOSE_AND_PAYOUT && Math.abs(balance) < 0.000001) {
            accountStatus = AccountStatus.CLOSED;
            getAccountOwner().moveToClosedAccounts(this);
            allowTransferMode = false;

            System.out.println("Account closed and payout completed.");
        } else {
            System.out.printf("Payout completed: $%.2f | Remaining: $%.2f%n", amount, balance);
        }
    }

    @Override
    public void withdraw(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            System.out.println("Error: Withdrawal amount must be greater than 0.");
            return;
        }

        Transaction transaction = new Transaction(this, TransactionType.Withdrawal, amount);

        try {
            TransactionValidator.validateFixedDepositAccountTransactions(
                    TransactionType.Withdrawal, this, amount, null);

            double newBalance = this.balance - amount;
            this.setBalance(newBalance);
            GlobalData.addWithdrawMoney(amount);
            transaction.markAsCompleted(balance);
            transactionHistory.add(transaction);

            System.out.printf("Withdrawal successful: $%.2f | Remaining balance: $%.2f%n", amount, balance);

        } catch (EarlyWithdrawalException e) {
            System.out.println(e.getMessage());

            applyFee();

            double newBalance = this.balance - amount;
            this.setBalance(newBalance);
            transaction.markAsCompleted(balance);
            transactionHistory.add(transaction);

            System.out.printf(
                    "Early withdrawal processed: $%.2f | Penalty: $%.2f | Remaining: $%.2f%n",
                    amount,
                    this.penaltyFee,
                    balance);

        } catch (UnsupportedAccountOperationException e) {
            System.out.println(e.getMessage());
            transaction.markAsFailed(balance);
            transactionHistory.add(transaction);
        } catch (InsufficientFundsException e) {
            System.out.println(e.getMessage());
            transaction.markAsFailed(balance);
            transactionHistory.add(transaction);
        }
    }

    public void processEndOfMonth() {
        System.out.println("Processing Fixed Deposit Account: " + this.accountNumber);

        if (term.isMatured() && (!interestDeposited || allowTransferMode)) {
            System.out.println("  Account matured. Processing maturity...");
            processMaturity();
        } else if (term.isMatured()) {
            System.out.println("  Account is matured and maturity processing is complete.");
        } else {
            System.out.println("  Account matures in " + term.getMonthsRemaining() + " months");
        }
    }

    public void processMaturity() {
        if (!term.isMatured()) {
            System.out.println("Error: Account has not yet matured.");
            return;
        }

        allowTransferMode = true;
        if (!interestDeposited) {
            applyInterest();
            if (!interestDeposited) {
                allowTransferMode = false;
                return;
            }
        }
        int currentTermMonths = term.getTotalMonths();
        double interestEarned = maturityInterestAmount;
        double totalAmount = balance;
        switch (depositType) {
            case AUTO_RENEW_PRINCIPAL_AND_INTEREST:
                renewTerm(currentTermMonths);
                break;
            case AUTO_RENEW_PRINCIPAL_ONLY:
                renewTerm(currentTermMonths);
                allowTransferMode = true;
                transferOut(interestEarned, linkedAccount);
                allowTransferMode = false;
                System.out.println("Principal renewed. Interest payout is completed or waiting for destination resolution. New maturity date: " + term.getMaturityDate());
                break;
            case CLOSE_AND_PAYOUT:
                transferOut(totalAmount, linkedAccount);
                break;
        }
    }

    public void renewTerm(int newTermMonths) {
        if (newTermMonths <= 0) {
            throw new IllegalArgumentException("Term months must be greater than 0");
        }
        term.renew(newTermMonths);
        calculateInterestRateAndPenalty(newTermMonths);
        this.interestDeposited = false;
        this.maturityInterestAmount = 0.0;
        this.allowTransferMode = false;
    }

    private void calculateInterestRateAndPenalty(int termMonths) {
        if (termMonths <= 6) {
            this.annualInterestRate = AccountFeeConstants.FD_INTEREST_SHORT_TERM.getValue();
            this.penaltyFee = AccountFeeConstants.FD_PENALTY_SHORT_TERM.getValue();
        } else if (termMonths <= 12) {
            this.annualInterestRate = AccountFeeConstants.FD_INTEREST_MEDIUM_TERM.getValue();
            this.penaltyFee = AccountFeeConstants.FD_PENALTY_MEDIUM_TERM.getValue();
        } else {
            this.annualInterestRate = AccountFeeConstants.FD_INTEREST_LONG_TERM.getValue();
            this.penaltyFee = AccountFeeConstants.FD_PENALTY_LONG_TERM.getValue();
        }
    }

    private double calculateMaturityInterest() {
        double termYears = (double) term.getTotalMonths() / 12.0;
        return this.balance * (this.annualInterestRate / 100.0) * termYears;
    }

    public double getAnnualInterestRate() {
        return this.annualInterestRate;
    }

    public FixedDepositAccountType getDepositType() {
        return this.depositType;
    }

    public Account getLinkedAccount() {
        return this.linkedAccount;
    }

    public String getMaturityStatus() {
        if (this.term.isMatured()) {
            return "MATURED";
        } else {
            return "ACTIVE (" + this.term.getMonthsRemaining() + " months remaining)";
        }
    }

    public double getPenaltyFee() {
        return this.penaltyFee;
    }

    public Term getTerm() {
        return this.term;
    }

    public boolean isAllowTransfer() {
        return this.allowTransferMode;
    }

    public boolean isInterestDeposited() {
        return this.interestDeposited;
    }

    public void displayFixedAccountInfo()
    {

        String linkedAccNum = (linkedAccount != null) ? linkedAccount.getAccountNumber() : "None";
        double expectedInterest = calculateMaturityInterest();

        System.out.println("\n==================================================");
        System.out.println("           FIXED DEPOSIT ACCOUNT INFO             ");
        System.out.println("==================================================");
        System.out.println(" Account Number      : " + getAccountNumber());
        System.out.println(" Owner ID            : " + getAccountOwnerId());
        System.out.println(" Account Status      : " + getAccountStatus());
        System.out.println(" Current Balance     : $" + String.format("%.2f", balance));
        System.out.println("--------------------------------------------------");
        System.out.println(" Term Duration       : " + term.getTotalMonths() + " months");
        System.out.println(" Maturity Status     : " + getMaturityStatus());
        System.out.println(" Annual Interest Rate: " + String.format("%.2f", annualInterestRate) + "%");
        System.out.println(" Estimated Interest  : $" + String.format("%.2f", expectedInterest));
        System.out.println(" Early Penalty Fee   : $" + String.format("%.2f", penaltyFee));
        System.out.println("--------------------------------------------------");
        System.out.println(" Deposit Type        : " + depositType);
        System.out.println(" Linked Account      : " + linkedAccNum);
        System.out.println(" Interest Deposited  : " + (interestDeposited ? "Yes" : "No"));
        System.out.println(" Transfer Allowed    : " + (allowTransferMode ? "Yes" : "No"));
        System.out.println("==================================================\n");
    }

}
