package tw.wally.dixit.usecases;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.events.EventBus;
import tw.wally.dixit.events.roundstate.DixitRoundCardPlayedEvent;
import tw.wally.dixit.events.roundstate.DixitRoundStoryGuessedEvent;
import tw.wally.dixit.exceptions.InvalidGameOperationException;
import tw.wally.dixit.model.*;
import tw.wally.dixit.repositories.DixitRepository;

import javax.inject.Named;

import static tw.wally.dixit.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 */
@Named
public class PlayCardUseCase extends AbstractDixitUseCase {

    public PlayCardUseCase(DixitRepository dixitRepository, EventBus eventBus) {
        super(dixitRepository, eventBus);
    }

    public void execute(Request request) {
        Dixit dixit = findDixit(request.gameId);
        validateRound(dixit, request.round);

        playCard(request, dixit);

        publishDixitRoundCardPlayedEvents(dixit);
        mayPublishDixitRoundStoryGuessedEvents(dixit);
        dixitRepository.save(dixit);
    }

    private void playCard(Request request, Dixit dixit) {
        Player guesser = dixit.getPlayer(request.playerId);
        Card card = guesser.playCard(request.cardId);
        try {
            dixit.playCard(guesser, card);
        } catch (InvalidGameOperationException e) {
            guesser.addHandCard(card);
            throw e;
        }
    }

    private void publishDixitRoundCardPlayedEvents(Dixit dixit) {
        var players = dixit.getPlayers();
        String dixitId = dixit.getId();
        int rounds = dixit.getNumberOfRounds();
        Story story = dixit.getCurrentStory();
        var playCards = dixit.getCurrentPlayCards();
        var dixitRoundCardPlayedEvents = mapToList(players, player -> new DixitRoundCardPlayedEvent(dixitId, rounds, player.getId(), RoundState.CARD_PLAYING, story, playCards));
        eventBus.publish(dixitRoundCardPlayedEvents);
    }

    private void mayPublishDixitRoundStoryGuessedEvents(Dixit dixit) {
        RoundState roundState = dixit.getCurrentRoundState();
        if (RoundState.STORY_GUESSING == roundState) {
            String dixitId = dixit.getId();
            int rounds = dixit.getNumberOfRounds();
            var players = dixit.getPlayers();
            Story story = dixit.getCurrentStory();
            var playCards = dixit.getCurrentPlayCards();
            var guesses = dixit.getCurrentGuesses();
            var dixitRoundStoryGuessedEvents = mapToList(players, player -> new DixitRoundStoryGuessedEvent(dixitId, rounds, player.getId(), roundState, story, playCards, guesses));
            eventBus.publish(dixitRoundStoryGuessedEvents);
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request extends AbstractDixitUseCase.Request {
        public int round;
        public int cardId;

        public Request(String gameId, int round, String playerId, int cardId) {
            super(gameId, playerId);
            this.round = round;
            this.cardId = cardId;
        }
    }

}
