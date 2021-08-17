package tw.wally.dixit.usecases;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.model.Player;
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
        var cards = cardRepository.findAll();
        var game = request.game;
        var dixit = new Dixit(game.id, game.gameSetting.toVictoryCondition(), cards);
        dixit.join(game.host.toPlayer());
        mapToList(game.players, Gamer::toPlayer).forEach(dixit::join);
        dixit.start();
        dixitRepository.save(dixit);
        publishEvents(dixit);
    }

    // TODO: 發佈事件 開始遊戲、回合說故事
    private void publishEvents(Dixit dixit) {

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
        public Gamer host;
        public Collection<Gamer> players;
        public GameSetting gameSetting;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Gamer {
        public String id;
        public String name;

        private Player toPlayer() {
            return new Player(id, name);
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
