package codes.nh.webvideobrowser.screens.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import codes.nh.webvideobrowser.R;

public class PreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }

}