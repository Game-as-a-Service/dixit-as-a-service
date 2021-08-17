package tw.wally.dixit.advices;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import tw.wally.dixit.exceptions.InvalidGameOperationException;
import tw.wally.dixit.exceptions.InvalidGameStateException;
import tw.wally.dixit.exceptions.NotFoundException;

/**
 * @author - wally55077@gmail.com
 */
@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> notFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler({InvalidGameStateException.class, InvalidGameOperationException.class})
    public ResponseEntity<?> badRequest() {
        return ResponseEntity.badRequest().build();
    }

}
