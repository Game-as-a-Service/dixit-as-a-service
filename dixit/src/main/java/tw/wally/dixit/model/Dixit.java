package tw.wally.dixit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tw.wally.dixit.exceptions.InvalidGameOperationException;
import tw.wally.dixit.exceptions.InvalidGameStateException;
import tw.wally.dixit.exceptions.NotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.shuffle;
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
    private final VictoryCondition victoryCondition;
    private final LinkedList<Card> deck;
    private final List<Player> players;
    private final LinkedList<Round> rounds;
    private int currentStoryTellerPosition = -1;
    private GameState gameState;
    private Collection<Player> winners;

    public Dixit(String id, VictoryCondition victoryCondition, Collection<Card> cards) {
        this.gameState = GameState.PREPARING;
        this.id = id;
        this.victoryCondition = victoryCondition;
        this.deck = new LinkedList<>(cards);
        this.players = new ArrayList<>(MAX_NUMBER_OF_PLAYERS);
        this.rounds = new LinkedList<>();
        this.winners = new ArrayList<>(MAX_NUMBER_OF_PLAYERS);
    }

    public void join(Player player) {
        if (GameState.PREPARING != gameState) {
            throw new InvalidGameStateException("When the game is preparing, player can't join the game");
        }
        if (players.size() >= MAX_NUMBER_OF_PLAYERS) {
            throw new InvalidGameOperationException(format("Number of players can't be higher than %d.", MAX_NUMBER_OF_PLAYERS));
        }
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
        currentStoryTellerPosition++;
        Player storyteller = players.get(currentStoryTellerPosition % players.size());
        var guessers = filter(players, player -> player != storyteller);
        rounds.add(new Round(storyteller, guessers));
    }

    public void tellStory(String phrase, Player storyteller, Card card) {
        getCurrentRound().tellStory(new Story(phrase, new PlayCard(storyteller, card)));
    }

    public void playCard(Player player, Card card) {
        getCurrentRound().playCard(new PlayCard(player, card));
    }

    public void guessStory(Player guesser, PlayCard playCard) {
        getCurrentRound().guessStory(new Guess(guesser, playCard));
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
        getCurrentRound().withdrawCards().forEach(deck::addFirst);
    }

    public Player getCurrentStoryteller() {
        return getCurrentRound().getStoryteller();
    }

    public List<Player> getCurrentGuessers() {
        return getCurrentRound().getGuessers();
    }

    public List<Player> getCurrentGuessersWhoPlayedCard() {
        return getCurrentRound().getGuessersWhoPlayedCard();
    }

    public List<Player> getCurrentGuessersWhoGuessed() {
        return getCurrentRound().getGuessersWhoGuessed();
    }

    public List<PlayCard> getCurrentPlayCards() {
        return getCurrentRound().getPlayCards();
    }

    public Player getPlayer(String playerId) {
        return findFirst(players, player -> playerId.equals(player.getId()))
                .orElseThrow(() -> new NotFoundException(format("Player: %s not found", playerId)));
    }

    public PlayCard getPlayCard(int cardId) {
        return getCurrentRound().getPlayCardByCardId(cardId);
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
