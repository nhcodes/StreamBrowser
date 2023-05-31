package codes.nh.webvideobrowser;

import android.app.Application;

import codes.nh.webvideobrowser.utils.AppUtils;
import codes.nh.webvideobrowser.utils.ImageUtils;
import codes.nh.webvideobrowser.utils.async.TimeoutHandler;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AppUtils.log("APP STARTED");

        TimeoutHandler.startTimeoutScheduler();

        ImageUtils.initializeBitmapCache();
    }

}
