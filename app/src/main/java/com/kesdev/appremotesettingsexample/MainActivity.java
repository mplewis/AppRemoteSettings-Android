package com.kesdev.appremotesettingsexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppRemoteSettingsClient.updatePreferencesWithAppRemoteSettings(this,
                "https://appremotesettings-herokuapp-com-nz573rgl9zyn.runscope.net/api/v1/",
                null
        );
    }
}
