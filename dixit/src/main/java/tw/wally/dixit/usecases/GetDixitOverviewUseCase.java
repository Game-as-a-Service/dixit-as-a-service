package tw.wally.dixit.usecases;

import tw.wally.dixit.events.EventBus;
import tw.wally.dixit.model.*;
import tw.wally.dixit.repositories.DixitRepository;

import javax.inject.Named;
import java.util.Collection;

import static java.util.Comparator.comparing;
import static tw.wally.dixit.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 */
@Named
public class GetDixitOverviewUseCase extends AbstractDixitUseCase {

    public GetDixitOverviewUseCase(DixitRepository dixitRepository, EventBus eventBus) {
        super(dixitRepository, eventBus);
    }

    public void execute(Request request, Presenter presenter) {
        Dixit dixit = findDixit(request.gameId);

        presenter.showGameState(dixit.getGameState());
        presenter.showRoundState(dixit.getCurrentRoundState());
        presenter.showRounds(dixit.getNumberOfRounds());
        presenter.showPlayers(mapToList(dixit.getPlayers(), this::rePlayer));
        presenter.showStoryteller(rePlayer(dixit.getCurrentStoryteller()));
        showHandCards(request, dixit, presenter);
        showStory(dixit, presenter);
        showPlayCards(dixit, presenter);
        showGuesses(dixit, presenter);
        presenter.showWinners(mapToList(dixit.getWinners(), this::rePlayer));
    }

    private void showHandCards(Request request, Dixit dixit, Presenter presenter) {
        RoundState currentRoundState = dixit.getCurrentRoundState();
        Player player = dixit.getPlayer(request.playerId);
        if (RoundState.STORY_TELLING == currentRoundState
                || RoundState.CARD_PLAYING == currentRoundState) {
            presenter.showHandCards(player.getHandCards());
        }
    }

    private void showStory(Dixit dixit, Presenter presenter) {
        dixit.mayHaveCurrentStory()
                .ifPresent(story -> presenter.showStory(new Story(story.getPhrase(), rePlayCard(story.getPlayCard()))));
    }

    private void showPlayCards(Dixit dixit, Presenter presenter) {
        RoundState currentRoundState = dixit.getCurrentRoundState();
        var playCards = mapToList(dixit.getCurrentPlayCards(), this::rePlayCard);
        dixit.mayHaveCurrentStory().ifPresent(story -> playCards.add(rePlayCard(story.getPlayCard())));
        playCards.sort(comparing(PlayCard::getCardId));
        if (RoundState.CARD_PLAYING == currentRoundState
                || RoundState.PLAYER_GUESSING == currentRoundState) {
            presenter.showPlayCards(playCards);
        }
    }

    private void showGuesses(Dixit dixit, Presenter presenter) {
        RoundState currentRoundState = dixit.getCurrentRoundState();
        if (RoundState.PLAYER_GUESSING == currentRoundState
                || RoundState.SCORING == currentRoundState) {
            presenter.showGuesses(mapToList(dixit.getCurrentGuesses(), this::reGuess));
        }
    }

    private Guess reGuess(Guess guess) {
        return new Guess(rePlayer(guess.getGuesser()), rePlayCard(guess.getPlayCard()));
    }

    private PlayCard rePlayCard(PlayCard playCard) {
        return new PlayCard(rePlayer(playCard.getPlayer()), playCard.getCard());
    }

    private Player rePlayer(Player player) {
        return new Player(player.getId(), player.getName(), player.getColor(), player.getScore());
    }

    public interface Presenter {
        void showGameState(GameState gameState);

        void showRoundState(RoundState roundState);

        void showRounds(int rounds);

        void showPlayers(Collection<Player> players);

        void showStoryteller(Player storyteller);

        void showHandCards(Collection<Card> handCards);

        void showStory(Story story);

        void showPlayCards(Collection<PlayCard> playCards);

        void showGuesses(Collection<Guess> guesses);

        void showWinners(Collection<Player> winners);
    }
}
