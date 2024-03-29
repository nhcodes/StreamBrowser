package codes.nh.streambrowser.screens.cast;

import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_IDLE;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.mediarouter.app.MediaRouteChooserDialog;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaError;
import com.google.android.gms.cast.MediaSeekOptions;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import codes.nh.streambrowser.R;
import codes.nh.streambrowser.proxy.ProxyServiceConnector;
import codes.nh.streambrowser.screens.stream.Stream;
import codes.nh.streambrowser.utils.AppUtils;
import codes.nh.streambrowser.utils.async.Async;

public class CastManager {

    private final Context context;

    private final CastContext castContext;

    private Stream streamQueue;

    public CastManager(Context context) {
        AppUtils.log("init CastManager");
        this.context = context;
        this.castContext = CastContext.getSharedInstance(context);
                /*.addOnSuccessListener(castContext -> {
                    AppUtils.log("init CastManager addOnSuccessListener");
                    this.castContext = castContext;
                    startSessionListener();
                })
                .addOnFailureListener(e -> {
                    AppUtils.log("CastManager.getSharedInstance", e);
                });*/
    }

    private SessionManager getSessionManager() {
        return castContext.getSessionManager();
    }

    private CastSession getCastSession() {
        return getSessionManager().getCurrentCastSession();
    }

    private RemoteMediaClient getRemoteMediaClient() {
        return getCastSession().getRemoteMediaClient();
    }

    public boolean isConnected() {
        CastSession session = getCastSession();
        return session != null && session.isConnected();
    }

    public boolean isPlaying() {
        return isConnected() && getRemoteMediaClient().hasMediaSession();
    }

    public boolean isMute() {
        return getCastSession().isMute();
    }

    public void requestStream(Activity activity, Stream stream) {
        listener.onPlaybackRequested(stream);

        if (!isConnected()) {
            streamQueue = stream;
            showCastDevicesDialog(activity);
            return;
        }

        String contentUrl = stream.getStreamUrl();

        if (stream.useProxy()) {

            HashMap<String, String> newHeaders = new HashMap<>();
            newHeaders.put("Referer", stream.getHeaders().get("Referer"));
            newHeaders.put("Origin", stream.getHeaders().get("Origin"));
            AppUtils.log("stream headers: " + AppUtils.mapToJson(newHeaders));

            proxyServiceConnector.startProxyServer(context, proxyServer -> {
                String proxyUrl = proxyServer.convertToProxyUrl(contentUrl, newHeaders);
                Async.executeOnMainThread(() -> {
                    loadStream(proxyUrl, stream);
                });
            });

        } else {
            loadStream(contentUrl, stream);
        }

    }

    private final int requestTimeoutSeconds = 10;

    private void loadStream(String contentUrl, Stream stream) {
        getRemoteMediaClient()
                .load(stream.createMediaLoadRequestData(contentUrl))
                .setResultCallback(getRequestCallback(stream), requestTimeoutSeconds, TimeUnit.SECONDS);
    }

    public void seekToLive() {
        RemoteMediaClient remoteMediaClient = getRemoteMediaClient();
        if (!remoteMediaClient.isLiveStream()) return;
        MediaSeekOptions toEnd = new MediaSeekOptions.Builder().setIsSeekToInfinite(true).build();
        remoteMediaClient.seek(toEnd);
    }

    public void stopStream() {
        getRemoteMediaClient().stop();
    }

    public void disconnect() {
        getSessionManager().endCurrentSession(true);
    }

    public void showCastDevicesDialog(Context context) {
        MediaRouteChooserDialog dialog = new MediaRouteChooserDialog(context, R.style.AppTheme);
        dialog.setRouteSelector(castContext.getMergedSelector());
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    //devices

    public CastDevice getCastDevice() {
        return getCastSession().getCastDevice();
    }

    public List<MediaRouter.RouteInfo> scanCastDevices(Application application) {
        MediaRouteSelector selector = castContext.getMergedSelector();
        if (selector == null) return new ArrayList<>();
        MediaRouter mediaRouter = MediaRouter.getInstance(application);
        return mediaRouter.getRoutes().stream().filter(route -> route.matchesSelector(selector)).collect(Collectors.toList());
    }

    //request callback

    private ResultCallback<RemoteMediaClient.MediaChannelResult> getRequestCallback(Stream stream) {
        return mediaChannelResult -> {

            Status status = mediaChannelResult.getStatus();
            MediaError mediaError = mediaChannelResult.getMediaError();
            if (!status.isSuccess() || mediaError != null) {

                String errorMessage = "";

                if (mediaError != null && mediaError.getDetailedErrorCode() != null) {
                    errorMessage += "error=" + AppUtils.getMediaErrorName(mediaError.getDetailedErrorCode()) + "\n";
                }

                errorMessage += "status=" + mediaChannelResult.getStatus() + "\n";

                AppUtils.log(errorMessage);
                listener.onPlaybackStarted(stream, errorMessage);
                return;
            }

            listener.onPlaybackStarted(stream, null);

            seekToLive();

        };
    }

    //proxy service

    private final ProxyServiceConnector proxyServiceConnector = new ProxyServiceConnector();

    //session listener

    public void startSessionListener() {
        AppUtils.log("startSessionListener");
        getSessionManager().addSessionManagerListener(sessionListener, CastSession.class);
    }

    public void stopSessionListener() {
        AppUtils.log("stopSessionListener");
        getSessionManager().removeSessionManagerListener(sessionListener, CastSession.class);
    }

    private void onSessionStart(CastSession session) {
        startPlaybackListener();
        startMessageListener(session);

        if (streamQueue != null) {
            loadStream(streamQueue.getStreamUrl(), streamQueue);
            streamQueue = null;
        }
    }

    private void onSessionEnd(CastSession session) {
        stopPlaybackListener();

        proxyServiceConnector.stopProxyServer(context);
    }

    private final SessionManagerListener<CastSession> sessionListener = new SessionManagerListener<>() {

        @Override
        public void onSessionStarting(@NonNull CastSession session) {
            listener.onSessionUpdate(SessionStatus.STARTING);
        }

        @Override
        public void onSessionStarted(@NonNull CastSession session, @NonNull String id) {
            listener.onSessionUpdate(SessionStatus.STARTED, id);
            onSessionStart(session);
        }

        @Override
        public void onSessionStartFailed(@NonNull CastSession session, int error) {
            listener.onSessionUpdate(SessionStatus.START_FAILED, error);
        }

        @Override
        public void onSessionEnding(@NonNull CastSession session) {
            listener.onSessionUpdate(SessionStatus.ENDING);
        }

        @Override
        public void onSessionEnded(@NonNull CastSession session, int error) {
            listener.onSessionUpdate(SessionStatus.ENDED, error);
            onSessionEnd(session);
        }

        @Override
        public void onSessionResuming(@NonNull CastSession session, @NonNull String id) {
            listener.onSessionUpdate(SessionStatus.RESUMING, id);
        }

        @Override
        public void onSessionResumed(@NonNull CastSession session, boolean wasSuspended) {
            listener.onSessionUpdate(SessionStatus.RESUMED, wasSuspended);
            onSessionStart(session);
        }

        @Override
        public void onSessionResumeFailed(@NonNull CastSession session, int error) {
            listener.onSessionUpdate(SessionStatus.RESUME_FAILED, error);
        }

        @Override
        public void onSessionSuspended(@NonNull CastSession session, int reason) {
            listener.onSessionUpdate(SessionStatus.SUSPENDED, reason);
        }
    };

    public enum SessionStatus {
        STARTING, STARTED, START_FAILED, ENDING, ENDED, RESUMING, RESUMED, RESUME_FAILED, SUSPENDED
    }

    //playback listener

    private RemoteMediaClient.Callback playbackListener;

    private void startPlaybackListener() {
        if (playbackListener != null) return;
        AppUtils.log("startPlaybackListener");
        playbackListener = getPlaybackListener();
        getRemoteMediaClient().registerCallback(playbackListener);
    }

    private void stopPlaybackListener() {
        if (playbackListener == null) return;
        AppUtils.log("stopPlaybackListener");
        RemoteMediaClient remoteMediaClient = getRemoteMediaClient();
        if (remoteMediaClient != null) {
            remoteMediaClient.unregisterCallback(playbackListener);
        }
        playbackListener = null;
    }

    private RemoteMediaClient.Callback getPlaybackListener() {
        return new RemoteMediaClient.Callback() {

            @Override
            public void onStatusUpdated() {
                super.onStatusUpdated();

                RemoteMediaClient remoteMediaClient = getRemoteMediaClient();

                int stateId = remoteMediaClient.getPlayerState();
                String[] states = new String[]{
                        "PLAYER_STATE_UNKNOWN",
                        "PLAYER_STATE_IDLE",
                        "PLAYER_STATE_PLAYING",
                        "PLAYER_STATE_PAUSED",
                        "PLAYER_STATE_BUFFERING",
                        "PLAYER_STATE_LOADING"
                };
                String stateDescription = states[stateId];
                AppUtils.log("onStatusUpdated " + stateDescription);

                listener.onPlaybackUpdate(remoteMediaClient, stateId);

                if (stateId == PLAYER_STATE_IDLE) {
                    proxyServiceConnector.stopProxyServer(context);
                }
            }

            @Override
            public void onMediaError(@NonNull MediaError mediaError) {
                super.onMediaError(mediaError);
                AppUtils.log("onMediaError " + mediaError.toJson());
            }

        };
    }

    //message listener

    private static final String NAMESPACE = "urn:x-cast:streambrowser";

    private void startMessageListener(CastSession session) {
        try {
            session.setMessageReceivedCallbacks(NAMESPACE, messageListener);
        } catch (IOException e) {
            AppUtils.log("setMessageReceivedCallbacks", e);
        }
    }

    public void sendMessage(String message) throws JSONException {
        CastSession session = getCastSession();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", message);
        session.sendMessage(NAMESPACE, jsonObject.toString());
    }

    private final Cast.MessageReceivedCallback messageListener = new Cast.MessageReceivedCallback() {
        @Override
        public void onMessageReceived(@NonNull CastDevice castDevice, @NonNull String namespace, @NonNull String message) {
            listener.onReceiveMessage(message);
        }
    };

    //listener

    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {

        void onSessionUpdate(SessionStatus sessionStatus, Object... data);

        void onPlaybackRequested(Stream stream);

        void onPlaybackStarted(Stream stream, String error);

        void onPlaybackUpdate(RemoteMediaClient remoteMediaClient, int stateId);

        void onReceiveMessage(String message);

    }
}
