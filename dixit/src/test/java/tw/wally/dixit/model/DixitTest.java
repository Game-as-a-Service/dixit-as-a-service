package tw.wally.dixit.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tw.wally.dixit.exceptions.InvalidGameOperationException;
import tw.wally.dixit.exceptions.InvalidGameStateException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static tw.wally.dixit.model.Dixit.NUMBER_OF_PLAYER_HAND_CARDS;
import static tw.wally.dixit.model.Round.GUESS_CORRECTLY_SCORE;
import static tw.wally.dixit.utils.StreamUtils.limit;

public class DixitTest extends AbstractDixitTest {

    public static final String DIXIT_ID = "dixitId";
    public static final int DEFAULT_CARD_SIZE = 36;
    public static final int DEFAULT_WINNING_SCORE = 30;
    private Dixit dixit;
    private Map<Player, Card> cardOfPlayers;

    @BeforeEach
    public void beforeTest() {
        this.dixit = new Dixit(DIXIT_ID, new VictoryCondition(DEFAULT_WINNING_SCORE), generateCards(DEFAULT_CARD_SIZE));
        this.cardOfPlayers = new HashMap<>(Dixit.MAX_NUMBER_OF_PLAYERS);
    }

    @Test
    public void WhenFourPlayersJoinCreatedGame_ThenGameShouldHaveFourPlayers() {
        var expectedPlayers = generatePlayers(4);
        joinGame(expectedPlayers);

        assertGameIsPreparingAndPlayersJoinedGame(expectedPlayers);
    }

    @Test
    public void WhenSevenPlayersJoinCreatedGame_ThenGameShouldFail() {
        var players = generatePlayers(7);

        assertThrows(InvalidGameOperationException.class, () -> joinGame(players));
    }

    @Test
    public void GivenTwoPlayersJoinedGame_WhenGameStart_ThenGameShouldFail() {
        givenPlayersJoinGame(2);

        assertThrows(InvalidGameOperationException.class, () -> dixit.start());
    }

    @Test
    public void GivenFourPlayersJoinedGameAndGameStarted_WhenTwoPlayersJoinGame_ThenShouldFail() {
        givenPlayersJoinGame(4);
        dixit.start();

        assertThrows(InvalidGameStateException.class, () -> givenPlayersJoinGame(2));
    }

    @Test
    public void GivenFourPlayersJoinedGame_WhenGameStart_ThenEachPlayerShouldHaveSixCardsAndGameShouldStartAndCurrentRoundShouldBeFirstRound() {
        givenPlayersJoinGame(4);

        dixit.start();

        dixit.getPlayers().forEach(player -> assertEquals(NUMBER_OF_PLAYER_HAND_CARDS, player.getHandCards().size()));
        assertEquals(getCurrentDeckSizeAfterDealCard(4), dixit.getDeckSize());
        assertGameStartedAndNumberOfRound(1);
    }

    @Test
    public void GivenFirstRoundScored_WhenGameWithdrawsCards_ThenShouldSuccess() {
        givenGameStartedAndRoundStateIsStoryGuessing(5);
        dixit.score();

        dixit.withdrawCards();

        int expectedDeckSize = getCurrentDeckSizeAfterDealCard(5) + 5;
        int actualDeckSize = dixit.getDeckSize();
        assertEquals(expectedDeckSize, actualDeckSize);
    }

    @Test
    public void GivenFirstRoundScored_WhenSecondRoundStart_ThenEachPlayerShouldHaveSixCardsAndCurrentRoundShouldBeSecondRound() {
        givenGameStartedAndRoundStateIsStoryGuessing(6);
        dixit.score();
        dixit.withdrawCards();

        dixit.startNextRound();

        dixit.getPlayers().forEach(player -> assertEquals(NUMBER_OF_PLAYER_HAND_CARDS, player.getHandCards().size()));
        assertGameStartedAndNumberOfRound(2);
    }

    @Test
    public void GivenGameScored_WhenOnePlayerAchievedWinningScore_ThenGameStateShouldBeOverAndShouldHaveOneWinner() {
        givenGameStartedAndRoundStateIsStoryGuessing(4);
        var players = limit(dixit.getCurrentGuessers(), 1);
        makePlayersAchieveWinningScore(players);

        dixit.score();

        assertGameIsEndAndShouldHaveWinners(players);
    }

    @Test
    public void GivenGameScored_WhenTwoPlayersAchievedWinningScore_ThenGameShouldHaveTwoWinners() {
        givenGameStartedAndRoundStateIsStoryGuessing(4);
        var players = limit(dixit.getCurrentGuessers(), 2);
        makePlayersAchieveWinningScore(players);

        dixit.score();

        assertGameIsEndAndShouldHaveWinners(players);
    }

    private void givenGameStartedAndRoundStateIsStoryGuessing(int numberOfPlayers) {
        givenPlayersJoinGame(numberOfPlayers);
        dixit.start();
        tellStory();

        var guessers = dixit.getCurrentGuessers();

        guessers.forEach(this::playCard);
        guessers.forEach(guesser -> guessStory(guesser, dixit.getCurrentStoryteller()));
    }

    private void givenPlayersJoinGame(int numberOfPlayers) {
        joinGame(generatePlayers(numberOfPlayers));
    }

    private void joinGame(Collection<Player> players) {
        players.forEach(dixit::join);
    }

    private void tellStory() {
        Player player = dixit.getCurrentStoryteller();
        Card card = getRandomCard(player);
        dixit.tellStory(DIXIT_PHRASE, player.getId(), card.getId());
        cardOfPlayers.put(player, card);
    }

    private void playCard(Player player) {
        Card card = getRandomCard(player);
        dixit.playCard(player.getId(), card.getId());
    }

    private Card getRandomCard(Player player) {
        var handCards = player.getHandCards();
        return handCards.get(new Random().nextInt(handCards.size()));
    }

    private void guessStory(Player guesser, Player playerWhoBeGuessed) {
        int cardId = cardOfPlayers.get(playerWhoBeGuessed).getId();
        PlayCard playCard = dixit.getRound().getPlayCard(cardId);
        dixit.guessStory(guesser.getId(), playCard.getCardId());
    }

    private int getCurrentDeckSizeAfterDealCard(int numberOfPlayers) {
        return DEFAULT_CARD_SIZE - numberOfPlayers * NUMBER_OF_PLAYER_HAND_CARDS;
    }

    private void makePlayersAchieveWinningScore(Collection<Player> players) {
        int scoreTimes = DEFAULT_WINNING_SCORE / GUESS_CORRECTLY_SCORE;
        for (Player player : players) {
            for (int currentTime = 0; currentTime < scoreTimes; currentTime++) {
                player.addScore(GUESS_CORRECTLY_SCORE);
            }
        }
    }

    private void assertGameIsPreparingAndPlayersJoinedGame(Collection<Player> expectedPlayers) {
        assertEquals(GameState.PREPARING, dixit.getGameState());
        var actualPlayers = dixit.getPlayers();
        assertEquals(expectedPlayers.size(), actualPlayers.size());
        assertEquals(expectedPlayers, actualPlayers);
    }

    private void assertGameStartedAndNumberOfRound(int numberOfRounds) {
        assertEquals(GameState.STARTED, dixit.getGameState());
        assertEquals(numberOfRounds, dixit.getNumberOfRounds());
    }

    private void assertGameIsEndAndShouldHaveWinners(Collection<Player> players) {
        assertEquals(GameState.OVER, dixit.getGameState());
        var winners = dixit.getWinners();
        assertEquals(players.size(), winners.size());
        assertTrue(winners.containsAll(players));
    }
}