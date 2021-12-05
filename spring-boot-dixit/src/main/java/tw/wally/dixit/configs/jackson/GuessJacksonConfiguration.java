package tw.wally.dixit.configs.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.boot.jackson.JsonObjectSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.wally.dixit.model.Guess;
import tw.wally.dixit.model.PlayCard;
import tw.wally.dixit.model.Player;

import java.io.IOException;

/**
 * @author - wally55077@gmail.com
 */
@Configuration
public class GuessJacksonConfiguration {

    public static final JsonDeserializer<Guess> DESERIALIZER = new JsonObjectDeserializer<>() {
        @Override
        public Class<Guess> handledType() {
            return Guess.class;
        }

        @Override
        protected Guess deserializeObject(JsonParser jsonParser, DeserializationContext context, ObjectCodec codec, JsonNode tree) throws IOException {
            Player guesser = codec.treeToValue(tree.get("guesser"), Player.class);
            PlayCard playCard = codec.treeToValue(tree.get("playCard"), PlayCard.class);
            return new Guess(guesser, playCard);
        }
    };


    public static final JsonSerializer<Guess> SERIALIZER = new JsonObjectSerializer<>() {
        @Override
        public Class<Guess> handledType() {
            return Guess.class;
        }

        @Override
        protected void serializeObject(Guess Guess, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeObjectField("guesser", Guess.getGuesser());
            jgen.writeObjectField("playCard", Guess.getPlayCard());
        }
    };


    @Bean
    public JsonDeserializer<Guess> guessJsonDeserializer() {
        return DESERIALIZER;
    }

    @Bean
    public JsonSerializer<Guess> guessJsonSerializer() {
        return SERIALIZER;
    }
}
