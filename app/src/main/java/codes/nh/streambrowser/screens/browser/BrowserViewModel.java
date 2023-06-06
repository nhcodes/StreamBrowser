package codes.nh.streambrowser.screens.browser;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import codes.nh.streambrowser.database.AppDatabase;
import codes.nh.streambrowser.database.BrowserHistoryDao;
import codes.nh.streambrowser.screens.settings.SettingsManager;
import codes.nh.streambrowser.screens.stream.Stream;
import codes.nh.streambrowser.utils.AppUtils;
import codes.nh.streambrowser.utils.async.Async;

public class BrowserViewModel extends AndroidViewModel {

    private final BrowserHistoryDao historyDao;

    private final LiveData<List<BrowserDestination>> destinationList;

    public BrowserViewModel(@NonNull Application application) {
        super(application);
        AppUtils.log("init BrowserViewModel");

        AppDatabase database = AppDatabase.getInstance(application);
        historyDao = database.getBrowserHistoryDao();
        destinationList = historyDao.getAll();

        clearDestinations(20, count -> {
            AppUtils.log("deleted " + count + " BrowserDestinations");
        });
    }

    //settings

    private final SettingsManager settingsManager = new SettingsManager(getApplication());

    public boolean getBlockRedirects() {
        return settingsManager.getBlockRedirects();
    }

    //streams

    private final List<Stream> rawStreams = new ArrayList<>();
    private final MutableLiveData<List<Stream>> streams = new MutableLiveData<>(rawStreams);

    public void addStream(Stream stream) {
        Stream duplicate = getStream(stream);
        if (duplicate != null) {
            rawStreams.remove(duplicate);
        }
        rawStreams.add(stream);
        notifyUpdate();
    }

    public void clearStreams() {
        rawStreams.clear();
        notifyUpdate();
    }

    private void notifyUpdate() {
        streams.setValue(rawStreams);
    }

    private Stream getStream(Stream stream) {
        return rawStreams.stream().filter(s -> s.equals(stream)).findFirst().orElse(null);
    }

    public MutableLiveData<List<Stream>> getStreams() {
        return streams;
    }

    //request load url

    private final MutableLiveData<BrowserRequest> requestLoadUrl = new MutableLiveData<>();

    public MutableLiveData<BrowserRequest> getRequestLoadUrl() {
        return requestLoadUrl;
    }

    public void setRequestLoadUrl(BrowserRequest request) {
        requestLoadUrl.setValue(request);
    }

    //request desktop mode change

    private final MutableLiveData<Boolean> desktopMode = new MutableLiveData<>(settingsManager.getDesktopMode());

    public MutableLiveData<Boolean> getDesktopMode() {
        return desktopMode;
    }

    public void setDesktopMode(boolean enabled) {
        desktopMode.setValue(enabled);
    }

    //browser history

    public LiveData<List<BrowserDestination>> getDestinationList() {
        return destinationList;
    }

    public BrowserDestination getDestination(int index) {
        List<BrowserDestination> destinations = getDestinationList().getValue();
        return destinations.size() > index ? destinations.get(index) : null;
    }

    public boolean goBack() {
        BrowserDestination previousDestination = getDestination(1);
        if (previousDestination == null) return false;
        goBack(previousDestination);
        return true;
    }

    public void goBack(BrowserDestination destination) {
        deleteDestinationsAfter(destination, success -> {
        });
        BrowserRequest browserRequest = new BrowserRequest(destination.getUrl());
        setRequestLoadUrl(browserRequest);
    }

    //browser history database

    public void addDestination(BrowserDestination destination, Consumer<Long> callback) {
        Async.execute(() -> historyDao.insert(destination), callback);
    }

    private void deleteDestinationsAfter(BrowserDestination destination, Consumer<Integer> callback) {
        Async.execute(() -> historyDao.deleteAfter(destination.getTime()), callback);
    }

    private void clearDestinations(int keepCount, Consumer<Integer> callback) {
        Async.execute(() -> historyDao.clear(keepCount), callback);
    }

}