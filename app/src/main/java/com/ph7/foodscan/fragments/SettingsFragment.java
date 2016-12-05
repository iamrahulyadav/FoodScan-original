package com.ph7.foodscan.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.ph7.foodscan.R;

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
