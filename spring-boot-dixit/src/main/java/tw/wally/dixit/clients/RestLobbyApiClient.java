package tw.wally.dixit.clients;

import lombok.AllArgsConstructor;
import org.springframework.web.client.RestTemplate;
import tw.wally.dixit.Registration;

/**
 * @author - wally55077@gmail.com
 */
@AllArgsConstructor
public class RestLobbyApiClient implements LobbyServiceDriver {
    public static final String API_PREFIX = "/api";
    private final RestTemplate restTemplate;

    @Override
    public void registerService(Registration registration) {
        restTemplate.postForEntity(String.format("%s/register", API_PREFIX), registration, Object.class);
    }
}
