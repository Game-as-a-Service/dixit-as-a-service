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
    private final PlayerData player;
    private final CardData card;

    public String getPlayerId() {
        return player.getId();
    }

    public int getCardId() {
        return card.getId();
    }

    public static PlayCardData toData(PlayCard playCard) {
        return new PlayCardData(PlayerData.toData(playCard.getPlayer()), CardData.toData(playCard.getCard()));
    }

    public PlayCard toEntity(Player player) {
        return new PlayCard(player, card.toEntity());
    }
}
