package codes.nh.webvideobrowser.proxy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import codes.nh.webvideobrowser.HomeActivity;
import codes.nh.webvideobrowser.R;
import codes.nh.webvideobrowser.utils.AppUtils;

public class ProxyService extends Service {

    public static void start(Context context) {
        AppUtils.log("ProxyService.start");
        Intent intent = new Intent(context, ProxyService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stop(Context context) {
        AppUtils.log("ProxyService.stop");
        Intent intent = new Intent(context, ProxyService.class);
        context.stopService(intent);
    }

    //

    @Override
    public void onCreate() {
        super.onCreate();
        AppUtils.log("ProxyService onCreate");

        initNotificationManager();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AppUtils.log("ProxyService onStartCommand");

        if (notification == null) {
            String notificationText = getNotificationText(/*System.currentTimeMillis()*/);
            notification = createNotification(notificationText);
        }

        startForeground(NOTIFICATION_ID, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        AppUtils.log("ProxyService onDestroy");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //notification

    private static final String CHANNEL_ID = "PROXY_SERVER";
    private static final String CHANNEL_NAME = "Proxy Server";

    private static final int NOTIFICATION_ID = 11;

    private Notification notification;

    private void initNotificationManager() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private String getNotificationText(/*long lastUpdate*/) {
        //String localAddress = proxyServer.getLocalAddress();
        String notificationText = "Proxy running";// @ :" + localAddress + " | "
                //+ "Last update: " + AppUtils.getTimeStringFromTimestamp(lastUpdate);
        return notificationText;
    }

    private Notification createNotification(String notificationText) {
        Intent startHomeActivityIntent = new Intent(getApplicationContext(), HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, startHomeActivityIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(notificationText)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.logo_icon);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
        }

        return notificationBuilder.build();
    }
}
