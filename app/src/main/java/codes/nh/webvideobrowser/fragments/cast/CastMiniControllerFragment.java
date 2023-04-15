package codes.nh.webvideobrowser.fragments.cast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.cast.framework.media.widget.MiniControllerFragment;

import codes.nh.webvideobrowser.MainViewModel;
import codes.nh.webvideobrowser.fragments.sheet.SheetRequest;

public class CastMiniControllerFragment extends MiniControllerFragment {

    private MainViewModel mainViewModel;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle bundle) {
        View view = super.onCreateView(inflater, container, bundle);
        ViewGroup layout = (ViewGroup) view;
        View contentView = layout.getChildAt(1); //layout has 2 views, 0:seekbar (probably) and 1:relativeview
        contentView.setOnClickListener(v -> {
            mainViewModel.openSheet(new SheetRequest(CastFullControllerFragment.class));
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    }
}

/*import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.cast.MediaMetadata;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import codes.nh.webvideobrowser.R;

public class CastMiniControllerFragment extends Fragment {

    public CastMiniControllerFragment() {
        super(R.layout.fragment_cast_mini_controller);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CastViewModel castViewModel = new ViewModelProvider(requireActivity()).get(CastViewModel.class);

        CastUiController ui = new CastUiController(requireActivity(), castViewModel.getCastManager());

        TextView titleText = view.findViewById(R.id.fragment_cast_mini_controller_text_title);
        ui.bindTextViewToMetadataOfCurrentItem(titleText, MediaMetadata.KEY_TITLE);
        titleText.setSelected(true);

        TextView urlText = view.findViewById(R.id.fragment_cast_mini_controller_text_url);
        ui.bindTextViewToMetadataOfCurrentItem(urlText, MediaMetadata.KEY_SUBTITLE);
        urlText.setSelected(true);

        ImageButton playButton = view.findViewById(R.id.fragment_cast_mini_controller_button_play);
        CircularProgressIndicator progressIndicator = view.findViewById(R.id.fragment_cast_mini_controller_loader);
        ui.bindImageViewToPlayPauseButton(playButton, progressIndicator);

    }
}*/