package tw.wally.dixit.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.model.GameState;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static tw.wally.dixit.repositories.MongoDixitDAO.DIXIT;
import static tw.wally.dixit.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@Builder
@Document(DIXIT)
@AllArgsConstructor
public class DixitData {

    @Id
    private final String id;
    private final VictoryConditionData victoryCondition;
    private final LinkedList<CardData> deck;
    private final List<PlayerData> players;
    private final LinkedList<RoundData> rounds;
    private int currentStoryTellerPosition;
    private GameState gameState;
    private Collection<PlayerData> winners;

    public static DixitData toData(Dixit dixit) {
        return DixitData.builder()
                .id(dixit.getId())
                .victoryCondition(VictoryConditionData.toData(dixit.getVictoryCondition()))
                .deck(new LinkedList<>(mapToList(dixit.getDeck(), CardData::toData)))
                .players(mapToList(dixit.getPlayers(), PlayerData::toData))
                .rounds(new LinkedList<>(mapToList(dixit.getRounds(), RoundData::toData)))
                .currentStoryTellerPosition(dixit.getCurrentStoryTellerPosition())
                .gameState(dixit.getGameState())
                .winners(mapToList(dixit.getWinners(), PlayerData::toData))
                .build();
    }

    public Dixit toEntity() {
        return Dixit.builder()
                .id(id)
                .victoryCondition(victoryCondition.toEntity())
                .deck(new LinkedList<>(mapToList(deck, CardData::toEntity)))
                .players(mapToList(players, PlayerData::toEntity))
                .rounds(new LinkedList<>(mapToList(rounds, RoundData::toEntity)))
                .currentStoryTellerPosition(currentStoryTellerPosition)
                .gameState(gameState)
                .winners(mapToList(winners, PlayerData::toEntity))
                .build();
    }

}
