package tw.wally.dixit.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static java.util.Objects.hash;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    private int id;
    private String image;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Card card = (Card) o;
        return id == card.id;
    }

    @Override
    public int hashCode() {
        return hash(id);
    }
}
