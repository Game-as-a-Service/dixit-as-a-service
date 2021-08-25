package tw.wally.dixit.usecases;

import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.model.PlayCard;
import tw.wally.dixit.model.Player;
import tw.wally.dixit.repositories.DixitRepository;

import javax.inject.Named;

/**
 * @author - wally55077@gmail.com
 */
@Named
public class GuessStoryUseCase extends AbstractDixitUseCase {
    public GuessStoryUseCase(DixitRepository dixitRepository) {
        super(dixitRepository);
    }

    public void execute(Request request) {
        Dixit dixit = findDixit(request.gameId);
        validateRound(dixit, request.round);

        guessStory(request, dixit);

        dixit = dixitRepository.save(dixit);
        mayPublishEvents(dixit);
    }

    private void guessStory(Request request, Dixit dixit) {
        Player guesser = dixit.getPlayer(request.playerId);
        PlayCard playCard = dixit.getPlayCard(request.cardId);
        dixit.guessStory(guesser, playCard);
    }

    // TODO: 發佈事件 回合玩家結算
    private void mayPublishEvents(Dixit dixit) {

    }

}
