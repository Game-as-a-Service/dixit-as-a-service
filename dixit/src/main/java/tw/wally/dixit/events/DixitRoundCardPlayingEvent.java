package tw.wally.dixit.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.EventBus.Event;
import tw.wally.dixit.model.Card;
import tw.wally.dixit.model.RoundState;

import java.util.Collection;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@NoArgsConstructor
public class DixitRoundCardPlayingEvent extends Event {
    private RoundState roundState;
    private Collection<Card> handCards;

    public DixitRoundCardPlayingEvent(String gameId, int rounds, String playerId, RoundState roundState, Collection<Card> handCards) {
        super(gameId, rounds, playerId);
        this.roundState = roundState;
        this.handCards = handCards;
    }
}
