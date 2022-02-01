package tw.wally.dixit.events.roundstate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.events.DixitRoundEvent;
import tw.wally.dixit.model.Card;
import tw.wally.dixit.model.Player;
import tw.wally.dixit.model.RoundState;

import java.util.Collection;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@NoArgsConstructor
public class DixitRoundStoryToldEvent extends DixitRoundEvent {
    private Player storyteller;
    private Collection<Card> handCards;

    public DixitRoundStoryToldEvent(String gameId, int rounds, RoundState roundState, Player storyteller, Player player) {
        super(gameId, rounds, player.getId(), roundState);
        this.roundState = roundState;
        this.storyteller = new Player(storyteller.getId(), storyteller.getName(), storyteller.getColor(), storyteller.getScore());
        this.handCards = player.getHandCards();
    }
}
