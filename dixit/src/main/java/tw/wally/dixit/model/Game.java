package tw.wally.dixit.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.shuffle;
import static tw.wally.dixit.utils.StreamUtils.filter;
import static tw.wally.dixit.utils.StreamUtils.generate;

/**
 * @author - wally55077@gmail.com
 */
public class Game {
    public static final int NUMBER_OF_PLAYER_HAND_CARDS = 6;
    public static final int NUMBER_OF_DEALT_CARD = 1;
    public static final int MIN_NUMBER_OF_PLAYERS = 4;
    public static final int MAX_NUMBER_OF_PLAYERS = 6;
    private final VictoryCondition victoryCondition;
    private final LinkedList<Card> deck;
    private final List<Player> players;
    private final LinkedList<Round> rounds;
    private int currentStoryTellerPosition = -1;
    private GameState gameState;
    private Collection<Player> winners;

    public Game(VictoryCondition victoryCondition, Collection<Card> cards) {
        this.gameState = GameState.PREPARING;
        this.victoryCondition = victoryCondition;
        this.deck = new LinkedList<>(cards);
        this.players = new ArrayList<>(MAX_NUMBER_OF_PLAYERS);
        this.rounds = new LinkedList<>();
    }

    public void join(Player player) {
        if (GameState.PREPARING != gameState) {
            throw new IllegalArgumentException("When the game is preparing, player can't join the game");
        }
        if (players.size() >= MAX_NUMBER_OF_PLAYERS) {
            throw new IllegalArgumentException(format("Number of players can't be higher than %d.", MAX_NUMBER_OF_PLAYERS));
        }
        players.add(player);
    }

    public void start() {
        validatePlayersAmount();
        gameState = GameState.STARTED;
        dealAllPlayersCards(NUMBER_OF_PLAYER_HAND_CARDS);
        startNewRound();
    }

    private void validatePlayersAmount() {
        int numberOfPlayers = players.size();
        if (numberOfPlayers < MIN_NUMBER_OF_PLAYERS || numberOfPlayers > MAX_NUMBER_OF_PLAYERS) {
            throw new IllegalArgumentException(format("Number of players can't be lower than %d or higher than %d.", MIN_NUMBER_OF_PLAYERS, MAX_NUMBER_OF_PLAYERS));
        }
    }

    public void startNextRound() {
        dealAllPlayersCards(NUMBER_OF_DEALT_CARD);
        startNewRound();
    }

    private void dealAllPlayersCards(int numberOfCard) {
        players.forEach(player -> player.addHandCards(generate(numberOfCard, index -> deck.pollLast())));
    }

    private void startNewRound() {
        shuffle(deck);
        currentStoryTellerPosition++;
        var storyteller = players.get(currentStoryTellerPosition % players.size());
        var otherPlayers = filter(players, player -> player != storyteller);
        rounds.add(new Round(storyteller, otherPlayers));
    }

    public void tellStory(String phrase, Player player, Card card) {
        getCurrentRound().setStory(new Story(phrase, player, card));
    }

    public void playCard(Player player, Card card) {
        getCurrentRound().addPlayCard(new PlayCard(player, card));
    }

    public void guessStory(Player guesser, PlayCard playCard) {
        getCurrentRound().addGuess(new Guess(guesser, playCard));
    }

    public void score() {
        getCurrentRound().score();
        var winners = filter(players, victoryCondition::isWinning);
        if (!winners.isEmpty()) {
            gameState = GameState.ENDED;
            this.winners = winners;
        }
    }

    public void withdrawCards() {
        getCurrentRound().getCards().forEach(deck::addFirst);
    }

    public Player getCurrentStoryteller() {
        return getCurrentRound().getStoryteller();
    }

    public List<Player> getCurrentGuessers() {
        return getCurrentRound().getGuessers();
    }

    public Round getCurrentRound() {
        return rounds.getLast();
    }

    public GameState getGameState() {
        return gameState;
    }

    public int getNumberOfRounds() {
        return rounds.size();
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getDeckSize() {
        return deck.size();
    }

    public Collection<Player> getWinners() {
        return winners;
    }
}
