package codes.nh.streambrowser.utils.async;

import android.os.Looper;

import com.google.android.gms.common.util.concurrent.HandlerExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Async {

    //todo exception handling

    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    private static final HandlerExecutor THREAD_MAIN = new HandlerExecutor(Looper.getMainLooper());

    public static <T> void execute(Supplier<T> async, Consumer<T> sync, long timeoutMs) {
        CompletableFuture<?> future = CompletableFuture.supplyAsync(async, THREAD_POOL).thenAcceptAsync(sync, THREAD_MAIN);
        if (timeoutMs > 0) {
            TimeoutHandler.add(future, timeoutMs);
        }
    }

    public static <T> void execute(Supplier<T> async, Consumer<T> sync) {
        execute(async, sync, 0);
    }

    public static void execute(Runnable async, long timeoutMs) {
        CompletableFuture<?> future = CompletableFuture.runAsync(async, THREAD_POOL);
        if (timeoutMs > 0) {
            TimeoutHandler.add(future, timeoutMs);
        }
    }

    public static void execute(Runnable async) {
        execute(async, 0);
    }


    public static void executeOnMainThread(Runnable async, long timeoutMs) {
        CompletableFuture<?> future = CompletableFuture.runAsync(async, THREAD_MAIN);
        if (timeoutMs > 0) {
            TimeoutHandler.add(future, timeoutMs);
        }
    }

    public static void executeOnMainThread(Runnable async) {
        executeOnMainThread(async, 0);
    }

}
