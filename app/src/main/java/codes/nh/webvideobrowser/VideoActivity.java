package codes.nh.webvideobrowser;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.Player;
import androidx.media3.ui.PlayerView;

import org.json.JSONObject;

import codes.nh.webvideobrowser.fragments.stream.Stream;
import codes.nh.webvideobrowser.utils.AppUtils;

public class VideoActivity extends AppCompatActivity {

    private PlayerViewModel playerViewModel;

    private PlayerView video;

    private Stream stream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemBars();
        setContentView(R.layout.activity_video);

        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);

        try {
            stream = Stream.fromJson(new JSONObject(getIntent().getStringExtra("stream")));
        } catch (Exception exception) {
            AppUtils.log("VideoActivity intent", exception);
            Toast.makeText(getApplicationContext(), "an error occured", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        video = findViewById(R.id.activity_video_video);
        video.setKeepScreenOn(true);
        video.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
        video.setControllerAutoShow(true);
        video.setShowSubtitleButton(true);
        video.setFullscreenButtonClickListener(isFullScreen -> {
            finish();
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        Player player = playerViewModel.start(stream);
        video.setPlayer(player);
    }

    @Override
    public void onStop() {
        super.onStop();

        playerViewModel.stop();
    }

    private void hideSystemBars() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        controller.hide(WindowInsetsCompat.Type.systemBars());
    }

}