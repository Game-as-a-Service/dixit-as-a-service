package tw.wally.dixit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import tw.wally.dixit.clients.LobbyServiceDriver;
import tw.wally.dixit.model.Card;
import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.model.Player;
import tw.wally.dixit.model.Story;
import tw.wally.dixit.repositories.CardRepository;
import tw.wally.dixit.repositories.DixitRepository;
import tw.wally.dixit.services.TokenService;
import tw.wally.dixit.usecases.CreateDixitUseCase;
import tw.wally.dixit.usecases.GuessStoryUseCase;
import tw.wally.dixit.usecases.PlayCardUseCase;
import tw.wally.dixit.usecases.TellStoryUseCase;

import java.util.Collection;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.wally.dixit.model.Round.GUESS_CORRECTLY_SCORE;
import static tw.wally.dixit.utils.StreamUtils.*;

/**
 * @author - wally55077@gmail.com
 */
@ContextConfiguration(classes = {DixitApplication.class})
public class AbstractDixitSpringBootTest extends AbstractSpringBootTest {
    protected static final String API_PREFIX = "/api/dixit";
    protected static final String ROOM_ID = "roomId";
    protected static final String DIXIT_ID = "dixitId";
    protected static final String DIXIT_PLAYER = "dixitPlayer";
    protected static final String PHRASE = "phrase";
    protected static final int FIRST_ROUND = 1, SECOND_ROUND = 2;
    protected static final int NUMBER_OF_PLAYERS = 4;
    protected static final int DEFAULT_WINNING_SCORE = 30;

    @MockBean
    protected LobbyServiceDriver lobbyServiceDriver;

    @MockBean
    protected TokenService tokenService;

    @MockBean
    protected CardRepository cardRepository;

    @Autowired
    protected DixitRepository dixitRepository;

    @BeforeAll
    public static void setup() {
        System.setProperty("dixit.jwt.secret", "SpringBootDixitJwtSecretKeyForTest");
        System.setProperty("dixit.mongodb.uri", "mongodb://root:root@localhost:27017/dixit");
    }

    @BeforeEach
    public void cleanUp() {
        when(cardRepository.findAll())
                .thenReturn(generate(36, number -> new Card(number, "image: " + number)));
        dixitRepository.deleteAll();
    }

    protected Dixit createDixitWithPlayersAndGet(int numberOfPlayers) throws Exception {
        createDixitWithPlayers(numberOfPlayers)
                .andExpect(status().isOk());
        return dixitRepository.findDixitById(DIXIT_ID).orElseThrow();
    }

    protected ResultActions createDixitWithPlayers(int numberOfPlayers) throws Exception {
        var players = generate(numberOfPlayers, number -> new Player(String.valueOf(number), DIXIT_PLAYER + number));
        Player dixitHost = players.get(0);
        var options = singletonList(new CreateDixitUseCase.Option("winningScore", 25));
        var request = new CreateDixitUseCase.Request(ROOM_ID, DIXIT_ID, dixitHost.getId(), players, options);
        return mockMvc.perform(post(API_PREFIX + "/{dixitId}", DIXIT_ID)
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
        String playerId = player.getId();
        var request = new TellStoryUseCase.Request(DIXIT_ID, currentRound, PHRASE, playerId, getPlayCard(player).getId());
        return mockMvc.perform(put(API_PREFIX + "/{dixitId}/rounds/{round}/players/{playerId}/story", DIXIT_ID, currentRound, playerId)
                .contentType(APPLICATION_JSON)
                .content(toJson(request)));
    }

    protected Dixit givenAllGuessersPlayedCardAndGetDixit() throws Exception {
        return givenGuessersPlayedCardAndGetDixit(NUMBER_OF_PLAYERS);
    }

    protected Dixit givenGuessersPlayedCardAndGetDixit(int numberOfPlayers) throws Exception {
        Dixit dixit = givenStoryToldAndGetDixit();
        int currentRound = dixit.getNumberOfRounds();
        var guessers = limit(dixit.getCurrentGuessers(), numberOfPlayers);
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
        String playerId = player.getId();
        var request = new PlayCardUseCase.Request(DIXIT_ID, currentRound, playerId, getPlayCard(player).getId());
        return mockMvc.perform(put(API_PREFIX + "/{dixitId}/rounds/{round}/players/{playerId}/playcard", DIXIT_ID, currentRound, playerId)
                .contentType(APPLICATION_JSON)
                .content(toJson(request)));
    }

    private Card getPlayCard(Player player) {
        return player.getHandCards().get(0);
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

    protected Dixit givenGuessersGuessedStoryAndGetDixit(int numberOfPlayers) throws Exception {
        Dixit dixit = givenAllGuessersPlayedCardAndGetDixit();
        int currentRound = dixit.getNumberOfRounds();
        var guessers = limit(dixit.getCurrentGuessers(), numberOfPlayers);
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
        String playerId = player.getId();
        var request = new GuessStoryUseCase.Request(DIXIT_ID, currentRound, playerId, getStory().getId());
        return mockMvc.perform(put(API_PREFIX + "/{dixitId}/rounds/{round}/players/{playerId}/guess", DIXIT_ID, currentRound, playerId)
                .contentType(APPLICATION_JSON)
                .content(toJson(request)));
    }

    private Card getStory() {
        return dixitRepository.findDixitById(DIXIT_ID)
                .map(Dixit::getCurrentStory)
                .map(Story::getCard)
                .orElseThrow();
    }

    protected void makePlayersAchieveWinningScore(Dixit dixit) {
        makePlayersAchieveWinningScore(dixit, dixit.getPlayers());
    }

    protected void makePlayersAchieveWinningScore(Dixit dixit, Collection<Player> achievedVictoryConditionPlayers) {
        var players = filterToList(dixit.getPlayers(), achievedVictoryConditionPlayers::contains);
        int scoreTimes = DEFAULT_WINNING_SCORE / GUESS_CORRECTLY_SCORE;
        for (Player player : players) {
            for (int currentTime = 0; currentTime < scoreTimes; currentTime++) {
                player.addScore(GUESS_CORRECTLY_SCORE);
            }
        }
        dixitRepository.save(dixit);
    }
}
