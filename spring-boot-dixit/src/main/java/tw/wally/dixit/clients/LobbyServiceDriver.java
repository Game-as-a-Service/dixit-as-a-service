package tw.wally.dixit.clients;

import tw.wally.dixit.Registration;

/**
 * @author - wally55077@gmail.com
 */
@FunctionalInterface
public interface LobbyServiceDriver {
    void registerService(Registration registration);
}
