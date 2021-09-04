package tw.wally.dixit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import tw.wally.dixit.model.*;
import tw.wally.dixit.repositories.CardRepository;
import tw.wally.dixit.repositories.DixitRepository;
import tw.wally.dixit.services.TokenService;
import tw.wally.dixit.usecases.CreateDixitUseCase;
import tw.wally.dixit.usecases.GuessStoryUseCase;
import tw.wally.dixit.usecases.PlayCardUseCase;
import tw.wally.dixit.usecases.TellStoryUseCase;

import java.util.Collection;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.wally.dixit.utils.StreamUtils.*;

@ContextConfiguration(classes = {DixitApplication.class, DixitControllerTest.DixitConfiguration.class})
public class DixitControllerTest extends AbstractSpringBootTest {
    protected static final String API_PREFIX = "/api/dixit";
    private static final String ROOM_ID = "roomId";
    private static final String DIXIT_ID = "dixitId";
    private static final String DIXIT_PLAYER = "dixitPlayer";
    private static final String PHRASE = "phrase";
    private static final int FIRST_ROUND = 1, SECOND_ROUND = 2;

    @Autowired
    private DixitRepository dixitRepository;

    @Configuration
    public static class DixitConfiguration {

        @Bean
        @Primary
        public TokenService testTokenService() {
            return new FakeTokenService();
        }

        @Bean
        @Primary
        public CardRepository testCardRepository() {
            return new FakeCardRepository();
        }
    }

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
        Dixit dixit = createDixitWithPlayersAndGet(6);
        assertNull(dixit.getCurrentRound().getStory());

        var storyteller = dixit.getCurrentStoryteller();
        tellStory(dixit.getNumberOfRounds(), storyteller)
                .andExpect(status().isOk());

        assertCurrentRoundHasStoryWhichToldByStoryteller(storyteller);
    }

    @Test
    public void GivenDixitStarted_WhenGuesserTellStory_ThenShouldRespondBadRequest() throws Exception {
        Dixit dixit = createDixitWithPlayersAndGet(5);
        assertNull(dixit.getCurrentRound().getStory());

        Player guesser = dixit.getCurrentGuessers().get(0);
        tellStory(dixit.getNumberOfRounds(), guesser)
                .andExpect(status().isBadRequest());
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

        assertCurrentRoundHasCardsWhichPlayedByGuessers(guessers);
    }

    @Test
    public void GivenStoryTold_WhenStorytellerPlayCard_ThenShouldFail() throws Exception {
        Dixit dixit = givenStoryToldAndGetDixit();

        int currentRound = dixit.getNumberOfRounds();
        Player storyteller = dixit.getCurrentStoryteller();
        playCard(currentRound, storyteller)
                .andExpect(status().isBadRequest());
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

        assertCurrentRoundHasGuessesWhichGuessedByGuessers(guessers);
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

    private Dixit givenAllGuessersPlayedCardAndGetDixit() throws Exception {
        Dixit dixit = createDixitWithPlayersAndGet(6);
        tellStory(dixit.getNumberOfRounds(), dixit.getCurrentStoryteller())
                .andExpect(status().isOk());
        int currentRound = dixit.getNumberOfRounds();
        var guessers = dixit.getCurrentGuessers();
        eachGuesserPlayCard(currentRound, guessers);
        return dixit;
    }

    private Dixit givenStoryToldAndGetDixit() throws Exception {
        Dixit dixit = createDixitWithPlayersAndGet(6);
        tellStory(dixit.getNumberOfRounds(), dixit.getCurrentStoryteller())
                .andExpect(status().isOk());
        return dixit;
    }

    private Dixit createDixitWithPlayersAndGet(int numberOfPlayers) throws Exception {
        createDixitWithPlayers(numberOfPlayers)
                .andExpect(status().isOk());
        return dixitRepository.findDixitById(DIXIT_ID).orElseThrow();
    }

    private void eachGuesserPlayCard(int currentRound, Collection<Player> players) throws Exception {
        for (Player player : players) {
            playCard(currentRound, player)
                    .andExpect(status().isOk());
        }
    }

    private void eachGuesserGuessStory(int currentRound, Collection<Player> players) throws Exception {
        for (Player player : players) {
            guessStory(currentRound, player)
                    .andExpect(status().isOk());
        }
    }

    private ResultActions createDixitWithPlayers(int numberOfPlayers) throws Exception {
        var players = generate(numberOfPlayers, number -> new CreateDixitUseCase.Player("id:" + number, DIXIT_PLAYER + number));
        var dixitHost = players.get(0);
        var dixitPlayers = skip(players, 1);
        var gameSetting = new CreateDixitUseCase.GameSetting(30);
        var game = new CreateDixitUseCase.Game(DIXIT_ID, dixitHost, dixitPlayers, gameSetting);
        var request = new CreateDixitUseCase.Request(ROOM_ID, game);
        return mockMvc.perform(post(API_PREFIX)
                .contentType(APPLICATION_JSON)
                .content(toJson(request)));
    }

    private ResultActions tellStory(int currentRound, Player player) throws Exception {
        var handCard = getPlayedCard(player);
        var playerId = player.getId();
        var request = new TellStoryUseCase.Request(DIXIT_ID, currentRound, PHRASE, handCard.getId());
        return mockMvc.perform(put(API_PREFIX + "/{dixitId}/rounds/{round}/players/{playerId}/story", DIXIT_ID, currentRound, playerId)
                .contentType(APPLICATION_JSON)
                .content(toJson(request)));
    }

    private ResultActions playCard(int currentRound, Player player) throws Exception {
        var handCard = getPlayedCard(player);
        var playerId = player.getId();
        var request = new PlayCardUseCase.Request(DIXIT_ID, currentRound, handCard.getId());
        return mockMvc.perform(put(API_PREFIX + "/{dixitId}/rounds/{round}/players/{playerId}/playcard", DIXIT_ID, currentRound, playerId)
                .contentType(APPLICATION_JSON)
                .content(toJson(request)));
    }

    private ResultActions guessStory(int currentRound, Player player) throws Exception {
        var guessCard = getGuessedCard(player);
        var playerId = player.getId();
        var request = new GuessStoryUseCase.Request(DIXIT_ID, currentRound, guessCard.getId());
        return mockMvc.perform(put(API_PREFIX + "/{dixitId}/rounds/{round}/players/{playerId}/guess", DIXIT_ID, currentRound, playerId)
                .contentType(APPLICATION_JSON)
                .content(toJson(request)));
    }

    private Card getPlayedCard(Player player) {
        var handCards = player.getHandCards();
        var card = handCards.get(new Random().nextInt(handCards.size()));
        return player.playCard(card.getId());
    }

    private Card getGuessedCard(Player player) {
        return dixitRepository.findDixitById(DIXIT_ID)
                .map(Dixit::getCurrentPlayCards)
                .flatMap(playCards -> findFirst(playCards, playCard -> !playCard.getPlayer().equals(player)))
                .map(PlayCard::getCard)
                .orElseThrow();
    }

    private void assertCurrentRoundHasStoryWhichToldByStoryteller(Player expectedStoryteller) {
        Story story = dixitRepository.findDixitById(DIXIT_ID)
                .map(Dixit::getCurrentRound)
                .map(Round::getStory)
                .orElseThrow();
        assertNotNull(story);
        var actualStoryteller = story.getPlayer();
        assertEquals(expectedStoryteller, actualStoryteller);
    }

    private void assertCurrentRoundHasCardsWhichPlayedByGuessers(Collection<Player> expectedGuessers) {
        var playCards = dixitRepository.findDixitById(DIXIT_ID)
                .map(Dixit::getCurrentPlayCards)
                .orElseThrow();
        assertEquals(playCards.size(), expectedGuessers.size());
        var actualGuessers = mapToList(playCards, PlayCard::getPlayer);
        assertEqualsIgnoreOrder(expectedGuessers, actualGuessers);
    }

    private void assertCurrentRoundHasGuessesWhichGuessedByGuessers(Collection<Player> expectedGuessers) {
        var guesses = dixitRepository.findDixitById(DIXIT_ID)
                .map(Dixit::getCurrentGuesses)
                .orElseThrow();
        assertEquals(guesses.size(), expectedGuessers.size());
        var actualGuessers = mapToList(guesses, Guess::getGuesser);
        assertEqualsIgnoreOrder(expectedGuessers, actualGuessers);
    }
}