package tw.wally.dixit.usecases;

import tw.wally.dixit.exceptions.InvalidGameOperationException;
import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.repositories.DixitRepository;

import javax.inject.Named;

import static java.lang.String.format;
import static tw.wally.dixit.utils.StreamUtils.findFirst;

/**
 * @author - wally55077@gmail.com
 */
@Named
public class PlayCardUseCase extends AbstractDixitUseCase {
    public PlayCardUseCase(DixitRepository dixitRepository) {
        super(dixitRepository);
    }

    public void execute(Request request) {
        var dixit = findDixit(request.gameId);
        validateRound(dixit, request.round);
        var playerId = request.playerId;
        var guesser = findFirst(dixit.getCurrentGuessers(), player -> playerId.equals(player.getId()))
                .orElseThrow(() -> new InvalidGameOperationException(format("Player: %s is not guesser", playerId)));
        var card = guesser.playCard(request.cardId);
        dixit.playCard(guesser, card);
        dixit = dixitRepository.save(dixit);
        mayPublishEvents(dixit);
    }

    // TODO: 發佈事件 回合玩家猜故事
    private void mayPublishEvents(Dixit dixit) {

    }

}
