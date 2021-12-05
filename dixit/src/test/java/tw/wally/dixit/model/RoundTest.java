package tw.wally.dixit.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tw.wally.dixit.exceptions.InvalidGameOperationException;
import tw.wally.dixit.exceptions.InvalidGameStateException;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static tw.wally.dixit.model.Dixit.NUMBER_OF_PLAYER_HAND_CARDS;
import static tw.wally.dixit.model.DixitTest.DEFAULT_CARD_SIZE;
import static tw.wally.dixit.utils.StreamUtils.filterToList;
import static tw.wally.dixit.utils.StreamUtils.generate;

public class RoundTest extends AbstractDixitTest {

    private static final int NUMBER_OF_GUESSERS = 3;
    private Round currentRound;
    private Player storyteller;
    private List<Player> guessers;
    private Map<Player, Card> cardOfPlayers;

    @BeforeEach
    public void beforeTest() {
        int numberOfPlayers = NUMBER_OF_GUESSERS + 1;
        var players = generatePlayers(numberOfPlayers);
        this.storyteller = players.get(0);
        this.guessers = filterToList(players, player -> !player.equals(storyteller));
        this.currentRound = new Round(storyteller, guessers);
        dealAllPlayersCards(players);
        assertEquals(RoundState.STORY_TELLING, currentRound.getState());
        this.cardOfPlayers = new HashMap<>(numberOfPlayers);
    }

    private void dealAllPlayersCards(Collection<Player> players) {
        var deck = new LinkedList<>(generateCards(DEFAULT_CARD_SIZE));
        players.forEach(player -> player.addHandCards(generate(NUMBER_OF_PLAYER_HAND_CARDS, index -> deck.pollLast())));
    }

    @Test
    public void WhenStorytellerTellStory_ThenGoToCardPlayingState() {
        tellStory();

        assertEquals(RoundState.CARD_PLAYING, currentRound.getState());
    }

    @Test
    public void WhenGuesserTellStory_ThenShouldFail() {
        Player guesser = guessers.get(0);

        assertThrows(InvalidGameOperationException.class, () -> tellStory(guesser));
        assertEquals(NUMBER_OF_PLAYER_HAND_CARDS, guesser.getHandCards().size());
    }

    @Test
    public void GivenStoryTold_WhenStorytellerTellStoryAgain_ThenShouldFail() {
        tellStory();

        assertThrows(InvalidGameStateException.class, this::tellStory);
        int expectedHandCards = NUMBER_OF_PLAYER_HAND_CARDS - 1;
        assertEquals(expectedHandCards, storyteller.getHandCards().size());
    }

    @Test
    public void GivenStoryTold_WhenAllGuessersPlayCard_ThenGoToPlayerGuessingState() {
        tellStory();

        guessers.forEach(this::playCard);

        assertEquals(RoundState.PLAYER_GUESSING, currentRound.getState());
    }

    @Test
    public void GivenStoryToldAndGuesser1PlayedCard_WhenGuesser1PlayCardAgain_ThenShouldFail() {
        tellStory();
        Player guesser1 = guessers.get(0);
        playCard(guesser1);

        assertThrows(InvalidGameOperationException.class, () -> playCard(guesser1));
        int expectedHandCards = NUMBER_OF_PLAYER_HAND_CARDS - 1;
        assertEquals(expectedHandCards, guesser1.getHandCards().size());
    }

    @Test
    public void GivenStoryToldAndAllGuessersPlayedCard_WhenGuesser1PlayCardAgain_ThenShouldFail() {
        givenRoundStateIsPlayerGuessing();

        Player guesser1 = guessers.get(0);
        assertThrows(InvalidGameStateException.class, () -> playCard(guesser1));
        int expectedHandCards = NUMBER_OF_PLAYER_HAND_CARDS - 1;
        assertEquals(expectedHandCards, guesser1.getHandCards().size());
    }

    @Test
    public void GivenRoundStateIsPlayerGuessing_WhenAllGuessersGuessStory_ThenGoToScoringState() {
        givenRoundStateIsPlayerGuessing();

        guessers.forEach(player -> guessStory(player, storyteller));

        assertEquals(RoundState.SCORING, currentRound.getState());
    }

    @Test
    public void GivenRoundStateIsPlayerGuessingAndGuesser1GuessedStory_WhenGuesser1GuessStoryAgain_ThenShouldFail() {
        givenRoundStateIsPlayerGuessing();
        Player guesser1 = guessers.get(0);
        guessStory(guesser1, storyteller);

        assertThrows(InvalidGameOperationException.class, () -> guessStory(guesser1, storyteller));
    }

    @Test
    public void GivenRoundStateIsPlayerGuessingAndAllGuessersGuessedStory_WhenGuesser1GuessStoryAgain_ThenShouldFail() {
        givenRoundStateIsPlayerGuessing();

        Player guesser1 = guessers.get(0);
        guessStory(guesser1, storyteller);

        assertThrows(InvalidGameOperationException.class, () -> guessStory(guesser1, storyteller));
    }

    @DisplayName("Given the story told and all guessers played the card and guessed the story correctly" +
            "and storyteller whose score is 0 points and all guessers whose score is 0 points, " +
            "When the round score, " +
            "Then storyteller should be 0 points and all guessers should be 2 points")
    @Test
    public void testAllGuessersFindStorytellerCard() {
        givenRoundStateIsPlayerGuessing();
        assertEquals(0, storyteller.getScore());
        guessers.forEach(guesser -> assertEquals(0, guesser.getScore()));
        guessers.forEach(guesser -> guessStory(guesser, storyteller));

        currentRound.score();

        int expectedStorytellerScore = 0;
        var expectedGuessersScores = generate(guessers.size(), score -> 2);
        assertStorytellerScoreAndAllGuesserScore(expectedStorytellerScore, expectedGuessersScores);
    }

    @DisplayName("Given story told and all guessers (A, B, C) played card and no guessers guessed the story, " +
            "but A and B guessed C's card and C guessed B's card" +
            "and storyteller whose score is 0 points and all guessers (A, B, C) whose score is 0 points, " +
            "When the round score, " +
            "Then storyteller should be 0 points and all guessers (A, B, C) should be 2 points" +
            "and guesser (B) should get 1 bonus and guesser (C) should get 2 bonus")
    @Test
    public void testNoGuessersFindStorytellerCard() {
        givenRoundStateIsPlayerGuessing();
        assertEquals(0, storyteller.getScore());
        guessers.forEach(guesser -> assertEquals(0, guesser.getScore()));
        var A = guessers.get(0);
        var B = guessers.get(1);
        var C = guessers.get(2);
        guessStory(of(A, B), C);
        guessStory(C, B);

        currentRound.score();

        int expectedStorytellerScore = 0;
        var expectedGuessersScores = asList(2, 3, 4);
        assertStorytellerScoreAndAllGuesserScore(expectedStorytellerScore, expectedGuessersScores);
    }

    @DisplayName("Given story told and all guessers (A, B, C) played card " +
            "and A guessed the story correctly, " +
            "but B guessed A's card and C guessed B's card" +
            "and storyteller whose score is 0 points and all guessers (A, B, C) whose score is 0 points, " +
            "When the round score, " +
            "Then storyteller should be 3 points and guesser (A) should be 3 points" +
            "and guesser (A) should get 1 bonus and guesser (B) should get 1 bonus")
    @Test
    public void testAtLeastOneGuesserButNotAllGuessersFindStorytellerCard() {
        givenRoundStateIsPlayerGuessing();
        assertEquals(0, storyteller.getScore());
        guessers.forEach(guesser -> assertEquals(0, guesser.getScore()));
        var A = guessers.get(0);
        var B = guessers.get(1);
        var C = guessers.get(2);
        guessStory(A, storyteller);
        guessStory(B, A);
        guessStory(C, B);

        currentRound.score();

        int expectedStorytellerScore = 3;
        var expectedGuessersScores = asList(4, 1, 0);
        assertStorytellerScoreAndAllGuesserScore(expectedStorytellerScore, expectedGuessersScores);
    }

    @Test
    public void GivenRoundStateIsPlayerGuessing_WhenRoundScore_ThenShouldFail() {
        givenRoundStateIsPlayerGuessing();

        assertThrows(InvalidGameStateException.class, () -> currentRound.score());
    }

    private void tellStory() {
        tellStory(storyteller);
    }

    private void tellStory(Player player) {
        Card card = getRandomCard(player);
        currentRound.tellStory(new Story(DIXIT_PHRASE, new PlayCard(player, card)));
        cardOfPlayers.put(player, card);
    }

    private void playCard(Player player) {
        Card card = getRandomCard(player);
        currentRound.playCard(new PlayCard(player, card));
        cardOfPlayers.put(player, card);
    }

    private Card getRandomCard(Player player) {
        var handCards = player.getHandCards();
        Card card = handCards.get(new Random().nextInt(handCards.size()));
        return player.playCard(card.getId());
    }

    private void guessStory(Collection<Player> guessers, Player playerWhoisGuessed) {
        guessers.forEach(player -> guessStory(player, playerWhoisGuessed));
    }

    private void guessStory(Player guesser, Player playerWhoBeGuessed) {
        int cardId = cardOfPlayers.get(playerWhoBeGuessed).getId();
        PlayCard playCard = currentRound.getPlayCard(cardId);
        currentRound.guessStory(new Guess(guesser, playCard));
    }

    private void givenRoundStateIsPlayerGuessing() {
        tellStory();
        guessers.forEach(this::playCard);
        assertEquals(NUMBER_OF_GUESSERS, guessers.size());
    }

    private void assertStorytellerScoreAndAllGuesserScore(int expectedStorytellerScore, List<Integer> expectedGuessersScores) {
        assertEquals(expectedStorytellerScore, storyteller.getScore());
        assertEquals(guessers.size(), expectedGuessersScores.size());
        for (int index = 0; index < guessers.size(); index++) {
            assertEquals(expectedGuessersScores.get(index), guessers.get(index).getScore());
        }
    }

}