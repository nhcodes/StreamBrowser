package codes.nh.streambrowser.proxy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;

import java.util.function.Consumer;

import codes.nh.streambrowser.utils.AppUtils;

public class ProxyServiceConnector {

    //https://developer.android.com/guide/components/bound-services

    private Consumer<ProxyServer> callback = (proxy) -> {};

    private ProxyServer proxyServer;

    private final ServiceConnection proxyServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            AppUtils.log("onServiceConnected");

            ProxyService proxyService = ((ProxyService.LocalBinder) service).getService();
            proxyServer = proxyService.getProxyServer();
            callback.accept(proxyServer);
            callback = (proxy) -> {};
        }

        public void onServiceDisconnected(ComponentName className) {
            AppUtils.log("onServiceDisconnected");
        }
    };

    public void startProxyServer(Context context, Consumer<ProxyServer> callback) {
        if (proxyServer != null) {
            callback.accept(proxyServer);
            return;
        }
        this.callback = callback;
        startProxyService(context);
        bindToProxyService(context);
    }

    public void stopProxyServer(Context context) {
        if (proxyServer == null) return;
        unbindFromProxyService(context);
        stopProxyService(context);
        proxyServer = null;
    }

    private void startProxyService(Context context) {
        Intent intent = new Intent(context, ProxyService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    private void stopProxyService(Context context) {
        Intent intent = new Intent(context, ProxyService.class);
        context.stopService(intent);
    }

    private void bindToProxyService(Context context) {
        Intent intent = new Intent(context, ProxyService.class);
        context.bindService(intent, proxyServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindFromProxyService(Context context) {
        context.unbindService(proxyServiceConnection);
    }

}
