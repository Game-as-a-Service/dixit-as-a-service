package tw.wally.dixit.brokers;

import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tw.wally.dixit.events.*;

/**
 * @author - wally55077@gmail.com
 */
@Component
@AllArgsConstructor
public class DixitBroker extends AbstractDixitBroker {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void publish(Event event) {
        String topic;
        String gameId = event.getGameId();
        String playerId = event.getPlayerId();
        if (event instanceof DixitGameStartedEvent) {
            var dixitGameStartedEvent = ((DixitGameStartedEvent) event);
            topic = getDixitGameStateEventTopic(gameId, dixitGameStartedEvent.getGameState(), playerId);
        } else if (event instanceof DixitRoundStoryTellingEvent) {
            var dixitRoundStoryTellingEvent = ((DixitRoundStoryTellingEvent) event);
            topic = getDixitRoundStateEventTopic(gameId, dixitRoundStoryTellingEvent.getRoundState(), playerId);
        } else if (event instanceof DixitRoundCardPlayingEvent) {
            var dixitRoundCardPlayingEvent = ((DixitRoundCardPlayingEvent) event);
            topic = getDixitRoundStateEventTopic(gameId, dixitRoundCardPlayingEvent.getRoundState(), playerId);
        } else if (event instanceof DixitRoundPlayerGuessingEvent) {
            var dixitRoundPlayerGuessingEvent = ((DixitRoundPlayerGuessingEvent) event);
            topic = getDixitRoundStateEventTopic(gameId, dixitRoundPlayerGuessingEvent.getRoundState(), playerId);
        } else if (event instanceof DixitRoundScoringEvent) {
            var dixitRoundScoringEvent = ((DixitRoundScoringEvent) event);
            topic = getDixitRoundStateEventTopic(gameId, dixitRoundScoringEvent.getRoundState(), playerId);
        } else if (event instanceof DixitRoundOverEvent) {
            var dixitRoundOverEvent = ((DixitRoundOverEvent) event);
            topic = getDixitRoundStateEventTopic(gameId, dixitRoundOverEvent.getRoundState(), playerId);
        } else if (event instanceof DixitGameOverEvent) {
            var dixitGameOverEvent = ((DixitGameOverEvent) event);
            topic = getDixitGameStateEventTopic(gameId, dixitGameOverEvent.getGameState(), playerId);
        } else {
            throw new RuntimeException("Do not forget to add the event's condition");
        }
        simpMessagingTemplate.convertAndSend(topic, event);
    }

}
