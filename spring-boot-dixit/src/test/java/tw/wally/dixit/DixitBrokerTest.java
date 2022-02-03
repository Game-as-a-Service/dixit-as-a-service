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
import tw.wally.dixit.events.roundstate.DixitRoundCardPlayedEvent;
import tw.wally.dixit.events.roundstate.DixitRoundStoryGuessedEvent;
import tw.wally.dixit.events.roundstate.DixitRoundScoredEvent;
import tw.wally.dixit.events.roundstate.DixitRoundStoryToldEvent;
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
            DixitRoundStoryToldEvent.class, format("%s/%s", BASE_DIXIT_ROUND_STATE_TOPIC, RoundState.STORY_TELLING),
            DixitRoundCardPlayedEvent.class, format("%s/%s", BASE_DIXIT_ROUND_STATE_TOPIC, RoundState.CARD_PLAYING),
            DixitRoundStoryGuessedEvent.class, format("%s/%s", BASE_DIXIT_ROUND_STATE_TOPIC, RoundState.STORY_GUESSING),
            DixitRoundScoredEvent.class, format("%s/%s", BASE_DIXIT_ROUND_STATE_TOPIC, RoundState.SCORING),
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
    public void GivenAllPlayersSubscribeDixitRoundStoryToldTopic_WhenDixitCreated_ThenAllPlayersShouldReceiveDixitRoundStoryToldEvent() throws Exception {
        subscribeEvents(DixitRoundStoryToldEvent.class);

        createDixitWithPlayers(NUMBER_OF_PLAYERS);

        Dixit dixit = dixitRepository.findDixitById(DIXIT_ID).orElseThrow();
        Player storyteller = dixit.getCurrentStoryteller();
        int currentRound = dixit.getNumberOfRounds();
        dixit.getPlayers().forEach(player -> assertPlayerReceiveDixitRoundStoryToldEvent(storyteller, player, currentRound));
    }

    @Test
    public void GivenAllPlayersSubscribeDixitRoundCardPlayedTopic_WhenDixitStoryToldAndTwoGuessersPlayedCard_ThenAllPlayersShouldReceiveDixitRoundCardPlayedEvent() throws Exception {
        subscribeEvents(DixitRoundCardPlayedEvent.class);

        Dixit dixit = givenGuessersPlayedCardAndGetDixit(2);

        Story story = dixit.getCurrentStory();
        var playCards = dixit.getCurrentPlayCards();
        dixit.getPlayers().forEach(player -> assertPlayerReceiveDixitRoundCardPlayedEvent(player, story, playCards));
    }

    @Test
    public void GivenAllPlayersSubscribeDixitRoundStoryGuessedTopic_WhenAllGuessersPlayedCardAndTwoGuessersGuessedStory_ThenAllPlayersShouldReceiveDixitRoundStoryGuessedEvent() throws Exception {
        subscribeEvents(DixitRoundStoryGuessedEvent.class);

        Dixit dixit = givenGuessersGuessedStoryAndGetDixit(2);

        Story story = dixit.getCurrentStory();
        var playCards = dixit.getCurrentPlayCards();
        var guesses = dixit.getCurrentGuesses();
        dixit.getPlayers().forEach(player -> assertPlayerReceiveDixitRoundStoryGuessedEvent(player, story, playCards, guesses));
    }

    @Test
    public void GivenAllPlayersSubscribeDixitRoundScoredTopic_WhenAllGuessersGuessedStory_ThenAllPlayersShouldReceiveDixitRoundScoredEvent() throws Exception {
        subscribeEvents(DixitRoundScoredEvent.class);

        Dixit dixit = givenAllGuessersGuessedStoryAndGetDixit();

        var players = dixit.getPlayers();
        players.forEach(player -> assertPlayerReceiveDixitRoundScoredEvent(player, players));
    }

    @Test
    public void GivenAllPlayersSubscribeDixitRoundStoryToldTopic_WhenDixitSecondRoundStarted_ThenAllPlayersShouldReceiveDixitRoundStoryToldEvent() throws Exception {
        subscribeEvents(DixitRoundStoryToldEvent.class);

        Dixit dixit = givenAllGuessersGuessedStoryAndGetDixit();

        Player storyteller = dixit.getCurrentStoryteller();
        int currentRound = dixit.getNumberOfRounds();
        dixit.getPlayers().forEach(player -> assertPlayerReceiveDixitRoundStoryToldEvent(storyteller, player, currentRound));
    }

    @Test
    public void GivenAllPlayersSubscribeDixitGameOverTopic_WhenAllGuessersGuessedStoryAndDixitHasWinners_ThenAllPlayersShouldReceiveDixitGameOverEvent() throws Exception {
        subscribeEvents(DixitGameOverEvent.class);

        Dixit dixit = givenAllGuessersPlayedCardAndGetDixit();
        makePlayersAchieveWinningGoal(dixit);
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
        String destination = format("%s/players/%s", BASE_DIXIT_EVENT_TOPICS.getOrDefault(eventClass, BASE_DIXIT_TOPIC), playerId);
        var dixitEventStompFrameHandler = new DixitEventStompFrameHandler<>(eventClass);
        stompSession.subscribe(destination, dixitEventStompFrameHandler);

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

    private void assertPlayerReceiveDixitRoundStoryToldEvent(Player storyteller,
                                                                Player player, int currentRound) {
        var dixitRoundStoryToldEvent = receiveEvent(player, DixitRoundStoryToldEvent.class);
        assertNotNull(dixitRoundStoryToldEvent);
        assertEquals(currentRound, dixitRoundStoryToldEvent.getRounds());
        assertEquals(player.getId(), dixitRoundStoryToldEvent.getPlayerId());
        assertEquals(RoundState.STORY_TELLING, dixitRoundStoryToldEvent.getRoundState());

        Player eventStoryteller = dixitRoundStoryToldEvent.getStoryteller();
        assertEquals(storyteller, eventStoryteller);
        assertTrue(eventStoryteller.getHandCards().isEmpty());
        assertEqualsIgnoreOrder(player.getHandCards(), dixitRoundStoryToldEvent.getHandCards());
    }

    private void assertPlayerReceiveDixitRoundCardPlayedEvent(Player player, Story story,
                                                               Collection<PlayCard> playCards) {
        var dixitRoundCardPlayedEvent = receiveEvent(player, DixitRoundCardPlayedEvent.class);
        assertNotNull(dixitRoundCardPlayedEvent);
        assertEquals(FIRST_ROUND, dixitRoundCardPlayedEvent.getRounds());
        assertEquals(player.getId(), dixitRoundCardPlayedEvent.getPlayerId());
        assertEquals(RoundState.CARD_PLAYING, dixitRoundCardPlayedEvent.getRoundState());

        Story eventStory = dixitRoundCardPlayedEvent.getStory();
        assertEquals(story, eventStory);
        assertEquals(EMPTY_CARD_IMAGE, eventStory.getCard().getImage());

        var eventPlayCards = dixitRoundCardPlayedEvent.getPlayCards();
        assertEquals(playCards.size() + 1, eventPlayCards.size());
        assertTrue(eventPlayCards.contains(story.getPlayCard()));
        assertTrue(eventPlayCards.containsAll(playCards));
        eventPlayCards.forEach(playCard -> assertTrue(playCard.getPlayer().getHandCards().isEmpty()));
    }

    private void assertPlayerReceiveDixitRoundStoryGuessedEvent(Player player, Story story,
                                                                  Collection<PlayCard> playCards,
                                                                  Collection<Guess> guesses) {
        var dixitRoundStoryGuessedEvent = receiveEvent(player, DixitRoundStoryGuessedEvent.class);
        assertNotNull(dixitRoundStoryGuessedEvent);
        assertEquals(FIRST_ROUND, dixitRoundStoryGuessedEvent.getRounds());
        assertEquals(player.getId(), dixitRoundStoryGuessedEvent.getPlayerId());
        assertEquals(RoundState.STORY_GUESSING, dixitRoundStoryGuessedEvent.getRoundState());

        var eventPlayCards = dixitRoundStoryGuessedEvent.getPlayCards();
        assertEquals(1 + playCards.size(), eventPlayCards.size());
        assertTrue(eventPlayCards.contains(story.getPlayCard()));
        assertTrue(eventPlayCards.containsAll(playCards));
        eventPlayCards.forEach(playCard -> assertTrue(playCard.getPlayer().getHandCards().isEmpty()));

        var eventGuesses = dixitRoundStoryGuessedEvent.getGuesses();
        assertEqualsIgnoreOrder(guesses, eventGuesses);
        eventGuesses.forEach(guess -> assertTrue(guess.getPlayCardPlayer().getHandCards().isEmpty()));
    }

    private void assertPlayerReceiveDixitRoundScoredEvent(Player player, Collection<Player> players) {
        var dixitRoundScoredEvent = receiveEvent(player, DixitRoundScoredEvent.class);
        assertNotNull(dixitRoundScoredEvent);
        assertEquals(FIRST_ROUND, dixitRoundScoredEvent.getRounds());
        assertEquals(player.getId(), dixitRoundScoredEvent.getPlayerId());
        assertEquals(RoundState.SCORING, dixitRoundScoredEvent.getRoundState());

        assertEqualsIgnoreOrder(players, dixitRoundScoredEvent.getPlayers());
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
