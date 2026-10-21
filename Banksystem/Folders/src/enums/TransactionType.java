package enums;

public enum TransactionType {

    Deposit(),
    Withdrawal(),

    interestPayout(),
    feeCharge(),
    earlyWithdrawalPenalty(),

    transfer_in(),
    transfer_out(),
    FixedDepositAccountPayout(),

    ActivateAccountFee(),
    UnpaidFeePayment();

}
