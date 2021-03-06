package tw.wally.dixit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tw.wally.dixit.model.*;
import tw.wally.dixit.views.*;

import static java.util.function.Function.identity;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.wally.dixit.model.Dixit.NUMBER_OF_PLAYER_HAND_CARDS;
import static tw.wally.dixit.utils.StreamUtils.*;

public class DixitControllerTest extends AbstractDixitSpringBootTest {
    
    @Test
    public void WhenCreateDixitWithFourPlayers_ThenShouldSuccess() throws Exception {
        createDixitWithPlayers(4)
                .andExpect(status().isOk());

        assertTrue(dixitRepository.findDixitById(DIXIT_ID).isPresent());
    }

    @Test
    public void WhenCreateDixitWithEightPlayers_ThenShouldRespondBadRequest() throws Exception {
        createDixitWithPlayers(8)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void GivenDixitStarted_WhenStorytellerTellStory_ThenCurrentRoundShouldHaveStory() throws Exception {
        Dixit dixit = createDixitWithPlayersAndGet(4);
        assertFalse(dixit.mayHaveCurrentStory().isPresent());

        var storyteller = dixit.getCurrentStoryteller();
        tellStory(dixit.getNumberOfRounds(), storyteller)
                .andExpect(status().isOk());

        Dixit actualDixit = dixitRepository.findDixitById(DIXIT_ID).orElseThrow();
        assertCurrentRoundHasStoryWhichToldByStoryteller(actualDixit);
    }

    @Test
    public void GivenDixitStarted_WhenGuesserTellStory_ThenShouldRespondBadRequest() throws Exception {
        Dixit dixit = createDixitWithPlayersAndGet(5);
        assertFalse(dixit.mayHaveCurrentStory().isPresent());

        Player guesser = dixit.getCurrentGuessers().get(0);
        tellStory(dixit.getNumberOfRounds(), guesser)
                .andExpect(status().isBadRequest());

        Player actualGuesser = dixitRepository.findDixitById(DIXIT_ID).map(Dixit::getCurrentGuessers).orElseThrow().get(0);
        assertEquals(NUMBER_OF_PLAYER_HAND_CARDS, actualGuesser.getHandCards().size());
    }

    @Test
    public void GivenDixitStartedInFirstRound_WhenStorytellerTellsStoryInSecondRound_ThenShouldRespondBadRequest() throws Exception {
        Dixit dixit = createDixitWithPlayersAndGet(6);
        assertEquals(FIRST_ROUND, dixit.getNumberOfRounds());

        Player storyteller = dixit.getCurrentStoryteller();
        tellStory(SECOND_ROUND, storyteller)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void GivenStoryTold_WhenTwoGuessersPlayCard_ThenCurrentRoundShouldHaveTwoPlayCards() throws Exception {
        Dixit dixit = givenStoryToldAndGetDixit();

        int currentRound = dixit.getNumberOfRounds();
        var guessers = limit(dixit.getCurrentGuessers(), 2);
        eachGuesserPlayCard(currentRound, guessers);

        Dixit actualDixit = dixitRepository.findDixitById(DIXIT_ID)
                .orElseThrow();
        assertCurrentRoundHasCardsWhichPlayedByGuessers(actualDixit);
    }

    @Test
    public void GivenStoryTold_WhenStorytellerPlayCard_ThenShouldFail() throws Exception {
        Dixit dixit = givenStoryToldAndGetDixit();

        int currentRound = dixit.getNumberOfRounds();
        Player storyteller = dixit.getCurrentStoryteller();
        playCard(currentRound, storyteller)
                .andExpect(status().isBadRequest());
        Player actualStoryteller = dixitRepository.findDixitById(DIXIT_ID)
                .map(Dixit::getCurrentStoryteller)
                .orElseThrow();
        assertEquals(NUMBER_OF_PLAYER_HAND_CARDS - 1, actualStoryteller.getHandCards().size());
    }

    @Test
    public void GivenStoryToldInFirstRound_WhenOneGuesserPlaysCardInSecondRound_ThenShouldRespondBadRequest() throws Exception {
        Dixit dixit = givenStoryToldAndGetDixit();
        assertEquals(FIRST_ROUND, dixit.getNumberOfRounds());

        Player guesser = dixit.getCurrentGuessers().get(0);
        playCard(SECOND_ROUND, guesser)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void GiveAllGuessersPlayedCard_WhenTwoGuessersGuessStory_ThenCurrentRoundShouldHaveTwoGuesses() throws Exception {
        Dixit dixit = givenAllGuessersPlayedCardAndGetDixit();

        int currentRound = dixit.getNumberOfRounds();
        var guessers = limit(dixit.getCurrentGuessers(), 2);
        eachGuesserGuessStory(currentRound, guessers);

        Dixit actualDixit = dixitRepository.findDixitById(DIXIT_ID)
                .orElseThrow();
        assertCurrentRoundHasGuessesWhichGuessedByGuessers(actualDixit);
    }

    @Test
    public void GiveAllGuessersPlayedCard_WhenStorytellerGuessStory_ThenShouldFail() throws Exception {
        Dixit dixit = givenAllGuessersPlayedCardAndGetDixit();

        int currentRound = dixit.getNumberOfRounds();
        Player storyteller = dixit.getCurrentStoryteller();
        guessStory(currentRound, storyteller)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void GivenAllGuessersPlayedCardInFirstRound_WhenOneGuesserGuessStoryInSecondRound_ThenShouldRespondBadRequest() throws Exception {
        Dixit dixit = givenAllGuessersPlayedCardAndGetDixit();
        assertEquals(FIRST_ROUND, dixit.getNumberOfRounds());

        var guesser = dixit.getCurrentGuessers().get(0);
        guessStory(SECOND_ROUND, guesser)
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Given dixit started with 4 players" +
            "When get dixit overview" +
            "Then should respond {gameState = STARTED, rounds = 1, players = Player[4]}")
    @Test
    public void TestDixitStartedDixitOverview() throws Exception {
        Dixit dixit = createDixitWithPlayersAndGet(4);

        Player storyteller = dixit.getCurrentStoryteller();
        var dixitOverview = getDixitOverview(storyteller);

        assertEquals(GameState.STARTED, dixitOverview.gameState);
        assertEquals(1, dixitOverview.rounds);
        var players = dixitOverview.players;
        assertEquals(4, players.size());
        players.forEach(player -> assertTrue(player.handCards.isEmpty()));
    }

    @DisplayName("Given dixit round is story telling" +
            "When get dixit overview with storyteller" +
            "Then should respond {roundState = STORY_TELLING, handCards = Card[6]}")
    @Test
    public void TestStorytellerGetDixitOverviewOnStoryTelling() throws Exception {
        Dixit dixit = createDixitWithPlayersAndGet(NUMBER_OF_PLAYERS);

        Player storyteller = dixit.getCurrentStoryteller();
        var dixitOverview = getDixitOverview(storyteller);

        assertEquals(RoundState.STORY_TELLING, dixitOverview.roundState);
        assertEqualsIgnoreOrder(storyteller.getHandCards(), mapToList(dixitOverview.handCards, CardView::toEntity));
    }

    @DisplayName("Given dixit round is story telling" +
            "When get dixit overview with guesser" +
            "Then should respond {storyteller = Player, handCards = Card[0]}")
    @Test
    public void TestGuesserGetDixitOverviewOnStoryTelling() throws Exception {
        Dixit dixit = createDixitWithPlayersAndGet(NUMBER_OF_PLAYERS);

        Player guesser = dixit.getCurrentGuessers().get(0);
        var dixitOverview = getDixitOverview(guesser);

        assertEquals(dixit.getCurrentStoryteller(), dixitOverview.storyteller.toEntity());
        assertTrue(dixitOverview.storyteller.handCards.isEmpty());
        assertFalse(dixitOverview.handCards.isEmpty());
    }

    @DisplayName("Given dixit round is card playing" +
            "When get dixit overview with storyteller" +
            "Then should respond {handCards: Card[0]}")
    @Test
    public void TestStorytellerGetDixitOverviewOnCardPlaying() throws Exception {
        Dixit dixit = givenStoryToldAndGetDixit();

        Player storyteller = dixit.getCurrentStoryteller();
        var dixitOverview = getDixitOverview(storyteller);

        assertFalse(dixitOverview.handCards.isEmpty());
    }

    @DisplayName("Given dixit round is card playing" +
            "When get dixit overview with guesser" +
            "Then should respond {roundState = CARD_PLAYING, story = Story, handCards: Card[6]}")
    @Test
    public void TestGuesserGetDixitOverviewOnCardPlaying() throws Exception {
        Dixit dixit = givenStoryToldAndGetDixit();

        Player guesser = dixit.getCurrentGuessers().get(0);
        var dixitOverview = getDixitOverview(guesser);

        assertEquals(RoundState.CARD_PLAYING, dixitOverview.roundState);
        Story story = dixitOverview.story.toEntity();
        assertEquals(dixit.getCurrentStory(), story);
        assertTrue(story.getPlayer().getHandCards().isEmpty());
        assertEqualsIgnoreOrder(guesser.getHandCards(), mapToList(dixitOverview.handCards, CardView::toEntity));
    }

    @DisplayName("Given 3 guessers played cards and dixit round is player guessing" +
            "When get dixit overview" +
            "Then should respond {roundState = STORY_GUESSING, playCards = PlayCard[4]}")
    @Test
    public void TestDixitOverviewOnStoryGuessing() throws Exception {
        Dixit dixit = givenGuessersPlayedCardAndGetDixit(3);

        Player guesser = dixit.getCurrentGuessers().get(0);
        var dixitOverview = getDixitOverview(guesser);

        assertEquals(RoundState.STORY_GUESSING, dixitOverview.roundState);
        var playCards = mapToList(dixitOverview.playCards, PlayCardView::toEntity);
        assertEquals(4, playCards.size());
        assertTrue(playCards.contains(dixit.getCurrentStory().getPlayCard()));
        assertTrue(playCards.containsAll(dixit.getCurrentPlayCards()));
    }

    @DisplayName("Given dixit round is player guessing and 2 guessers guessed story" +
            "When get dixit overview" +
            "Then should respond {guesses = Guess[2]}")
    @Test
    public void TestDixitOverviewWithOneGuesserGuessedStory() throws Exception {
        Dixit dixit = givenGuessersGuessedStoryAndGetDixit(2);

        Player guesser = dixit.getCurrentGuessers().get(0);
        var dixitOverview = getDixitOverview(guesser);

        var guesses = dixitOverview.guesses;
        assertEquals(2, guesses.size());
        assertEqualsIgnoreOrder(dixit.getCurrentGuesses(), mapToList(guesses, GuessView::toEntity));
    }

    @DisplayName("Given dixit over and 2 players achieved the victory condition" +
            "When get dixit overview" +
            "Then should respond {gameState = OVER, winners = Player[2]}")
    @Test
    public void TestDixitOverDixitOverview() throws Exception {
        Dixit dixit = givenAllGuessersPlayedCardAndGetDixit();
        makePlayersAchieveWinningScore(dixit, limit(dixit.getCurrentGuessers(), 2));
        dixit = givenAllGuessersGuessedStoryAndGetDixit(dixit);

        Player storyteller = dixit.getCurrentStoryteller();
        var dixitOverview = getDixitOverview(storyteller);

        assertEquals(GameState.OVER, dixitOverview.gameState);
        var winners = dixitOverview.winners;
        assertEquals(2, winners.size());
        assertEqualsIgnoreOrder(dixit.getWinners(), mapToList(dixitOverview.winners, PlayerView::toEntity));
    }

    public DixitOverview getDixitOverview(Player player) throws Exception {
        return getBody(mockMvc.perform(get(API_PREFIX + "/{dixitId}/players/{playerId}/overview", DIXIT_ID, player.getId()))
                .andExpect(status().isOk()), DixitOverview.class);
    }

    private void assertCurrentRoundHasStoryWhichToldByStoryteller(Dixit dixit) {
        Player expectedStoryteller = dixit.getCurrentStoryteller();
        Story story = dixit.getCurrentStory();
        Player actualStoryteller = story.getPlayer();
        assertSame(expectedStoryteller, actualStoryteller);
        assertEquals(NUMBER_OF_PLAYER_HAND_CARDS - 1, expectedStoryteller.getHandCards().size());
        assertEquals(NUMBER_OF_PLAYER_HAND_CARDS - 1, actualStoryteller.getHandCards().size());
    }

    private void assertCurrentRoundHasCardsWhichPlayedByGuessers(Dixit dixit) {
        var playCards = dixit.getCurrentPlayCards();
        var expectedPlayers = limit(dixit.getCurrentGuessers(), playCards.size());
        var actualPlayers = toMap(playCards, playCard -> playCard.getPlayer().getId(), PlayCard::getPlayer);
        expectedPlayers.forEach(expectedPlayer -> assertSame(expectedPlayer, actualPlayers.get(expectedPlayer.getId())));

        expectedPlayers.forEach(player -> assertEquals(NUMBER_OF_PLAYER_HAND_CARDS - 1, player.getHandCards().size()));
        actualPlayers.values().forEach(player -> assertEquals(NUMBER_OF_PLAYER_HAND_CARDS - 1, player.getHandCards().size()));
    }

    private void assertCurrentRoundHasGuessesWhichGuessedByGuessers(Dixit dixit) {
        var guesses = dixit.getCurrentGuesses();
        var expectedGuessers = limit(dixit.getCurrentGuessers(), guesses.size());
        var actualGuessers = toMap(guesses, guess -> guess.getGuesser().getId(), Guess::getGuesser);
        expectedGuessers.forEach(expectedGuesser -> assertSame(expectedGuesser, actualGuessers.get(expectedGuesser.getId())));

        var expectedPlayers = mapToList(guesses, Guess::getPlayCardPlayer);
        var actualPlayers = toMap(dixit.getPlayers(), Player::getId, identity());
        expectedPlayers.forEach(expectedPlayer -> assertSame(expectedPlayer, actualPlayers.get(expectedPlayer.getId())));
    }
}