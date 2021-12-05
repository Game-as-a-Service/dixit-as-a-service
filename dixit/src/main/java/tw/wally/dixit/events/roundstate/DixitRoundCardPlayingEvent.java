package tw.wally.dixit.events.roundstate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.events.DixitRoundEvent;
import tw.wally.dixit.model.Card;
import tw.wally.dixit.model.PlayCard;
import tw.wally.dixit.model.RoundState;
import tw.wally.dixit.model.Story;

import java.util.Collection;

import static tw.wally.dixit.repositories.CardRepository.EMPTY_CARD_IMAGE;
import static tw.wally.dixit.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@NoArgsConstructor
public class DixitRoundCardPlayingEvent extends DixitRoundEvent {
    private Story story;
    private Collection<PlayCard> playCards;

    public DixitRoundCardPlayingEvent(String gameId, int rounds, String playerId, RoundState roundState, Story story, Collection<PlayCard> playCards) {
        super(gameId, rounds, playerId, roundState);
        this.story = new Story(story.getPhrase(), new PlayCard(renewPlayer(story.getPlayer()), new Card(story.getCardId(), EMPTY_CARD_IMAGE)));
        this.playCards = mapToList(playCards, this::renewPlayCard);
        this.playCards.add(renewPlayCard(story.getPlayCard()));
    }
}
