package tw.wally.dixit.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PlayCard {
    private Player player;
    private Card card;

    public String getPlayerId() {
        return player.getId();
    }

    public int getCardId() {
        return card.getId();
    }
}
