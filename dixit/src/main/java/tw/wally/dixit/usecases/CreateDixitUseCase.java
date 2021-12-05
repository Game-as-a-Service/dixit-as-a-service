package tw.wally.dixit.usecases;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.events.EventBus;
import tw.wally.dixit.events.gamestate.DixitGameStartedEvent;
import tw.wally.dixit.events.roundstate.DixitRoundStoryTellingEvent;
import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.model.GameState;
import tw.wally.dixit.model.RoundState;
import tw.wally.dixit.model.VictoryCondition;
import tw.wally.dixit.repositories.CardRepository;
import tw.wally.dixit.repositories.DixitRepository;

import javax.inject.Named;
import java.util.Collection;
import java.util.LinkedList;

import static tw.wally.dixit.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 */
@Named
public class CreateDixitUseCase extends AbstractDixitUseCase {
    private final CardRepository cardRepository;

    public CreateDixitUseCase(DixitRepository dixitRepository, CardRepository cardRepository, EventBus eventBus) {
        super(dixitRepository, eventBus);
        this.cardRepository = cardRepository;
    }

    public void execute(Request request) {
        Dixit dixit = dixit(request);

        players(request).forEach(dixit::join);
        dixit.start();

        publishDixitGameStartedAndDixitRoundStoryTellingEvents(dixit);
        dixitRepository.save(dixit);
    }

    public Dixit dixit(Request request) {
        var cards = cardRepository.findAll();
        var game = request.game;
        return new Dixit(game.id, game.gameSetting.toVictoryCondition(), cards);
    }

    public Collection<tw.wally.dixit.model.Player> players(Request request) {
        var game = request.game;
        var players = new LinkedList<>(game.players);
        players.addFirst(game.host);
        return mapToList(players, Player::toPlayer);
    }

    private void publishDixitGameStartedAndDixitRoundStoryTellingEvents(Dixit dixit) {
        var players = dixit.getPlayers();
        String dixitId = dixit.getId();
        int rounds = dixit.getNumberOfRounds();
        GameState gameState = dixit.getGameState();
        var dixitGameStartedEvents = mapToList(players, player -> new DixitGameStartedEvent(dixitId, rounds, player.getId(), gameState, players));
        eventBus.publish(dixitGameStartedEvents);

        publishDixitRoundStoryTellingEvents(dixit);
    }

    private void publishDixitRoundStoryTellingEvents(Dixit dixit) {
        var players = dixit.getPlayers();
        String dixitId = dixit.getId();
        int rounds = dixit.getNumberOfRounds();
        RoundState roundState = dixit.getCurrentRoundState();
        var storyteller = dixit.getCurrentStoryteller();
        var dixitRoundStoryTellingEvents = mapToList(players, player -> new DixitRoundStoryTellingEvent(dixitId, rounds, roundState, storyteller, player));
        eventBus.publish(dixitRoundStoryTellingEvents);
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
