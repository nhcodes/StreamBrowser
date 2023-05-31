package codes.nh.webvideobrowser.screens.cast;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.google.android.gms.cast.framework.media.CastMediaOptions;
import com.google.android.gms.cast.framework.media.MediaIntentReceiver;
import com.google.android.gms.cast.framework.media.NotificationOptions;

import java.util.Arrays;
import java.util.List;

import codes.nh.webvideobrowser.screens.main.MainActivity;
import codes.nh.webvideobrowser.R;
import codes.nh.webvideobrowser.screens.settings.SettingsManager;


public class CastOptionsProvider implements OptionsProvider {

    @NonNull
    @Override
    public CastOptions getCastOptions(@NonNull Context context) {
        String receiverApplicationId = context.getString(R.string.cast_receiver_application_id);

        SettingsManager settingsManager = new SettingsManager(context);
        int skipTime = settingsManager.getSkipTime() * 1000;

        CastMediaOptions mediaOptions = new CastMediaOptions.Builder()
                .setNotificationOptions(createNotificationOptions(skipTime))
                .build();

        return new CastOptions.Builder()
                .setReceiverApplicationId(receiverApplicationId)
                .setCastMediaOptions(mediaOptions)
                .build();
    }

    @Nullable
    @Override
    public List<SessionProvider> getAdditionalSessionProviders(@NonNull Context context) {
        return null;
    }

    private NotificationOptions createNotificationOptions(int skipTime) {

        List<String> actions = Arrays.asList(
                MediaIntentReceiver.ACTION_REWIND,
                MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK,
                MediaIntentReceiver.ACTION_FORWARD,
                MediaIntentReceiver.ACTION_STOP_CASTING
        );

        int[] compactActions = {0, 1, 2};

        return new NotificationOptions.Builder()
                .setActions(actions, compactActions)
                .setTargetActivityClassName(MainActivity.class.getName())
                .setSkipStepMs(skipTime)
                .setSmallIconDrawableResId(R.drawable.logo_icon)
                .build();
    }
}