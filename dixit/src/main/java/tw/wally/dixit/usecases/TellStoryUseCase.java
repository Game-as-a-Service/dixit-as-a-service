package tw.wally.dixit.usecases;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.events.EventBus;
import tw.wally.dixit.events.roundstate.DixitRoundCardPlayedEvent;
import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.model.RoundState;
import tw.wally.dixit.model.Story;
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

        dixit.tellStory(request.phrase, request.playerId , request.cardId);

        publishDixitRoundCardPlayedEvents(dixit);
        dixitRepository.save(dixit);
    }

    private void publishDixitRoundCardPlayedEvents(Dixit dixit) {
        var players = dixit.getPlayers();
        String dixitId = dixit.getId();
        int rounds = dixit.getNumberOfRounds();
        RoundState roundState = dixit.getCurrentRoundState();
        Story story = dixit.getCurrentStory();
        var playCards = dixit.getCurrentPlayCards();
        var dixitRoundCardPlayedEvents = mapToList(players, player -> new DixitRoundCardPlayedEvent(dixitId, rounds, player.getId(), roundState, story, playCards));
        eventBus.publish(dixitRoundCardPlayedEvents);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request extends AbstractDixitUseCase.Request {
        public int round;
        public String phrase;
        public int cardId;

        public Request(String gameId, int round, String playerId, String phrase, int cardId) {
            super(gameId, playerId);
            this.round = round;
            this.phrase = phrase;
            this.cardId = cardId;
        }
    }

}
