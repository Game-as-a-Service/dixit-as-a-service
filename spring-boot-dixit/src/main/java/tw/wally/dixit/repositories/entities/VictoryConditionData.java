package tw.wally.dixit.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tw.wally.dixit.model.VictoryCondition;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@AllArgsConstructor
public class VictoryConditionData {
    public int winningScore;

    public static VictoryConditionData toData(VictoryCondition victoryCondition) {
        return new VictoryConditionData(victoryCondition.getWinningScore());
    }

    public VictoryCondition toEntity() {
        return new VictoryCondition(winningScore);
    }
}
