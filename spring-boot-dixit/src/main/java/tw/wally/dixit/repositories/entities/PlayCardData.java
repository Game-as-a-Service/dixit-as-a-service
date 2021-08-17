package tw.wally.dixit.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import tw.wally.dixit.model.PlayCard;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@AllArgsConstructor
public class PlayCardData {
    private final PlayerData player;
    private final CardData card;

    public int getCardId() {
        return card.getId();
    }

    public static PlayCardData toData(PlayCard playCard) {
        return new PlayCardData(PlayerData.toData(playCard.getPlayer()), CardData.toData(playCard.getCard()));
    }

    public PlayCard toEntity() {
        return new PlayCard(player.toEntity(), card.toEntity());
    }
}
