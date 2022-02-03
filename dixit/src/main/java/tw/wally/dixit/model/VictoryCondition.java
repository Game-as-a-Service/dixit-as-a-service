package tw.wally.dixit.model;

import lombok.Getter;
import tw.wally.dixit.exceptions.InvalidGameOperationException;

import java.util.Set;

import static java.lang.String.format;
import static java.util.Set.of;

/**
 * @author - wally55077@gmail.com
 */
@Getter
public class VictoryCondition {

    private static final Set<Integer> WINNING_GOAL_RANGE = of(25, 30, 35);
    private final int winningGoal;

    public VictoryCondition(int winningGoal) {
        validateWinningGoal(winningGoal);
        this.winningGoal = winningGoal;
    }

    public boolean isWinning(Player player) {
        return player.getScore() >= winningGoal;
    }

    private void validateWinningGoal(int winningGoal) {
        if (!WINNING_GOAL_RANGE.contains(winningGoal)) {
            throw new InvalidGameOperationException(format("Winning goal should be %s", WINNING_GOAL_RANGE));
        }
    }

}
