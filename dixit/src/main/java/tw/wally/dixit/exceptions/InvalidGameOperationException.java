package tw.wally.dixit.exceptions;

/**
 * @author - wally55077@gmail.com
 */
public class InvalidGameOperationException extends RuntimeException {

    public InvalidGameOperationException() {
    }

    public InvalidGameOperationException(String message) {
        super(message);
    }

    public InvalidGameOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidGameOperationException(Throwable cause) {
        super(cause);
    }

    public InvalidGameOperationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
