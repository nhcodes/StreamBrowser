package codes.nh.webvideobrowser.fragments.player;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import codes.nh.webvideobrowser.CastHandler;
import codes.nh.webvideobrowser.fragments.stream.Stream;
import codes.nh.webvideobrowser.utils.AppUtils;

public class PlayerViewModel extends AndroidViewModel {

    private final CastHandler castHandler;

    public PlayerViewModel(Application application) {
        super(application);
        AppUtils.log("init PlayerViewModel");

        castHandler = new CastHandler(application);
    }

    // play


    public CastHandler getCastHandler() {
        return castHandler;
    }

    public void play(Context context, Stream stream) {
        castHandler.start(context, stream);
    }

    private final MutableLiveData<Stream> currentStream = new MutableLiveData<>();

    public MutableLiveData<Stream> getCurrentStream() {
        return currentStream;
    }

    public void setCurrentStream(Stream stream) {
        currentStream.setValue(stream);
    }

}
