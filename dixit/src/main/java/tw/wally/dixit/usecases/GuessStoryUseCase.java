package tw.wally.dixit.usecases;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.events.EventBus;
import tw.wally.dixit.events.gamestate.DixitGameOverEvent;
import tw.wally.dixit.events.roundstate.DixitRoundPlayerGuessingEvent;
import tw.wally.dixit.events.roundstate.DixitRoundScoringEvent;
import tw.wally.dixit.events.roundstate.DixitRoundStoryTellingEvent;
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

        publishDixitRoundPlayerGuessingEvents(dixit);
        mayPublishDixitRoundScoringEvents(dixit);
        dixitRepository.save(dixit);
    }

    private void guessStory(Request request, Dixit dixit) {
        Player guesser = dixit.getPlayer(request.playerId);
        PlayCard playCard = dixit.getPlayCard(request.cardId);
        dixit.guessStory(guesser, playCard);
    }

    private void publishDixitRoundPlayerGuessingEvents(Dixit dixit) {
        var players = dixit.getPlayers();
        String dixitId = dixit.getId();
        int rounds = dixit.getNumberOfRounds();
        Story story = dixit.getCurrentStory();
        var playCards = dixit.getCurrentPlayCards();
        var guesses = dixit.getCurrentGuesses();
        var dixitRoundPlayerGuessingEvents = mapToList(players, player -> new DixitRoundPlayerGuessingEvent(dixitId, rounds, player.getId(), RoundState.PLAYER_GUESSING, story, playCards, guesses));
        eventBus.publish(dixitRoundPlayerGuessingEvents);
    }

    private void mayPublishDixitRoundScoringEvents(Dixit dixit) {
        RoundState roundState = dixit.getCurrentRoundState();
        if (RoundState.SCORING == roundState) {
            dixit.score();
            dixit.withdrawCards();
            var players = dixit.getPlayers();
            String dixitId = dixit.getId();
            int rounds = dixit.getNumberOfRounds();
            var dixitRoundScoringEvents = mapToList(players, player -> new DixitRoundScoringEvent(dixitId, rounds, player.getId(), roundState, players));
            eventBus.publish(dixitRoundScoringEvents);

            mayPublishDixitGameOverOrDixitRoundStoryTellingEvents(dixit);
        }
    }

    private void mayPublishDixitGameOverOrDixitRoundStoryTellingEvents(Dixit dixit) {
        String dixitId = dixit.getId();
        GameState gameState = dixit.getGameState();
        if (GameState.OVER == gameState) {
            int rounds = dixit.getNumberOfRounds();
            var winners = dixit.getWinners();
            var dixitGameOverEvents = mapToList(dixit.getPlayers(), player -> new DixitGameOverEvent(dixitId, rounds, player.getId(), gameState, winners));
            eventBus.publish(dixitGameOverEvents);
        } else {
            dixit.startNextRound();
            publishDixitRoundStoryTellingEvents(dixit);
        }
    }

    private void publishDixitRoundStoryTellingEvents(Dixit dixit) {
        RoundState currentRoundState = dixit.getCurrentRoundState();
        if (RoundState.STORY_TELLING == currentRoundState) {
            String dixitId = dixit.getId();
            int rounds = dixit.getNumberOfRounds();
            var players = dixit.getPlayers();
            var storyteller = dixit.getCurrentStoryteller();
            var dixitRoundStoryTellingEvents = mapToList(players, player -> new DixitRoundStoryTellingEvent(dixitId, rounds, currentRoundState, storyteller, player));

            eventBus.publish(dixitRoundStoryTellingEvents);
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
