package tw.wally.dixit.usecases;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.model.VictoryCondition;
import tw.wally.dixit.repositories.CardRepository;
import tw.wally.dixit.repositories.DixitRepository;

import javax.inject.Named;
import java.util.Collection;

import static tw.wally.dixit.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class CreateDixitUseCase {
    private final DixitRepository dixitRepository;
    private final CardRepository cardRepository;

    public void execute(Request request) {
        Dixit dixit = dixit(request);

        players(request).forEach(dixit::join);
        dixit.start();

        dixit = dixitRepository.save(dixit);
        mayPublishEvents(dixit);
    }

    public Dixit dixit(Request request) {
        var cards = cardRepository.findAll();
        var game = request.game;
        return new Dixit(game.id, game.gameSetting.toVictoryCondition(), cards);
    }

    public Collection<tw.wally.dixit.model.Player> players(Request request) {
        var game = request.game;
        var players = mapToList(game.players, Player::toPlayer);
        players.add(game.host.toPlayer());
        return players;
    }

    // TODO: 發佈事件 開始遊戲、遊戲發牌、、新回合說故事
    private void mayPublishEvents(Dixit dixit) {

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public String roomId;
        public Game game;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Game {
        public String id;
        public Player host;
        public Collection<Player> players;
        public GameSetting gameSetting;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Player {
        public String id;
        public String name;

        private tw.wally.dixit.model.Player toPlayer() {
            return new tw.wally.dixit.model.Player(id, name);
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameSetting {
        public int winningScore;

        private VictoryCondition toVictoryCondition() {
            return new VictoryCondition(winningScore);
        }
    }
}
