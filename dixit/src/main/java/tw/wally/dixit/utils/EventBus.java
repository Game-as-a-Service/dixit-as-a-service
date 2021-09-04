package tw.wally.dixit.utils;

/**
 * @author - wally55077@gmail.com
 */
public interface EventBus {

    void post(Event... events);

    interface Event {

    }
}
