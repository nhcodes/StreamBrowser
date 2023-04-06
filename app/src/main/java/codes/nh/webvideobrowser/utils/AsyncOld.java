package codes.nh.webvideobrowser.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsyncOld {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private static final long TIMEOUT_DEFAULT = 1000L;

    public static <T> void execute(Task task, long timeoutMs) {
        Future<?> future = (task instanceof ResultTask) ? startResultTask((ResultTask<T>) task) : startNoResultTask((NoResultTask) task);
        TimeoutHandler.add(future, timeoutMs);
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
}
