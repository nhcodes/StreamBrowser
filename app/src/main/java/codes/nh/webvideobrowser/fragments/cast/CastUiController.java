package codes.nh.webvideobrowser.fragments.cast;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.cast.framework.media.uicontroller.UIMediaController;

import codes.nh.webvideobrowser.R;
import codes.nh.webvideobrowser.fragments.settings.SettingsManager;

public class CastUiController extends UIMediaController {

    private final CastManager castManager;

    private final Drawable unMuteDrawable, muteDrawable;

    public CastUiController(Activity activity, CastManager castManager) {
        super(activity);

        this.castManager = castManager;

        this.unMuteDrawable = ContextCompat.getDrawable(activity, R.drawable.icon_control_unmute);
        this.muteDrawable = ContextCompat.getDrawable(activity, R.drawable.icon_control_mute);
    }

    public void bindViewToRewind(View view) {
        SettingsManager settingsManager = new SettingsManager(view.getContext());
        int skipTime = settingsManager.getSkipTime();
        bindViewToRewind(view, skipTime * 1000L);
    }

    public void bindViewToForward(View view) {
        SettingsManager settingsManager = new SettingsManager(view.getContext());
        int skipTime = settingsManager.getSkipTime();
        bindViewToForward(view, skipTime * 1000L);
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

        castManager.seekToLive();
    }

    public void onStopClicked(View view) {
        castManager.stopStream();
    }

    public void onDisconnectClicked(View view) {
        castManager.disconnect();
    }

    public void bindImageViewToPlayPauseButton(ImageView imageView, View loadingIndicator) {
        Context context = imageView.getContext();
        Drawable playDrawable = ContextCompat.getDrawable(context, R.drawable.icon_control_play);
        Drawable pauseDrawable = ContextCompat.getDrawable(context, R.drawable.icon_control_pause);
        Drawable stopDrawable = null;//ContextCompat.getDrawable(context, R.drawable.icon_control_stop);
        bindImageViewToPlayPauseToggle(imageView, playDrawable, pauseDrawable, stopDrawable, loadingIndicator, true);
    }

    public void bindViewToStopButton(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStopClicked(view);
            }
        });
    }

    public void bindViewToDisconnectButton(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDisconnectClicked(view);
            }
        });
    }
}