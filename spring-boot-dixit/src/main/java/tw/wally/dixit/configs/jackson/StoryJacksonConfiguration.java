package tw.wally.dixit.configs.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.boot.jackson.JsonObjectSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.wally.dixit.model.PlayCard;
import tw.wally.dixit.model.Story;

import java.io.IOException;

/**
 * @author - wally55077@gmail.com
 */
@Configuration
public class StoryJacksonConfiguration {

    public static final JsonDeserializer<Story> DESERIALIZER = new JsonObjectDeserializer<>() {
        @Override
        public Class<Story> handledType() {
            return Story.class;
        }

        @Override
        protected Story deserializeObject(JsonParser jsonParser, DeserializationContext context, ObjectCodec codec, JsonNode tree) throws IOException {
            String phrase = tree.get("phrase").asText();
            PlayCard playCard = codec.treeToValue(tree.get("playCard"), PlayCard.class);
            return new Story(phrase, playCard);
        }
    };


    public static final JsonSerializer<Story> SERIALIZER = new JsonObjectSerializer<>() {
        @Override
        public Class<Story> handledType() {
            return Story.class;
        }

        @Override
        protected void serializeObject(Story Story, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStringField("phrase", Story.getPhrase());
            jgen.writeObjectField("playCard", Story.getPlayCard());
        }
    };


    @Bean
    public JsonDeserializer<Story> storyJsonDeserializer() {
        return DESERIALIZER;
    }

    @Bean
    public JsonSerializer<Story> storyJsonSerializer() {
        return SERIALIZER;
    }
}
