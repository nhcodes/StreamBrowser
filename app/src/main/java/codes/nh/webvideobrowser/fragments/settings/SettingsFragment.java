package codes.nh.webvideobrowser.fragments.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import codes.nh.webvideobrowser.MainViewModel;
import codes.nh.webvideobrowser.R;
import codes.nh.webvideobrowser.fragments.browser.BrowserViewModel;
import codes.nh.webvideobrowser.fragments.sheet.SheetFragment;
import codes.nh.webvideobrowser.utils.AppUtils;
import codes.nh.webvideobrowser.utils.SnackbarRequest;

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
        settingsManager.registerListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equalsIgnoreCase("preference_desktop_mode")) {
                    browserViewModel.setDesktopMode(settingsManager.getDesktopMode());
                } else if (key.equalsIgnoreCase("preference_block_redirects")) {
                    //webView.setBlockRedirects(settingsManager.getBlockRedirects());
                } else if (key.equalsIgnoreCase("preference_skip_time")) {
                    mainViewModel.showSnackbar(new SnackbarRequest(getString(R.string.toast_restart_to_apply)));
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        settingsManager.unregisterListener();
    }
}