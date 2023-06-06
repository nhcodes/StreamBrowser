package codes.nh.streambrowser.screens.history;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.function.Consumer;

import codes.nh.streambrowser.database.AppDatabase;
import codes.nh.streambrowser.database.StreamHistoryDao;
import codes.nh.streambrowser.screens.stream.Stream;
import codes.nh.streambrowser.utils.AppUtils;
import codes.nh.streambrowser.utils.async.Async;

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
