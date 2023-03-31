package codes.nh.webvideobrowser.fragments.cast;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
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
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import codes.nh.webvideobrowser.fragments.stream.Stream;
import codes.nh.webvideobrowser.utils.AppUtils;

public class CastManager {

    private CastContext castContext;

    private Stream streamQueue;

    public CastManager(Context context) {
        CastContext
                .getSharedInstance(context, Executors.newSingleThreadExecutor())
                .addOnSuccessListener(castContext -> {
                    this.castContext = castContext;
                    startSessionListener();
                })
                .addOnFailureListener(e -> {
                    AppUtils.log("CastManager.getSharedInstance", e);
                });
    }

    private SessionManager getSessionManager() {
        return castContext.getSessionManager();
    }

    protected CastSession getCastSession() {
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
        return isConnected() && getRemoteMediaClient() != null && getRemoteMediaClient().hasMediaSession();
    }

    public boolean playStream(Stream stream) {

        if (!isConnected()) {
            streamQueue = stream;
            return false;
        }

        listener.onStreamRequested(stream);

        getRemoteMediaClient()
                .load(stream.createMediaLoadRequestData())
                .setResultCallback(getRequestCallback(stream), requestTimeoutSeconds, TimeUnit.SECONDS);

        return true;
    }

    public boolean goToLive() {
        RemoteMediaClient remoteMediaClient = getRemoteMediaClient();
        if (remoteMediaClient.isLiveStream()) {
            MediaSeekOptions toEnd = new MediaSeekOptions.Builder().setIsSeekToInfinite(true).build();
            remoteMediaClient.seek(toEnd);
            return true;
        }
        return false;
    }

    public void stopStream() {
        getRemoteMediaClient().stop();
    }

    public void disconnect() {
        getSessionManager().endCurrentSession(true);
    }

    //request callback

    private final int requestTimeoutSeconds = 10;

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
                listener.onStreamStart(stream, errorMessage);
                return;
            }

            listener.onStreamStart(stream, null);

            stopPlaybackListener();
            startPlaybackListener();

            goToLive();

        };
    }

    public CastDevice getCastDevice() {
        return getCastSession().getCastDevice();
    }

    public List<MediaRouter.RouteInfo> scanCastDevices(Application application) {
        MediaRouteSelector selector = castContext.getMergedSelector();
        if (selector == null) return new ArrayList<>();
        MediaRouter mediaRouter = MediaRouter.getInstance(application);
        return mediaRouter.getRoutes().stream().filter(route -> route.matchesSelector(selector)).collect(Collectors.toList());
    }

    //playback listener

    private RemoteMediaClient.Callback playbackListener;

    public void startPlaybackListener() {
        if (castContext == null) return;
        if (playbackListener == null && isConnected()) {
            AppUtils.log("startPlaybackListener");
            playbackListener = getPlaybackListener();
            getRemoteMediaClient().registerCallback(playbackListener);
        }
    }

    public void stopPlaybackListener() {
        if (castContext == null) return;
        if (playbackListener != null && isConnected()) {
            AppUtils.log("stopPlaybackListener");
            getRemoteMediaClient().unregisterCallback(playbackListener);
            playbackListener = null;
        }
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

                listener.onStreamUpdate(remoteMediaClient, stateId);
            }

            @Override
            public void onMediaError(@NonNull MediaError mediaError) {
                super.onMediaError(mediaError);
                AppUtils.log("onMediaError " + mediaError.toJson());
            }

        };
    }

    //session listener

    private SessionManagerListener<CastSession> sessionListener;

    public void startSessionListener() {
        if (castContext == null) return;
        if (sessionListener == null) {
            AppUtils.log("startSessionListener");
            sessionListener = getSessionListener();
            getSessionManager().addSessionManagerListener(sessionListener, CastSession.class);
        }
    }

    public void stopSessionListener() {
        if (castContext == null) return;
        if (sessionListener != null) {
            AppUtils.log("stopSessionListener");
            getSessionManager().removeSessionManagerListener(sessionListener, CastSession.class);
            sessionListener = null;
        }
    }

    private SessionManagerListener<CastSession> getSessionListener() {
        return new SessionManagerListener<>() {

            @Override
            public void onSessionStarting(@NonNull CastSession castSession) {
                listener.onSessionUpdate(SessionStatus.STARTING);
            }

            @Override
            public void onSessionStarted(@NonNull CastSession castSession, @NonNull String id) {
                listener.onSessionUpdate(SessionStatus.STARTED, id);

                if (streamQueue != null) {
                    playStream(streamQueue);
                    streamQueue = null;
                }
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
            }

            @Override
            public void onSessionResuming(@NonNull CastSession session, @NonNull String id) {
                listener.onSessionUpdate(SessionStatus.RESUMING, id);
            }

            @Override
            public void onSessionResumed(@NonNull CastSession session, boolean wasSuspended) {
                listener.onSessionUpdate(SessionStatus.RESUMED, wasSuspended);
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
    }

    public enum SessionStatus {
        STARTING, STARTED, START_FAILED, ENDING, ENDED, RESUMING, RESUMED, RESUME_FAILED, SUSPENDED
    }

    //message channel

    private final Cast.MessageReceivedCallback messageCallback = new Cast.MessageReceivedCallback() {
        @Override
        public void onMessageReceived(@NonNull CastDevice castDevice, @NonNull String namespace, @NonNull String message) {
            AppUtils.log("onMessageReceived: " + message);
            listener.onReceiveMessage(message);
        }
    };

    private static final String NAMESPACE = "urn:x-cast:webvideobrowser";

    public void sendMessage(String message) throws IOException, JSONException {
        CastSession session = getCastSession();

        session.setMessageReceivedCallbacks(NAMESPACE, messageCallback);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", message);

        session.sendMessage(NAMESPACE, jsonObject.toString());
    }

    //listener

    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {

        void onStreamRequested(Stream stream);

        void onStreamStart(Stream stream, String error);

        void onStreamUpdate(RemoteMediaClient remoteMediaClient, int stateId);

        void onSessionUpdate(SessionStatus sessionStatus, Object... data);

        void onReceiveMessage(String message);

    }
}
