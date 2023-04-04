package codes.nh.webvideobrowser.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Async {

    //

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    public static <T> void execute(Task task, long timeoutMs) {
        Execution execution = new Execution();
        execution.timeout = System.currentTimeMillis() + timeoutMs;
        execution.future = (task instanceof ResultTask) ? startResultTask((ResultTask<T>) task) : startNoResultTask((NoResultTask) task);
        EXECUTIONS.add(execution);
    }

    public static void execute(Task task) {
        execute(task, TIMEOUT_DEFAULT);
    }

    public static void executeOnMainThread(Runnable runnable) {
        HANDLER.post(runnable);
    }

    private static <T> Future<?> startResultTask(ResultTask<T> task) {
        return EXECUTOR_SERVICE.submit(() -> {
            T result = task.doAsync();
            HANDLER.post(() -> {
                task.doSync(result);
            });
        });
    }

    private static Future<?> startNoResultTask(NoResultTask task) {
        return EXECUTOR_SERVICE.submit(() -> {
            task.doAsync();
        });
    }

    private interface Task {
    }

    public interface NoResultTask extends Task {
        void doAsync();
    }

    public interface ResultTask<T> extends Task {
        T doAsync();

        void doSync(T t);
    }

    //timeout scheduler

    private static final long TIMEOUT_DEFAULT = 1000L;

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
                if (time > currentExecution.timeout) {
                    if (!currentExecution.future.isDone()) {
                        boolean cancelled = currentExecution.future.cancel(true);
                        count++;
                    }
                    iterator.remove();
                }
            }
            if (count > 0) {
                AppUtils.log("timeout " + count + " tasks");
            }
        }, 0L, TIMEOUT_SCHEDULER_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private static class Execution {
        Future<?> future;
        long timeout;
    }
}
