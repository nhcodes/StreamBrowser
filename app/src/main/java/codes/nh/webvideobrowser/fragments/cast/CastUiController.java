package codes.nh.webvideobrowser.fragments.cast;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.cast.framework.media.uicontroller.UIMediaController;

import codes.nh.webvideobrowser.R;

public class CastUiController extends UIMediaController {

    private final CastManager castManager;

    private final Drawable unMuteDrawable, muteDrawable;

    public CastUiController(Activity activity, CastManager castManager) {
        super(activity);

        this.castManager = castManager;

        this.unMuteDrawable = ContextCompat.getDrawable(activity, R.drawable.icon_control_unmute);
        this.muteDrawable = ContextCompat.getDrawable(activity, R.drawable.icon_control_mute);
    }

    @Override
    protected void onMuteToggleClicked(@NonNull ImageView imageView) {
        super.onMuteToggleClicked(imageView);

        if (castManager.isMute()) {
            imageView.setImageDrawable(unMuteDrawable);
        } else {
            imageView.setImageDrawable(muteDrawable);
        }
    }

    @Override
    protected void onForwardClicked(@NonNull View view, long skipStepMs) {
        super.onForwardClicked(view, skipStepMs);

        castManager.goToLive();
    }

    public void onStopClicked(ImageView imageView) {
        castManager.stopStream();
    }

    public void onDisconnectClicked(ImageView imageView) {
        castManager.disconnect();
    }
}