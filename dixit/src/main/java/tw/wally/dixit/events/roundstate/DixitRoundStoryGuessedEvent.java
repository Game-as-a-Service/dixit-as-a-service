package tw.wally.dixit.events.roundstate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.events.DixitRoundEvent;
import tw.wally.dixit.model.Guess;
import tw.wally.dixit.model.PlayCard;
import tw.wally.dixit.model.RoundState;
import tw.wally.dixit.model.Story;

import java.util.Collection;

import static tw.wally.dixit.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@NoArgsConstructor
public class DixitRoundStoryGuessedEvent extends DixitRoundEvent {
    private Collection<PlayCard> playCards;
    private Collection<Guess> guesses;

    public DixitRoundStoryGuessedEvent(String gameId, int rounds, String playerId, RoundState roundState, Story story, Collection<PlayCard> playCards, Collection<Guess> guesses) {
        super(gameId, rounds, playerId, roundState);
        this.playCards = mapToList(playCards, this::renewPlayCard);
        this.playCards.add(renewPlayCard(story.getPlayCard()));
        this.guesses = mapToList(guesses, this::renewGuess);
    }
}
