package codes.nh.webvideobrowser;

import android.content.Context;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;

import java.util.Map;

import codes.nh.webvideobrowser.fragments.stream.Stream;
import codes.nh.webvideobrowser.utils.AppUtils;

public class MediaPlayer {

    private Player player;

    private boolean state = true;
    private Integer index;
    private Long position;

    public void start(Context context, Stream stream) { //todo
        DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory();
        httpDataSourceFactory.setDefaultRequestProperties(stream.getHeaders());

        DefaultMediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(context);
        mediaSourceFactory.setDataSourceFactory(httpDataSourceFactory);

        player = new ExoPlayer.Builder(context).setMediaSourceFactory(mediaSourceFactory).build();

        MediaItem mediaItem = stream.createMediaItem();
        player.setMediaItem(mediaItem);

        player.setPlayWhenReady(state);
        if (index != null && position != null) {
            player.seekTo(index, position);
        }
        player.addListener(playerListener);
        player.prepare();
    }

    public void start(Context context, String streamUrl, Map<String, String> headers) { //todo
        DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory();
        httpDataSourceFactory.setDefaultRequestProperties(headers);

        DefaultMediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(context);
        mediaSourceFactory.setDataSourceFactory(httpDataSourceFactory);

        player = new ExoPlayer.Builder(context).setMediaSourceFactory(mediaSourceFactory).build();

        MediaItem mediaItem = MediaItem.fromUri(streamUrl);
        player.setMediaItem(mediaItem);

        player.setPlayWhenReady(state);
        if (index != null && position != null) {
            player.seekTo(index, position);
        }
        player.addListener(playerListener);
        player.prepare();
    }

    public void stop() {
        state = player.getPlayWhenReady();
        index = player.getCurrentMediaItemIndex();
        position = player.getCurrentPosition();
        player.removeListener(playerListener);
        player.release();
    }

    public Player getPlayer() {
        return player;
    }

    private final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onPlayerError(PlaybackException error) {
            Player.Listener.super.onPlayerError(error);

            AppUtils.log("=============" + error.errorCode + "....." + error.getErrorCodeName() + "!!!!" + error.getMessage());
        }
    };

}
