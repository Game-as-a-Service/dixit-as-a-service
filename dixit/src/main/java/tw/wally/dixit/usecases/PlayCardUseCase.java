package tw.wally.dixit.usecases;

import tw.wally.dixit.EventBus;
import tw.wally.dixit.events.DixitRoundCardPlayingEvent;
import tw.wally.dixit.events.DixitRoundPlayerGuessingEvent;
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
public class PlayCardUseCase extends AbstractDixitUseCase {

    public PlayCardUseCase(DixitRepository dixitRepository, EventBus eventBus) {
        super(dixitRepository, eventBus);
    }

    public void execute(Request request) {
        Dixit dixit = findDixit(request.gameId);
        validateRound(dixit, request.round);

        playCard(request, dixit);
        mayPublishDixitRoundPlayerGuessingEvents(dixit);

        dixitRepository.save(dixit);
    }

    private void playCard(Request request, Dixit dixit) {
        Player guesser = dixit.getPlayer(request.playerId);
        Card card = guesser.playCard(request.cardId);
        dixit.playCard(guesser, card);
    }

    private void mayPublishDixitRoundPlayerGuessingEvents(Dixit dixit) {
        RoundState currentRoundState = dixit.getCurrentRoundState();
        if (RoundState.PLAYER_GUESSING == currentRoundState) {
            String dixitId = dixit.getId();
            var currentPlayCards = dixit.getCurrentPlayCards();
            var dixitRoundPlayerGuessingEvents = mapToList(dixit.getCurrentGuessers(), guesser -> new DixitRoundPlayerGuessingEvent(dixitId, guesser.getId(), currentRoundState, currentPlayCards));
            eventBus.publish(dixitRoundPlayerGuessingEvents);
        }
    }

}
