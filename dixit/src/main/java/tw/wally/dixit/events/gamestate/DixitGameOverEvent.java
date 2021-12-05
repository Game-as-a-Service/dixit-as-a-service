package tw.wally.dixit.events.gamestate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.events.DixitGameEvent;
import tw.wally.dixit.model.GameState;
import tw.wally.dixit.model.Player;

import java.util.Collection;

import static tw.wally.dixit.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@NoArgsConstructor
public class DixitGameOverEvent extends DixitGameEvent {
    private Collection<Player> winners;

    public DixitGameOverEvent(String gameId, int rounds, String playerId, GameState gameState, Collection<Player> winners) {
        super(gameId, rounds, playerId, gameState);
        this.winners = mapToList(winners, this::renewPlayer);
    }
}
