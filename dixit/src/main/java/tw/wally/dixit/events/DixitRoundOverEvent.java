package tw.wally.dixit.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.EventBus.Event;
import tw.wally.dixit.model.RoundState;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@NoArgsConstructor
public class DixitRoundOverEvent extends Event {
    private RoundState roundState;
    private int score;

    public DixitRoundOverEvent(String gameId, String playerId, RoundState roundState, int score) {
        super(gameId, playerId);
        this.roundState = roundState;
        this.score = score;
    }
}
