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
import static java.util.Arrays.asList;
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
        Player storyteller = story.getPlayer();
        Card handCard = story.getCard();
        validateRoundState(RoundState.STORY_TELLING, () -> "When the round state isn't story telling, storyteller can't tell the story.",
                () -> storyteller.addHandCard(handCard));
        if (this.story != null) {
            storyteller.addHandCard(handCard);
            throw new InvalidGameOperationException("Story has existed, storyteller can't tell the story again.");
        }
        if (!this.storyteller.equals(story.getPlayer())) {
            storyteller.addHandCard(handCard);
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
        Player cardPlayer = playCard.getPlayer();
        Card handCard = playCard.getCard();
        validateRoundState(RoundState.CARD_PLAYING, () -> "When the round state isn't card playing, guesser can't play the card.",
                () -> cardPlayer.addHandCard(handCard));
        if (playCards.size() == numberOfGuessers) {
            cardPlayer.addHandCard(handCard);
            throw new InvalidGameOperationException(format("Number of playCards can't be higher than %d.", numberOfGuessers));
        }
        String cardPlayerName = cardPlayer.getName();
        if (!guessers.contains(cardPlayer)) {
            cardPlayer.addHandCard(handCard);
            throw new InvalidGameOperationException(format("Player: %s is not a guesser.", cardPlayerName));
        }
        if (contains(playCards.values(), card -> card.getPlayer().equals(cardPlayer))) {
            cardPlayer.addHandCard(handCard);
            throw new InvalidGameOperationException(format("Player: %s can't play the card twice in same round.", cardPlayerName));
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
        validateRoundState(RoundState.PLAYER_GUESSING, () -> "When the round state isn't player guessing, guesser can't guess the story.");
        if (guesses.size() == numberOfGuessers) {
            throw new InvalidGameOperationException(format("Number of guesses can't be higher than %d.", numberOfGuessers));
        }
        String guesserName = guesser.getName();
        if (!guessers.contains(guesser)) {
            throw new InvalidGameOperationException(format("Player: %s is not a guesser.", guesserName));
        }
        if (guesses.containsKey(guesser)) {
            throw new InvalidGameOperationException(format("Guesser: %s can't guess the story twice in same round.", guesserName));
        }
    }

    public void score() {
        validateRoundState(RoundState.SCORING, () -> "When the round state isn't scoring, Round can't score.");
        var numberOfCorrectGuessers =
                count(guesses.values(), guess -> storyteller.equals(guess.getPlayCardPlayer()));
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

    private void validateRoundState(RoundState expectedState, Supplier<String> errorMessageSupplier, Runnable... actionsBeforeErrorThrow) {
        if (expectedState != roundState) {
            asList(actionsBeforeErrorThrow).forEach(Runnable::run);
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
                .map(Guess::getPlayCard)
                .map(PlayCard::getPlayer)
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
