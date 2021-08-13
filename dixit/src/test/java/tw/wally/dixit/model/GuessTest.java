package tw.wally.dixit.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class GuessTest extends AbstractDixitTest {

    @Test
    public void WhenPlayerGuessHimselfCard_ThenShouldFail() {
        var playCard = new PlayCard(FAKE_PLAYER, FAKE_CARD);
        assertThrows(IllegalArgumentException.class, () -> new Guess(FAKE_PLAYER, playCard));
    }

}