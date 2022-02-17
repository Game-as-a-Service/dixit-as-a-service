package tw.wally.dixit.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tw.wally.dixit.model.Card;
import tw.wally.dixit.model.Player;
import tw.wally.dixit.model.Story;

import java.util.Map;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@AllArgsConstructor
public class StoryData {
    private final String phrase;
    private final PlayCardData playCard;

    public static StoryData toData(Story story) {
        return new StoryData(story.getPhrase(), PlayCardData.toData(story.getPlayCard()));
    }

    public Story toEntity(Map<String, Player> players, Map<Integer, Card> cards) {
        return new Story(phrase, playCard.toEntity(players, cards));
    }
}
