package validation;

import enums.AccountFeeConstants;
import enums.AccountStatus;
import enums.AccountType;
import enums.TransactionType;
import exception.AccountNotMaturedException;
import exception.CheckingAccountFeeNotPaidException;
import exception.EarlyWithdrawalException;
import exception.FrozenAccountFeeException;
import exception.InsufficientFundsException;
import exception.LinkedAccountInactiveException;
import exception.LinkedAccountNotFoundException;
import exception.MinimumBalanceException;
import exception.OverdraftLimitException;
import exception.UnsupportedAccountOperationException;
import model.account.Account;
import model.account.FixedDepositAccount;

public class TransactionValidator {

    public static void validateCheckingAccountTransactions(
            TransactionType transactionType,
            Account account,
            double amount,
            Account transferToAccount)
            throws UnsupportedAccountOperationException,
                   OverdraftLimitException,
                   CheckingAccountFeeNotPaidException,
                           FrozenAccountFeeException,
                                   LinkedAccountInactiveException{

        validateAccountStatus(account, transactionType);

        if (transactionType != TransactionType.transfer_in) {
            validateAmount(amount, transactionType);
        }

        switch (transactionType) {

            case feeCharge:
                validateCheckingAccountFeeCharge(account);
                break;

            case Withdrawal:
                validateCheckingAccountWithdrawal(account, amount);
                break;

            case Deposit:
                validateCheckingAccountDeposit(account, amount);
                break;

            case transfer_in:
                validateCheckingAccountTransferIn(account);
                break;

            case transfer_out:
                validateCheckingAccountTransferOut(account, amount, transferToAccount);
                break;

            default:
                throw new UnsupportedAccountOperationException(
                        "Transaction type " + transactionType + " is not supported for Checking accounts.");
        }
    }

    public static void validateFixedDepositAccountTransactions(
            TransactionType transactionType,
            Account account,
            double amount,
            Account transferToAccount)
            throws LinkedAccountInactiveException,
                   LinkedAccountNotFoundException,
                   UnsupportedAccountOperationException,
                   AccountNotMaturedException,
                   InsufficientFundsException,
                   MinimumBalanceException,
                   EarlyWithdrawalException {

        validateAccountStatus(account, transactionType);

        if (transactionType != TransactionType.transfer_in) {
            validateAmount(amount, transactionType);
        }

        switch (transactionType) {

            case interestPayout:
                validateFixedDepositInterestPayout(account);
                break;

            case Withdrawal:
                validateFixedDepositWithdrawal(account, amount);
                break;

            case Deposit:
                validateFixedDepositDeposit();
                break;

            case feeCharge:
                validateFixedDepositFeeCharge(account);
                break;

            case transfer_in:
                validateFixedDepositTransferIn(account);
                break;

            default:
                throw new UnsupportedAccountOperationException(
                        "Transaction type " + transactionType + " is not supported for Fixed Deposit accounts.");
        }
    }

    public static void validateSavingsAccountTransactions(
            TransactionType transactionType,
            Account account,
            double amount , Account transferToAccount)
            throws UnsupportedAccountOperationException,
                   InsufficientFundsException,
                   MinimumBalanceException {

        validateAccountStatus(account, transactionType);
        validateAmount(amount, transactionType);

        switch (transactionType) {

            case interestPayout:
                validateSavingsAccountInterestPayout(account);
                break;

            case Withdrawal:
                validateSavingsAccountWithdrawal(account, amount);
                break;

            case Deposit:
                validateSavingsAccountDeposit(account, amount);
                break;

            case transfer_in:
                validateSavingsAccountTransferIn(account);
                break;

            case transfer_out:
                validateSavingsAccountTransferOut(account, amount,transferToAccount);
                break;

            default:
                throw new UnsupportedAccountOperationException(
                        "Transaction type " + transactionType + " is not supported for Saving accounts.");
        }
    }

    private static void validateAccountStatus(Account account, TransactionType transactionType)
            throws UnsupportedAccountOperationException {

        if (account == null) {
            throw new UnsupportedAccountOperationException("Account cannot be null");
        }

        if (account.getAccountStatus() == null) {
            throw new UnsupportedAccountOperationException("Account status not initialized");
        }

        AccountStatus status = account.getAccountStatus();

        if (status == AccountStatus.CLOSED) {
            throw new UnsupportedAccountOperationException(
                    "Transaction cannot be performed. This account is CLOSED and no longer accessible. " +
                    "Please contact customer service for more information.");
        }

        if (status == AccountStatus.FROZEN) {
            if (!isTransferInOperation(transactionType)) {
                throw new UnsupportedAccountOperationException(
                        "Transaction cannot be performed. This account is FROZEN. " +
                        "Only incoming transfers are permitted. " +
                        "To reactivate, please pay the pending fees or contact support.");
            }
        }

    }

    private static void validateAmount(double amount, TransactionType transactionType)
            throws UnsupportedAccountOperationException {

        if (!Double.isFinite(amount)) {
            throw new UnsupportedAccountOperationException(
                    "Transaction amount must be a finite number. Provided amount: " + amount);
        }

        if (amount <= 0 && transactionType != TransactionType.interestPayout) {
            throw new UnsupportedAccountOperationException(
                    "Transaction amount must be greater than 0. Provided amount: " + amount);
        }
    }

    private static void validateCheckingAccountDeposit(Account account, double amount)
            throws UnsupportedAccountOperationException {

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new UnsupportedAccountOperationException(
                    "Deposit cannot be performed. Account must be ACTIVE.");
        }

        if (!account.getAccountType().isAllowDeposit()) {
            throw new UnsupportedAccountOperationException(
                    "Deposit operation not supported for this account type.");
        }
    }

    private static void validateCheckingAccountFeeCharge(Account account)
            throws UnsupportedAccountOperationException, CheckingAccountFeeNotPaidException,
                    FrozenAccountFeeException {

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new FrozenAccountFeeException(
                    "Fee cannot be charged Now. Account must be ACTIVE.");
        }

        if (!account.getAccountType().isHaveFee()) {
            throw new UnsupportedAccountOperationException(
                    "Fee operation not supported for this account type.");
        }

        double fee = AccountFeeConstants.CHECKING_ACCOUNT_MONTHLY_FEE.getValue();
        double minBalance = AccountFeeConstants.CHECKING_ACCOUNT_MIN_BALANCE_FOR_FEE.getValue();

        if ((account.getBalance() - fee) < minBalance) {
            throw new CheckingAccountFeeNotPaidException(
                    "Fee charging failed. Insufficient funds. " +
                    "Required balance: " + fee +
                    ", Current balance: " + account.getBalance() +
                    ", Minimum balance required after fee: " + minBalance);
        }
    }

    private static void validateCheckingAccountTransferIn(Account account)
            throws UnsupportedAccountOperationException {

        AccountStatus status = account.getAccountStatus();

        if (status == AccountStatus.CLOSED) {
            throw new UnsupportedAccountOperationException(
                    "Transfer cannot be received. Account is CLOSED.");
        }

    }

    private static void validateCheckingAccountTransferOut(
            Account account,
            double amount,
            Account transferToAccount)
            throws UnsupportedAccountOperationException,
                    LinkedAccountInactiveException {

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new UnsupportedAccountOperationException(
                    "Transfer cannot be sent. Account must be ACTIVE.");
        }

        if (!account.getAccountType().isAllowTransfer()) {
            throw new UnsupportedAccountOperationException(
                    "Transfer operation not supported for this account type.");
        }
        double overdraftLimit = AccountType.CHECKING.getOverdraftLimit();

        if ((account.getBalance() - amount) < overdraftLimit) {
            throw new OverdraftLimitException(
                    "Withdrawal failed. Would exceed overdraft limit of " + overdraftLimit
                            + ". Current balance: " + account.getBalance()
                            + ", Requested withdrawal: " + amount
                            + ", Resulting balance would be: " + (account.getBalance() - amount));
        }

        if (transferToAccount == null) {
            throw new LinkedAccountNotFoundException(
                    "Transfer destination account cannot be null.");
        }

        if (transferToAccount.getAccountStatus() == AccountStatus.CLOSED) {
            throw new UnsupportedAccountOperationException(
                    "Transfer destination account is closed");
        }

        if (transferToAccount.getAccountStatus() != AccountStatus.ACTIVE)
        {
            throw new LinkedAccountInactiveException(
                    "Transfer destination account is not ACTIVE.");
        }

        if (transferToAccount.getAccountType() == AccountType.FIXED_DEPOSIT) {
            throw new UnsupportedAccountOperationException(
                    "Transfer destination cannot be a Fixed Deposit account.");
        }
    }

    private static void validateCheckingAccountWithdrawal(Account account, double amount)
            throws OverdraftLimitException, UnsupportedAccountOperationException {

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new UnsupportedAccountOperationException(
                    "Withdrawal cannot be performed. Account must be ACTIVE.");
        }

        if (!account.getAccountType().isAllowWithdrawal()) {
            throw new UnsupportedAccountOperationException(
                    "Withdrawal operation not supported for this account type.");
        }

        double overdraftLimit = AccountType.CHECKING.getOverdraftLimit();

        if ((account.getBalance() - amount) < overdraftLimit) {
            throw new OverdraftLimitException(
                    "Withdrawal failed. Would exceed overdraft limit of " + overdraftLimit +
                    ". Current balance: " + account.getBalance() +
                    ", Requested withdrawal: " + amount +
                    ", Resulting balance would be: " + (account.getBalance() - amount));
        }
    }

    private static void validateFixedDepositAccountPayout(Account account, Account destinationAccount)
            throws LinkedAccountNotFoundException,
                   LinkedAccountInactiveException,
                   UnsupportedAccountOperationException {

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new UnsupportedAccountOperationException(
                    "Payout cannot be processed. Account must be ACTIVE.");
        }

        FixedDepositAccount fdAccount = (FixedDepositAccount) account;

        if (!fdAccount.isAllowTransfer()) {
            throw new UnsupportedAccountOperationException(
                    "Payout cannot be processed. Account is not in payout mode.");
        }

        if (fdAccount.getLinkedAccount() == null) {
            throw new LinkedAccountNotFoundException(
                    "Payout failed. No linked account configured for this Fixed Deposit.");
        }

        Account linkedAccount = fdAccount.getLinkedAccount();

        if (linkedAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new LinkedAccountInactiveException(
                    "Payout failed. Linked account is not ACTIVE. " +
                    "Status: " + linkedAccount.getAccountStatus() +
                    ", Please activate the linked account before maturity.");
        }

        if (linkedAccount.getAccountType() == AccountType.FIXED_DEPOSIT) {
            throw new UnsupportedAccountOperationException(
                    "Payout failed. Linked account cannot be another Fixed Deposit account.");
        }
    }

    private static void validateFixedDepositDeposit()
            throws UnsupportedAccountOperationException {
        throw new UnsupportedAccountOperationException(
                "Deposit failed. Fixed Deposit accounts do not allow deposits after opening. " +
                "The principal amount is locked for the entire term duration.");
    }

    private static void validateFixedDepositFeeCharge(Account account)
            throws UnsupportedAccountOperationException {

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new UnsupportedAccountOperationException(
                    "Fee cannot be charged. Account must be ACTIVE.");
        }

        if (!account.getAccountType().isHaveFee()) {
            throw new UnsupportedAccountOperationException(
                    "Fee operation not supported for this account type.");
        }
    }

    private static void validateFixedDepositInterestPayout(Account account)
            throws UnsupportedAccountOperationException, AccountNotMaturedException {

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new UnsupportedAccountOperationException(
                    "Interest cannot be applied. Account must be ACTIVE.");
        }

        if (!account.getAccountType().isHaveInterest()) {
            throw new UnsupportedAccountOperationException(
                    "Interest operation not supported for this account type.");
        }

        FixedDepositAccount fdAccount = (FixedDepositAccount) account;

        if (!fdAccount.getTerm().isMatured()) {
            throw new AccountNotMaturedException(
                    "Interest payout failed. Account has not yet matured. " +
                    "Maturity date: " + fdAccount.getTerm().getMaturityDate() +
                    ", Months remaining: " + fdAccount.getTerm().getMonthsRemaining());
        }

        if (fdAccount.isInterestDeposited()) {
            throw new UnsupportedAccountOperationException(
                    "Interest payout failed. Interest has already been credited to this account.");
        }
    }

    private static void validateFixedDepositTransferIn(Account account)
            throws UnsupportedAccountOperationException {

        throw new UnsupportedAccountOperationException(
                "Transfer cannot be received. Fixed Deposit accounts cannot receive transfers. " +
                "Funds are locked for the contract duration.");
    }

    private static void validateFixedDepositTransferOut(
            Account account,
            double amount,
            Account transferToAccount)
            throws LinkedAccountInactiveException, UnsupportedAccountOperationException , LinkedAccountNotFoundException {

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new UnsupportedAccountOperationException(
                    "Transfer cannot be sent. Account must be ACTIVE.");
        }

        FixedDepositAccount fdAccount = (FixedDepositAccount) account;

        if (!fdAccount.isAllowTransfer()) {
            throw new UnsupportedAccountOperationException(
                    "Transfer cannot be sent. Account is not in transfer mode. " +
                    "Transfers only allowed during maturity processing.");
        }

        if (transferToAccount == null) {
            throw new LinkedAccountNotFoundException(
                    "Transfer destination account cannot be null.");
        }

        if (transferToAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new LinkedAccountInactiveException(
                    "Transfer failed. Destination account is not ACTIVE. " +
                    "Status: " + transferToAccount.getAccountStatus());
        }

        if (transferToAccount.getAccountType() == AccountType.FIXED_DEPOSIT) {
            throw new UnsupportedAccountOperationException(
                    "Transfer failed. Cannot transfer to another Fixed Deposit account. " +
                    "Transfer only to Checking or Saving accounts.");
        }
    }

    private static void validateFixedDepositWithdrawal(Account account, double amount)
            throws InsufficientFundsException,
                   MinimumBalanceException,
                   EarlyWithdrawalException {

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new UnsupportedAccountOperationException(
                    "Withdrawal cannot be performed. Account must be ACTIVE.");
        }

        FixedDepositAccount fdAccount = (FixedDepositAccount) account;

        if (fdAccount.getTerm().isMatured()) {
            if (amount > fdAccount.getBalance()) {
                throw new InsufficientFundsException(
                        "Withdrawal failed. Insufficient funds. Requested: " + amount +
                        ", Available: " + fdAccount.getBalance());
            }
            return;
        }

        double penaltyFee = fdAccount.getPenaltyFee();
            double totalCost = amount + penaltyFee;

            if (totalCost > fdAccount.getBalance()) {
                throw new InsufficientFundsException(
                        "Withdrawal failed. Insufficient funds for early withdrawal. " +
                        "Requested amount: " + amount +
                        ", Penalty fee: " + penaltyFee +
                        ", Total required: " + totalCost +
                        ", Available balance: " + fdAccount.getBalance());
            }

            double remainingBalance = fdAccount.getBalance() - totalCost;
            if (remainingBalance < 0.01) {
                throw new MinimumBalanceException(
                        "Withdrawal declined. Cannot completely empty account before maturity. " +
                        "Must maintain minimum balance of 0.01");
            }

            throw new EarlyWithdrawalException(
                    "Early withdrawal notice: Penalty of " + penaltyFee +
                    " will be deducted from your account. " +
                    "Maturity date: " + fdAccount.getTerm().getMaturityDate());

    }

    private static void validateSavingsAccountDeposit(Account account, double amount)
            throws UnsupportedAccountOperationException {

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new UnsupportedAccountOperationException(
                    "Deposit cannot be performed. Account must be ACTIVE.");
        }

        if (!account.getAccountType().isAllowDeposit()) {
            throw new UnsupportedAccountOperationException(
                    "Deposit operation not supported for this account type.");
        }
    }

    private static void validateSavingsAccountInterestPayout(Account account)
            throws UnsupportedAccountOperationException {

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new UnsupportedAccountOperationException(
                    "Interest cannot be applied. Account must be ACTIVE.");
        }

        if (!account.getAccountType().isHaveInterest()) {
            throw new UnsupportedAccountOperationException(
                    "Interest operation not supported for this account type.");
        }
    }

    private static void validateSavingsAccountTransferIn(Account account)
            throws UnsupportedAccountOperationException {

        AccountStatus status = account.getAccountStatus();

        if (status == AccountStatus.CLOSED) {
            throw new UnsupportedAccountOperationException(
                    "Transfer cannot be received. Account is CLOSED.");
        }
    }

    private static void validateSavingsAccountTransferOut(Account account, double amount , Account transferToAccount)
            throws UnsupportedAccountOperationException, MinimumBalanceException, LinkedAccountNotFoundException {

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new UnsupportedAccountOperationException(
                    "Transfer cannot be sent. Account must be ACTIVE.");
        }

        if (!account.getAccountType().isAllowTransfer()) {
            throw new UnsupportedAccountOperationException(
                    "Transfer operation not supported for this account type.");
        }

        double minimumBalance = AccountType.SAVINGS.getMinimumBalance();

        if (amount > account.getBalance()) {
            throw new InsufficientFundsException(
                    "Withdrawal failed. Insufficient funds. "
                    + "Requested: " + amount
                    + ", Available: " + account.getBalance());
        }

        if ((account.getBalance() - amount) < minimumBalance) {
            double maxWithdrawable = account.getBalance() - minimumBalance;
            throw new MinimumBalanceException(
                    "Withdrawal declined. Must maintain minimum balance of " + minimumBalance
                    + ". Current balance: " + account.getBalance()
                    + ", Requested withdrawal: " + amount
                    + ", Maximum you can withdraw: " + maxWithdrawable);
        }

        if (transferToAccount == null) {
            throw new LinkedAccountNotFoundException(
                    "Transfer destination account cannot be null.");
        }

        if (transferToAccount.getAccountStatus() == AccountStatus.CLOSED) {
            throw new UnsupportedAccountOperationException(
                    "Transfer destination account is closed");
        }

        if (transferToAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new LinkedAccountInactiveException(
                    "Transfer destination account is not ACTIVE.");
        }

        if (transferToAccount.getAccountType() == AccountType.FIXED_DEPOSIT) {
            throw new UnsupportedAccountOperationException(
                    "Transfer destination cannot be a Fixed Deposit account.");
        }
    }

    private static void validateSavingsAccountWithdrawal(Account account, double amount)
            throws InsufficientFundsException, MinimumBalanceException {

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new UnsupportedAccountOperationException(
                    "Withdrawal cannot be performed. Account must be ACTIVE.");
        }

        if (!account.getAccountType().isAllowWithdrawal()) {
            throw new UnsupportedAccountOperationException(
                    "Withdrawal operation not supported for this account type.");
        }

        double minimumBalance = AccountType.SAVINGS.getMinimumBalance();

        if (amount > account.getBalance()) {
            throw new InsufficientFundsException(
                    "Withdrawal failed. Insufficient funds. " +
                    "Requested: " + amount +
                    ", Available: " + account.getBalance());
        }

        if ((account.getBalance() - amount) < minimumBalance) {
            double maxWithdrawable = account.getBalance() - minimumBalance;
            throw new MinimumBalanceException(
                    "Withdrawal declined. Must maintain minimum balance of " + minimumBalance +
                    ". Current balance: " + account.getBalance() +
                    ", Requested withdrawal: " + amount +
                    ", Maximum you can withdraw: " + maxWithdrawable);
        }
    }

    private static boolean isTransferInOperation(TransactionType type) {
        return type == TransactionType.transfer_in ||
               type == TransactionType.FixedDepositAccountPayout ||
               type == TransactionType.feeCharge;
    }
}
