package tw.wally.dixit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import tw.wally.dixit.clients.LobbyServiceDriver;

import static tw.wally.dixit.model.Dixit.MAX_NUMBER_OF_PLAYERS;
import static tw.wally.dixit.model.Dixit.MIN_NUMBER_OF_PLAYERS;

/**
 * @author - wally55077@gmail.com
 */
@Component
public class DixitApplicationRunner implements ApplicationRunner {
    private final String name;
    private final String serviceHost;
    private final RetryTemplate retryTemplate;
    private final LobbyServiceDriver lobbyServiceDriver;

    public DixitApplicationRunner(@Value("${dixit.service.name}") String name,
                                  @Value("${dixit.service.host}") String serviceHost,
                                  RetryTemplate retryTemplate,
                                  LobbyServiceDriver lobbyServiceDriver) {
        this.name = name;
        this.serviceHost = serviceHost;
        this.retryTemplate = retryTemplate;
        this.lobbyServiceDriver = lobbyServiceDriver;
    }

    @Override
    public void run(ApplicationArguments args) {
        retryTemplate.execute(this::registerService);
    }

    private Void registerService(RetryContext retryContext) throws RestClientException {
        Registration registration = new Registration(name, serviceHost, MIN_NUMBER_OF_PLAYERS, MAX_NUMBER_OF_PLAYERS);
        registration.addOption(new Registration.Option("winningScore", "range", 25, 35, 5));
        lobbyServiceDriver.registerService(registration);
        return null;
    }
}
