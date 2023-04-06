package codes.nh.webvideobrowser.fragments.history;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.function.Consumer;

import codes.nh.webvideobrowser.database.AppDatabase;
import codes.nh.webvideobrowser.database.StreamHistoryDao;
import codes.nh.webvideobrowser.fragments.stream.Stream;
import codes.nh.webvideobrowser.utils.AppUtils;
import codes.nh.webvideobrowser.utils.Async;

public class HistoryViewModel extends AndroidViewModel {

    private final StreamHistoryDao historyDao;

    private final LiveData<List<Stream>> historyList;

    public HistoryViewModel(Application application) {
        super(application);
        AppUtils.log("init HistoryViewModel");

        AppDatabase database = AppDatabase.getInstance(application);
        historyDao = database.getStreamHistoryDao();
        historyList = historyDao.getAll();
    }

    public LiveData<List<Stream>> getHistoryList() {
        return historyList;
    }

    public void getHistory(String streamUrl, Consumer<List<Stream>> callback) {
        Async.execute(
                () -> historyDao.getByUrl(streamUrl),
                (history) -> callback.accept(history)
        );
    }

    public void addHistory(Stream stream, Consumer<Boolean> callback) {
        Async.execute(
                () -> historyDao.insert(stream),
                (row) -> callback.accept(row != -1)
        );
    }

    public void updateHistory(String streamUrl, long startTime, Consumer<Boolean> callback) {
        Async.execute(
                () -> historyDao.update(streamUrl, startTime),
                (changed) -> callback.accept(changed > 0)
        );
    }

    public void removeHistory(Stream stream, Consumer<Boolean> callback) {
        Async.execute(
                () -> historyDao.delete(stream),
                (changed) -> callback.accept(changed > 0)
        );
    }

    public void clearHistory(Consumer<Boolean> callback) {
        Async.execute(
                () -> historyDao.deleteAll(),
                (changed) -> callback.accept(changed > 0)
        );
    }
}
