package tw.wally.dixit.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.List.copyOf;
import static tw.wally.dixit.utils.StreamUtils.*;

/**
 * @author - wally55077@gmail.com
 */
public class Round {
    public static final int BONUS_SCORE = 1;
    public static final int ANSWER_CORRECTLY_SCORE = 3;
    private final Player storyteller;
    private final List<Player> guessers;
    private final Map<Integer, PlayCard> playCards;
    private final Map<Player, Guess> guesses;
    private final int numberOfGuessers;
    private RoundState roundState;
    private Story story;

    public Round(Player storyTeller, List<Player> guessers) {
        this.roundState = RoundState.STORY_TELLING;
        this.storyteller = storyTeller;
        this.guessers = copyOf(guessers);
        this.numberOfGuessers = guessers.size();
        this.playCards = new HashMap<>(numberOfGuessers);
        this.guesses = new HashMap<>(numberOfGuessers);
    }

    public void setStory(Story story) {
        if (RoundState.STORY_TELLING != roundState) {
            throw new IllegalArgumentException("When the round state isn't story telling, storyteller can't tell the story.");
        }
        if (this.story != null) {
            throw new IllegalArgumentException("Story has existed, storyteller can't tell the story again.");
        }
        if (!storyteller.equals(story.getPlayer())) {
            throw new IllegalArgumentException("Guesser can't tell the story.");
        }
        this.story = story;
        roundState = RoundState.CARD_PLAYING;
    }

    public void addPlayCard(PlayCard playCard) {
        if (RoundState.CARD_PLAYING != roundState) {
            throw new IllegalArgumentException("When the round state isn't card playing, guesser can't play the card.");
        }
        if (playCards.size() == numberOfGuessers) {
            throw new IllegalArgumentException(format("Number of playCards can't be higher than %d.", numberOfGuessers));
        }
        var player = playCard.getPlayer();
        if (contains(playCards.values(), card -> card.getPlayer().equals(player))) {
            throw new IllegalArgumentException(format("Player: %s can't play the card twice in same round.", player.getName()));
        }
        playCards.put(playCard.getCardId(), playCard);
        if (playCards.size() == numberOfGuessers) {
            roundState = RoundState.PLAYER_GUESSING;
        }
    }

    public void addGuess(Guess guess) {
        if (RoundState.PLAYER_GUESSING != roundState) {
            throw new IllegalArgumentException("When the round state isn't player guessing, guesser can't guess the story.");
        }
        if (guesses.size() == numberOfGuessers) {
            throw new IllegalArgumentException(format("Number of guesses can't be higher than %d.", numberOfGuessers));
        }
        var guesser = guess.getGuesser();
        if (guesses.containsKey(guesser)) {
            throw new IllegalArgumentException(format("Guesser: %s can't guess the story twice in same round.", guesser.getName()));
        }
        guesses.put(guesser, guess);
        if (guesses.size() == numberOfGuessers) {
            roundState = RoundState.SCORING;
        }
    }

    public void score() {
        if (RoundState.SCORING != roundState) {
            throw new IllegalArgumentException("When the round state isn't scoring, Round can't score.");
        }
        long numberOfAllGuessersWhoGuessesStoryCorrectly =
                count(guesses.values(), guess -> storyteller.equals(guess.getPlayCard().getPlayer()));
        if (numberOfAllGuessersWhoGuessesStoryCorrectly == numberOfGuessers) {
            guessers.forEach(player -> player.addScore(2));
        } else if (numberOfAllGuessersWhoGuessesStoryCorrectly == 0) {
            guessers.forEach(player -> player.addScore(2));
            addBonusScoreToGuesserWhoseCardIsGuessedByOtherGuesser();
        } else if (numberOfAllGuessersWhoGuessesStoryCorrectly < numberOfGuessers) {
            storyteller.addScore(ANSWER_CORRECTLY_SCORE);
            addScoreToGuesserWhoGuessedStoryCorrectly();
            addBonusScoreToGuesserWhoseCardIsGuessedByOtherGuesser();
        }
        roundState = RoundState.ENDED;
    }

    private void addScoreToGuesserWhoGuessedStoryCorrectly() {
        guesses.values().stream()
                .filter(guess -> storyteller.equals(guess.getPlayCard().getPlayer()))
                .map(Guess::getGuesser)
                .forEach(player -> player.addScore(ANSWER_CORRECTLY_SCORE));
    }

    private void addBonusScoreToGuesserWhoseCardIsGuessedByOtherGuesser() {
        guesses.values().stream()
                .map(Guess::getPlayCard)
                .map(PlayCard::getPlayer)
                .filter(player -> !storyteller.equals(player))
                .forEach(player -> player.addScore(BONUS_SCORE));
    }

    public PlayCard getPlayCardByCardId(int cardId) {
        if (story.getCardId() == cardId) {
            return new PlayCard(storyteller, story.getCard());
        }
        return playCards.get(cardId);
    }

    public RoundState getState() {
        return roundState;
    }

    public List<Card> getCards() {
        var cards = mapToList(playCards.values(), PlayCard::getCard);
        cards.add(story.getCard());
        return cards;
    }

    public Player getStoryteller() {
        return storyteller;
    }

    public List<Player> getGuessers() {
        return guessers;
    }
}
