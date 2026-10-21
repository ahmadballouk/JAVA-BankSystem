package exception;

public class ClosedAccountHasBalanceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ClosedAccountHasBalanceException() {
        super();
    }

    public ClosedAccountHasBalanceException(String message) {
        super(message);
    }

}
