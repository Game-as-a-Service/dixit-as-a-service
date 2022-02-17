package tw.wally.dixit.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tw.wally.dixit.model.Card;
import tw.wally.dixit.model.Color;
import tw.wally.dixit.model.Player;

import java.util.Collection;
import java.util.Map;

import static tw.wally.dixit.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@AllArgsConstructor
public class PlayerData {
    private final String id;
    private final String name;
    private Color color;
    private final Collection<Integer> handCardIds;
    private int score;

    public static PlayerData toData(Player player) {
        var handCardIds = mapToList(player.getHandCards(), Card::getId);
        return new PlayerData(player.getId(), player.getName(), player.getColor(), handCardIds, player.getScore());
    }

    public Player toEntity(Map<Integer, Card> cards) {
        var handCards = mapToList(handCardIds, cards::get);
        return new Player(id, name, color, handCards, score);
    }

}
