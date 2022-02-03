package tw.wally.dixit;

import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import tw.wally.dixit.clients.LobbyServiceDriver;

import static tw.wally.dixit.model.Dixit.MAX_NUMBER_OF_PLAYERS;
import static tw.wally.dixit.model.Dixit.MIN_NUMBER_OF_PLAYERS;

/**
 * @author - wally55077@gmail.com
 */
@Component
@AllArgsConstructor
public class DixitApplicationRunner implements ApplicationRunner {
    private final LobbyServiceDriver lobbyServiceDriver;

    @Override
    public void run(ApplicationArguments args) {
        Registration registration = new Registration(MIN_NUMBER_OF_PLAYERS, MAX_NUMBER_OF_PLAYERS);
        registration.addOption(new Registration.Option("winningGoal", "range", 25, 35, 5));
        lobbyServiceDriver.registerService(registration);
    }
}
