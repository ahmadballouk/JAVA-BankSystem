package exception;

public class ClosedAccountHasUnpaidFeeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ClosedAccountHasUnpaidFeeException() {
        super();
    }

    public ClosedAccountHasUnpaidFeeException(String message) {
        super(message);
    }

}
