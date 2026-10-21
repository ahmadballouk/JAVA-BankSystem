package enums;

public enum AccountFeeConstants {

    CHECKING_ACCOUNT_MONTHLY_FEE(50.0),
    CHECKING_ACCOUNT_OVERDRAFT_LIMIT(-500.0),
    CHECKING_ACCOUNT_MIN_BALANCE_FOR_FEE(10.0),

    ACCOUNT_ACTIVATION_FEE(30.0),

    SAVING_ACCOUNT_MINIMUM_BALANCE(25.0),
    SAVING_ACCOUNT_MONTHLY_INTEREST_RATE(0.04),

    FD_PENALTY_SHORT_TERM(25.0),
    FD_PENALTY_MEDIUM_TERM(50.0),
    FD_PENALTY_LONG_TERM(100.0),

    FD_INTEREST_SHORT_TERM(3.75),
    FD_INTEREST_MEDIUM_TERM(4.50),
    FD_INTEREST_LONG_TERM(5.00);

    private final double value;

    AccountFeeConstants(double value) {
        this.value = value;
    }

    public double getValue() {
        return this.value;
    }
}
