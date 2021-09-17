package tw.wally.dixit;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import tw.wally.dixit.model.Card;
import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.model.PlayCard;
import tw.wally.dixit.model.Player;
import tw.wally.dixit.repositories.DixitRepository;
import tw.wally.dixit.usecases.CreateDixitUseCase;
import tw.wally.dixit.usecases.GuessStoryUseCase;
import tw.wally.dixit.usecases.PlayCardUseCase;
import tw.wally.dixit.usecases.TellStoryUseCase;

import java.util.Collection;
import java.util.Random;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.wally.dixit.utils.StreamUtils.*;

/**
 * @author - wally55077@gmail.com
 */
@ContextConfiguration(classes = {DixitApplication.class, DixitControllerTest.DixitConfiguration.class})
public class AbstractDixitSpringBootTest extends AbstractSpringBootTest {
    protected static final String API_PREFIX = "/api/dixit";
    protected static final String ROOM_ID = "roomId";
    protected static final String DIXIT_ID = "dixitId";
    protected static final String DIXIT_PLAYER = "dixitPlayer";
    protected static final String PHRASE = "phrase";
    protected static final int FIRST_ROUND = 1, SECOND_ROUND = 2;
    protected static final int NUMBER_OF_PLAYERS = 4;
    protected static final int DEFAULT_WINNING_SCORE = 30;

    @Autowired
    protected DixitRepository dixitRepository;

    @BeforeEach
    public void cleanUp() {
        dixitRepository.deleteAll();
    }

    protected Dixit createDixitWithPlayersAndGet(int numberOfPlayers) throws Exception {
        createDixitWithPlayers(numberOfPlayers)
                .andExpect(status().isOk());
        return dixitRepository.findDixitById(DIXIT_ID).orElseThrow();
    }

    protected ResultActions createDixitWithPlayers(int numberOfPlayers) throws Exception {
        var players = generate(numberOfPlayers, number -> new CreateDixitUseCase.Player(String.valueOf(number), DIXIT_PLAYER + number));
        var dixitHost = players.get(0);
        var dixitPlayers = skip(players, 1);
        var gameSetting = new CreateDixitUseCase.GameSetting(DEFAULT_WINNING_SCORE);
        var game = new CreateDixitUseCase.Game(DIXIT_ID, dixitHost, dixitPlayers, gameSetting);
        var request = new CreateDixitUseCase.Request(ROOM_ID, game);
        return mockMvc.perform(post(API_PREFIX)
                .contentType(APPLICATION_JSON)
                .content(toJson(request)));
    }

    protected Dixit givenStoryToldAndGetDixit() throws Exception {
        Dixit dixit = createDixitWithPlayersAndGet(NUMBER_OF_PLAYERS);
        tellStory(dixit.getNumberOfRounds(), dixit.getCurrentStoryteller())
                .andExpect(status().isOk());
        return dixitRepository.findDixitById(DIXIT_ID).orElseThrow();
    }

    protected ResultActions tellStory(int currentRound, Player player) throws Exception {
        var handCard = getPlayCard(player);
        var playerId = player.getId();
        var request = new TellStoryUseCase.Request(DIXIT_ID, currentRound, PHRASE, handCard.getId());
        return mockMvc.perform(put(API_PREFIX + "/{dixitId}/rounds/{round}/players/{playerId}/story", DIXIT_ID, currentRound, playerId)
                .contentType(APPLICATION_JSON)
                .content(toJson(request)));
    }

    protected Dixit givenAllGuessersPlayedCardAndGetDixit() throws Exception {
        Dixit dixit = givenStoryToldAndGetDixit();
        int currentRound = dixit.getNumberOfRounds();
        var guessers = dixit.getCurrentGuessers();
        eachGuesserPlayCard(currentRound, guessers);
        return dixitRepository.findDixitById(DIXIT_ID).orElseThrow();
    }

    protected void eachGuesserPlayCard(int currentRound, Collection<Player> players) throws Exception {
        for (Player player : players) {
            playCard(currentRound, player)
                    .andExpect(status().isOk());
        }
    }

    protected ResultActions playCard(int currentRound, Player player) throws Exception {
        var handCard = getPlayCard(player);
        var playerId = player.getId();
        var request = new PlayCardUseCase.Request(DIXIT_ID, currentRound, handCard.getId());
        return mockMvc.perform(put(API_PREFIX + "/{dixitId}/rounds/{round}/players/{playerId}/playcard", DIXIT_ID, currentRound, playerId)
                .contentType(APPLICATION_JSON)
                .content(toJson(request)));
    }

    private Card getPlayCard(Player player) {
        var handCards = player.getHandCards();
        var card = handCards.get(new Random().nextInt(handCards.size()));
        return player.playCard(card.getId());
    }

    protected Dixit givenAllGuessersGuessedStoryAndGetDixit() throws Exception {
        Dixit dixit = givenAllGuessersPlayedCardAndGetDixit();
        return givenAllGuessersGuessedStoryAndGetDixit(dixit);
    }

    protected Dixit givenAllGuessersGuessedStoryAndGetDixit(Dixit dixit) throws Exception {
        int currentRound = dixit.getNumberOfRounds();
        var guessers = dixit.getCurrentGuessers();
        eachGuesserGuessStory(currentRound, guessers);
        return dixitRepository.findDixitById(DIXIT_ID).orElseThrow();
    }

    protected void eachGuesserGuessStory(int currentRound, Collection<Player> players) throws Exception {
        for (Player player : players) {
            guessStory(currentRound, player)
                    .andExpect(status().isOk());
        }
    }

    protected ResultActions guessStory(int currentRound, Player player) throws Exception {
        var guessCard = getGuessedCard(player);
        var playerId = player.getId();
        var request = new GuessStoryUseCase.Request(DIXIT_ID, currentRound, guessCard.getId());
        return mockMvc.perform(put(API_PREFIX + "/{dixitId}/rounds/{round}/players/{playerId}/guess", DIXIT_ID, currentRound, playerId)
                .contentType(APPLICATION_JSON)
                .content(toJson(request)));
    }

    private Card getGuessedCard(Player player) {
        return dixitRepository.findDixitById(DIXIT_ID)
                .map(Dixit::getCurrentPlayCards)
                .flatMap(playCards -> findFirst(playCards, playCard -> !playCard.getPlayer().equals(player)))
                .map(PlayCard::getCard)
                .orElseThrow();
    }

}
