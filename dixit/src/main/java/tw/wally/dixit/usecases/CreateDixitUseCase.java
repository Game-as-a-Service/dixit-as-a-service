package tw.wally.dixit.usecases;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.events.EventBus;
import tw.wally.dixit.events.gamestate.DixitGameStartedEvent;
import tw.wally.dixit.events.roundstate.DixitRoundStoryToldEvent;
import tw.wally.dixit.model.*;
import tw.wally.dixit.repositories.CardRepository;
import tw.wally.dixit.repositories.DixitRepository;

import javax.inject.Named;
import java.util.Collection;

import static tw.wally.dixit.utils.StreamUtils.mapToList;
import static tw.wally.dixit.utils.StreamUtils.toMap;

/**
 * @author - wally55077@gmail.com
 */
@Named
public class CreateDixitUseCase extends AbstractDixitUseCase {
    private final CardRepository cardRepository;

    public CreateDixitUseCase(DixitRepository dixitRepository,
                              CardRepository cardRepository,
                              EventBus eventBus) {
        super(dixitRepository, eventBus);
        this.cardRepository = cardRepository;
    }

    public void execute(Request request) {
        Dixit dixit = dixit(request);

        request.players.forEach(dixit::join);
        dixit.start();

        publishDixitGameStartedAndDixitRoundStoryToldEvents(dixit);
        dixitRepository.save(dixit);
    }

    public Dixit dixit(Request request) {
        var options = toMap(request.options, Option::getName, Option::getValue);
        var cards = cardRepository.findAll();
        return new Dixit(request.gameId, new VictoryCondition(options.get("winningScore")), cards);
    }

    private void publishDixitGameStartedAndDixitRoundStoryToldEvents(Dixit dixit) {
        var players = dixit.getPlayers();
        String dixitId = dixit.getId();
        int rounds = dixit.getNumberOfRounds();
        GameState gameState = dixit.getGameState();
        var dixitGameStartedEvents = mapToList(players, player -> new DixitGameStartedEvent(dixitId, rounds, player.getId(), gameState, players));
        eventBus.publish(dixitGameStartedEvents);

        publishDixitRoundStoryToldEvents(dixit);
    }

    private void publishDixitRoundStoryToldEvents(Dixit dixit) {
        var players = dixit.getPlayers();
        String dixitId = dixit.getId();
        int rounds = dixit.getNumberOfRounds();
        RoundState roundState = dixit.getCurrentRoundState();
        var storyteller = dixit.getCurrentStoryteller();
        var dixitRoundStoryToldEvents = mapToList(players, player -> new DixitRoundStoryToldEvent(dixitId, rounds, roundState, storyteller, player));
        eventBus.publish(dixitRoundStoryToldEvents);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public String roomId;
        public String gameId;
        public String hostId;
        public Collection<Player> players;
        public Collection<Option> options;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {
        public String name;
        public int value;
    }
}
