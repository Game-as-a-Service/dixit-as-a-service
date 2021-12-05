package tw.wally.dixit.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.model.GameState;
import tw.wally.dixit.model.Player;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static java.util.function.Function.identity;
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
    private final Collection<CardData> deck;
    private final List<PlayerData> players;
    private GameState gameState;
    private int numberOfRounds;
    private final RoundData round;
    private Collection<PlayerData> winners;

    public static DixitData toData(Dixit dixit) {
        var storyteller = PlayerData.toData(dixit.getCurrentStoryteller());
        var guessers = mapToList(dixit.getCurrentGuessers(), PlayerData::toData);
        var round = RoundData.toData(dixit.getRound());
        round.setStoryteller(storyteller);
        round.setGuessers(guessers);
        return DixitData.builder()
                .id(dixit.getId())
                .victoryCondition(VictoryConditionData.toData(dixit.getVictoryCondition()))
                .deck(mapToList(dixit.getDeck(), CardData::toData))
                .players(mapToList(dixit.getPlayers(), PlayerData::toData))
                .gameState(dixit.getGameState())
                .numberOfRounds(dixit.getNumberOfRounds())
                .round(round)
                .winners(mapToList(dixit.getWinners(), PlayerData::toData))
                .build();
    }

    public Dixit toEntity() {
        var players = mapToList(this.players, PlayerData::toEntity);
        return Dixit.builder()
                .id(id)
                .victoryCondition(victoryCondition.toEntity())
                .deck(new LinkedList<>(mapToList(deck, CardData::toEntity)))
                .players(players)
                .gameState(gameState)
                .numberOfRounds(numberOfRounds)
                .round(round.toEntity(toMap(players, Player::getId, identity())))
                .winners(mapToList(winners, PlayerData::toEntity))
                .build();
    }

}
