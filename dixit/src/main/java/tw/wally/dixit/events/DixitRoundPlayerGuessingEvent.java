package tw.wally.dixit.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.EventBus.Event;
import tw.wally.dixit.model.PlayCard;
import tw.wally.dixit.model.Player;
import tw.wally.dixit.model.RoundState;

import java.util.Collection;

import static tw.wally.dixit.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@NoArgsConstructor
public class DixitRoundPlayerGuessingEvent extends Event {
    private RoundState roundState;
    private Collection<PlayCard> playCards;

    public DixitRoundPlayerGuessingEvent(String gameId, String playerId, RoundState roundState, Collection<PlayCard> playCards) {
        super(gameId, playerId);
        this.roundState = roundState;
        this.playCards = mapToList(playCards, this::renewPlayCard);
    }

    private PlayCard renewPlayCard(PlayCard playCard) {
        return new PlayCard(renewPlayer(playCard.getPlayer()), playCard.getCard());
    }

    private Player renewPlayer(Player player) {
        return new Player(player.getId(), player.getName(), player.getScore());
    }
}
