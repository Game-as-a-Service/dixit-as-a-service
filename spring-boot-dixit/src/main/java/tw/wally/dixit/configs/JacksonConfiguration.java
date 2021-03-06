package tw.wally.dixit.configs;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import tw.wally.dixit.configs.jackson.GuessJacksonConfiguration;
import tw.wally.dixit.configs.jackson.PlayCardJacksonConfiguration;
import tw.wally.dixit.configs.jackson.PlayerJacksonConfiguration;

/**
 * @author - wally55077@gmail.com
 */
@Configuration
public class JacksonConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        var objectMapperBuilder = new Jackson2ObjectMapperBuilder();
        jsonCustomizer(new JsonDeserializer<?>[]{
                        PlayerJacksonConfiguration.DESERIALIZER,
                        PlayCardJacksonConfiguration.DESERIALIZER,
                        GuessJacksonConfiguration.DESERIALIZER
                },
                new JsonSerializer<?>[]{
                        PlayerJacksonConfiguration.SERIALIZER,
                        PlayCardJacksonConfiguration.SERIALIZER,
                        GuessJacksonConfiguration.SERIALIZER
                })
                .customize(objectMapperBuilder);
        return objectMapperBuilder.build();
    }

    private Jackson2ObjectMapperBuilderCustomizer jsonCustomizer(JsonDeserializer<?>[] jsonDeserializers,
                                                                 JsonSerializer<?>[] jsonSerializers) {
        return builder -> builder.serializationInclusion(Include.NON_NULL)
                .featuresToEnable(SerializationFeature.INDENT_OUTPUT)
                .deserializers(jsonDeserializers)
                .serializers(jsonSerializers);
    }

}
