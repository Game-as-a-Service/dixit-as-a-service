package tw.wally.dixit.brokers;

import tw.wally.dixit.EventBus;
import tw.wally.dixit.model.GameState;
import tw.wally.dixit.model.RoundState;

import static java.lang.String.format;
import static tw.wally.dixit.configs.WebSocketConfiguration.STOMP_ROOT_DESTINATION_PREFIX;

/**
 * @author - wally55077@gmail.com
 */
public abstract class AbstractDixitBroker implements EventBus {

    // topic/dixit/{dixitId}/gameStates/{gameState}/players/{playerId} GameState: STARTED | OVER
    protected String getDixitRoundStateEventTopic(String gameId, RoundState roundState, String playerId) {
        return format("%s/dixit/%s/roundStates/%s/players/%s", STOMP_ROOT_DESTINATION_PREFIX, gameId, roundState, playerId);
    }

    // topic/dixit/{dixitId}/roundStates/{roundState}/players/{playerId} RoundState: STORY_TELLING | CARD_PLAYING | PLAYER_GUESSING | SCORING | OVER
    protected String getDixitGameStateEventTopic(String gameId, GameState gameState, String playerId) {
        return format("%s/dixit/%s/gameStates/%s/players/%s", STOMP_ROOT_DESTINATION_PREFIX, gameId, gameState, playerId);
    }
}
