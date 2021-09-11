package tw.wally.dixit.usecases;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.EventBus;
import tw.wally.dixit.events.DixitRoundCardPlayingEvent;
import tw.wally.dixit.model.Card;
import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.model.Player;
import tw.wally.dixit.model.RoundState;
import tw.wally.dixit.repositories.DixitRepository;

import javax.inject.Named;

import static tw.wally.dixit.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 */
@Named
public class TellStoryUseCase extends AbstractDixitUseCase {

    public TellStoryUseCase(DixitRepository dixitRepository, EventBus eventBus) {
        super(dixitRepository, eventBus);
    }

    public void execute(Request request) {
        Dixit dixit = findDixit(request.gameId);
        validateRound(dixit, request.round);

        tellStory(request, dixit);
        mayPublishDixitRoundCardPlayingEvent(dixit);

        dixitRepository.save(dixit);
    }

    private void tellStory(Request request, Dixit dixit) {
        Player storyteller = dixit.getPlayer(request.playerId);
        Card card = storyteller.playCard(request.cardId);
        dixit.tellStory(request.phrase, storyteller, card);
    }

    private void mayPublishDixitRoundCardPlayingEvent(Dixit dixit) {
        RoundState currentRoundState = dixit.getCurrentRoundState();
        if (RoundState.CARD_PLAYING == currentRoundState) {
            String dixitId = dixit.getId();
            var dixitRoundCardPlayingEvents = mapToList(dixit.getCurrentGuessers(), guesser -> new DixitRoundCardPlayingEvent(dixitId, guesser.getId(), currentRoundState, guesser.getHandCards()));
            eventBus.publish(dixitRoundCardPlayingEvents);
        }
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
