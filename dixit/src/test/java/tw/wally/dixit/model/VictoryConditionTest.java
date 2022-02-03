package tw.wally.dixit.model;

import org.junit.jupiter.api.Test;
import tw.wally.dixit.exceptions.InvalidGameOperationException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VictoryConditionTest {

    @Test
    public void WhenCreateVictoryConditionWithWinningScoreIs30_ThenShouldSuccess() {
        assertDoesNotThrow(() -> new VictoryCondition(30));
    }

    @Test
    public void WhenCreateVictoryConditionWithInvalidWinningScore_ThenShouldFail() {
        assertThrows(InvalidGameOperationException.class, () -> new VictoryCondition(22));
        assertThrows(InvalidGameOperationException.class, () -> new VictoryCondition(27));
        assertThrows(InvalidGameOperationException.class, () -> new VictoryCondition(32));
        assertThrows(InvalidGameOperationException.class, () -> new VictoryCondition(37));
    }
}