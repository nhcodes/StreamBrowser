package codes.nh.webvideobrowser.screens.player;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;

import codes.nh.webvideobrowser.screens.stream.Stream;
import codes.nh.webvideobrowser.utils.AppUtils;

public class PlayerViewModel extends AndroidViewModel {

    public PlayerViewModel(@NonNull Application application) {
        super(application);
        AppUtils.log("init PlayerViewModel");
    }

    private Player player;

    private boolean playWhenReady = true;

    private Integer index;

    private Long position;

    public Player start(Stream stream) { //todo
        Context context = getApplication().getApplicationContext();

        DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory();
        httpDataSourceFactory.setDefaultRequestProperties(stream.getHeaders());

        DefaultMediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(context);
        mediaSourceFactory.setDataSourceFactory(httpDataSourceFactory);

        player = new ExoPlayer.Builder(context).setMediaSourceFactory(mediaSourceFactory).build();

        MediaItem mediaItem = stream.createMediaItem();
        player.setMediaItem(mediaItem);

        player.setPlayWhenReady(playWhenReady);
        if (index != null && position != null) { //todo what if stream changes?
            player.seekTo(index, position);
        }
        player.addListener(playerListener);
        player.prepare();
        return player;
    }

    public void stop() {
        playWhenReady = player.getPlayWhenReady();
        index = player.getCurrentMediaItemIndex();
        position = player.getCurrentPosition();
        player.removeListener(playerListener);
        player.release();
        player = null;
    }

    private final Player.Listener playerListener = new Player.Listener() {

        /*@Override
        public void onTracksChanged(Tracks tracks) {
            Player.Listener.super.onTracksChanged(tracks);

            AppUtils.log("onTracksChanged");
            tracks.getGroups().forEach(group -> {
                AppUtils.log("===========");
                int type = group.getType();
                AppUtils.log("type = " + type);
                for (int i = 0; i < group.length; i++) {
                    AppUtils.log("format=" + group.getTrackFormat(i) + ", support=" + group.getTrackSupport(i));
                }
                AppUtils.log("===========");
            });
        }*/

        @Override
        public void onPlayerError(PlaybackException error) {
            Player.Listener.super.onPlayerError(error);

            AppUtils.log("=============" + error.errorCode + "....." + error.getErrorCodeName() + "!!!!" + error.getMessage());
        }
    };

}
