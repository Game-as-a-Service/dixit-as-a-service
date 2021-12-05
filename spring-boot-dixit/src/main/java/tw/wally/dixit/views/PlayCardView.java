package tw.wally.dixit.views;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tw.wally.dixit.model.PlayCard;

/**
 * @author - wally55077@gmail.com
 */
@NoArgsConstructor
@AllArgsConstructor
public class PlayCardView {
    public PlayerView player;
    public CardView card;

    public PlayCard toEntity() {
        return new PlayCard(player.toEntity(), card.toEntity());
    }

    public static PlayCardView toViewModel(PlayCard playCard) {
        var player = PlayerView.toViewModel(playCard.getPlayer());
        var card = CardView.toViewModel(playCard.getCard());
        return new PlayCardView(player, card);
    }
}
