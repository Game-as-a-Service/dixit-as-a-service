package tw.wally.dixit.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tw.wally.dixit.model.PlayCard;
import tw.wally.dixit.model.Player;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@AllArgsConstructor
public class PlayCardData {
    private final String playerId;
    private final CardData card;


    public int getCardId() {
        return card.getId();
    }

    public static PlayCardData toData(PlayCard playCard) {
        return new PlayCardData(playCard.getPlayer().getId(), CardData.toData(playCard.getCard()));
    }

    public PlayCard toEntity(Player player) {
        return new PlayCard(player, card.toEntity());
    }
}
