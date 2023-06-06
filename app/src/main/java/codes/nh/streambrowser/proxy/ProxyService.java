package codes.nh.streambrowser.proxy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import codes.nh.streambrowser.R;
import codes.nh.streambrowser.screens.main.MainActivity;
import codes.nh.streambrowser.utils.AppUtils;

public class ProxyService extends Service {

    private final int proxyPort = 1111;

    private ProxyServer proxyServer;

    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppUtils.log("ProxyService onCreate");

        proxyServer = new ProxyServer(getApplicationContext(), proxyPort);
        proxyServer.start();

        initNotificationManager();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AppUtils.log("ProxyService onStartCommand");

        if (notification == null) {
            notification = createNotification();
        }

        startForeground(NOTIFICATION_ID, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        AppUtils.log("ProxyService onDestroy");
        proxyServer.stop();
        super.onDestroy();
    }

    //binder

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public ProxyService getService() {
            return ProxyService.this;
        }
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

    private Notification createNotification() {
        Intent startMainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, startMainActivityIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        String httpAddress = proxyServer.getHttpAddress();
        String notificationText = "Proxy running @ " + httpAddress;

        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(notificationText)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.logo_icon)
                .setOngoing(true);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(CHANNEL_ID);
        }

        return notificationBuilder.build();
    }

}
