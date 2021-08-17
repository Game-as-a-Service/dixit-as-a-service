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
import tw.wally.dixit.model.Player;
import tw.wally.dixit.model.Round;
import tw.wally.dixit.repositories.CardRepository;
import tw.wally.dixit.repositories.DixitRepository;
import tw.wally.dixit.usecases.CreateDixitUseCase;
import tw.wally.dixit.usecases.TellStoryUseCase;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.wally.dixit.utils.StreamUtils.generate;
import static tw.wally.dixit.utils.StreamUtils.skip;

@ContextConfiguration(classes = {DixitApplication.class, DixitControllerTest.DixitConfig.class})
public class DixitControllerTest extends AbstractSpringBootTest {

    protected static final String API_PREFIX = "/api/dixit";
    private static final String ROOM_ID = "roomId";
    private static final String DIXIT_ID = "dixitId";
    private static final String DIXIT_PLAYER = "dixitPlayer";
    private static final String PHRASE = "phrase";

    @Autowired
    private DixitRepository dixitRepository;

    @Configuration
    public static class DixitConfig {

        @Bean
        @Primary
        public CardRepository testCardRepository() {
            return new FakeCardRepository();
        }
    }

    @Test
    public void WhenCreateDixitWithFourPlayers_ThenShouldSuccess() throws Exception {
        createDixitWithGamers(4)
                .andExpect(status().isOk());

        assertTrue(dixitRepository.findDixitById(DIXIT_ID).isPresent());
    }

    @Test
    public void WhenCreateDixitWithEightPlayers_ThenShouldFail() throws Exception {
        createDixitWithGamers(8)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void GivenDixitStarted_WhenStorytellerTellStory_ThenCurrentRoundShouldHaveStory() throws Exception {
        var dixit = createDixitWithGamersAndGet(6);
        assertNull(dixit.getCurrentRound().getStory());

        tellStory(dixit.getCurrentStoryteller(), dixit.getNumberOfRounds())
                .andExpect(status().isOk());

        assertTrue(dixitRepository.findDixitById(DIXIT_ID)
                .map(Dixit::getCurrentRound)
                .map(Round::getStory)
                .isPresent());
    }

    @Test
    public void GivenDixitStarted_WhenGuesserTellStory_ThenShouldFail() throws Exception {
        var dixit = createDixitWithGamersAndGet(6);
        assertNull(dixit.getCurrentRound().getStory());

        tellStory(dixit.getCurrentGuessers().get(0), dixit.getNumberOfRounds())
                .andExpect(status().isBadRequest());
    }

    private Dixit createDixitWithGamersAndGet(int numberOfGamers) throws Exception {
        createDixitWithGamers(numberOfGamers);
        return dixitRepository.findDixitById(DIXIT_ID).orElseThrow();
    }

    private ResultActions createDixitWithGamers(int numberOfGamers) throws Exception {
        var gamers = generate(numberOfGamers, number -> new CreateDixitUseCase.Gamer("id:" + number, DIXIT_PLAYER + number));
        var host = gamers.get(0);
        var players = skip(gamers, 1);
        var gameSetting = new CreateDixitUseCase.GameSetting(30);
        var game = new CreateDixitUseCase.Game(DIXIT_ID, host, players, gameSetting);
        var request = new CreateDixitUseCase.Request(ROOM_ID, game);
        return mockMvc.perform(post(API_PREFIX)
                .contentType(APPLICATION_JSON)
                .content(toJson(request)));
    }

    private ResultActions tellStory(Player player, int numberOfRounds) throws Exception {
        var handCard = getRandomCard(player);
        var request = new TellStoryUseCase.Request(DIXIT_ID, PHRASE, player.getId(), handCard.getId());
        return mockMvc.perform(post(API_PREFIX + "/{dixitId}/rounds/{rounds}/story", DIXIT_ID, numberOfRounds)
                .contentType(APPLICATION_JSON)
                .content(toJson(request)));
    }

    private Card getRandomCard(Player player) {
        var handCards = player.getHandCards();
        var card = handCards.get(new Random().nextInt(handCards.size()));
        return player.playCard(card.getId());
    }

}