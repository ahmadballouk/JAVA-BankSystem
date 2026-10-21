package exception;

public class UnsupportedAccountOperationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UnsupportedAccountOperationException() {
        super();
    }

    public UnsupportedAccountOperationException(String message) {
        super(message);
    }

}
