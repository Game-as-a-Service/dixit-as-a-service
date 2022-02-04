package tw.wally.dixit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tw.wally.dixit.exceptions.InvalidGameOperationException;
import tw.wally.dixit.exceptions.InvalidGameStateException;
import tw.wally.dixit.exceptions.NotFoundException;

import java.util.Collection;
import java.util.HashMap;
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
    private RoundState roundState;
    private final int numberOfGuessers;
    private final Player storyteller;
    private final List<Player> guessers;
    private Story story;
    private final Map<Integer, PlayCard> playCards;
    private final Map<Player, Guess> guesses;

    public Round(Player storyteller, List<Player> guessers) {
        this.roundState = RoundState.STORY_TELLING;
        this.numberOfGuessers = guessers.size();
        this.storyteller = storyteller;
        this.guessers = copyOf(guessers);
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
        Player player = story.getPlayer();
        String playerName = player.getName();
        if (!this.storyteller.equals(player)) {
            throw new InvalidGameOperationException(format("Player: %s is not a storyteller.", playerName));
        }
        if (this.story != null) {
            throw new InvalidGameOperationException(format("Player: %s can't tell the story twice in same round.", playerName));
        }
    }

    public void playCard(PlayCard playCard) {
        validatePlayCardAction(playCard);
        playCards.put(playCard.getCardId(), playCard);
        if (playCards.size() == numberOfGuessers) {
            roundState = RoundState.STORY_GUESSING;
        }
    }

    private void validatePlayCardAction(PlayCard playCard) {
        validateRoundState(RoundState.CARD_PLAYING, () -> "When the round state isn't card playing, guesser can't play the card.");
        Player player = playCard.getPlayer();
        String playerName = player.getName();
        if (!guessers.contains(player)) {
            throw new InvalidGameOperationException(format("Player: %s is not a guesser.", playerName));
        }
        if (contains(playCards.values(), card -> card.getPlayer().equals(player))) {
            throw new InvalidGameOperationException(format("Player: %s can't play the card twice in same round.", playerName));
        }
        if (playCards.size() == numberOfGuessers) {
            throw new InvalidGameOperationException(format("Number of playCards can't be higher than %d.", numberOfGuessers));
        }
    }

    public void guessStory(Guess guess) {
        validateGuessStoryAction(guess);
        Player guesser = guess.getGuesser();
        guesses.put(guesser, guess);
        if (guesses.size() == numberOfGuessers) {
            roundState = RoundState.SCORING;
        }
    }

    private void validateGuessStoryAction(Guess guess) {
        validateRoundState(RoundState.STORY_GUESSING, () -> "When the round state isn't story guessing, guesser can't guess the story.");
        Player guesser = guess.getGuesser();
        String guesserName = guesser.getName();
        if (!guessers.contains(guesser)) {
            throw new InvalidGameOperationException(format("Player: %s is not a guesser.", guesserName));
        }
        if (guesses.containsKey(guesser)) {
            throw new InvalidGameOperationException(format("Guesser: %s can't guess the story twice in same round.", guesserName));
        }
        if (guesses.size() == numberOfGuessers) {
            throw new InvalidGameOperationException(format("Number of guesses can't be higher than %d.", numberOfGuessers));
        }
    }

    public void score() {
        validateRoundState(RoundState.SCORING, () -> "When the round state isn't scoring, Round can't score.");
        var numberOfCorrectGuessers = count(guesses.values(), guess -> storyteller.equals(guess.getPlayCardPlayer()));
        if (numberOfCorrectGuessers == numberOfGuessers) {
            guessers.forEach(guesser -> guesser.addScore(NORMAL_SCORE));
        } else if (numberOfCorrectGuessers == 0) {
            guessers.forEach(guesser -> guesser.addScore(NORMAL_SCORE));
            scoreBonusGuessers();
        } else if (numberOfCorrectGuessers < numberOfGuessers) {
            story.getPlayer().addScore(GUESS_CORRECTLY_SCORE);
            scoreCorrectGuessers();
            scoreBonusGuessers();
        }
    }

    private void validateRoundState(RoundState expectedState, Supplier<String> errorMessageSupplier) {
        if (expectedState != roundState) {
            throw new InvalidGameStateException(errorMessageSupplier.get());
        }
    }

    private void scoreCorrectGuessers() {
        guesses.values().stream()
                .filter(guess -> storyteller.equals(guess.getPlayCardPlayer()))
                .map(Guess::getGuesser)
                .forEach(guesser -> guesser.addScore(GUESS_CORRECTLY_SCORE));
    }

    private void scoreBonusGuessers() {
        guesses.values().stream()
                .map(Guess::getPlayCardPlayer)
                .filter(player -> !storyteller.equals(player))
                .forEach(guesser -> guesser.addScore(BONUS_SCORE));
    }

    public PlayCard getPlayCard(int cardId) {
        if (story.getCardId() == cardId) {
            return story.getPlayCard();
        }
        if (!playCards.containsKey(cardId)) {
            throw new NotFoundException(format("Card: %d does not found", cardId));
        }
        return playCards.get(cardId);
    }

    public RoundState getState() {
        return roundState;
    }

    public Collection<Card> withdrawCards() {
        var playCards = mapToList(this.playCards.values(), PlayCard::getCard);
        playCards.add(this.story.getCard());
        return copyOf(playCards);
    }

    public List<PlayCard> getPlayCards() {
        return copyOf(this.playCards.values());
    }

    public List<Guess> getGuesses() {
        return copyOf(this.guesses.values());
    }
}
