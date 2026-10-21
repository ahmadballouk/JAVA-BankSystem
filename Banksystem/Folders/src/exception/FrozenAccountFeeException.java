package exception;

public class FrozenAccountFeeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FrozenAccountFeeException() {
        super();
    }

    public FrozenAccountFeeException(String message) {
        super(message);
    }

}
