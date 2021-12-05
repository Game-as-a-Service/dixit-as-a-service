package tw.wally.dixit.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.model.GameState;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@NoArgsConstructor
public class DixitGameEvent extends DixitEvent {
    protected GameState gameState;

    public DixitGameEvent(String gameId, int rounds, String playerId, GameState gameState) {
        super(gameId, rounds, playerId);
        this.gameState = gameState;
    }
}
