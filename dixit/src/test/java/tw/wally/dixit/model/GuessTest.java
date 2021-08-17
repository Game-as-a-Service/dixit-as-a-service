package tw.wally.dixit.model;

import org.junit.jupiter.api.Test;
import tw.wally.dixit.exceptions.InvalidGameOperationException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class GuessTest extends AbstractDixitTest {

    @Test
    public void WhenGuesserGuessesHisCard_ThenShouldFail() {
        var playCard = new PlayCard(FAKE_PLAYER, FAKE_CARD);
        assertThrows(InvalidGameOperationException.class, () -> new Guess(FAKE_PLAYER, playCard));
    }

}