package codes.nh.streambrowser.screens.settings;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import codes.nh.streambrowser.R;
import codes.nh.streambrowser.screens.browser.BrowserViewModel;
import codes.nh.streambrowser.screens.main.MainViewModel;
import codes.nh.streambrowser.screens.main.SnackbarRequest;
import codes.nh.streambrowser.screens.sheet.SheetFragment;
import codes.nh.streambrowser.utils.AppUtils;

public class SettingsFragment extends SheetFragment {

    public SettingsFragment() {
        super(R.layout.fragment_settings, R.string.navigation_title_settings);
        AppUtils.log("init SettingsFragment");
    }

    private BrowserViewModel browserViewModel;

    private MainViewModel mainViewModel;

    private SettingsManager settingsManager;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        browserViewModel = new ViewModelProvider(requireActivity()).get(BrowserViewModel.class);

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        settingsManager = new SettingsManager(getApplicationContext());
        settingsManager.registerListener((sharedPreferences, key) -> {
            if (key.equalsIgnoreCase("preference_desktop_mode")) {
                browserViewModel.setDesktopMode(settingsManager.getDesktopMode());
            } else if (key.equalsIgnoreCase("preference_block_redirects")) {

            } else if (key.equalsIgnoreCase("preference_skip_time")) {
                mainViewModel.showSnackbar(new SnackbarRequest(getString(R.string.toast_restart_to_apply)));
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        settingsManager.unregisterListener();
    }

}