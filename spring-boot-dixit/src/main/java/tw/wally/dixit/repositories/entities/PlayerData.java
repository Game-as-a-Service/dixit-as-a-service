package tw.wally.dixit.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tw.wally.dixit.model.Player;

import java.util.Collection;

import static tw.wally.dixit.utils.StreamUtils.mapToList;
import static tw.wally.dixit.utils.StreamUtils.toMap;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@AllArgsConstructor
public class PlayerData {
    private final String id;
    private final String name;
    private final Collection<CardData> handCards;
    private int score;

    public static PlayerData toData(Player player) {
        var handCards = mapToList(player.getHandCards(), CardData::toData);
        return new PlayerData(player.getId(), player.getName(), handCards, player.getScore());
    }

    public Player toEntity() {
        var handCards = toMap(this.handCards, CardData::getId, CardData::toEntity);
        return new Player(id, name, handCards, score);
    }

}
