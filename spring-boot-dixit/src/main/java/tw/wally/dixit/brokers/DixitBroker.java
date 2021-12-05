package tw.wally.dixit.brokers;

import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tw.wally.dixit.events.DixitGameEvent;
import tw.wally.dixit.events.DixitRoundEvent;

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
        if (event instanceof DixitGameEvent) {
            var dixitGameEvent = ((DixitGameEvent) event);
            topic = generateDixitGameStateEventTopic(gameId, dixitGameEvent.getGameState(), playerId);
        } else if (event instanceof DixitRoundEvent) {
            var dixitRoundEvent = ((DixitRoundEvent) event);
            topic = generateDixitRoundStateEventTopic(gameId, dixitRoundEvent.getRoundState(), playerId);
        } else {
            throw new RuntimeException("Do not forget to add the event's condition");
        }
        simpMessagingTemplate.convertAndSend(topic, event);
    }

}
