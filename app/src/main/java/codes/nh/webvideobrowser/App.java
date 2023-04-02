package codes.nh.webvideobrowser;

import android.app.Application;

import codes.nh.webvideobrowser.fragments.cast.CastManager;
import codes.nh.webvideobrowser.utils.AppUtils;
import codes.nh.webvideobrowser.utils.Async;
import codes.nh.webvideobrowser.utils.ImageUtils;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AppUtils.log("APP STARTED");

        Async.startTimeoutScheduler();

        ImageUtils.initializeBitmapCache();
    }

}
