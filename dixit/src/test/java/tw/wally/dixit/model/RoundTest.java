package tw.wally.dixit.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static tw.wally.dixit.model.Game.NUMBER_OF_PLAYER_HAND_CARDS;
import static tw.wally.dixit.model.GameTest.DEFAULT_CARD_SIZE;
import static tw.wally.dixit.utils.StreamUtils.generate;
import static tw.wally.dixit.utils.StreamUtils.skip;

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
        this.guessers = skip(players, 1);
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
    public void WhenStorytellerTellStory_ThenRoundStateShouldBeCardPlaying() {
        tellStory();

        assertEquals(RoundState.CARD_PLAYING, currentRound.getState());
    }

    @Test
    public void WhenGuesserTellStory_ThenShouldFail() {
        var guesser = guessers.get(0);

        assertThrows(IllegalArgumentException.class, () -> tellStory(guesser));
    }

    @Test
    public void GivenStoryTold_WhenStorytellerTellStoryAgain_ThenShouldFail() {
        tellStory();

        assertThrows(IllegalArgumentException.class, this::tellStory);
    }

    @Test
    public void GivenStoryTold_WhenAllGuessersPlayCard_ThenRoundStateShouldBePlayerGuessing() {
        tellStory();

        guessers.forEach(this::playCard);

        assertEquals(RoundState.PLAYER_GUESSING, currentRound.getState());
    }

    @Test
    public void GivenStoryToldAndGuesser1PlayedCard_WhenGuesserAPlayCardAgain_ThenShouldFail() {
        tellStory();
        var Guesser1 = guessers.get(0);
        playCard(Guesser1);

        assertThrows(IllegalArgumentException.class, () -> playCard(Guesser1));
    }

    @Test
    public void GivenStoryToldAndAllGuessersPlayedCard_WhenFirstGuesserPlayCardAgain_ThenShouldFail() {
        givenStoryToldAndAllGuessersPlayedCard();

        var firstGuesser = guessers.get(0);
        assertThrows(IllegalArgumentException.class, () -> playCard(firstGuesser));
    }

    @Test
    public void GivenStoryToldAndAllGuessersPlayedCard_WhenAllGuessersGuessStory_ThenRoundStateShouldBeScoring() {
        givenStoryToldAndAllGuessersPlayedCard();

        guessers.forEach(player -> guessStory(player, storyteller));

        assertEquals(RoundState.SCORING, currentRound.getState());
    }

    @Test
    public void GivenStoryToldAndAllGuessersPlayedCardAndGuesser1GuessedStory_WhenGuesser1GuessStoryAgain_ThenShouldFail() {
        givenStoryToldAndAllGuessersPlayedCard();
        var Guesser1 = guessers.get(0);
        guessStory(Guesser1, storyteller);

        assertThrows(IllegalArgumentException.class, () -> guessStory(Guesser1, storyteller));
    }

    @Test
    public void GivenStoryToldAndAllGuessersPlayedCardAndGuessedStory_WhenFirstGuesserGuessStoryAgain_ThenShouldFail() {
        givenStoryToldAndAllGuessersPlayedCardAndGuessedStory();

        var surplusGuesser = new Player(Integer.MIN_VALUE, DIXIT_PLAYER);
        assertThrows(IllegalArgumentException.class, () -> guessStory(surplusGuesser, storyteller));
    }

    @DisplayName("Given the story told and all guessers played the card and guessed the story correctly" +
            "and storyteller whose score is 0 points and all guessers whose score is 0 points, " +
            "When the round score, " +
            "Then storyteller should be 0 points and all guessers should be 2 points")
    @Test
    public void testAllGuessersFindStorytellerCard() {
        givenStoryToldAndAllGuessersPlayedCard();
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
        givenStoryToldAndAllGuessersPlayedCard();
        assertEquals(0, storyteller.getScore());
        guessers.forEach(guesser -> assertEquals(0, guesser.getScore()));
        var A = guessers.get(0);
        var B = guessers.get(1);
        var C = guessers.get(2);
        guessStory(of(A, B), C);
        guessStory(C, B);

        currentRound.score();

        int expectedStorytellerScore = 0;
        var expectedGuessersScores = of(2, 3, 4);
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
        givenStoryToldAndAllGuessersPlayedCard();
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
        var expectedGuessersScores = of(4, 1, 0);
        assertStorytellerScoreAndAllGuesserScore(expectedStorytellerScore, expectedGuessersScores);
    }

    @Test
    public void GivenStoryToldAndAllGuessersPlayedCardAndGuessedStory_WhenRoundScore_ThenRoundStateShouldBeEnded() {
        givenStoryToldAndAllGuessersPlayedCardAndGuessedStory();

        currentRound.score();

        assertEquals(RoundState.ENDED, currentRound.getState());
    }

    @Test
    public void GivenStoryToldAndAllGuessersPlayedCard_WhenRoundScore_ThenShouldFail() {
        givenStoryToldAndAllGuessersPlayedCard();

        assertThrows(IllegalArgumentException.class, () -> currentRound.score());
    }

    private void tellStory() {
        tellStory(storyteller);
    }

    private void tellStory(Player player) {
        var card = getRandomCard(player);
        currentRound.tellStory(new Story(FAKE_PHRASE, new PlayCard(player, card)));
        cardOfPlayers.put(player, card);
    }

    private void playCard(Player player) {
        var card = getRandomCard(player);
        currentRound.playCard(new PlayCard(player, card));
        cardOfPlayers.put(player, card);
    }

    private Card getRandomCard(Player player) {
        var handCards = player.getHandCards();
        var card = handCards.get(new Random().nextInt(handCards.size()));
        return player.playCard(card.getId());
    }

    private void guessStory(Collection<Player> guessers, Player playerWhoisGuessed) {
        guessers.forEach(player -> guessStory(player, playerWhoisGuessed));
    }

    private void guessStory(Player guesser, Player playerWhoBeGuessed) {
        int cardId = cardOfPlayers.get(playerWhoBeGuessed).getId();
        var playCard = currentRound.getPlayCardByCardId(cardId);
        currentRound.guessStory(new Guess(guesser, playCard));
    }

    private void givenStoryToldAndAllGuessersPlayedCardAndGuessedStory() {
        givenStoryToldAndAllGuessersPlayedCard();
        guessers.forEach(guesser -> guessStory(guesser, storyteller));
    }

    private void givenStoryToldAndAllGuessersPlayedCard() {
        tellStory();
        guessers.forEach(this::playCard);
        assertEquals(NUMBER_OF_GUESSERS, guessers.size());
    }

    private void assertStorytellerScoreAndAllGuesserScore(int expectedStorytellerScore, List<Integer> expectedGuessersScores) {
        assertEquals(expectedStorytellerScore, storyteller.getScore());
        assertEquals(guessers.size(), expectedGuessersScores.size());
        for (int index = 0; index < guessers.size(); index++) {
            var guesser = guessers.get(index);
            assertEquals(expectedGuessersScores.get(index), guesser.getScore());
        }
    }

}