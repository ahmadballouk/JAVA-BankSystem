package exception;

public class LinkedAccountInactiveException extends RuntimeException{

    private static final long serialVersionUID = 1L;

        public LinkedAccountInactiveException() {
        super();
    }

    public LinkedAccountInactiveException(String message) {
        super(message);
    }

}
