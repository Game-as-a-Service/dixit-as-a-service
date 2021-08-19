package tw.wally.dixit.usecases;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.exceptions.InvalidGameOperationException;
import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.repositories.DixitRepository;

import javax.inject.Named;

import static java.lang.String.format;

/**
 * @author - wally55077@gmail.com
 */
@Named
public class TellStoryUseCase extends AbstractDixitUseCase {

    public TellStoryUseCase(DixitRepository dixitRepository) {
        super(dixitRepository);
    }

    public void execute(Request request) {
        var dixit = findDixit(request.gameId);
        validateRound(dixit, request.round);
        validateStoryteller(dixit, request.playerId);
        var storyteller = dixit.getCurrentStoryteller();
        var card = storyteller.playCard(request.cardId);
        dixit.tellStory(request.phrase, storyteller, card);
        dixit = dixitRepository.save(dixit);
        mayPublishEvents(dixit);
    }

    private void validateStoryteller(Dixit dixit, String playerId) {
        var storyteller = dixit.getCurrentStoryteller();
        if (!storyteller.getId().equals(playerId)) {
            throw new InvalidGameOperationException(format("Player: %s is not storyteller", playerId));
        }
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
