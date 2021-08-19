package tw.wally.dixit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tw.wally.dixit.exceptions.InvalidGameOperationException;
import tw.wally.dixit.exceptions.InvalidGameStateException;
import tw.wally.dixit.exceptions.NotFoundException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.List.copyOf;
import static tw.wally.dixit.utils.StreamUtils.*;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@Builder
@AllArgsConstructor
public class Round {
    public static final int BONUS_SCORE = 1;
    public static final int NORMAL_SCORE = 2;
    public static final int GUESS_CORRECTLY_SCORE = 3;
    private final Player storyteller;
    private final List<Player> guessers;
    private final Map<Integer, PlayCard> playCards;
    private final Map<Player, Guess> guesses;
    private final int numberOfGuessers;
    private RoundState roundState;
    private Story story;

    public Round(Player storyteller, List<Player> guessers) {
        this.roundState = RoundState.STORY_TELLING;
        this.storyteller = storyteller;
        this.guessers = copyOf(guessers);
        this.numberOfGuessers = guessers.size();
        this.playCards = new HashMap<>(numberOfGuessers);
        this.guesses = new HashMap<>(numberOfGuessers);
    }

    public void tellStory(Story story) {
        validateTellStoryAction(story);
        this.story = story;
        roundState = RoundState.CARD_PLAYING;
    }

    private void validateTellStoryAction(Story story) {
        validateRoundState(RoundState.STORY_TELLING, () -> "When the round state isn't story telling, storyteller can't tell the story.");
        if (this.story != null) {
            throw new InvalidGameOperationException("Story has existed, storyteller can't tell the story again.");
        }
        if (!storyteller.equals(story.getPlayer())) {
            throw new InvalidGameOperationException("Guesser can't tell the story.");
        }
    }

    public void playCard(PlayCard playCard) {
        validatePlayCard(playCard);
        playCards.put(playCard.getCardId(), playCard);
        if (playCards.size() == numberOfGuessers) {
            roundState = RoundState.PLAYER_GUESSING;
        }
    }

    private void validatePlayCard(PlayCard playCard) {
        validateRoundState(RoundState.CARD_PLAYING, () -> "When the round state isn't card playing, guesser can't play the card.");
        if (playCards.size() == numberOfGuessers) {
            throw new InvalidGameOperationException(format("Number of playCards can't be higher than %d.", numberOfGuessers));
        }
        var player = playCard.getPlayer();
        if (contains(playCards.values(), card -> card.getPlayer().equals(player))) {
            throw new InvalidGameOperationException(format("Player: %s can't play the card twice in same round.", player.getName()));
        }
    }

    public void guessStory(Guess guess) {
        validateGuessAction(guess);
        var guesser = guess.getGuesser();
        guesses.put(guesser, guess);
        if (guesses.size() == numberOfGuessers) {
            roundState = RoundState.SCORING;
        }
    }

    private void validateGuessAction(Guess guess) {
        validateRoundState(RoundState.PLAYER_GUESSING, () -> "When the round state isn't player guessing, guesser can't guess the story.");
        if (guesses.size() == numberOfGuessers) {
            throw new InvalidGameOperationException(format("Number of guesses can't be higher than %d.", numberOfGuessers));
        }
        var guesser = guess.getGuesser();
        if (guesses.containsKey(guesser)) {
            throw new InvalidGameOperationException(format("Guesser: %s can't guess the story twice in same round.", guesser.getName()));
        }
    }

    public void score() {
        validateRoundState(RoundState.SCORING, () -> "When the round state isn't scoring, Round can't score.");
        var numberOfCorrectGuessers =
                count(guesses.values(), guess -> storyteller.equals(guess.getPlayCard().getPlayer()));
        if (numberOfCorrectGuessers == numberOfGuessers) {
            guessers.forEach(player -> player.addScore(NORMAL_SCORE));
        } else if (numberOfCorrectGuessers == 0) {
            guessers.forEach(player -> player.addScore(NORMAL_SCORE));
            scoreBonusGuessers();
        } else if (numberOfCorrectGuessers < numberOfGuessers) {
            storyteller.addScore(GUESS_CORRECTLY_SCORE);
            scoreCorrectGuessers();
            scoreBonusGuessers();
        }
        roundState = RoundState.ENDED;
    }

    private void validateRoundState(RoundState expectedState, Supplier<String> errorMessageSupplier) {
        if (expectedState != roundState) {
            throw new InvalidGameStateException(errorMessageSupplier.get());
        }
    }

    private void scoreCorrectGuessers() {
        guesses.values().stream()
                .filter(guess -> storyteller.equals(guess.getPlayCard().getPlayer()))
                .map(Guess::getGuesser)
                .forEach(player -> player.addScore(GUESS_CORRECTLY_SCORE));
    }

    private void scoreBonusGuessers() {
        guesses.values().stream()
                .map(Guess::getPlayCard)
                .map(PlayCard::getPlayer)
                .filter(player -> !storyteller.equals(player))
                .forEach(player -> player.addScore(BONUS_SCORE));
    }

    public PlayCard getPlayCardByCardId(int cardId) {
        if (story.getCardId() == cardId) {
            return story.getPlayCard();
        }
        if (!playCards.containsKey(cardId)) {
            throw new NotFoundException(format("CardId: %d does not exist", cardId));
        }
        return playCards.get(cardId);
    }

    public RoundState getState() {
        return roundState;
    }

    public List<Card> withdrawCards() {
        var playCards = new LinkedList<>(this.playCards.values());
        playCards.add(story.getPlayCard());
        return mapToList(playCards, PlayCard::getCard);
    }

    public Player getStoryteller() {
        return storyteller;
    }

    public List<Player> getGuessers() {
        return guessers;
    }
}
