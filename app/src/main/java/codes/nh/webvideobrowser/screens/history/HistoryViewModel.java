package codes.nh.webvideobrowser.screens.history;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.function.Consumer;

import codes.nh.webvideobrowser.database.AppDatabase;
import codes.nh.webvideobrowser.database.StreamHistoryDao;
import codes.nh.webvideobrowser.screens.stream.Stream;
import codes.nh.webvideobrowser.utils.AppUtils;
import codes.nh.webvideobrowser.utils.async.Async;

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

    //stream history database

    public LiveData<List<Stream>> getHistoryList() {
        return historyList;
    }

    public void getHistory(String streamUrl, Consumer<List<Stream>> callback) {
        Async.execute(() -> historyDao.getByUrl(streamUrl), callback);
    }

    public void addHistory(Stream stream, Consumer<Long> callback) {
        Async.execute(() -> historyDao.insert(stream), callback);
    }

    public void clearHistory(Consumer<Integer> callback) {
        Async.execute(() -> historyDao.deleteAll(), callback);
    }

}
