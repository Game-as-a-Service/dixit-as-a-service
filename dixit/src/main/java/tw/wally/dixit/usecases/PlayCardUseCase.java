package tw.wally.dixit.usecases;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.events.EventBus;
import tw.wally.dixit.events.roundstate.DixitRoundCardPlayingEvent;
import tw.wally.dixit.events.roundstate.DixitRoundPlayerGuessingEvent;
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
        validateStoryteller(request, dixit);

        playCard(request, dixit);

        publishDixitRoundCardPlayingEvents(dixit);
        mayPublishDixitRoundPlayerGuessingEvents(dixit);
        dixitRepository.save(dixit);
    }

    private void playCard(Request request, Dixit dixit) {
        Player guesser = dixit.getPlayer(request.playerId);
        Card card = guesser.playCard(request.cardId);
        dixit.playCard(guesser, card);
    }

    private void validateStoryteller(Request request, Dixit dixit) {
        Player storyteller = dixit.getCurrentStoryteller();
        Player player = dixit.getPlayer(request.playerId);
        if (storyteller.equals(player)) {
            throw new InvalidGameOperationException("Storyteller can't play card in the CardPlaying");
        }
    }

    private void publishDixitRoundCardPlayingEvents(Dixit dixit) {
        var players = dixit.getPlayers();
        String dixitId = dixit.getId();
        int rounds = dixit.getNumberOfRounds();
        Story story = dixit.getCurrentStory();
        var playCards = dixit.getCurrentPlayCards();
        var dixitRoundCardPlayingEvents = mapToList(players, player -> new DixitRoundCardPlayingEvent(dixitId, rounds, player.getId(), RoundState.CARD_PLAYING, story, playCards));
        eventBus.publish(dixitRoundCardPlayingEvents);
    }

    private void mayPublishDixitRoundPlayerGuessingEvents(Dixit dixit) {
        RoundState roundState = dixit.getCurrentRoundState();
        if (RoundState.PLAYER_GUESSING == roundState) {
            String dixitId = dixit.getId();
            int rounds = dixit.getNumberOfRounds();
            var players = dixit.getPlayers();
            Story story = dixit.getCurrentStory();
            var playCards = dixit.getCurrentPlayCards();
            var guesses = dixit.getCurrentGuesses();
            var dixitRoundPlayerGuessingEvents = mapToList(players, player -> new DixitRoundPlayerGuessingEvent(dixitId, rounds, player.getId(), roundState, story, playCards, guesses));
            eventBus.publish(dixitRoundPlayerGuessingEvents);
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
