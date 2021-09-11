package tw.wally.dixit.usecases;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.EventBus;
import tw.wally.dixit.events.DixitGameStartedEvent;
import tw.wally.dixit.events.DixitRoundStoryTellingEvent;
import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.model.GameState;
import tw.wally.dixit.model.RoundState;
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
        mayPublishDixitGameStartedEvents(dixit);

        dixitRepository.save(dixit);
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

    private void mayPublishDixitGameStartedEvents(Dixit dixit) {
        GameState gameState = dixit.getGameState();
        if (GameState.STARTED == gameState) {
            String dixitId = dixit.getId();
            var players = dixit.getPlayers();
            var dixitGameStartedEvents = mapToList(players, player -> new DixitGameStartedEvent(dixitId, player.getId(), gameState, players));
            eventBus.publish(dixitGameStartedEvents);

            mayPublishDixitRoundStoryTellingEvent(dixit);
        }
    }

    private void mayPublishDixitRoundStoryTellingEvent(Dixit dixit) {
        String dixitId = dixit.getId();
        RoundState currentRoundState = dixit.getCurrentRoundState();
        if (RoundState.STORY_TELLING == currentRoundState) {
            var storyteller = dixit.getCurrentStoryteller();
            var dixitRoundStoryTellingEvent = new DixitRoundStoryTellingEvent(dixitId, storyteller.getId(), currentRoundState, storyteller.getHandCards());
            eventBus.publish(dixitRoundStoryTellingEvent);
        }
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
