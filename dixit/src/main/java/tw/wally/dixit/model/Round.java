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
import static java.util.List.of;
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
        Player player = story.getPlayer();
        validateRoundState(RoundState.STORY_TELLING, () -> "When the round state isn't story telling, storyteller can't tell the story.",
                () -> player.addHandCard(story.getCard()));
        if (this.story != null) {
            player.addHandCard(story.getCard());
            throw new InvalidGameOperationException("Story has existed, storyteller can't tell the story again.");
        }
        if (!storyteller.equals(story.getPlayer())) {
            player.addHandCard(story.getCard());
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
        Player player = playCard.getPlayer();
        validateRoundState(RoundState.CARD_PLAYING, () -> "When the round state isn't card playing, guesser can't play the card.",
                () -> player.addHandCard(playCard.getCard()));
        if (playCards.size() == numberOfGuessers) {
            player.addHandCard(playCard.getCard());
            throw new InvalidGameOperationException(format("Number of playCards can't be higher than %d.", numberOfGuessers));
        }
        String playerName = player.getName();
        if (!guessers.contains(player)) {
            player.addHandCard(playCard.getCard());
            throw new InvalidGameOperationException(format("Player: %s is not a guesser.", playerName));
        }
        if (contains(playCards.values(), card -> card.getPlayer().equals(player))) {
            player.addHandCard(playCard.getCard());
            throw new InvalidGameOperationException(format("Player: %s can't play the card twice in same round.", playerName));
        }
    }

    public void guessStory(Guess guess) {
        validateGuessAction(guess);
        Player guesser = guess.getGuesser();
        guesses.put(guesser, guess);
        if (guesses.size() == numberOfGuessers) {
            roundState = RoundState.SCORING;
        }
    }

    private void validateGuessAction(Guess guess) {
        Player guesser = guess.getGuesser();
        validateRoundState(RoundState.PLAYER_GUESSING, () -> "When the round state isn't player guessing, guesser can't guess the story.",
                () -> guesser.addHandCard(guess.getCard()));
        if (guesses.size() == numberOfGuessers) {
            guesser.addHandCard(guess.getCard());
            throw new InvalidGameOperationException(format("Number of guesses can't be higher than %d.", numberOfGuessers));
        }
        String guesserName = guesser.getName();
        if (!guessers.contains(guesser)) {
            guesser.addHandCard(guess.getCard());
            throw new InvalidGameOperationException(format("Player: %s is not a guesser.", guesserName));
        }
        if (guesses.containsKey(guesser)) {
            guesser.addHandCard(guess.getCard());
            throw new InvalidGameOperationException(format("Guesser: %s can't guess the story twice in same round.", guesserName));
        }
    }

    public void score() {
        validateRoundState(RoundState.SCORING, () -> "When the round state isn't scoring, Round can't score.");
        var numberOfCorrectGuessers =
                count(guesses.values(), guess -> storyteller.equals(guess.getPlayerWhoPlayedCard()));
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
        roundState = RoundState.OVER;
    }

    private void validateRoundState(RoundState expectedState, Supplier<String> errorMessageSupplier, Runnable... actionsBeforeErrorThrow) {
        if (expectedState != roundState) {
            of(actionsBeforeErrorThrow).forEach(Runnable::run);
            throw new InvalidGameStateException(errorMessageSupplier.get());
        }
    }

    private void scoreCorrectGuessers() {
        guesses.values().stream()
                .filter(guess -> storyteller.equals(guess.getPlayerWhoPlayedCard()))
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
            throw new NotFoundException(format("Card: %d does not found", cardId));
        }

        return playCards.get(cardId);
    }

    public RoundState getState() {
        return roundState;
    }

    public List<Card> withdrawCards() {
        var playCards = new LinkedList<>(this.playCards.values());
        playCards.addFirst(story.getPlayCard());
        return mapToList(playCards, PlayCard::getCard);
    }

    public List<PlayCard> getPlayCards() {
        return copyOf(playCards.values());
    }

    public List<Guess> getGuesses() {
        return copyOf(guesses.values());
    }
}
