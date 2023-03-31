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
        Async.execute(new Async.ResultTask<List<Stream>>() {
            @Override
            public List<Stream> doAsync() {
                return historyDao.getByUrl(streamUrl);
            }

            @Override
            public void doSync(List<Stream> history) {
                callback.accept(history);
            }
        });
    }

    public void addHistory(Stream stream, Consumer<Boolean> callback) {
        Async.execute(new Async.ResultTask<Long>() {
            @Override
            public Long doAsync() {
                return historyDao.insert(stream);
            }

            @Override
            public void doSync(Long row) {
                callback.accept(row != -1);
            }
        });
    }

    public void updateHistory(String streamUrl, long startTime, Consumer<Boolean> callback) {
        Async.execute(new Async.ResultTask<Integer>() {
            @Override
            public Integer doAsync() {
                return historyDao.update(streamUrl, startTime);
            }

            @Override
            public void doSync(Integer changed) {
                callback.accept(changed > 0);
            }
        });
    }

    public void removeHistory(Stream stream, Consumer<Boolean> callback) {
        Async.execute(new Async.ResultTask<Integer>() {
            @Override
            public Integer doAsync() {
                return historyDao.delete(stream);
            }

            @Override
            public void doSync(Integer changed) {
                callback.accept(changed > 0);
            }
        });
    }

    public void clearHistory(Consumer<Boolean> callback) {
        Async.execute(new Async.ResultTask<Integer>() {
            @Override
            public Integer doAsync() {
                return historyDao.deleteAll();
            }

            @Override
            public void doSync(Integer changed) {
                callback.accept(changed > 0);
            }
        });
    }
}
