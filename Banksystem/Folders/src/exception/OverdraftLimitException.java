package exception;

public class OverdraftLimitException extends  RuntimeException{

    private static final long serialVersionUID = 1L;

    public OverdraftLimitException() {
        super();
    }

    public OverdraftLimitException(String message) {
        super(message);
    }

}
