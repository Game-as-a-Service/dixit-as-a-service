package tw.wally.dixit.utils;

import tw.wally.dixit.model.Card;
import tw.wally.dixit.repositories.CardRepository;

import javax.inject.Named;
import java.util.List;

import static tw.wally.dixit.utils.StreamUtils.generate;

/**
 * @author - wally55077@gmail.com
 */
@Named
public class FakeCardRepository implements CardRepository {

    private static final String CARD_IMAGE = "cardImage";
    private static final int DEFAULT_CARD_SIZE = 36;
    private final List<Card> cards;

    public FakeCardRepository() {
        this.cards = generate(DEFAULT_CARD_SIZE, number -> new Card(number, CARD_IMAGE + number));
    }

    @Override
    public List<Card> findAll() {
        return cards;
    }
}
