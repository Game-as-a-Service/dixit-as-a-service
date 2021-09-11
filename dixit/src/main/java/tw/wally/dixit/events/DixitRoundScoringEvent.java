package tw.wally.dixit.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.EventBus.Event;
import tw.wally.dixit.model.Guess;
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
public class DixitRoundScoringEvent extends Event {
    private RoundState roundState;
    private Collection<Guess> guesses;

    public DixitRoundScoringEvent(String gameId, String playerId, RoundState roundState, Collection<Guess> guesses) {
        super(gameId, playerId);
        this.roundState = roundState;
        this.guesses = mapToList(guesses, this::renewGuess);
    }

    private Guess renewGuess(Guess guess) {
        return new Guess(renewPlayer(guess.getGuesser()), renewPlayCard(guess.getPlayCard()));
    }

    private PlayCard renewPlayCard(PlayCard playCard) {
        return new PlayCard(renewPlayer(playCard.getPlayer()), playCard.getCard());
    }

    private Player renewPlayer(Player player) {
        return new Player(player.getId(), player.getName(), player.getScore());
    }
}
