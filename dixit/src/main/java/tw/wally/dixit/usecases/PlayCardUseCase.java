package tw.wally.dixit.usecases;

import tw.wally.dixit.exceptions.InvalidGameOperationException;
import tw.wally.dixit.model.Card;
import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.model.Player;
import tw.wally.dixit.repositories.DixitRepository;

import javax.inject.Named;

import static java.lang.String.format;

/**
 * @author - wally55077@gmail.com
 */
@Named
public class PlayCardUseCase extends AbstractDixitUseCase {
    public PlayCardUseCase(DixitRepository dixitRepository) {
        super(dixitRepository);
    }

    public void execute(Request request) {
        Dixit dixit = findDixit(request.gameId);
        validateRound(dixit, request.round);
        playCard(request, dixit);
        dixit = dixitRepository.save(dixit);
        mayPublishEvents(dixit);
    }

    private void playCard(Request request, Dixit dixit) {
        Player guesser = dixit.getPlayer(request.playerId);
        Card card = guesser.playCard(request.cardId);
        dixit.playCard(guesser, card);
    }

    // TODO: 發佈事件 回合玩家猜故事
    private void mayPublishEvents(Dixit dixit) {

    }

}
