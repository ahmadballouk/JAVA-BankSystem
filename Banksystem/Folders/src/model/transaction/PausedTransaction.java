package model.transaction;

import enums.TransactionStatus;
import enums.TransactionType;
import java.util.ArrayList;
import java.util.List;
import model.account.Account;

public class PausedTransaction implements java.io.Serializable {

    private static final List<PausedTransaction> pausedTransactionList = new ArrayList<>();

    private final String transactionId;
    private final TransactionType transactionType;
    private final String transactionTime;
    private final double amount;
    private final Account sourceAccount;
    private final Transaction originalTransaction;

    private final TransactionStatus transactionStatus;

    public PausedTransaction(
            String transactionId,
            TransactionType transactionType,
            String transactionTime,
            double amount,
            TransactionStatus transactionStatus,
            Account sourceAccount,
            Transaction originalTransaction) {

        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.transactionTime = transactionTime;
        this.amount = amount;
        this.transactionStatus = transactionStatus;
        this.sourceAccount = sourceAccount;
        this.originalTransaction = originalTransaction;
    }

    public static void add(PausedTransaction pausedTransaction) {
        pausedTransactionList.add(pausedTransaction);
    }

    public static PausedTransaction findPausedTransactionById(String transactionId) {
        if (transactionId == null) {
            return null;
        }
        for (PausedTransaction pausedTx : pausedTransactionList) {
            if (pausedTx.getTransactionId().equals(transactionId)) {
                return pausedTx;
            }
        }
        return null;
    }

    public static void remove(PausedTransaction pausedTransaction) {
        if (pausedTransaction != null) {
            pausedTransactionList.remove(pausedTransaction);
        }
    }

    public double getAmount() {
        return amount;
    }

    public Account getDestinationAccount() {
        return originalTransaction != null ? originalTransaction.getDestinationAccount() : null;
    }

    public Transaction getOriginalTransaction() {
        return originalTransaction;
    }

    public static List<PausedTransaction> getPausedList() {
        return new ArrayList<>(pausedTransactionList);
    }

    public static void restorePausedList(List<PausedTransaction> saved) {
        pausedTransactionList.clear();
        if (saved != null) pausedTransactionList.addAll(saved);
    }

    public Account getSourceAccount() {
        return sourceAccount;
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

    public boolean hasValidDestination() {
        Account destination = getDestinationAccount();
        return destination != null && service.GlobalData.getAllAccounts().contains(destination);
    }

    public boolean isOrphaned() {
        Account destination = getDestinationAccount();
        return destination == null
                || !hasValidDestination()
                || destination.getAccountStatus() == enums.AccountStatus.CLOSED;
    }
}
