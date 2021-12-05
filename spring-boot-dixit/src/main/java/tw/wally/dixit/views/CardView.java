package tw.wally.dixit.views;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tw.wally.dixit.model.Card;

/**
 * @author - wally55077@gmail.com
 */
@NoArgsConstructor
@AllArgsConstructor
public class CardView {
    public int id;
    public String image;

    public Card toEntity() {
        return new Card(id, image);
    }

    public static CardView toViewModel(Card card) {
        return new CardView(card.getId(), card.getImage());
    }
}
