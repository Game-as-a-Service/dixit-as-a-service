package tw.wally.dixit.usecases;

import tw.wally.dixit.EventBus;
import tw.wally.dixit.events.DixitGameOverEvent;
import tw.wally.dixit.events.DixitRoundOverEvent;
import tw.wally.dixit.events.DixitRoundScoringEvent;
import tw.wally.dixit.events.DixitRoundStoryTellingEvent;
import tw.wally.dixit.model.*;
import tw.wally.dixit.repositories.DixitRepository;

import javax.inject.Named;

import java.util.List;

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
        mayPublishDixitRoundScoringEvent(dixit);

        dixitRepository.save(dixit);
    }

    private void guessStory(Request request, Dixit dixit) {
        Player guesser = dixit.getPlayer(request.playerId);
        PlayCard playCard = dixit.getPlayCard(request.cardId);
        dixit.guessStory(guesser, playCard);
    }

    private void mayPublishDixitRoundScoringEvent(Dixit dixit) {
        RoundState currentRoundState = dixit.getCurrentRoundState();
        if (RoundState.SCORING == currentRoundState) {
            dixit.score();
            dixit.withdrawCards();
            String dixitId = dixit.getId();
            int currentRound = dixit.getNumberOfRounds();
            Story story = dixit.getCurrentRound().getStory();
            var currentGuesses = dixit.getCurrentGuesses();
            var dixitRoundScoringEvent = mapToList(dixit.getPlayers(), player -> new DixitRoundScoringEvent(dixitId, currentRound, player.getId(), currentRoundState, story, currentGuesses));
            eventBus.publish(dixitRoundScoringEvent);

            mayPublishDixitGameOverOrDixitRoundOverEvent(dixit);
        }
    }

    private void mayPublishDixitGameOverOrDixitRoundOverEvent(Dixit dixit) {
        String dixitId = dixit.getId();
        GameState gameState = dixit.getGameState();
        if (GameState.OVER == gameState) {
            int currentRound = dixit.getNumberOfRounds();
            var winners = dixit.getWinners();
            var dixitGameOverEvent = mapToList(dixit.getPlayers(), player -> new DixitGameOverEvent(dixitId, currentRound, player.getId(), gameState, winners));
            eventBus.publish(dixitGameOverEvent);
        } else {
            mayPublishDixitRoundOverEvent(dixit);
        }
    }

    private void mayPublishDixitRoundOverEvent(Dixit dixit) {
        RoundState currentRoundStateAfterScored = dixit.getCurrentRoundState();
        if (RoundState.OVER == currentRoundStateAfterScored) {
            dixit.startNextRound();
            String dixitId = dixit.getId();
            int currentRound = dixit.getNumberOfRounds();
            var players = dixit.getPlayers();
            var dixitRoundOverEvents = mapToList(players, player -> new DixitRoundOverEvent(dixitId, currentRound, player.getId(), currentRoundStateAfterScored, players));
            eventBus.publish(dixitRoundOverEvents);

            mayPublishDixitRoundStoryTellingEvent(dixit);
        }
    }

    private void mayPublishDixitRoundStoryTellingEvent(Dixit dixit) {
        RoundState currentRoundState = dixit.getCurrentRoundState();
        if (RoundState.STORY_TELLING == currentRoundState) {
            String dixitId = dixit.getId();
            int currentRound = dixit.getNumberOfRounds();
            var storyteller = dixit.getCurrentStoryteller();
            var dixitRoundStoryTellingEvent = new DixitRoundStoryTellingEvent(dixitId, currentRound, storyteller.getId(), currentRoundState, storyteller.getHandCards());
            eventBus.publish(dixitRoundStoryTellingEvent);
        }
    }

}
