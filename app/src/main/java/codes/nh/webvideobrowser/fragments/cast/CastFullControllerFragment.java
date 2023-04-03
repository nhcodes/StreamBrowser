package codes.nh.webvideobrowser.fragments.cast;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.cast.MediaMetadata;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import codes.nh.webvideobrowser.R;
import codes.nh.webvideobrowser.fragments.sheet.SheetFragment;
import codes.nh.webvideobrowser.utils.AppUtils;

public class CastFullControllerFragment extends SheetFragment {

    public CastFullControllerFragment() {
        super(R.layout.fragment_cast_full_controller, R.string.navigation_title_cast);
        AppUtils.log("init CastFullControllerFragment");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CastViewModel castViewModel = new ViewModelProvider(requireActivity()).get(CastViewModel.class);

        CastUiController ui = new CastUiController(requireActivity(), castViewModel.getCastManager());

        /*ImageView thumbnailImage = view.findViewById(R.id.fragment_cast_full_controller_image_thumbnail);
        thumbnailImage.setImageResource(R.drawable.icon_stream_thumbnail);
        ImageHints imageHints = new ImageHints(ImagePicker.IMAGE_TYPE_UNKNOWN, 1920, 1080);
        ui.bindImageViewToImageOfCurrentItem(thumbnailImage, imageHints, R.drawable.placeholder); //TODO remove placeholder*/

        TextView titleText = view.findViewById(R.id.fragment_cast_full_controller_text_title);
        ui.bindTextViewToMetadataOfCurrentItem(titleText, MediaMetadata.KEY_TITLE);
        titleText.setSelected(true);

        TextView urlText = view.findViewById(R.id.fragment_cast_full_controller_text_url);
        ui.bindTextViewToMetadataOfCurrentItem(urlText, MediaMetadata.KEY_SUBTITLE);
        urlText.setSelected(true);

        ImageButton subtitlesButton = view.findViewById(R.id.fragment_cast_full_controller_button_subtitles);
        ui.bindViewToClosedCaption(subtitlesButton);

        ImageButton muteButton = view.findViewById(R.id.fragment_cast_full_controller_button_mute);
        ui.bindImageViewToMuteToggle(muteButton);

        ImageButton stopButton = view.findViewById(R.id.fragment_cast_full_controller_button_stop);
        ui.bindViewToStopButton(stopButton);

        ImageButton disconnectButton = view.findViewById(R.id.fragment_cast_full_controller_button_disconnect);
        ui.bindViewToDisconnectButton(disconnectButton);

        ImageButton rewindButton = view.findViewById(R.id.fragment_cast_full_controller_button_rewind);
        ui.bindViewToRewind(rewindButton);

        ImageButton playButton = view.findViewById(R.id.fragment_cast_full_controller_button_play);
        CircularProgressIndicator progressIndicator = view.findViewById(R.id.fragment_cast_full_controller_loader);
        ui.bindImageViewToPlayPauseButton(playButton, progressIndicator);

        ImageButton forwardButton = view.findViewById(R.id.fragment_cast_full_controller_button_forward);
        ui.bindViewToForward(forwardButton);

        TextView progressText = view.findViewById(R.id.fragment_cast_full_controller_text_progress);
        ui.bindTextViewToStreamPosition(progressText, true);

        TextView durationText = view.findViewById(R.id.fragment_cast_full_controller_text_duration);
        ui.bindTextViewToStreamDuration(durationText);

        SeekBar progressBar = view.findViewById(R.id.fragment_cast_full_controller_seekbar_progress);
        ui.bindSeekBar(progressBar);

    }
}