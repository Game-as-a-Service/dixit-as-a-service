package tw.wally.dixit.events.roundstate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.events.DixitRoundEvent;
import tw.wally.dixit.model.Player;
import tw.wally.dixit.model.RoundState;

import java.util.Collection;

import static tw.wally.dixit.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@NoArgsConstructor
public class DixitRoundScoredEvent extends DixitRoundEvent {
    private Collection<Player> players;

    public DixitRoundScoredEvent(String gameId, int rounds, String playerId, RoundState roundState, Collection<Player> players) {
        super(gameId, rounds, playerId, roundState);
        this.players = mapToList(players, this::renewPlayer);
    }
}
