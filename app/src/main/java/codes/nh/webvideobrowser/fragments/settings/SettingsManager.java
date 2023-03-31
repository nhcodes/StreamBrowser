package codes.nh.webvideobrowser.fragments.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class SettingsManager {

    private final SharedPreferences preferences;

    public SettingsManager(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean getDesktopMode() {
        return preferences.getBoolean("preference_desktop_mode", false);
    }

    public boolean getBlockRedirects() {
        return preferences.getBoolean("preference_block_redirects", true);
    }

    public int getSkipTime() {
        return preferences.getInt("preference_skip_time", 15);
    }

    //listener

    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    public void registerListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        this.listener = listener;
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterListener() {
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
        listener = null;
    }
}
