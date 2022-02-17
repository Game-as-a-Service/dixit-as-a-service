package tw.wally.dixit.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tw.wally.dixit.model.Card;
import tw.wally.dixit.model.PlayCard;
import tw.wally.dixit.model.Player;

import java.util.Map;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@AllArgsConstructor
public class PlayCardData {
    private final String playerId;
    private final int cardId;

    public static PlayCardData toData(PlayCard playCard) {
        return new PlayCardData(playCard.getPlayerId(), playCard.getCardId());
    }

    public PlayCard toEntity(Map<String, Player> players, Map<Integer, Card> cards) {
        return new PlayCard(players.get(playerId), cards.get(cardId));
    }
}
