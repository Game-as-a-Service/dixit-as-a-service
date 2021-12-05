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
import tw.wally.dixit.model.PlayCard;
import tw.wally.dixit.model.Player;

import java.io.IOException;

/**
 * @author - wally55077@gmail.com
 */
@Configuration
public class PlayCardJacksonConfiguration {

    public static final JsonDeserializer<PlayCard> DESERIALIZER = new JsonObjectDeserializer<>() {
        @Override
        public Class<PlayCard> handledType() {
            return PlayCard.class;
        }

        @Override
        protected PlayCard deserializeObject(JsonParser jsonParser, DeserializationContext context, ObjectCodec codec, JsonNode tree) throws IOException {
            Player player = codec.treeToValue(tree.get("player"), Player.class);
            Card card = codec.treeToValue(tree.get("card"), Card.class);
            return new PlayCard(player, card);
        }
    };


    public static final JsonSerializer<PlayCard> SERIALIZER = new JsonObjectSerializer<>() {
        @Override
        public Class<PlayCard> handledType() {
            return PlayCard.class;
        }

        @Override
        protected void serializeObject(PlayCard PlayCard, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeObjectField("player", PlayCard.getPlayer());
            jgen.writeObjectField("card", PlayCard.getCard());
        }
    };


    @Bean
    public JsonDeserializer<PlayCard> playCardJsonDeserializer() {
        return DESERIALIZER;
    }

    @Bean
    public JsonSerializer<PlayCard> playCardJsonSerializer() {
        return SERIALIZER;
    }
}
