package tw.wally.dixit;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

import java.lang.reflect.Type;

/**
 * @author - wally55077@gmail.com
 */
public class DixitEventStompFrameHandler<T> implements StompFrameHandler {

    private final Class<T> tClass;
    private T t;

    public DixitEventStompFrameHandler(Class<T> tClass) {
        this.tClass = tClass;
    }

    @Override
    public Type getPayloadType(StompHeaders stompHeaders) {
        return this.tClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleFrame(StompHeaders stompHeaders, Object o) {
        if (o != null && o.getClass() == tClass) {
            this.t = (T) o;
        }
    }

    public T getPayload() {
        return this.t;
    }
}
