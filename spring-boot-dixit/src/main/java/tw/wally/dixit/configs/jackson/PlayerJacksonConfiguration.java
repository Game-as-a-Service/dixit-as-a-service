package tw.wally.dixit.configs.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.boot.jackson.JsonObjectSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.wally.dixit.model.Card;
import tw.wally.dixit.model.Color;
import tw.wally.dixit.model.Player;

import java.io.IOException;

import static java.util.Arrays.asList;

/**
 * @author - wally55077@gmail.com
 */
@Configuration
public class PlayerJacksonConfiguration {
    public static final JsonDeserializer<Player> DESERIALIZER = new JsonObjectDeserializer<>() {
        @Override
        public Class<Player> handledType() {
            return Player.class;
        }

        @Override
        protected Player deserializeObject(JsonParser jsonParser, DeserializationContext context, ObjectCodec codec, JsonNode tree) throws IOException {
            String id = tree.get("id").asText();
            String name = tree.get("name").asText();
            Color color = codec.treeToValue(tree.get("color"), Color.class);
            var handCards = asList(color == null ? new Card[0] : codec.treeToValue(tree.get("handCards"), Card[].class));
            int score = color == null ? 0 : tree.get("score").asInt();
            return new Player(id, name, color, handCards, score);
        }
    };

    public static final JsonSerializer<Player> SERIALIZER = new JsonObjectSerializer<>() {
        @Override
        public Class<Player> handledType() {
            return Player.class;
        }

        @Override
        protected void serializeObject(Player player, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStringField("id", player.getId());
            jgen.writeStringField("name", player.getName());
            jgen.writeObjectField("color", player.getColor());
            jgen.writeObjectField("handCards", player.getHandCards());
            jgen.writeNumberField("score", player.getScore());
        }
    };


    @Bean
    public JsonDeserializer<Player> playerJsonDeserializer() {
        return DESERIALIZER;
    }

    @Bean
    public JsonSerializer<Player> playerJsonSerializer() {
        return SERIALIZER;
    }

}
