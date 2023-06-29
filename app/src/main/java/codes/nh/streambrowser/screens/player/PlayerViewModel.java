package codes.nh.streambrowser.screens.player;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;

import codes.nh.streambrowser.screens.settings.SettingsManager;
import codes.nh.streambrowser.screens.stream.Stream;
import codes.nh.streambrowser.utils.AppUtils;

public class PlayerViewModel extends AndroidViewModel {

    public PlayerViewModel(@NonNull Application application) {
        super(application);
        AppUtils.log("init PlayerViewModel");
    }

    private Player player;

    public Player start(Stream stream) { //todo
        Context context = getApplication().getApplicationContext();

        DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory();
        httpDataSourceFactory.setDefaultRequestProperties(stream.getHeaders());

        DefaultMediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(context);
        mediaSourceFactory.setDataSourceFactory(httpDataSourceFactory);

        SettingsManager settingsManager = new SettingsManager(context);
        long skipTime = settingsManager.getSkipTime() * 1000L;

        player = new ExoPlayer.Builder(context)
                .setSeekForwardIncrementMs(skipTime)
                .setSeekBackIncrementMs(skipTime)
                .setMediaSourceFactory(mediaSourceFactory)
                .build();

        MediaItem mediaItem = stream.createMediaItem(stream.getStreamUrl()); //todo proxy url
        player.setMediaItem(mediaItem);
        if (player.isCurrentMediaItemLive()) {
            player.seekToDefaultPosition();
        } else {
            player.seekTo(stream.getStartTime());
        }
        player.addListener(playerListener);
        player.setPlayWhenReady(true);
        player.prepare();
        return player;
    }

    public void stop() {
        player.pause();
        player.removeListener(playerListener);
        player.release();
        player = null;
    }

    private final MutableLiveData<Long> positionState = new MutableLiveData<>();

    public MutableLiveData<Long> getPositionState() {
        return positionState;
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
        public void onPlaybackStateChanged(int playbackState) {
            Player.Listener.super.onPlaybackStateChanged(playbackState);
            long position = player.isCurrentMediaItemLive() ? -1 : player.getCurrentPosition();
            positionState.setValue(position);
        }

        @Override
        public void onPlayerError(PlaybackException error) {
            Player.Listener.super.onPlayerError(error);
            AppUtils.log("=============" + error.errorCode + "....." + error.getErrorCodeName() + "!!!!" + error.getMessage());
        }

    };

}
