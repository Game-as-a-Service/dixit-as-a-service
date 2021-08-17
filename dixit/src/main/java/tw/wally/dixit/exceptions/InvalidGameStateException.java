package tw.wally.dixit.exceptions;

/**
 * @author - wally55077@gmail.com
 */
public class InvalidGameStateException extends RuntimeException {

    public InvalidGameStateException() {
    }

    public InvalidGameStateException(String message) {
        super(message);
    }

    public InvalidGameStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidGameStateException(Throwable cause) {
        super(cause);
    }

    public InvalidGameStateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
