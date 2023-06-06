package codes.nh.streambrowser.screens.stream;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import codes.nh.streambrowser.utils.AppUtils;

public class StreamViewModel extends AndroidViewModel {

    public StreamViewModel(Application application) {
        super(application);
        AppUtils.log("init StreamViewModel");
    }

    //request stream

    private final MutableLiveData<StreamRequest> streamRequest = new MutableLiveData<>();

    public MutableLiveData<StreamRequest> getStreamRequest() {
        return streamRequest;
    }

    public void play(StreamRequest streamRequest) {
        this.streamRequest.setValue(streamRequest);
    }

    //info stream

    private Stream infoStream = null;

    public void setInfoStream(Stream stream) {
        infoStream = stream;
    }

    public Stream getInfoStream() {
        return infoStream;
    }

}
