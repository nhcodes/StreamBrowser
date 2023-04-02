package codes.nh.webvideobrowser;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.media3.cast.CastPlayer;
import androidx.media3.cast.SessionAvailabilityListener;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.mediarouter.app.MediaRouteChooserDialog;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import codes.nh.webvideobrowser.fragments.settings.SettingsManager;
import codes.nh.webvideobrowser.fragments.stream.Stream;
import codes.nh.webvideobrowser.proxy.ProxyService;
import codes.nh.webvideobrowser.utils.AppUtils;

public class CastHandler {

    private final CastContext castContext;

    private final CastPlayer castPlayer;

    private Listener listener;

    public CastHandler(Context context) {
        this.castContext = CastContext.getSharedInstance(context);

        SettingsManager settingsManager = new SettingsManager(context);
        int skipTime = settingsManager.getSkipTime() * 1000;

        this.castPlayer = new CastPlayer(castContext, new CastMediaItemConverter(), skipTime, skipTime);
        initCastPlayer();
    }

    private void initCastPlayer() {
        castPlayer.setSessionAvailabilityListener(sessionListener);
        castPlayer.addListener(playerListener);
    }

    public CastContext getCastContext() {
        return castContext;
    }

    public CastPlayer getCastPlayer() {
        return castPlayer;
    }

    public void openDeviceChooserDialog(Context context) {
        MediaRouteChooserDialog dialog = new MediaRouteChooserDialog(context, R.style.AppTheme);
        dialog.setRouteSelector(castContext.getMergedSelector());
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private Stream streamQueue = null;

    private String lastStreamUrl;

    public void start(Context context, Stream stream) {
        if (!castPlayer.isCastSessionAvailable()) {
            streamQueue = stream;
            openDeviceChooserDialog(context);
            return;
        }

        if (stream.getStreamUrl().equalsIgnoreCase(lastStreamUrl) && !stream.useProxy()) {
            stream.setUseProxy(true);
            ProxyService.start(context.getApplicationContext()); //todo improve
            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                play(stream);
            }, 1, TimeUnit.SECONDS);
        } else {
            lastStreamUrl = stream.getStreamUrl();
            play(stream);
        }

    }

    private void play(Stream stream) {
        /*DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory();
        httpDataSourceFactory.setDefaultRequestProperties(stream.getHeaders());

        DefaultMediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(context);
        mediaSourceFactory.setDataSourceFactory(httpDataSourceFactory);*/

        MediaItem mediaItem = stream.createMediaItem();
        AppUtils.log("kkkkkkk: " + mediaItem.localConfiguration.uri);
        castPlayer.setMediaItem(mediaItem);

        //castPlayer.setPlayWhenReady(true);
        //castPlayer.prepare();
    }

    public void stop() {
        //castPlayer.removeListener(playerListener);
        castPlayer.release();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void on();
    }

    //message channel

    private final String messageNamespace = "urn:x-cast:webvideobrowser";

    private final Cast.MessageReceivedCallback messageCallback = (device, namespace, message) -> {
        AppUtils.log("MessageReceivedCallback: " + message);

        /*stream.setUseProxy(true);
        ProxyService.start(context.getApplicationContext()); //todo improve
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            play(stream);
        }, 1, TimeUnit.SECONDS);*/
    };

    private void setMessageListener() throws IOException {
        CastSession session = castContext.getSessionManager().getCurrentCastSession();
        session.setMessageReceivedCallbacks(messageNamespace, messageCallback);
    }

    //session listener

    private final SessionAvailabilityListener sessionListener = new SessionAvailabilityListener() {

        @Override
        public void onCastSessionAvailable() {
            AppUtils.log("onCastSessionAvailable");

            try {
                setMessageListener();
            } catch (IOException e) {
                AppUtils.log("setMessageListener", e);
            }

            if (streamQueue != null) {
                play(streamQueue);
                streamQueue = null;
            }

        }

        @Override
        public void onCastSessionUnavailable() {
            AppUtils.log("onCastSessionUnavailable");
        }

    };

    //player listener

    private final Player.Listener playerListener = new Player.Listener() {

        @Override
        public void onEvents(Player player, Player.Events events) {
            Player.Listener.super.onEvents(player, events);

            StringBuilder s = new StringBuilder();
            for (int i = 0; i < events.size(); i++) {
                s.append(events.get(i)).append(", ");
            }
            AppUtils.log("onEvents: " + s);
        }

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            Player.Listener.super.onPlaybackStateChanged(playbackState);
            AppUtils.log("onPlaybackStateChanged: " + playbackState);
        }

        @Override
        public void onPlayerError(PlaybackException error) {
            Player.Listener.super.onPlayerError(error);
            AppUtils.log("onPlayerError: " + error.errorCode + "....." + error.getErrorCodeName() + "!!!!" + error.getMessage());
        }

        @Override
        public void onPlayerErrorChanged(@Nullable PlaybackException error) {
            Player.Listener.super.onPlayerErrorChanged(error);
            AppUtils.log("onPlayerErrorChanged: " + error.getErrorCodeName() + error.getMessage());
        }

    };
}
