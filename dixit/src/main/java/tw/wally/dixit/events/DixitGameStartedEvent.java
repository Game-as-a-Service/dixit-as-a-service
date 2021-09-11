package tw.wally.dixit.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.EventBus.Event;
import tw.wally.dixit.model.GameState;
import tw.wally.dixit.model.Player;

import java.util.Collection;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@NoArgsConstructor
public class DixitGameStartedEvent extends Event {
    private GameState gameState;
    private Collection<Player> players;

    public DixitGameStartedEvent(String gameId, String playerId, GameState gameState, Collection<Player> players) {
        super(gameId, playerId);
        this.gameState = gameState;
        this.players = players;
    }
}
