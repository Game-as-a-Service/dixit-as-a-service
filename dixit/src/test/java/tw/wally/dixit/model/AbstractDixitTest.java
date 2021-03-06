package tw.wally.dixit.model;

import java.util.List;

import static java.lang.Integer.MAX_VALUE;
import static tw.wally.dixit.utils.StreamUtils.generate;

/**
 * @author - wally55077@gmail.com
 */
public class AbstractDixitTest {

    protected static final String DIXIT_PLAYER = "dixitPlayer";
    protected static final String DIXIT_PHRASE = "dixitPhrase";
    protected static final String CARD_IMAGE = "cardImage";
    protected static final Player FAKE_PLAYER = new Player(DIXIT_PLAYER + "Id", DIXIT_PLAYER);
    protected static final Card FAKE_CARD = new Card(MAX_VALUE, CARD_IMAGE);

    protected List<Player> generatePlayers(int numberOfPlayers) {
        return generate(numberOfPlayers, number -> new Player("Id:" + number, DIXIT_PLAYER + number));
    }

    protected List<Card> generateCards(int numberOfCards) {
        return generate(numberOfCards, number -> new Card(number, CARD_IMAGE + number));
    }
}
