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
public class GuessStoryUseCase extends AbstractDixitUseCase {
    public GuessStoryUseCase(DixitRepository dixitRepository) {
        super(dixitRepository);
    }

    public void execute(Request request) {
        var dixit = findDixit(request.gameId);
        validateRound(dixit, request.round);
        var playerId = request.playerId;
        var guesser = findFirst(dixit.getCurrentGuessers(), player -> playerId.equals(player.getId()))
                .orElseThrow(() -> new InvalidGameOperationException(format("Player: %s is not guesser", playerId)));
        var playCard = dixit.getPlayCardByCardId(request.cardId);
        dixit.guessStory(guesser, playCard);
        dixit = dixitRepository.save(dixit);
        mayPublishEvents(dixit);
    }

    // TODO: 發佈事件 回合玩家結算
    private void mayPublishEvents(Dixit dixit) {

    }

}
