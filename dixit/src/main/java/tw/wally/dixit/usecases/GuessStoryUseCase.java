package tw.wally.dixit.usecases;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.events.EventBus;
import tw.wally.dixit.events.gamestate.DixitGameOverEvent;
import tw.wally.dixit.events.roundstate.DixitRoundStoryGuessedEvent;
import tw.wally.dixit.events.roundstate.DixitRoundScoredEvent;
import tw.wally.dixit.events.roundstate.DixitRoundStoryToldEvent;
import tw.wally.dixit.model.*;
import tw.wally.dixit.repositories.DixitRepository;

import javax.inject.Named;

import static tw.wally.dixit.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 */
@Named
public class GuessStoryUseCase extends AbstractDixitUseCase {

    public GuessStoryUseCase(DixitRepository dixitRepository, EventBus eventBus) {
        super(dixitRepository, eventBus);
    }

    public void execute(Request request) {
        Dixit dixit = findDixit(request.gameId);
        validateRound(dixit, request.round);

        guessStory(request, dixit);

        publishDixitRoundStoryGuessedEvents(dixit);
        mayPublishDixitRoundScoredEvents(dixit);
        dixitRepository.save(dixit);
    }

    private void guessStory(Request request, Dixit dixit) {
        Player guesser = dixit.getPlayer(request.playerId);
        PlayCard playCard = dixit.getPlayCard(request.cardId);
        dixit.guessStory(guesser, playCard);
    }

    private void publishDixitRoundStoryGuessedEvents(Dixit dixit) {
        var players = dixit.getPlayers();
        String dixitId = dixit.getId();
        int rounds = dixit.getNumberOfRounds();
        Story story = dixit.getCurrentStory();
        var playCards = dixit.getCurrentPlayCards();
        var guesses = dixit.getCurrentGuesses();
        var dixitRoundStoryGuessedEvents = mapToList(players, player -> new DixitRoundStoryGuessedEvent(dixitId, rounds, player.getId(), RoundState.STORY_GUESSING, story, playCards, guesses));
        eventBus.publish(dixitRoundStoryGuessedEvents);
    }

    private void mayPublishDixitRoundScoredEvents(Dixit dixit) {
        RoundState roundState = dixit.getCurrentRoundState();
        if (RoundState.SCORING == roundState) {
            dixit.score();
            dixit.withdrawCards();
            var players = dixit.getPlayers();
            String dixitId = dixit.getId();
            int rounds = dixit.getNumberOfRounds();
            var dixitRoundScoredEvents = mapToList(players, player -> new DixitRoundScoredEvent(dixitId, rounds, player.getId(), roundState, players));
            eventBus.publish(dixitRoundScoredEvents);

            mayPublishDixitGameOverOrDixitRoundStoryToldEvents(dixit);
        }
    }

    private void mayPublishDixitGameOverOrDixitRoundStoryToldEvents(Dixit dixit) {
        String dixitId = dixit.getId();
        GameState gameState = dixit.getGameState();
        if (GameState.OVER == gameState) {
            int rounds = dixit.getNumberOfRounds();
            var winners = dixit.getWinners();
            var dixitGameOverEvents = mapToList(dixit.getPlayers(), player -> new DixitGameOverEvent(dixitId, rounds, player.getId(), gameState, winners));
            eventBus.publish(dixitGameOverEvents);
        } else {
            dixit.startNextRound();
            publishDixitRoundStoryToldEvents(dixit);
        }
    }

    private void publishDixitRoundStoryToldEvents(Dixit dixit) {
        RoundState currentRoundState = dixit.getCurrentRoundState();
        if (RoundState.STORY_TELLING == currentRoundState) {
            String dixitId = dixit.getId();
            int rounds = dixit.getNumberOfRounds();
            var players = dixit.getPlayers();
            var storyteller = dixit.getCurrentStoryteller();
            var dixitRoundStoryToldEvents = mapToList(players, player -> new DixitRoundStoryToldEvent(dixitId, rounds, currentRoundState, storyteller, player));

            eventBus.publish(dixitRoundStoryToldEvents);
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
