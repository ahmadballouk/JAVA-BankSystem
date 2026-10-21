package enums;

public enum AccountStatus {

    ACTIVE(true,true), CLOSED(false,false), FROZEN (false,false);

    private final boolean allowWithdrawal;
    private final boolean allowDeposit;

    private AccountStatus(boolean allowDeposit, boolean allowWithdrawal) {

        this.allowDeposit = allowDeposit;
        this.allowWithdrawal = allowWithdrawal;
    }

    public boolean canWithdraw() {
        return allowWithdrawal;
    }

    public boolean canDeposit() {
        return allowDeposit;
    }

}
