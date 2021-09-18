package tw.wally.dixit;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tw.wally.dixit.model.*;
import tw.wally.dixit.repositories.CardRepository;
import tw.wally.dixit.services.TokenService;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.wally.dixit.utils.StreamUtils.limit;
import static tw.wally.dixit.utils.StreamUtils.mapToList;

public class DixitControllerTest extends AbstractDixitSpringBootTest {

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
        Dixit dixit = createDixitWithPlayersAndGet(4);
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