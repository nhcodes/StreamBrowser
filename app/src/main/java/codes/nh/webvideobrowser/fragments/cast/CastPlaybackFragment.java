package codes.nh.webvideobrowser.fragments.cast;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.PlayerView;

import com.google.android.material.color.MaterialColors;

import codes.nh.webvideobrowser.R;
import codes.nh.webvideobrowser.fragments.player.PlayerViewModel;
import codes.nh.webvideobrowser.fragments.sheet.SheetFragment;
import codes.nh.webvideobrowser.utils.AppUtils;

public class CastPlaybackFragment extends SheetFragment {

    public CastPlaybackFragment() {
        super(R.layout.fragment_cast_playback, R.string.navigation_title_cast);
        AppUtils.log("init CastPlaybackFragment");
    }

    private TextView titleText;

    private TextView urlText;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PlayerViewModel playerViewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
        playerViewModel.getCurrentStream().observe(getViewLifecycleOwner(), stream -> {

            titleText.setText(stream.getTitle());
            titleText.setSelected(true);

            urlText.setText(stream.getSourceUrl().replaceFirst("https?://", ""));
            urlText.setSelected(true);

        });

        titleText = view.findViewById(R.id.fragment_cast_playback_text_title);

        urlText = view.findViewById(R.id.fragment_cast_playback_text_url);

        PlayerView playerControl = view.findViewById(R.id.fragment_cast_playback_player);
        playerControl.setPlayer(playerViewModel.getCastHandler().getCastPlayer());
        playerControl.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
        //playerControl.setControllerAutoShow(true);
        playerControl.showController();
        playerControl.setControllerShowTimeoutMs(-1);
        playerControl.setShowSubtitleButton(true);
        //playerControl.setShowTimeoutMs(-1);

        /*int surfaceColor = MaterialColors.getColor(requireContext(), R.attr.colorSurface, "colorSurface missing");
        playerControl.setShutterBackgroundColor(surfaceColor);
        playerControl.setBackgroundColor(surfaceColor);*/

        /*HomeActivity activity = (HomeActivity) requireActivity();

        SettingsManager settingsManager = new SettingsManager(getApplicationContext());

        CastUiController ui = new CastUiController(activity);

        int skipTime = settingsManager.getSkipTime();*/

        /*ImageView thumbnailImage = view.findViewById(R.id.fragment_cast_playback_image_thumbnail);
        thumbnailImage.setImageResource(R.drawable.icon_stream_thumbnail);
        ImageHints imageHints = new ImageHints(ImagePicker.IMAGE_TYPE_UNKNOWN, 1920, 1080);
        ui.bindImageViewToImageOfCurrentItem(thumbnailImage, imageHints, R.drawable.placeholder); //TODO remove placeholder*/

        /*TextView titleText = view.findViewById(R.id.fragment_cast_playback_text_title);
        ui.bindTextViewToMetadataOfCurrentItem(titleText, MediaMetadata.KEY_TITLE);
        titleText.setSelected(true);

        TextView urlText = view.findViewById(R.id.fragment_cast_playback_text_url);
        ui.bindTextViewToMetadataOfCurrentItem(urlText, MediaMetadata.KEY_SUBTITLE);
        urlText.setSelected(true);

        ImageButton subtitlesButton = view.findViewById(R.id.fragment_cast_playback_button_subtitles);
        ui.bindViewToClosedCaption(subtitlesButton);

        ImageButton muteButton = view.findViewById(R.id.fragment_cast_playback_button_mute);
        ui.bindImageViewToMuteToggle(muteButton);

        ImageButton stopButton = view.findViewById(R.id.fragment_cast_playback_button_stop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ui.onStopClicked(stopButton);
            }
        });

        ImageButton disconnectButton = view.findViewById(R.id.fragment_cast_playback_button_disconnect);
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ui.onDisconnectClicked(disconnectButton);
            }
        });

        ImageButton rewindButton = view.findViewById(R.id.fragment_cast_playback_button_rewind);
        ui.bindViewToRewind(rewindButton, skipTime * 1000L);

        ImageButton playButton = view.findViewById(R.id.fragment_cast_playback_button_play);
        ui.bindImageViewToPlayPauseToggle(playButton,
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_control_play),
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_control_pause),
                null,//ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_control_stop),
                view.findViewById(R.id.fragment_cast_playback_loader),
                true);

        ImageButton forwardButton = view.findViewById(R.id.fragment_cast_playback_button_forward);
        ui.bindViewToForward(forwardButton, skipTime * 1000L);

        TextView progressText = view.findViewById(R.id.fragment_cast_playback_text_progress);
        ui.bindTextViewToStreamPosition(progressText, true);

        TextView durationText = view.findViewById(R.id.fragment_cast_playback_text_duration);
        ui.bindTextViewToStreamDuration(durationText);

        SeekBar progressBar = view.findViewById(R.id.fragment_cast_playback_seekbar_progress);
        ui.bindSeekBar(progressBar);*/

    }
}