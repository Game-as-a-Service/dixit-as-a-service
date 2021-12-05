package tw.wally.dixit.views;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tw.wally.dixit.model.Story;

/**
 * @author - wally55077@gmail.com
 */
@NoArgsConstructor
@AllArgsConstructor
public class StoryView {
    public String phrase;
    public PlayCardView playCard;

    public Story toEntity() {
        return new Story(phrase, playCard.toEntity());
    }

    public static StoryView toViewModel(Story story) {
        var playCard = PlayCardView.toViewModel(story.getPlayCard());
        return new StoryView(story.getPhrase(), playCard);
    }
}
