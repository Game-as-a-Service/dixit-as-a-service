package tw.wally.dixit.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.EventBus.Event;
import tw.wally.dixit.model.GameState;
import tw.wally.dixit.model.Player;

import java.util.Collection;

import static tw.wally.dixit.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@NoArgsConstructor
public class DixitGameStartedEvent extends Event {
    private GameState gameState;
    private Collection<Player> players;

    public DixitGameStartedEvent(String gameId, int rounds, String playerId, GameState gameState, Collection<Player> players) {
        super(gameId, rounds, playerId);
        this.gameState = gameState;
        this.players = mapToList(players, this::renewPlayer);
    }

    private Player renewPlayer(Player player) {
        Player newPlayer = new Player(player.getId(), player.getName(), player.getScore());
        newPlayer.setColor(player.getColor());
        return newPlayer;
    }
}
