package tw.wally.dixit.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.EventBus.Event;
import tw.wally.dixit.model.Player;
import tw.wally.dixit.model.RoundState;

import java.util.Collection;

import static tw.wally.dixit.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@NoArgsConstructor
public class DixitRoundOverEvent extends Event {
    private RoundState roundState;
    private Collection<Player> players;

    public DixitRoundOverEvent(String gameId, int rounds, String playerId, RoundState roundState, Collection<Player> players) {
        super(gameId, rounds, playerId);
        this.roundState = roundState;
        this.players = mapToList(players, this::renewPlayer);
    }

    private Player renewPlayer(Player player) {
        return new Player(player.getId(), player.getName(), player.getScore());
    }
}
