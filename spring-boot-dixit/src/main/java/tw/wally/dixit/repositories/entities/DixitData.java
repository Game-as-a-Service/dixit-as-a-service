package tw.wally.dixit.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.model.GameState;
import tw.wally.dixit.model.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static tw.wally.dixit.repositories.MongoDixitDAO.DIXIT;
import static tw.wally.dixit.utils.StreamUtils.mapToList;
import static tw.wally.dixit.utils.StreamUtils.toMap;

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
    private GameState gameState;
    private final VictoryConditionData victoryCondition;
    private final Collection<CardData> deck;
    private final List<PlayerData> players;
    private int numberOfRounds;
    private final RoundData round;
    private Collection<String> winnerIds;

    public static DixitData toData(Dixit dixit) {
        return DixitData.builder()
                .id(dixit.getId())
                .victoryCondition(VictoryConditionData.toData(dixit.getVictoryCondition()))
                .deck(mapToList(dixit.getDeck(), CardData::toData))
                .players(mapToList(dixit.getPlayers(), PlayerData::toData))
                .gameState(dixit.getGameState())
                .numberOfRounds(dixit.getNumberOfRounds())
                .round(RoundData.toData(dixit.getRound()))
                .winnerIds(mapToList(dixit.getWinners(), Player::getId))
                .build();
    }

    public Dixit toEntity() {
        var players = toMap(this.players, PlayerData::getId, PlayerData::toEntity);
        return Dixit.builder()
                .id(id)
                .gameState(gameState)
                .victoryCondition(victoryCondition.toEntity())
                .deck(new LinkedList<>(mapToList(deck, CardData::toEntity)))
                .players(new ArrayList<>(players.values()))
                .numberOfRounds(numberOfRounds)
                .round(round.toEntity(players))
                .winners(mapToList(winnerIds, players::get))
                .build();
    }
}
