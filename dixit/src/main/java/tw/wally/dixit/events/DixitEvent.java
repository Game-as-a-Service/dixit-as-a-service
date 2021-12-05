package tw.wally.dixit.events;

import lombok.NoArgsConstructor;
import tw.wally.dixit.events.EventBus.Event;
import tw.wally.dixit.model.Guess;
import tw.wally.dixit.model.PlayCard;
import tw.wally.dixit.model.Player;

/**
 * @author - wally55077@gmail.com
 */
@NoArgsConstructor
public class DixitEvent extends Event {

    public DixitEvent(String gameId, int rounds, String playerId) {
        super(gameId, rounds, playerId);
    }

    protected Guess renewGuess(Guess guess) {
        return new Guess(renewPlayer(guess.getGuesser()), renewPlayCard(guess.getPlayCard()));
    }

    protected PlayCard renewPlayCard(PlayCard playCard) {
        return new PlayCard(renewPlayer(playCard.getPlayer()), playCard.getCard());
    }

    protected Player renewPlayer(Player player) {
        return new Player(player.getId(), player.getName(), player.getColor(), player.getScore());
    }
}
