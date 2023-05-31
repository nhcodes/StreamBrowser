package codes.nh.webvideobrowser.utils.async;

import java.util.concurrent.Future;

public class Execution {

    private final Future<?> future;

    private final Long timeout;

    public Execution(Future<?> future, long timeout) {
        this.future = future;
        this.timeout = timeout;
    }

    public Future<?> getFuture() {
        return future;
    }

    public Long getTimeout() {
        return timeout;
    }

}