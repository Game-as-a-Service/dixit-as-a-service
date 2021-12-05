package tw.wally.dixit.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.model.RoundState;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@NoArgsConstructor
public class DixitRoundEvent extends DixitEvent {
    protected RoundState roundState;

    public DixitRoundEvent(String gameId, int rounds, String playerId, RoundState roundState) {
        super(gameId, rounds, playerId);
        this.roundState = roundState;
    }
}
