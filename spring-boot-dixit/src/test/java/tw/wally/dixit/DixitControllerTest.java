package tw.wally.dixit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import tw.wally.dixit.model.Card;
import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.model.PlayCard;
import tw.wally.dixit.model.Player;
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
    private static final String DIXIT_GAMER = "dixitGamer";
    private static final String PHRASE = "phrase";

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
    public void WhenCreateDixitWithFourGamers_ThenShouldSuccess() throws Exception {
        createDixitWithGamers(4)
                .andExpect(status().isOk());

        assertTrue(dixitRepository.findDixitById(DIXIT_ID).isPresent());
    }

    @Test
    public void WhenCreateDixitWithEightGamers_ThenShouldFail() throws Exception {
        createDixitWithGamers(8)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void GivenDixitStarted_WhenStorytellerTellStory_ThenCurrentRoundShouldHaveStory() throws Exception {
        Dixit dixit = createDixitWithGamersAndGet(6);
        assertNull(dixit.getCurrentRound().getStory());

        var storyteller = dixit.getCurrentStoryteller();
        tellStory(dixit.getNumberOfRounds(), storyteller)
                .andExpect(status().isOk());

        assertStorytellerTellStory(storyteller);
    }

    @Test
    public void GivenDixitStarted_WhenGuesserTellStory_ThenShouldFail() throws Exception {
        Dixit dixit = createDixitWithGamersAndGet(5);
        assertNull(dixit.getCurrentRound().getStory());

        Player guesser = dixit.getCurrentGuessers().get(0);
        tellStory(dixit.getNumberOfRounds(), guesser)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void GivenDixitStartedAndFirstRoundStarted_WhenStorytellerTellStoryAtSecondRound_ThenShouldFail() throws Exception {
        Dixit dixit = createDixitWithGamersAndGet(6);
        assertEquals(1, dixit.getNumberOfRounds());

        Player storyteller = dixit.getCurrentStoryteller();
        tellStory(2, storyteller)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void GivenStoryTold_WhenTwoGuessersPlayCard_ThenCurrentRoundShouldHaveTwoPlayCards() throws Exception {
        Dixit dixit = givenStoryToldAndGetDixit();

        int numberOfRounds = dixit.getNumberOfRounds();
        var guessers = limit(dixit.getCurrentGuessers(), 2);
        eachGuesserPlayCard(numberOfRounds, guessers);

        assertEachGuesserPlayCard(guessers);
    }

    @Test
    public void GivenStoryTold_WhenStorytellerPlayCard_ThenShouldFail() throws Exception {
        Dixit dixit = givenStoryToldAndGetDixit();

        int numberOfRounds = dixit.getNumberOfRounds();
        Player storyteller = dixit.getCurrentStoryteller();
        playCard(numberOfRounds, storyteller)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void GivenStoryToldAtFirstRound_WhenOneGuesserPlayCardAtSecondRound_ThenShouldFail() throws Exception {
        Dixit dixit = givenStoryToldAndGetDixit();
        assertEquals(1, dixit.getNumberOfRounds());

        Player guesser = dixit.getCurrentGuessers().get(0);
        playCard(2, guesser)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void GivenEachGuesserPlayedCard_WhenTwoGuessersGuessStory_ThenCurrentRoundShouldHaveTwoGuesses() throws Exception {
        Dixit dixit = givenEachGuesserPlayedCardAndGetDixit();

        int numberOfRounds = dixit.getNumberOfRounds();
        var guessers = limit(dixit.getCurrentGuessers(), 2);
        eachGuesserGuessStory(numberOfRounds, guessers);

        assertEachGuesserGuessStory(guessers);
    }

    @Test
    public void GivenEachGuesserPlayedCard_WhenStorytellerGuessStory_ThenShouldFail() throws Exception {
        Dixit dixit = givenEachGuesserPlayedCardAndGetDixit();

        int numberOfRounds = dixit.getNumberOfRounds();
        Player storyteller = dixit.getCurrentStoryteller();
        guessStory(numberOfRounds, storyteller)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void GivenEachGuesserPlayedCardAtFirstRound_WhenOneGuesserGuessStoryAtSecondRound_ThenShouldFail() throws Exception {
        Dixit dixit = givenEachGuesserPlayedCardAndGetDixit();
        assertEquals(1, dixit.getNumberOfRounds());

        var guesser = dixit.getCurrentGuessers().get(0);
        guessStory(2, guesser)
                .andExpect(status().isBadRequest());
    }

    private Dixit givenEachGuesserPlayedCardAndGetDixit() throws Exception {
        Dixit dixit = createDixitWithGamersAndGet(6);
        tellStory(dixit.getNumberOfRounds(), dixit.getCurrentStoryteller())
                .andExpect(status().isOk());
        int numberOfRounds = dixit.getNumberOfRounds();
        var guessers = dixit.getCurrentGuessers();
        eachGuesserPlayCard(numberOfRounds, guessers);
        return dixit;
    }

    private Dixit givenStoryToldAndGetDixit() throws Exception {
        Dixit dixit = createDixitWithGamersAndGet(6);
        tellStory(dixit.getNumberOfRounds(), dixit.getCurrentStoryteller())
                .andExpect(status().isOk());
        return dixit;
    }

    private Dixit createDixitWithGamersAndGet(int numberOfGamers) throws Exception {
        createDixitWithGamers(numberOfGamers)
                .andExpect(status().isOk());
        return dixitRepository.findDixitById(DIXIT_ID).orElseThrow();
    }

    private void eachGuesserPlayCard(int numberOfRounds, Collection<Player> players) throws Exception {
        for (Player player : players) {
            playCard(numberOfRounds, player)
                    .andExpect(status().isOk());
        }
    }

    private void eachGuesserGuessStory(int numberOfRounds, Collection<Player> players) throws Exception {
        for (Player player : players) {
            guessStory(numberOfRounds, player)
                    .andExpect(status().isOk());
        }
    }

    private ResultActions createDixitWithGamers(int numberOfGamers) throws Exception {
        var players = generate(numberOfGamers, number -> new CreateDixitUseCase.Player("id:" + number, DIXIT_GAMER + number));
        var dixitHost = players.get(0);
        var dixitPlayers = skip(players, 1);
        var gameSetting = new CreateDixitUseCase.GameSetting(30);
        var game = new CreateDixitUseCase.Game(DIXIT_ID, dixitHost, dixitPlayers, gameSetting);
        var request = new CreateDixitUseCase.Request(ROOM_ID, game);
        return mockMvc.perform(post(API_PREFIX)
                .contentType(APPLICATION_JSON)
                .content(toJson(request)));
    }

    private ResultActions tellStory(int numberOfRounds, Player player) throws Exception {
        var handCard = getPlayedCard(player);
        var playerId = player.getId();
        var request = new TellStoryUseCase.Request(DIXIT_ID, numberOfRounds, PHRASE, handCard.getId());
        return mockMvc.perform(put(API_PREFIX + "/{dixitId}/rounds/{round}/players/{playerId}/story", DIXIT_ID, numberOfRounds, playerId)
                .contentType(APPLICATION_JSON)
                .content(toJson(request)));
    }

    private ResultActions playCard(int numberOfRounds, Player player) throws Exception {
        var handCard = getPlayedCard(player);
        var playerId = player.getId();
        var request = new PlayCardUseCase.Request(DIXIT_ID, numberOfRounds, handCard.getId());
        return mockMvc.perform(put(API_PREFIX + "/{dixitId}/rounds/{round}/players/{playerId}/playcard", DIXIT_ID, numberOfRounds, playerId)
                .contentType(APPLICATION_JSON)
                .content(toJson(request)));
    }

    private ResultActions guessStory(int numberOfRounds, Player player) throws Exception {
        var guessCard = getGuessedCard(player);
        var playerId = player.getId();
        var request = new GuessStoryUseCase.Request(DIXIT_ID, numberOfRounds, guessCard.getId());
        return mockMvc.perform(put(API_PREFIX + "/{dixitId}/rounds/{round}/players/{playerId}/guess", DIXIT_ID, numberOfRounds, playerId)
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

    private void assertStorytellerTellStory(Player expectedStoryteller) {
        var actualStoryteller = dixitRepository.findDixitById(DIXIT_ID)
                .map(Dixit::getCurrentStoryteller)
                .orElseThrow();
        assertEquals(expectedStoryteller, actualStoryteller);
    }

    private void assertEachGuesserPlayCard(Collection<Player> expectedGuessers) {
        var actualGuessers = dixitRepository.findDixitById(DIXIT_ID)
                .map(Dixit::getCurrentGuessersWhoPlayedCard)
                .orElseThrow();
        assertEqualsIgnoreOrder(expectedGuessers, actualGuessers);
    }

    private void assertEachGuesserGuessStory(Collection<Player> expectedGuessers) {
        var actualGuessers = dixitRepository.findDixitById(DIXIT_ID)
                .map(Dixit::getCurrentGuessersWhoGuessed)
                .orElseThrow();
        assertEqualsIgnoreOrder(expectedGuessers, actualGuessers);
    }
}