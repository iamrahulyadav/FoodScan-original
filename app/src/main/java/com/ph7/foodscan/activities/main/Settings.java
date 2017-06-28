package com.ph7.foodscan.activities.main;

import android.os.Bundle;


import com.ph7.foodscan.activities.AppActivity;
import com.ph7.foodscan.fragments.SettingsFragment;


public class Settings extends AppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_settings);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }

}
