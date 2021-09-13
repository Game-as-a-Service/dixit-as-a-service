package tw.wally.dixit.usecases;

import tw.wally.dixit.EventBus;
import tw.wally.dixit.events.DixitGameOverEvent;
import tw.wally.dixit.events.DixitRoundOverEvent;
import tw.wally.dixit.events.DixitRoundScoringEvent;
import tw.wally.dixit.events.DixitRoundStoryTellingEvent;
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
            var currentGuesses = dixit.getCurrentGuesses();
            var dixitRoundScoringEvent = mapToList(dixit.getPlayers(), player -> new DixitRoundScoringEvent(dixitId, player.getId(), currentRoundState, currentGuesses));
            eventBus.publish(dixitRoundScoringEvent);

            mayPublishDixitGameOverOrDixitRoundOverEvent(dixit);
        }
    }

    private void mayPublishDixitGameOverOrDixitRoundOverEvent(Dixit dixit) {
        String dixitId = dixit.getId();
        GameState gameState = dixit.getGameState();
        if (GameState.OVER == gameState) {
            var winners = dixit.getWinners();
            var dixitGameOverEvent = mapToList(dixit.getPlayers(), player -> new DixitGameOverEvent(dixitId, player.getId(), gameState, winners));
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
            var dixitRoundOverEvents = mapToList(dixit.getPlayers(), player -> new DixitRoundOverEvent(dixitId, player.getId(), currentRoundStateAfterScored, player.getScore()));
            eventBus.publish(dixitRoundOverEvents);

            mayPublishDixitRoundStoryTellingEvent(dixit);
        }
    }

    private void mayPublishDixitRoundStoryTellingEvent(Dixit dixit) {
        RoundState currentRoundState = dixit.getCurrentRoundState();
        if (RoundState.STORY_TELLING == currentRoundState) {
            String dixitId = dixit.getId();
            var storyteller = dixit.getCurrentStoryteller();
            var dixitRoundStoryTellingEvent = new DixitRoundStoryTellingEvent(dixitId, storyteller.getId(), currentRoundState, storyteller.getHandCards());
            eventBus.publish(dixitRoundStoryTellingEvent);
        }
    }

}
