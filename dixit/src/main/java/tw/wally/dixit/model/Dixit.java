package tw.wally.dixit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tw.wally.dixit.exceptions.InvalidGameOperationException;
import tw.wally.dixit.exceptions.InvalidGameStateException;
import tw.wally.dixit.exceptions.NotFoundException;

import java.util.*;

import static java.lang.String.format;
import static java.util.Collections.shuffle;
import static java.util.Comparator.comparing;
import static java.util.List.copyOf;
import static java.util.Optional.ofNullable;
import static tw.wally.dixit.utils.StreamUtils.*;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@Builder
@AllArgsConstructor
public class Dixit {
    public static final int NUMBER_OF_PLAYER_HAND_CARDS = 6;
    public static final int NUMBER_OF_DEALT_CARD = 1;
    public static final int MIN_NUMBER_OF_PLAYERS = 4;
    public static final int MAX_NUMBER_OF_PLAYERS = 6;
    private final String id;
    private GameState gameState;
    private final VictoryCondition victoryCondition;
    private final LinkedList<Card> deck;
    private List<Player> players;
    private int numberOfRounds;
    private Round round;
    private Collection<Player> winners;

    public Dixit(String id, VictoryCondition victoryCondition, Collection<Card> cards) {
        this.id = id;
        this.gameState = GameState.PREPARING;
        this.victoryCondition = victoryCondition;
        this.deck = new LinkedList<>(cards);
        shuffle(this.deck);
        this.players = new ArrayList<>(MAX_NUMBER_OF_PLAYERS);
        this.numberOfRounds = 0;
        this.winners = new ArrayList<>(MAX_NUMBER_OF_PLAYERS);
    }

    public void join(Player player) {
        if (GameState.PREPARING != gameState) {
            throw new InvalidGameStateException("When the game is preparing, player can't join the game");
        }
        int numberOfPlayers = players.size();
        if (numberOfPlayers >= MAX_NUMBER_OF_PLAYERS) {
            throw new InvalidGameOperationException(format("Number of players can't be higher than %d.", MAX_NUMBER_OF_PLAYERS));
        }
        player.setColor(Color.values()[numberOfPlayers]);
        players.add(player);
    }

    public void start() {
        validatePlayersAmount();
        gameState = GameState.STARTED;
        dealCardsToAllPlayers(NUMBER_OF_PLAYER_HAND_CARDS);
        startNewRound();
    }

    private void validatePlayersAmount() {
        int numberOfPlayers = players.size();
        if (numberOfPlayers < MIN_NUMBER_OF_PLAYERS || numberOfPlayers > MAX_NUMBER_OF_PLAYERS) {
            throw new InvalidGameOperationException(format("Number of players can't be lower than %d or higher than %d.", MIN_NUMBER_OF_PLAYERS, MAX_NUMBER_OF_PLAYERS));
        }
    }

    public void startNextRound() {
        dealCardsToAllPlayers(NUMBER_OF_DEALT_CARD);
        startNewRound();
    }

    private void dealCardsToAllPlayers(int numberOfCard) {
        players.forEach(player -> player.addHandCards(generate(numberOfCard, index -> deck.pollLast())));
    }

    private void startNewRound() {
        shuffle(deck);
        numberOfRounds++;
        int currentStoryTellerPosition = numberOfRounds - 1;
        Player storyteller = players.get(currentStoryTellerPosition % players.size());
        var guessers = filterToList(players, player -> player != storyteller);
        round = new Round(storyteller, guessers);
    }

    public void tellStory(String phrase, String storytellerId, int cardId) {
        getRound().tellStory(phrase, storytellerId, cardId);
    }

    public void playCard(String playerId, int cardId) {
        getRound().playCard(playerId, cardId);
    }

    public void guessStory(String guesserId, int playCardId) {
        getRound().guessStory(guesserId, playCardId);
    }

    public void score() {
        getRound().score();

        var winners = filterToList(players, victoryCondition::isWinning);
        if (!winners.isEmpty()) {
            winners.sort(comparing(Player::getScore).reversed()
                    .thenComparing(Player::getId));
            gameState = GameState.OVER;
            this.winners = winners;
        }
    }

    public void withdrawCards() {
        getRound().withdrawCards().forEach(deck::addFirst);
    }

    public Player getCurrentStoryteller() {
        int currentStoryTellerPosition = numberOfRounds - 1;
        return players.get(currentStoryTellerPosition % players.size());
    }

    public List<Player> getCurrentGuessers() {
        return filterToList(players, player -> player != getCurrentStoryteller());
    }

    public Optional<Story> mayHaveCurrentStory() {
        return ofNullable(getRound().getStory());
    }

    public Story getCurrentStory() {
        return mayHaveCurrentStory().orElseThrow(() -> new NotFoundException("There's no current story"));
    }

    public List<PlayCard> getCurrentPlayCards() {
        return getRound().getPlayCards();
    }

    public List<Guess> getCurrentGuesses() {
        return getRound().getGuesses();
    }

    public Player getPlayer(String playerId) {
        return findFirst(players, player -> player.getId().equals(playerId))
                .orElseThrow(() -> new NotFoundException(format("Player: %s not found", playerId)));
    }

    public Round getRound() {
        return round;
    }

    public GameState getGameState() {
        return gameState;
    }

    public RoundState getCurrentRoundState() {
        return getRound().getRoundState();
    }

    public int getNumberOfRounds() {
        return numberOfRounds;
    }

    public List<Player> getPlayers() {
        return copyOf(players);
    }

    public int getDeckSize() {
        return deck.size();
    }

    public Collection<Player> getWinners() {
        return copyOf(winners);
    }
}
