package tw.wally.dixit.usecases;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.model.Card;
import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.model.Player;
import tw.wally.dixit.repositories.DixitRepository;

import javax.inject.Named;

/**
 * @author - wally55077@gmail.com
 */
@Named
public class TellStoryUseCase extends AbstractDixitUseCase {

    public TellStoryUseCase(DixitRepository dixitRepository) {
        super(dixitRepository);
    }

    public void execute(Request request) {
        Dixit dixit = findDixit(request.gameId);
        validateRound(dixit, request.round);
        tellStory(request, dixit);
        dixit = dixitRepository.save(dixit);
        mayPublishEvents(dixit);
    }

    private void tellStory(Request request, Dixit dixit) {
        Player storyteller = dixit.getPlayer(request.playerId);
        Card card = storyteller.playCard(request.cardId);
        dixit.tellStory(request.phrase, storyteller, card);
    }

    // TODO: 發佈事件 回合玩家打牌
    private void mayPublishEvents(Dixit dixit) {

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request extends AbstractDixitUseCase.Request {
        public String phrase;

        public Request(String gameId, int round, String phrase, int cardId) {
            super(gameId, round, cardId);
            this.phrase = phrase;
        }
    }

}
