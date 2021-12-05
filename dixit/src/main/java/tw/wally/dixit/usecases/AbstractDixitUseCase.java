package tw.wally.dixit.usecases;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.events.EventBus;
import tw.wally.dixit.exceptions.InvalidGameOperationException;
import tw.wally.dixit.exceptions.NotFoundException;
import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.repositories.DixitRepository;

import static java.lang.String.format;

/**
 * @author - wally55077@gmail.com
 */
@AllArgsConstructor
public abstract class AbstractDixitUseCase {
    protected final DixitRepository dixitRepository;
    protected final EventBus eventBus;

    protected Dixit findDixit(String id) {
        return dixitRepository.findDixitById(id)
                .orElseThrow(() -> new NotFoundException(format("Dixit: %s not found", id)));
    }

    protected void validateRound(Dixit dixit, int round) {
        if (dixit.getNumberOfRounds() != round) {
            throw new InvalidGameOperationException(format("Round: %d is not the current round", round));
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public String gameId;
        public String playerId;
    }

}
