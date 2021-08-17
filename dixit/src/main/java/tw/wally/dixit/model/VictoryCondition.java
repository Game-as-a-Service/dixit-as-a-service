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

    private static final Set<Integer> WINNING_SCORE_RANGE = of(25, 30, 35);
    private final int winningScore;

    public VictoryCondition(int winningScore) {
        validateWinningScore(winningScore);
        this.winningScore = winningScore;
    }

    public boolean isWinning(Player player) {
        return player.getScore() >= winningScore;
    }

    private void validateWinningScore(int winningScore) {
        if (!WINNING_SCORE_RANGE.contains(winningScore)) {
            throw new InvalidGameOperationException(format("Winning score should be %s", WINNING_SCORE_RANGE));
        }
    }

}
