package com.ph7.analyserforscio.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.ph7.analyserforscio.R;

/**
 * Created by sony on 30-08-2016.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
    }
}
