package tw.wally.dixit.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWarDeployment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.wally.dixit.clients.LobbyServiceDriver;
import tw.wally.dixit.clients.RestLobbyApiClient;
import tw.wally.dixit.clients.RestTemplateFactory;

/**
 * @author - wally55077@gmail.com
 */
@Configuration
@ConditionalOnWarDeployment
public class ServiceDriverConfiguration {

    @Bean
    public RestTemplateFactory restTemplateFactory(ObjectMapper objectMapper) {
        return new RestTemplateFactory(objectMapper);
    }

    @Bean
    public LobbyServiceDriver lobbyServiceDriver(RestTemplateFactory restTemplateFactory,
                                                 @Value("${lobby.service.host}") String lobbyServiceServiceHost) {
        var restTemplate = restTemplateFactory.create(lobbyServiceServiceHost);
        return new RestLobbyApiClient(restTemplate);
    }
}
