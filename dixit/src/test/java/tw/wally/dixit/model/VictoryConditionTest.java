package tw.wally.dixit.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VictoryConditionTest {

    @Test
    public void WhenCreateVictoryConditionWithWinningScoreIs30_ThenShouldSuccess() {
        assertDoesNotThrow(() -> new VictoryCondition(30));
    }

    @Test
    public void WhenCreateVictoryConditionWithInvalidWinningScore_ThenShouldFail() {
        assertThrows(IllegalArgumentException.class, () -> new VictoryCondition(22));
        assertThrows(IllegalArgumentException.class, () -> new VictoryCondition(27));
        assertThrows(IllegalArgumentException.class, () -> new VictoryCondition(32));
        assertThrows(IllegalArgumentException.class, () -> new VictoryCondition(37));
    }
}