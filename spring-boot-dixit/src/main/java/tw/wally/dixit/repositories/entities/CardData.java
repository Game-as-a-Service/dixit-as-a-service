package tw.wally.dixit.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tw.wally.dixit.model.Card;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@AllArgsConstructor
public class CardData {
    private final int id;
    private final String image;

    public static CardData toData(Card card) {
        return new CardData(card.getId(), card.getImage());
    }

    public Card toEntity() {
        return new Card(id, image);
    }
}
