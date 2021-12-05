package tw.wally.dixit;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import tw.wally.dixit.configs.jackson.GuessJacksonConfiguration;
import tw.wally.dixit.configs.jackson.PlayCardJacksonConfiguration;
import tw.wally.dixit.configs.jackson.PlayerJacksonConfiguration;
import tw.wally.dixit.configs.jackson.StoryJacksonConfiguration;
import tw.wally.dixit.model.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tw.wally.dixit.utils.StreamUtils.generate;

/**
 * @author - wally55077@gmail.com
 */
public class JacksonConfigTest {

    private static final JsonDeserializer<?>[] DESERIALIZERS = {
            PlayerJacksonConfiguration.DESERIALIZER,
            StoryJacksonConfiguration.DESERIALIZER,
            PlayCardJacksonConfiguration.DESERIALIZER,
            GuessJacksonConfiguration.DESERIALIZER
    };

    private static final JsonSerializer<?>[] SERIALIZERS = {
            PlayerJacksonConfiguration.SERIALIZER,
            StoryJacksonConfiguration.SERIALIZER,
            PlayCardJacksonConfiguration.SERIALIZER,
            GuessJacksonConfiguration.SERIALIZER
    };

    private static final ObjectMapper OBJECT_MAPPER = new Jackson2ObjectMapperBuilder()
            .serializationInclusion(Include.NON_NULL)
            .featuresToEnable(SerializationFeature.INDENT_OUTPUT)
            .deserializers(DESERIALIZERS)
            .serializers(SERIALIZERS)
            .build();

    private static final String DIXIT_PLAYER = "dixitPlayer";
    protected static final String DIXIT_PHRASE = "dixitPhrase";
    private static final String CARD_IMAGE = "cardImage";
    private Player player;
    private PlayCard playCard;
    private Story story;
    private Guess guess;

    @BeforeEach
    public void beforeTest() {
        var handCards = generate(Dixit.NUMBER_OF_PLAYER_HAND_CARDS, number -> new Card(number, CARD_IMAGE + number));
        this.player = new Player(DIXIT_PLAYER + "Id:1", DIXIT_PLAYER, Color.RED, handCards, 0);
        this.playCard = new PlayCard(this.player, handCards.get(0));
        this.story = new Story(DIXIT_PHRASE, this.playCard);
        this.guess = new Guess(new Player(DIXIT_PLAYER + "Id:2", DIXIT_PLAYER, Color.ORANGE, handCards, 0), this.playCard);
    }

    @Test
    public void testPlayerJacksonConfig() throws Exception {
        String playerJson = OBJECT_MAPPER.writeValueAsString(this.player);
        Player player = OBJECT_MAPPER.readValue(playerJson, Player.class);
        assertPlayerEquals(this.player, player);
    }

    @Test
    public void testStoryJacksonConfig() throws Exception {
        String storyJson = OBJECT_MAPPER.writeValueAsString(this.story);
        Story story = OBJECT_MAPPER.readValue(storyJson, Story.class);
        assertStoryEquals(this.story, story);
    }

    @Test
    public void testPlayCardJacksonConfig() throws Exception {
        String playCardJson = OBJECT_MAPPER.writeValueAsString(this.playCard);
        PlayCard playCard = OBJECT_MAPPER.readValue(playCardJson, PlayCard.class);
        assertPlayCardEquals(this.playCard, playCard);
    }


    @Test
    public void testGuessJacksonConfig() throws Exception {
        String guessJson = OBJECT_MAPPER.writeValueAsString(this.guess);
        Guess guess = OBJECT_MAPPER.readValue(guessJson, Guess.class);
        assertGuessCardEquals(this.guess, guess);
    }

    private void assertPlayerEquals(Player expectedPlayer, Player actualPlayer) {
        assertEquals(expectedPlayer.getId(), actualPlayer.getId());
        assertEquals(expectedPlayer.getName(), actualPlayer.getName());
        assertEquals(expectedPlayer.getColor(), actualPlayer.getColor());
        assertEquals(expectedPlayer.getHandCards(), actualPlayer.getHandCards());
        assertEquals(expectedPlayer.getScore(), actualPlayer.getScore());
    }

    private void assertStoryEquals(Story expectedStory, Story actualStory) {
        assertEquals(expectedStory.getPhrase(), actualStory.getPhrase());
        assertPlayCardEquals(expectedStory.getPlayCard(), actualStory.getPlayCard());
    }

    private void assertPlayCardEquals(PlayCard expectedPlayCard, PlayCard actualPlayCard) {
        assertPlayerEquals(expectedPlayCard.getPlayer(), actualPlayCard.getPlayer());
        assertEquals(expectedPlayCard.getCard(), actualPlayCard.getCard());
    }

    private void assertGuessCardEquals(Guess expectedGuess, Guess actualGuess) {
        assertPlayerEquals(expectedGuess.getGuesser(), actualGuess.getGuesser());
        assertPlayCardEquals(expectedGuess.getPlayCard(), actualGuess.getPlayCard());
    }
}
