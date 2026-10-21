package exception;

public class CheckingAccountFeeNotPaidException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CheckingAccountFeeNotPaidException() {
        super();
    }

    public CheckingAccountFeeNotPaidException(String message) {
        super(message);
    }

}
