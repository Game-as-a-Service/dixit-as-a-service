package tw.wally.dixit.views;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tw.wally.dixit.model.Color;
import tw.wally.dixit.model.Player;

import java.util.Collection;

import static tw.wally.dixit.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 */
@NoArgsConstructor
@AllArgsConstructor
public class PlayerView {
    public String id;
    public String name;
    public Color color;
    public Collection<CardView> handCards;
    public int score;

    public Player toEntity() {
        return new Player(id, name, color, mapToList(handCards, CardView::toEntity), score);
    }

    public static PlayerView toViewModel(Player player) {
        var handCards = mapToList(player.getHandCards(), CardView::toViewModel);
        return new PlayerView(player.getId(), player.getName(), player.getColor(), handCards, player.getScore());
    }
}
