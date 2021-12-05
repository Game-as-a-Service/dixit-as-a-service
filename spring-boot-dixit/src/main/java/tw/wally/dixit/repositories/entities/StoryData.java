package tw.wally.dixit.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tw.wally.dixit.model.Player;
import tw.wally.dixit.model.Story;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@AllArgsConstructor
public class StoryData {
    private final String phrase;
    private final PlayCardData playCard;

    public String getStorytellerId() {
        return playCard.getPlayerId();
    }

    public static StoryData toData(Story story) {
        return new StoryData(story.getPhrase(), PlayCardData.toData(story.getPlayCard()));
    }

    public Story toEntity(Player storyteller) {
        return new Story(phrase, playCard.toEntity(storyteller));
    }
}
