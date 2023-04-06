package codes.nh.webvideobrowser.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TimeoutHandler {

    private static final long TIMEOUT_SCHEDULER_INTERVAL = 1000L;

    private static ScheduledFuture<?> TIMEOUT_SCHEDULER;

    private static final List<Execution> EXECUTIONS = new ArrayList<>(); //todo thread safe, maybe queue

    public static void startTimeoutScheduler() {
        if (TIMEOUT_SCHEDULER != null) {
            return;
        }
        AppUtils.log("startTimeoutScheduler");
        TIMEOUT_SCHEDULER = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            int count = 0;
            long time = System.currentTimeMillis();
            Iterator<Execution> iterator = EXECUTIONS.iterator();
            while (iterator.hasNext()) {
                Execution currentExecution = iterator.next();
                if (time <= currentExecution.timeout) {
                    continue;
                }
                if (!currentExecution.future.isDone()) {
                    boolean cancelled = currentExecution.future.cancel(true);
                    count++;
                }
                iterator.remove();
            }
            if (count > 0) {
                AppUtils.log("timeout " + count + " tasks");
            }
        }, 0L, TIMEOUT_SCHEDULER_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public static void add(Future<?> future, long timeoutMs) {
        Execution execution = new Execution();
        execution.timeout = System.currentTimeMillis() + timeoutMs;
        execution.future = future;
        EXECUTIONS.add(execution);
    }

    private static class Execution {
        Future<?> future;
        Long timeout;
    }
}
