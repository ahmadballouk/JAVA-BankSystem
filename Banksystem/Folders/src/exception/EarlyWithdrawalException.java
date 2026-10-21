package exception;

public class EarlyWithdrawalException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EarlyWithdrawalException() {
        super();
    }

    public EarlyWithdrawalException(String message) {
        super(message);
    }

}
