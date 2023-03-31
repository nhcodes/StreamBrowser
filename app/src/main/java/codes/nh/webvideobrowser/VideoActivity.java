package codes.nh.webvideobrowser;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.media3.ui.PlayerView;

import org.json.JSONObject;

import java.util.Map;

import codes.nh.webvideobrowser.utils.AppUtils;

public class VideoActivity extends AppCompatActivity {

    private PlayerView video;

    private String streamUrl;

    private Map<String, String> streamHeaders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemBars();
        setContentView(R.layout.activity_video);

        try {
            streamUrl = getIntent().getStringExtra("url");
            streamHeaders = AppUtils.jsonToMap(new JSONObject(getIntent().getStringExtra("headers")));
        } catch (Exception exception) {
            AppUtils.log("VideoActivity intent", exception);
            Toast.makeText(getApplicationContext(), "an error occured", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        video = findViewById(R.id.activity_video_video);
        video.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
        video.setControllerAutoShow(true);
        video.setKeepScreenOn(true);
    }


    private final MediaPlayer player = new MediaPlayer();

    @Override
    public void onStart() {
        super.onStart();

        player.start(getApplicationContext(), streamUrl, streamHeaders);
        video.setPlayer(player.getPlayer());
    }

    @Override
    public void onStop() {
        super.onStop();

        player.stop();
    }

    private void hideSystemBars() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        controller.hide(WindowInsetsCompat.Type.systemBars());
    }

}