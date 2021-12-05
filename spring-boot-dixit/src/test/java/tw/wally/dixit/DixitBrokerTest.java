package tw.wally.dixit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import tw.wally.dixit.events.EventBus.Event;
import tw.wally.dixit.events.gamestate.DixitGameOverEvent;
import tw.wally.dixit.events.gamestate.DixitGameStartedEvent;
import tw.wally.dixit.events.roundstate.DixitRoundCardPlayingEvent;
import tw.wally.dixit.events.roundstate.DixitRoundPlayerGuessingEvent;
import tw.wally.dixit.events.roundstate.DixitRoundScoringEvent;
import tw.wally.dixit.events.roundstate.DixitRoundStoryTellingEvent;
import tw.wally.dixit.model.*;
import tw.wally.dixit.utils.DixitEventStompFrameHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static tw.wally.dixit.configs.WebSocketConfiguration.STOMP_ROOT_DESTINATION_PREFIX;
import static tw.wally.dixit.repositories.CardRepository.EMPTY_CARD_IMAGE;
import static tw.wally.dixit.utils.StreamUtils.generate;


/**
 * @author - wally55077@gmail.com
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = {DixitBrokerTest.DixitConfiguration.class})
public class DixitBrokerTest extends AbstractDixitSpringBootTest {
    private static final Collection<Player> DIXIT_PLAYERS = generate(NUMBER_OF_PLAYERS, number -> new Player(String.valueOf(number), DIXIT_PLAYER + number));
    private static final String BASE_BROKER_URL = format("ws://localhost:%d/broker", 8080);
    private static final String BASE_DIXIT_TOPIC = format("%s/dixit/%s", STOMP_ROOT_DESTINATION_PREFIX, DIXIT_ID);
    private static final String BASE_DIXIT_GAME_STATE_TOPIC = BASE_DIXIT_TOPIC + "/gameStates";
    private static final String BASE_DIXIT_ROUND_STATE_TOPIC = BASE_DIXIT_TOPIC + "/roundStates";
    private static final Map<Class<? extends Event>, String> BASE_DIXIT_EVENT_TOPICS = Map.of(
            DixitGameStartedEvent.class, format("%s/%s", BASE_DIXIT_GAME_STATE_TOPIC, GameState.STARTED),
            DixitRoundStoryTellingEvent.class, format("%s/%s", BASE_DIXIT_ROUND_STATE_TOPIC, RoundState.STORY_TELLING),
            DixitRoundCardPlayingEvent.class, format("%s/%s", BASE_DIXIT_ROUND_STATE_TOPIC, RoundState.CARD_PLAYING),
            DixitRoundPlayerGuessingEvent.class, format("%s/%s", BASE_DIXIT_ROUND_STATE_TOPIC, RoundState.PLAYER_GUESSING),
            DixitRoundScoringEvent.class, format("%s/%s", BASE_DIXIT_ROUND_STATE_TOPIC, RoundState.SCORING),
            DixitGameOverEvent.class, format("%s/%s", BASE_DIXIT_GAME_STATE_TOPIC, GameState.OVER));
    private final Map<Class<? extends Event>, Map<String, DixitEventStompFrameHandler<? extends Event>>> dixitEventHandlers = new HashMap<>(BASE_DIXIT_EVENT_TOPICS.size());

    @Autowired
    private WebSocketStompClient stompClient;

    @Configuration
    public static class DixitConfiguration {

        @Bean
        public MockMvc mockMvc(WebApplicationContext webApplicationContext) {
            return webAppContextSetup(webApplicationContext).build();
        }

        @Bean
        public WebSocketStompClient stompClient(ObjectMapper objectMapper) {
            var stompClient = new WebSocketStompClient(new StandardWebSocketClient());
            var converter = new MappingJackson2MessageConverter();
            converter.setObjectMapper(objectMapper);
            stompClient.setMessageConverter(converter);
            return stompClient;
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
    public void GivenAllPlayersSubscribeDixitRoundStoryTellingTopic_WhenDixitCreated_ThenAllPlayersShouldReceiveDixitRoundStoryTellingEvent() throws Exception {
        subscribeEvents(DixitRoundStoryTellingEvent.class);

        createDixitWithPlayers(NUMBER_OF_PLAYERS);

        Dixit dixit = dixitRepository.findDixitById(DIXIT_ID).orElseThrow();
        Player storyteller = dixit.getCurrentStoryteller();
        int currentRound = dixit.getNumberOfRounds();
        dixit.getPlayers().forEach(player -> assertPlayerReceiveDixitRoundStoryTellingEvent(storyteller, player, currentRound));
    }

    @Test
    public void GivenAllPlayersSubscribeDixitRoundCardPlayingTopic_WhenDixitStoryToldAndTwoGuessersPlayedCard_ThenAllPlayersShouldReceiveDixitRoundCardPlayingEvent() throws Exception {
        subscribeEvents(DixitRoundCardPlayingEvent.class);

        Dixit dixit = givenGuessersPlayedCardAndGetDixit(2);

        Story story = dixit.getCurrentStory();
        var playCards = dixit.getCurrentPlayCards();
        dixit.getPlayers().forEach(player -> assertPlayerReceiveDixitRoundCardPlayingEvent(player, story, playCards));
    }

    @Test
    public void GivenAllPlayersSubscribeDixitRoundPlayerGuessingTopic_WhenAllGuessersPlayedCardAndTwoGuessersGuessedStory_ThenAllPlayersShouldReceiveDixitRoundPlayerGuessingEvent() throws Exception {
        subscribeEvents(DixitRoundPlayerGuessingEvent.class);

        Dixit dixit = givenGuessersGuessedStoryAndGetDixit(2);

        Story story = dixit.getCurrentStory();
        var playCards = dixit.getCurrentPlayCards();
        var guesses = dixit.getCurrentGuesses();
        dixit.getPlayers().forEach(player -> assertPlayerReceiveDixitRoundPlayerGuessingEvent(player, story, playCards, guesses));
    }

    @Test
    public void GivenAllPlayersSubscribeDixitRoundScoringTopic_WhenAllGuessersGuessedStory_ThenAllPlayersShouldReceiveDixitRoundScoringEvent() throws Exception {
        subscribeEvents(DixitRoundScoringEvent.class);

        Dixit dixit = givenAllGuessersGuessedStoryAndGetDixit();

        var players = dixit.getPlayers();
        players.forEach(player -> assertPlayerReceiveDixitRoundScoringEvent(player, players));
    }

    @Test
    public void GivenAllPlayersSubscribeDixitRoundStoryTellingTopic_WhenDixitSecondRoundStarted_ThenAllPlayersShouldReceiveDixitRoundStoryTellingEvent() throws Exception {
        subscribeEvents(DixitRoundStoryTellingEvent.class);

        Dixit dixit = givenAllGuessersGuessedStoryAndGetDixit();

        Player storyteller = dixit.getCurrentStoryteller();
        int currentRound = dixit.getNumberOfRounds();
        dixit.getPlayers().forEach(player -> assertPlayerReceiveDixitRoundStoryTellingEvent(storyteller, player, currentRound));
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
        for (Player player : DIXIT_PLAYERS) {
            subscribeEvent(player, dixitEventClass);
        }
    }

    private void subscribeEvent(Player player, Class<? extends Event> eventClass) throws Exception {
        var stompSession = stompClient.connect(BASE_BROKER_URL, new StompSessionHandlerAdapter() {
        }).get(10, SECONDS);

        String playerId = player.getId();
        String topic = format("%s/players/%s", BASE_DIXIT_EVENT_TOPICS.getOrDefault(eventClass, BASE_DIXIT_TOPIC), playerId);
        var dixitEventStompFrameHandler = new DixitEventStompFrameHandler<>(eventClass);
        stompSession.subscribe(topic, dixitEventStompFrameHandler);

        dixitEventHandlers.computeIfAbsent(eventClass, dixitEventClass -> new HashMap<>(NUMBER_OF_PLAYERS))
                .put(playerId, dixitEventStompFrameHandler);
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

    private void assertPlayerReceiveDixitRoundStoryTellingEvent(Player storyteller,
                                                                Player player, int currentRound) {
        var dixitRoundStoryTellingEvent = receiveEvent(player, DixitRoundStoryTellingEvent.class);
        assertNotNull(dixitRoundStoryTellingEvent);
        assertEquals(currentRound, dixitRoundStoryTellingEvent.getRounds());
        assertEquals(player.getId(), dixitRoundStoryTellingEvent.getPlayerId());
        assertEquals(RoundState.STORY_TELLING, dixitRoundStoryTellingEvent.getRoundState());

        Player eventStoryteller = dixitRoundStoryTellingEvent.getStoryteller();
        assertEquals(storyteller, eventStoryteller);
        assertTrue(eventStoryteller.getHandCards().isEmpty());
        assertEqualsIgnoreOrder(player.getHandCards(), dixitRoundStoryTellingEvent.getHandCards());
    }

    private void assertPlayerReceiveDixitRoundCardPlayingEvent(Player player, Story story,
                                                               Collection<PlayCard> playCards) {
        var dixitRoundCardPlayingEvent = receiveEvent(player, DixitRoundCardPlayingEvent.class);
        assertNotNull(dixitRoundCardPlayingEvent);
        assertEquals(FIRST_ROUND, dixitRoundCardPlayingEvent.getRounds());
        assertEquals(player.getId(), dixitRoundCardPlayingEvent.getPlayerId());
        assertEquals(RoundState.CARD_PLAYING, dixitRoundCardPlayingEvent.getRoundState());

        Story eventStory = dixitRoundCardPlayingEvent.getStory();
        assertEquals(story, eventStory);
        assertEquals(EMPTY_CARD_IMAGE, eventStory.getCard().getImage());

        var eventPlayCards = dixitRoundCardPlayingEvent.getPlayCards();
        assertEquals(playCards.size() + 1, eventPlayCards.size());
        assertTrue(eventPlayCards.contains(story.getPlayCard()));
        assertTrue(eventPlayCards.containsAll(playCards));
        eventPlayCards.forEach(playCard -> assertTrue(playCard.getPlayer().getHandCards().isEmpty()));
    }

    private void assertPlayerReceiveDixitRoundPlayerGuessingEvent(Player player, Story story,
                                                                  Collection<PlayCard> playCards,
                                                                  Collection<Guess> guesses) {
        var dixitRoundPlayerGuessingEvent = receiveEvent(player, DixitRoundPlayerGuessingEvent.class);
        assertNotNull(dixitRoundPlayerGuessingEvent);
        assertEquals(FIRST_ROUND, dixitRoundPlayerGuessingEvent.getRounds());
        assertEquals(player.getId(), dixitRoundPlayerGuessingEvent.getPlayerId());
        assertEquals(RoundState.PLAYER_GUESSING, dixitRoundPlayerGuessingEvent.getRoundState());

        var eventPlayCards = dixitRoundPlayerGuessingEvent.getPlayCards();
        assertEquals(1 + playCards.size(), eventPlayCards.size());
        assertTrue(eventPlayCards.contains(story.getPlayCard()));
        assertTrue(eventPlayCards.containsAll(playCards));
        eventPlayCards.forEach(playCard -> assertTrue(playCard.getPlayer().getHandCards().isEmpty()));

        var eventGuesses = dixitRoundPlayerGuessingEvent.getGuesses();
        assertEqualsIgnoreOrder(guesses, eventGuesses);
        eventGuesses.forEach(guess -> assertTrue(guess.getPlayCardPlayer().getHandCards().isEmpty()));
    }

    private void assertPlayerReceiveDixitRoundScoringEvent(Player player, Collection<Player> players) {
        var dixitRoundScoringEvent = receiveEvent(player, DixitRoundScoringEvent.class);
        assertNotNull(dixitRoundScoringEvent);
        assertEquals(FIRST_ROUND, dixitRoundScoringEvent.getRounds());
        assertEquals(player.getId(), dixitRoundScoringEvent.getPlayerId());
        assertEquals(RoundState.SCORING, dixitRoundScoringEvent.getRoundState());

        assertEqualsIgnoreOrder(players, dixitRoundScoringEvent.getPlayers());
    }

    private void assertPlayerReceiveDixitGameOverEvent(Player player, int currentRound,
                                                       Collection<Player> winners) {
        var dixitGameOverEvent = receiveEvent(player, DixitGameOverEvent.class);
        assertNotNull(dixitGameOverEvent);
        assertEquals(currentRound, dixitGameOverEvent.getRounds());
        assertEquals(player.getId(), dixitGameOverEvent.getPlayerId());
        assertEquals(GameState.OVER, dixitGameOverEvent.getGameState());
        assertEqualsIgnoreOrder(winners, dixitGameOverEvent.getWinners());
    }
}
