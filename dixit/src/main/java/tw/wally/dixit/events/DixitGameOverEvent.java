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
public class DixitGameOverEvent extends Event {
    private GameState gameState;
    private Collection<Player> winners;

    public DixitGameOverEvent(String gameId, int rounds, String playerId, GameState gameState, Collection<Player> winners) {
        super(gameId, rounds, playerId);
        this.gameState = gameState;
        this.winners = winners;
    }
}
