package tw.wally.dixit.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@AllArgsConstructor
public class PlayCard {
    private final Player player;
    private final Card card;

    public int getCardId() {
        return card.getId();
    }
}
