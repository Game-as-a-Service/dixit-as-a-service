package tw.wally.dixit.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;

import static java.util.List.of;

/**
 * @author - wally55077@gmail.com
 */
public interface EventBus {

    void publish(Event event);

    default void publish(Event... events) {
        publish(of(events));
    }

    default void publish(Collection<? extends Event> events) {
        events.forEach(this::publish);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    abstract class Event {
        private String gameId;
        private int rounds;
        private String playerId;
    }

}
