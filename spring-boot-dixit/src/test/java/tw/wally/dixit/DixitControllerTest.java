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
import tw.wally.dixit.usecases.CreateDixitUseCase;
import tw.wally.dixit.usecases.GuessStoryUseCase;
import tw.wally.dixit.usecases.PlayCardUseCase;
import tw.wally.dixit.usecases.TellStoryUseCase;

import java.util.Collection;
import java.util.Map;
import java.util.Random;

import static java.util.stream.Collectors.toList;
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
        var dixit = createDixitWithGamersAndGet(6);
        assertNull(dixit.getCurrentRound().getStory());

        var storyteller = dixit.getCurrentStoryteller();
        tellStory(dixit.getNumberOfRounds(), storyteller)
                .andExpect(status().isOk());

        assertStorytellerTellStory(storyteller);
    }

    @Test
    public void GivenDixitStarted_WhenGuesserTellStory_ThenShouldFail() throws Exception {
        var dixit = createDixitWithGamersAndGet(5);
        assertNull(dixit.getCurrentRound().getStory());

        var guesser = dixit.getCurrentGuessers().get(0);
        tellStory(dixit.getNumberOfRounds(), guesser)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void GivenDixitStartedAndFirstRoundStarted_WhenStorytellerTellStoryAtSecondRound_ThenShouldFail() throws Exception {
        var dixit = createDixitWithGamersAndGet(6);
        assertEquals(1, dixit.getNumberOfRounds());

        var storyteller = dixit.getCurrentStoryteller();
        tellStory(2, storyteller)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void GivenStoryTold_WhenTwoGuessersPlayCard_ThenCurrentRoundShouldHaveTwoPlayCards() throws Exception {
        var dixit = givenStoryToldAndGetDixit();

        int numberOfRounds = dixit.getNumberOfRounds();
        var guessers = limit(dixit.getCurrentGuessers(), 2);
        eachGuesserPlayCard(numberOfRounds, guessers);

        assertEachGamerPlayCard(guessers);
    }

    @Test
    public void GivenStoryTold_WhenStorytellerPlayCard_ThenShouldFail() throws Exception {
        var dixit = givenStoryToldAndGetDixit();

        int numberOfRounds = dixit.getNumberOfRounds();
        var storyteller = dixit.getCurrentStoryteller();
        playCard(numberOfRounds, storyteller)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void GivenStoryToldAtFirstRound_WhenOneGuesserPlayCardAtSecondRound_ThenShouldFail() throws Exception {
        var dixit = givenStoryToldAndGetDixit();
        assertEquals(1, dixit.getNumberOfRounds());

        var guesser = dixit.getCurrentGuessers().get(0);
        playCard(2, guesser)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void GivenEachGuesserPlayedCard_WhenTwoGuessersGuessStory_ThenCurrentRoundShouldHaveTwoGuesses() throws Exception {
        var dixit = givenEachGuesserPlayedCardAndGetDixit();

        int numberOfRounds = dixit.getNumberOfRounds();
        var guessers = limit(dixit.getCurrentGuessers(), 2);
        eachGuesserGuessStory(numberOfRounds, guessers);

        assertEachGuesserGuessStory(guessers);
    }

    @Test
    public void GivenEachGuesserPlayedCardAtFirstRound_WhenOneGuesserGuessStoryAtSecondRound_ThenShouldFail() throws Exception {
        var dixit = givenEachGuesserPlayedCardAndGetDixit();
        assertEquals(1, dixit.getNumberOfRounds());

        var guesser = dixit.getCurrentGuessers().get(0);
        guessStory(2, guesser)
                .andExpect(status().isBadRequest());
    }

    private Dixit givenEachGuesserPlayedCardAndGetDixit() throws Exception {
        var dixit = createDixitWithGamersAndGet(6);
        tellStory(dixit.getNumberOfRounds(), dixit.getCurrentStoryteller())
                .andExpect(status().isOk());
        int numberOfRounds = dixit.getNumberOfRounds();
        var guessers = dixit.getCurrentGuessers();
        eachGuesserPlayCard(numberOfRounds, guessers);
        return dixit;
    }

    private Dixit givenStoryToldAndGetDixit() throws Exception {
        var dixit = createDixitWithGamersAndGet(6);
        tellStory(dixit.getNumberOfRounds(), dixit.getCurrentStoryteller())
                .andExpect(status().isOk());
        return dixit;
    }

    private Dixit createDixitWithGamersAndGet(int numberOfGamers) throws Exception {
        createDixitWithGamers(numberOfGamers)
                .andExpect(status().isOk());
        return dixitRepository.findDixitById(DIXIT_ID).orElseThrow();
    }

    private void eachGuesserPlayCard(int numberOfRounds, Collection<Player> gamers) throws Exception {
        for (Player gamer : gamers) {
            playCard(numberOfRounds, gamer)
                    .andExpect(status().isOk());
        }
    }

    private void eachGuesserGuessStory(int numberOfRounds, Collection<Player> gamers) throws Exception {
        for (Player gamer : gamers) {
            guessStory(numberOfRounds, gamer)
                    .andExpect(status().isOk());
        }
    }

    private ResultActions createDixitWithGamers(int numberOfGamers) throws Exception {
        var gamers = generate(numberOfGamers, number -> new CreateDixitUseCase.Gamer("id:" + number, DIXIT_GAMER + number));
        var host = gamers.get(0);
        var players = skip(gamers, 1);
        var gameSetting = new CreateDixitUseCase.GameSetting(30);
        var game = new CreateDixitUseCase.Game(DIXIT_ID, host, players, gameSetting);
        var request = new CreateDixitUseCase.Request(ROOM_ID, game);
        return mockMvc.perform(post(API_PREFIX)
                .contentType(APPLICATION_JSON)
                .content(toJson(request)));
    }

    private ResultActions tellStory(int numberOfRounds, Player gamer) throws Exception {
        var handCard = getPlayedCard(gamer);
        var playerId = gamer.getId();
        var request = new TellStoryUseCase.Request(DIXIT_ID, numberOfRounds, PHRASE, handCard.getId());
        return mockMvc.perform(put(API_PREFIX + "/{dixitId}/rounds/{round}/players/{playerId}/story", DIXIT_ID, numberOfRounds, playerId)
                .contentType(APPLICATION_JSON)
                .content(toJson(request)));
    }

    private ResultActions playCard(int numberOfRounds, Player gamer) throws Exception {
        var handCard = getPlayedCard(gamer);
        var playerId = gamer.getId();
        var request = new PlayCardUseCase.Request(DIXIT_ID, numberOfRounds, handCard.getId());
        return mockMvc.perform(put(API_PREFIX + "/{dixitId}/rounds/{round}/players/{playerId}/playcard", DIXIT_ID, numberOfRounds, playerId)
                .contentType(APPLICATION_JSON)
                .content(toJson(request)));
    }

    private ResultActions guessStory(int numberOfRounds, Player gamer) throws Exception {
        var guessCard = getGuessedCard(gamer);
        var playerId = gamer.getId();
        var request = new GuessStoryUseCase.Request(DIXIT_ID, numberOfRounds, guessCard.getId());
        return mockMvc.perform(put(API_PREFIX + "/{dixitId}/rounds/{round}/players/{playerId}/guess", DIXIT_ID, numberOfRounds, playerId)
                .contentType(APPLICATION_JSON)
                .content(toJson(request)));
    }

    private Card getPlayedCard(Player gamer) {
        var handCards = gamer.getHandCards();
        var card = handCards.get(new Random().nextInt(handCards.size()));
        return gamer.playCard(card.getId());
    }

    private Card getGuessedCard(Player gamer) {
        return dixitRepository.findDixitById(DIXIT_ID)
                .map(Dixit::getCurrentRound)
                .map(Round::getPlayCards)
                .map(Map::values)
                .map(Collection::stream)
                .orElseThrow()
                .filter(playCard -> !playCard.getPlayer().equals(gamer))
                .map(PlayCard::getCard)
                .findFirst().orElseThrow();
    }

    private void assertStorytellerTellStory(Player expectedStoryteller) {
        var actualStoryteller = dixitRepository.findDixitById(DIXIT_ID)
                .map(Dixit::getCurrentRound)
                .map(Round::getStory)
                .map(Story::getPlayer).orElseThrow();
        assertEquals(expectedStoryteller, actualStoryteller);
    }

    private void assertEachGamerPlayCard(Collection<Player> expectedGamers) {
        var actualGamers = dixitRepository.findDixitById(DIXIT_ID)
                .map(Dixit::getCurrentRound)
                .map(Round::getPlayCards)
                .map(Map::values)
                .map(Collection::stream)
                .orElseThrow()
                .map(PlayCard::getPlayer)
                .collect(toList());
        assertEqualsIgnoreOrder(expectedGamers, actualGamers);
    }

    private void assertEachGuesserGuessStory(Collection<Player> expectedGamers) {
        var actualGamers = dixitRepository.findDixitById(DIXIT_ID)
                .map(Dixit::getCurrentRound)
                .map(Round::getGuesses)
                .map(Map::values)
                .map(Collection::stream)
                .orElseThrow()
                .map(Guess::getGuesser)
                .collect(toList());
        assertEqualsIgnoreOrder(expectedGamers, actualGamers);
    }
}