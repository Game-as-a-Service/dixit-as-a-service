package tw.wally.dixit.utils;

import lombok.SneakyThrows;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

import java.lang.reflect.Type;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author - wally55077@gmail.com
 */
public class DixitEventStompFrameHandler<T> implements StompFrameHandler {

    private final Class<T> tClass;
    private final BlockingQueue<T> blockingQueue;

    public DixitEventStompFrameHandler(Class<T> tClass) {
        this.tClass = tClass;
        this.blockingQueue = new ArrayBlockingQueue<>(1);
    }

    @Override
    public Type getPayloadType(StompHeaders stompHeaders) {
        return this.tClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleFrame(StompHeaders stompHeaders, Object o) {
        this.blockingQueue.clear();
        if (o != null && o.getClass() == tClass) {
            this.blockingQueue.add((T) o);
        }
    }

    @SneakyThrows
    public T getPayload() {
        return this.blockingQueue.poll(1, SECONDS);
    }
}
