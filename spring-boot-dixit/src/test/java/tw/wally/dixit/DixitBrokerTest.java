package tw.wally.dixit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import tw.wally.dixit.EventBus.Event;
import tw.wally.dixit.events.*;
import tw.wally.dixit.model.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Map.of;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;
import static tw.wally.dixit.configs.WebSocketConfiguration.STOMP_ROOT_DESTINATION_PREFIX;
import static tw.wally.dixit.model.Round.GUESS_CORRECTLY_SCORE;
import static tw.wally.dixit.utils.StreamUtils.*;


/**
 * @author - wally55077@gmail.com
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class DixitBrokerTest extends AbstractDixitSpringBootTest {
    private static final String BASE_BROKER_URL = format("ws://localhost:%d/broker", 8080);
    private static final String BASE_DIXIT_GAME_STATE_TOPIC = STOMP_ROOT_DESTINATION_PREFIX + "/dixit/" + DIXIT_ID + "/gameStates/";
    private static final String BASE_DIXIT_ROUND_STATE_TOPIC = STOMP_ROOT_DESTINATION_PREFIX + "/dixit/" + DIXIT_ID + "/roundStates/";
    private static final Map<Class<? extends Event>, String> BASE_DIXIT_EVENT_TOPICS = of(
            DixitGameStartedEvent.class, BASE_DIXIT_GAME_STATE_TOPIC + GameState.STARTED,
            DixitRoundStoryTellingEvent.class, BASE_DIXIT_ROUND_STATE_TOPIC + RoundState.STORY_TELLING,
            DixitRoundCardPlayingEvent.class, BASE_DIXIT_ROUND_STATE_TOPIC + RoundState.CARD_PLAYING,
            DixitRoundPlayerGuessingEvent.class, BASE_DIXIT_ROUND_STATE_TOPIC + RoundState.PLAYER_GUESSING,
            DixitRoundScoringEvent.class, BASE_DIXIT_ROUND_STATE_TOPIC + RoundState.SCORING,
            DixitRoundOverEvent.class, BASE_DIXIT_ROUND_STATE_TOPIC + RoundState.OVER,
            DixitGameOverEvent.class, BASE_DIXIT_GAME_STATE_TOPIC + GameState.OVER);
    private final Map<Class<? extends Event>, Map<String, DixitEventStompFrameHandler<? extends Event>>> dixitEventHandlers = new HashMap<>(BASE_DIXIT_EVENT_TOPICS.size());
    private WebSocketStompClient stompClient;

    @Configuration
    public static class DixitConfiguration {

        @Bean
        @Primary
        public MockMvc mockMvc(WebApplicationContext webApplicationContext) {
            return MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        }
    }

    @BeforeEach
    public void setup() {
        if (this.stompClient == null) {
            this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());
            this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        }
    }

    @Test
    public void GivenAllPlayersSubscribeDixitGameStartedTopic_WhenDixitCreatedAndFirstRoundStarted_ThenAllPlayersShouldReceiveDixitGameStartedEvent() throws Exception {
        subscribeEvents(DixitGameStartedEvent.class);

        createDixitWithPlayers(NUMBER_OF_PLAYERS);

        var dixit = dixitRepository.findDixitById(DIXIT_ID)
                .orElseThrow();
        assertEquals(FIRST_ROUND, dixit.getNumberOfRounds());
        var players = dixit.getPlayers();
        players.forEach(player -> assertPlayerReceiveDixitGameStartedEvent(player, players));
    }

    @Test
    public void GivenAllPlayersSubscribeDixitRoundStoryTellingTopic_WhenDixitCreated_ThenShouldOnlyStorytellerReceiveDixitRoundStoryTellingEvent() throws Exception {
        subscribeEvents(DixitRoundStoryTellingEvent.class);

        createDixitWithPlayers(NUMBER_OF_PLAYERS);

        Dixit dixit = dixitRepository.findDixitById(DIXIT_ID).orElseThrow();
        int currentRound = dixit.getNumberOfRounds();
        assertStorytellerReceiveDixitRoundStoryTellingEvent(dixit.getCurrentStoryteller(), currentRound);
        dixit.getCurrentGuessers().forEach(guesser -> receiveEvent(guesser, DixitRoundStoryTellingEvent.class));
    }

    @Test
    public void GivenAllPlayersSubscribeDixitRoundCardPlayingTopic_WhenDixitStoryTold_ThenShouldOnlyGuessersReceiveDixitRoundCardPlayingEvent() throws Exception {
        subscribeEvents(DixitRoundCardPlayingEvent.class);

        Dixit dixit = givenStoryToldAndGetDixit();

        Player storyteller = dixit.getCurrentStoryteller();
        assertNull(receiveEvent(storyteller, DixitRoundCardPlayingEvent.class));
        dixit.getCurrentGuessers().forEach(this::assertGuesserReceiveDixitRoundCardPlayingEvent);
    }

    @Test
    public void GivenAllPlayersSubscribeDixitRoundPlayerGuessingTopic_WhenAllGuessersPlayedCard_ThenShouldOnlyGuessersReceiveDixitRoundPlayerGuessingEvent() throws Exception {
        subscribeEvents(DixitRoundPlayerGuessingEvent.class);

        Dixit dixit = givenAllGuessersPlayedCardAndGetDixit();

        Player storyteller = dixit.getCurrentStoryteller();
        assertNull(receiveEvent(storyteller, DixitRoundPlayerGuessingEvent.class));
        var currentPlayCards = dixit.getCurrentPlayCards();
        dixit.getCurrentGuessers().forEach(guesser -> assertGuesserReceiveDixitRoundPlayerGuessingEvent(guesser, currentPlayCards));
    }

    @Test
    public void GivenAllPlayersSubscribeDixitRoundScoringTopic_WhenAllGuessersGuessedStory_ThenAllPlayersShouldReceiveDixitRoundScoringEvent() throws Exception {
        subscribeEvents(DixitRoundScoringEvent.class);

        Dixit dixit = givenAllGuessersGuessedStoryAndGetDixit();

        int currentRound = dixit.getNumberOfRounds() - 1;
        Round lastRound = dixit.getRounds().getFirst();
        Story story = lastRound.getStory();
        var playCards = lastRound.getPlayCards();
        var currentGuesses = lastRound.getGuesses();
        dixit.getPlayers().forEach(player -> assertGuesserReceiveDixitRoundScoringEvent(player, currentRound, story, playCards, currentGuesses));
    }

    @Test
    public void GivenAllPlayersSubscribeDixitRoundOverTopic_WhenAllGuessersGuessedStoryAndDixitRoundScored_ThenAllPlayersShouldReceiveDixitRoundOverEvent() throws Exception {
        subscribeEvents(DixitRoundOverEvent.class);

        Dixit dixit = givenAllGuessersGuessedStoryAndGetDixit();

        dixit.getPlayers().forEach(this::assertPlayerReceiveDixitRoundOverEvent);
    }

    @Test
    public void GivenAllPlayersSubscribeDixitRoundStoryTellingTopic_WhenDixitSecondRoundStarted_ThenShouldOnlyStorytellerReceiveDixitRoundStoryTellingEvent() throws Exception {
        subscribeEvents(DixitRoundStoryTellingEvent.class);

        givenAllGuessersGuessedStoryAndGetDixit();

        Dixit dixit = dixitRepository.findDixitById(DIXIT_ID)
                .orElseThrow();
        assertStorytellerReceiveDixitRoundStoryTellingEvent(dixit.getCurrentStoryteller(), dixit.getNumberOfRounds());
        dixit.getCurrentGuessers().forEach(guesser -> receiveEvent(guesser, DixitRoundStoryTellingEvent.class));
    }

    @Test
    public void GivenAllPlayersSubscribeDixitGameOverTopic_WhenAllGuessersGuessedStoryAndDixitHasWinners_ThenAllPlayersShouldReceiveDixitGameOverEvent() throws Exception {
        subscribeEvents(DixitGameOverEvent.class);

        Dixit dixit = givenAllGuessersPlayedCardAndGetDixit();
        makePlayersAchieveWinningScore(dixit);
        dixit = givenAllGuessersGuessedStoryAndGetDixit(dixit);
        var winners = dixit.getWinners();
        assertFalse(winners.isEmpty());

        int currentRound = dixit.getNumberOfRounds();
        dixit.getPlayers().forEach(player -> assertPlayerReceiveDixitGameOverEvent(player, currentRound, winners));
    }

    private void subscribeEvents(Class<? extends Event> dixitEventClass) throws Exception {
        var dixitPlayers = generate(NUMBER_OF_PLAYERS, number -> new Player(String.valueOf(number), DIXIT_PLAYER + number));
        for (Player player : dixitPlayers) {
            subscribeEvent(player, dixitEventClass);
        }
    }

    private void subscribeEvent(Player player, Class<? extends Event> eventClass) throws Exception {
        var stompSession = stompClient.connect(BASE_BROKER_URL, new StompSessionHandlerAdapter() {
        }).get(1, SECONDS);

        String playerId = player.getId();
        String topic = BASE_DIXIT_EVENT_TOPICS.get(eventClass) + "/players/" + playerId;
        var dixitEventStompFrameHandler = new DixitEventStompFrameHandler<>(eventClass);
        stompSession.subscribe(topic, dixitEventStompFrameHandler);

        dixitEventHandlers.computeIfAbsent(eventClass, dixitEventClass -> new HashMap<>(NUMBER_OF_PLAYERS))
                .put(playerId, dixitEventStompFrameHandler);
    }

    private void makePlayersAchieveWinningScore(Dixit dixit) {
        var players = dixit.getPlayers();
        int scoreTimes = DEFAULT_WINNING_SCORE / GUESS_CORRECTLY_SCORE;
        for (Player player : players) {
            for (int currentTime = 0; currentTime < scoreTimes; currentTime++) {
                player.addScore(GUESS_CORRECTLY_SCORE);
            }
        }
        dixitRepository.save(dixit);
    }

    @SuppressWarnings("unchecked")
    private <T> T receiveEvent(Player player, Class<T> dixitEventClass) {
        return (T) dixitEventHandlers.get(dixitEventClass)
                .get(player.getId())
                .getPayload();
    }

    private void assertPlayerReceiveDixitGameStartedEvent(Player player, Collection<Player> players) {
        var dixitGameStateEvent = receiveEvent(player, DixitGameStartedEvent.class);
        assertNotNull(dixitGameStateEvent);
        assertEquals(DIXIT_ID, dixitGameStateEvent.getGameId());
        assertEquals(FIRST_ROUND, dixitGameStateEvent.getRounds());
        assertEquals(player.getId(), dixitGameStateEvent.getPlayerId());
        assertEquals(GameState.STARTED, dixitGameStateEvent.getGameState());
        assertEqualsIgnoreOrder(players, dixitGameStateEvent.getPlayers());
    }

    private void assertStorytellerReceiveDixitRoundStoryTellingEvent(Player storyteller, int currentRound) {
        var dixitRoundStoryTellingEvent = receiveEvent(storyteller, DixitRoundStoryTellingEvent.class);
        assertNotNull(dixitRoundStoryTellingEvent);
        assertEquals(currentRound, dixitRoundStoryTellingEvent.getRounds());
        assertEquals(storyteller.getId(), dixitRoundStoryTellingEvent.getPlayerId());
        assertEquals(RoundState.STORY_TELLING, dixitRoundStoryTellingEvent.getRoundState());
        assertEqualsIgnoreOrder(storyteller.getHandCards(), dixitRoundStoryTellingEvent.getHandCards());
    }

    private void assertGuesserReceiveDixitRoundCardPlayingEvent(Player guesser) {
        var dixitRoundCardPlayingEvent = receiveEvent(guesser, DixitRoundCardPlayingEvent.class);
        assertNotNull(dixitRoundCardPlayingEvent);
        assertEquals(FIRST_ROUND, dixitRoundCardPlayingEvent.getRounds());
        assertEquals(guesser.getId(), dixitRoundCardPlayingEvent.getPlayerId());
        assertEquals(RoundState.CARD_PLAYING, dixitRoundCardPlayingEvent.getRoundState());
        assertEqualsIgnoreOrder(guesser.getHandCards(), dixitRoundCardPlayingEvent.getHandCards());
    }

    private void assertGuesserReceiveDixitRoundPlayerGuessingEvent(Player guesser, Collection<PlayCard> expectedPlayCards) {
        var dixitRoundPlayerGuessingEvent = receiveEvent(guesser, DixitRoundPlayerGuessingEvent.class);
        assertNotNull(dixitRoundPlayerGuessingEvent);
        assertEquals(FIRST_ROUND, dixitRoundPlayerGuessingEvent.getRounds());
        assertEquals(guesser.getId(), dixitRoundPlayerGuessingEvent.getPlayerId());
        assertEquals(RoundState.PLAYER_GUESSING, dixitRoundPlayerGuessingEvent.getRoundState());

        var actualPlayCards = dixitRoundPlayerGuessingEvent.getPlayCards();
        assertEqualsIgnoreOrder(expectedPlayCards, actualPlayCards);
        mapToList(actualPlayCards, PlayCard::getPlayer)
                .forEach(player -> assertTrue(player.getHandCards().isEmpty()));
    }

    private void assertGuesserReceiveDixitRoundScoringEvent(Player player, int currentRound, Story story,
                                                            Collection<PlayCard> expectedPlayCards, Collection<Guess> expectedGuesses) {
        var dixitRoundScoringEvent = receiveEvent(player, DixitRoundScoringEvent.class);
        assertNotNull(dixitRoundScoringEvent);
        assertEquals(currentRound, dixitRoundScoringEvent.getRounds());
        assertEquals(player.getId(), dixitRoundScoringEvent.getPlayerId());
        assertEquals(RoundState.SCORING, dixitRoundScoringEvent.getRoundState());
        assertEquals(story, dixitRoundScoringEvent.getStory());

        var actualPlayCards = dixitRoundScoringEvent.getPlayCards();
        assertEqualsIgnoreOrder(expectedPlayCards, actualPlayCards);
        mapToList(actualPlayCards, PlayCard::getPlayer)
                .forEach(guesser -> assertTrue(guesser.getHandCards().isEmpty()));

        var actualGuesses = dixitRoundScoringEvent.getGuesses();
        assertEqualsIgnoreOrder(expectedGuesses, actualGuesses);
        mapToList(actualGuesses, Guess::getGuesser)
                .forEach(guesser -> assertTrue(guesser.getHandCards().isEmpty()));
    }

    private void assertPlayerReceiveDixitRoundOverEvent(Player expectedPlayer) {
        var dixitRoundOverEvent = receiveEvent(expectedPlayer, DixitRoundOverEvent.class);
        assertNotNull(dixitRoundOverEvent);
        assertEquals(SECOND_ROUND, dixitRoundOverEvent.getRounds());
        assertEquals(expectedPlayer.getId(), dixitRoundOverEvent.getPlayerId());
        assertEquals(RoundState.OVER, dixitRoundOverEvent.getRoundState());

        Player actualPlayer = findFirst(dixitRoundOverEvent.getPlayers(), expectedPlayer::equals)
                .orElseThrow();
        assertEquals(expectedPlayer.getScore(), actualPlayer.getScore());
    }

    private void assertPlayerReceiveDixitGameOverEvent(Player player, int currentRound, Collection<Player> winners) {
        var dixitGameOverEvent = receiveEvent(player, DixitGameOverEvent.class);
        assertNotNull(dixitGameOverEvent);
        assertEquals(currentRound, dixitGameOverEvent.getRounds());
        assertEquals(player.getId(), dixitGameOverEvent.getPlayerId());
        assertEquals(GameState.OVER, dixitGameOverEvent.getGameState());
        assertEqualsIgnoreOrder(winners, dixitGameOverEvent.getWinners());
    }
}
