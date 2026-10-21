package exception;

public class AccountNotMaturedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AccountNotMaturedException() {
        super();
    }

    public AccountNotMaturedException(String message) {
        super(message);
    }
}
