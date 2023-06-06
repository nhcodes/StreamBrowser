package codes.nh.streambrowser;

import android.app.Application;

import codes.nh.streambrowser.utils.AppUtils;
import codes.nh.streambrowser.utils.ImageUtils;
import codes.nh.streambrowser.utils.async.TimeoutHandler;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AppUtils.log("APP STARTED");

        TimeoutHandler.startTimeoutScheduler();

        ImageUtils.initializeBitmapCache();
    }

}
