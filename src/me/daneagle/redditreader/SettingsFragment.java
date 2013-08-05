package me.daneagle.redditreader;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by deagle on 7/24/13.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
