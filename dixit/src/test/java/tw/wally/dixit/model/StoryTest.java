package tw.wally.dixit.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class StoryTest extends AbstractDixitTest {

    @Test
    public void WhenTellStoryWithInvalidPhraseLength_ThenShouldFail() {
        var invalidPhrase = "?".repeat(Story.MAX_LENGTH_OF_PHRASE + 1);
        assertThrows(IllegalArgumentException.class, () -> new Story(invalidPhrase, FAKE_PLAYER, FAKE_CARD));
    }

}