package exception;

public class LinkedAccountNotFoundException  extends  RuntimeException{

    private static final long serialVersionUID = 1L;

        public LinkedAccountNotFoundException() {
        super();
    }

    public LinkedAccountNotFoundException(String message) {
        super(message);
    }
}
