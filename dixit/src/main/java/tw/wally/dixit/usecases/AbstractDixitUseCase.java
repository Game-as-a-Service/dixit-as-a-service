package tw.wally.dixit.usecases;

import lombok.AllArgsConstructor;
import tw.wally.dixit.exceptions.NotFoundException;
import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.repositories.DixitRepository;

/**
 * @author - wally55077@gmail.com
 */
@AllArgsConstructor
public abstract class AbstractDixitUseCase {
    protected final DixitRepository dixitRepository;

    protected Dixit findDixit(String id) {
        return dixitRepository.findDixitById(id)
                .orElseThrow(() -> new NotFoundException(""));
    }

}
