package codes.nh.webvideobrowser.fragments.cast;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.cast.framework.media.uicontroller.UIMediaController;

import codes.nh.webvideobrowser.App;
import codes.nh.webvideobrowser.HomeActivity;
import codes.nh.webvideobrowser.R;

public class CastUiController extends UIMediaController {

    private final CastManager castManager;

    private final Drawable unMuteDrawable, muteDrawable;

    public CastUiController(HomeActivity activity) {
        super(activity);

        castManager = ((App) activity.getApplication()).getCastManager();

        unMuteDrawable = ContextCompat.getDrawable(activity, R.drawable.icon_control_unmute);
        muteDrawable = ContextCompat.getDrawable(activity, R.drawable.icon_control_mute);
    }

    @Override
    protected void onMuteToggleClicked(@NonNull ImageView imageView) {
        super.onMuteToggleClicked(imageView);
        if (castManager.getCastSession().isMute()) {
            imageView.setImageDrawable(unMuteDrawable);
        } else {
            imageView.setImageDrawable(muteDrawable);
        }
    }

    @Override
    protected void onForwardClicked(@NonNull View view, long skipStepMs) {
        boolean isLive = castManager.goToLive();
        if (!isLive) {
            super.onForwardClicked(view, skipStepMs);
        }
    }

    public void onStopClicked(ImageView imageView) {
        castManager.stopStream();
    }

    public void onDisconnectClicked(ImageView imageView) {
        castManager.disconnect();
    }
}