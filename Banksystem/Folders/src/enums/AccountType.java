package enums;

public enum AccountType {

    SAVINGS(true, true, true, false, true,0.00,25.0),

    CHECKING(true, true, false, true, true,-500,0.00),

    FIXED_DEPOSIT(true, true, true, true,false,0.0,0.0);

    private final boolean allowWithdrawal;
    private final boolean allowDeposit;
    private final boolean haveInterest;
    private final boolean haveFee;
    private final boolean allowTransfer;

    private final double overdraftLimit;
    private final double minimumBalance;

    private AccountType(boolean allowWithdrawal, boolean allowDeposit, boolean haveInterest, boolean haveFee,
            boolean allowTransfer, double overdraftLimit, double minimumBalance) {
        this.allowWithdrawal = allowWithdrawal;
        this.allowDeposit = allowDeposit;
        this.haveFee = haveFee;
        this.haveInterest = haveInterest;
        this.allowTransfer = allowTransfer;
        this.minimumBalance = minimumBalance;
        this.overdraftLimit = overdraftLimit;
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    public double getMinimumBalance() {
        return minimumBalance;
    }

    public boolean isAllowWithdrawal() {
        return allowWithdrawal;
    }

    public boolean isAllowDeposit() {
        return allowDeposit;
    }

    public boolean isHaveInterest() {
        return haveInterest;
    }

    public boolean isHaveFee() {
        return haveFee;
    }

    public boolean isAllowTransfer() {
        return allowTransfer;
    }

}
