package codes.nh.webvideobrowser;

import android.content.Context;

import androidx.media3.cast.CastPlayer;
import androidx.media3.cast.SessionAvailabilityListener;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;

import com.google.android.gms.cast.framework.CastContext;

import codes.nh.webvideobrowser.fragments.stream.Stream;
import codes.nh.webvideobrowser.utils.AppUtils;

@UnstableApi
public class CastMediaPlayer {

    public static CastPlayer player;

    private boolean state = true;
    private Integer index;
    private Long position;

    public boolean start(Context context, Stream stream) { //todo
        DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory();
        httpDataSourceFactory.setDefaultRequestProperties(stream.getHeaders());

        DefaultMediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(context);
        mediaSourceFactory.setDataSourceFactory(httpDataSourceFactory);

        player = new CastPlayer(CastContext.getSharedInstance(), new CastMediaItemConverter());//ExoPlayer.Builder(context).setMediaSourceFactory(mediaSourceFactory).build();

        if (!player.isCastSessionAvailable()) return false;

        player.setSessionAvailabilityListener(new SessionAvailabilityListener() {
            @Override
            public void onCastSessionAvailable() {
                AppUtils.log("onCastSessionAvailable");
            }

            @Override
            public void onCastSessionUnavailable() {
                AppUtils.log("onCastSessionUnavailable");
            }
        });

        player.addListener(new Player.Listener() {

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                AppUtils.log("=============" + playbackState);
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
                AppUtils.log("=============" + error.errorCode + "....." + error.getErrorCodeName() + "!!!!" + error.getMessage());
            }

        });

        MediaItem mediaItem = stream.createMediaItem();
        player.setMediaItem(mediaItem);

        player.setPlayWhenReady(state);
        if (index != null && position != null) {
            player.seekTo(index, position);
        }
        player.prepare();

        return true;
    }

    public void stop() {
        state = player.getPlayWhenReady();
        index = player.getCurrentMediaItemIndex();
        position = player.getCurrentPosition();
        //player.removeListener(playerListener);
        player.release();
    }

    public Player getPlayer() {
        return player;
    }

}
