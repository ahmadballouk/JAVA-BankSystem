package exception;

public class ClosedCheckingAccountOverdrawnException extends  RuntimeException {

    private static final long serialVersionUID = 1L;

    public ClosedCheckingAccountOverdrawnException() {
        super();
    }

    public ClosedCheckingAccountOverdrawnException(String message) {
        super(message);
    }

}
