package tw.wally.dixit.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tw.wally.dixit.model.Card;
import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.model.GameState;
import tw.wally.dixit.model.Player;

import java.util.*;

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
    private final VictoryConditionData victoryCondition;
    private final Collection<Integer> deckCardId;
    private final List<PlayerData> players;
    private final RoundData round;
    private GameState gameState;
    private int numberOfRounds;
    private Collection<String> winnerIds;

    public static DixitData toData(Dixit dixit) {
        return DixitData.builder()
                .id(dixit.getId())
                .victoryCondition(VictoryConditionData.toData(dixit.getVictoryCondition()))
                .deckCardId(mapToList(dixit.getDeck(), Card::getId))
                .players(mapToList(dixit.getPlayers(), PlayerData::toData))
                .gameState(dixit.getGameState())
                .numberOfRounds(dixit.getNumberOfRounds())
                .round(RoundData.toData(dixit.getRound()))
                .winnerIds(mapToList(dixit.getWinners(), Player::getId))
                .build();
    }

    public Dixit toEntity(Map<Integer, Card> cards) {
        var players = toMap(this.players, PlayerData::getId, player -> player.toEntity(cards));
        return Dixit.builder()
                .id(id)
                .gameState(gameState)
                .victoryCondition(victoryCondition.toEntity())
                .deck(new LinkedList<>(mapToList(deckCardId, cards::get)))
                .players(new ArrayList<>(players.values()))
                .numberOfRounds(numberOfRounds)
                .round(round.toEntity(players, cards))
                .winners(mapToList(winnerIds, players::get))
                .build();
    }
}
