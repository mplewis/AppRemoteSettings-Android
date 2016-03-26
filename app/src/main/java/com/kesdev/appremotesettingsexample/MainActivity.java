package com.kesdev.appremotesettingsexample;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        AppRemoteSettingsClient.updatePreferencesWithAppRemoteSettings(
                this,
                "https://appremotesettings.herokuapp.com/api/v1/",
                PreferenceManager.getDefaultSharedPreferences(this),
                new AppRemoteSettingsClient.Handler<Map<String, String>>() {
                    @Override
                    public void onResult(Map<String, String> addedToPrefs) {
                        Map<String, ?> keys = prefs.getAll();
                        for (Map.Entry<String, ?> entry : keys.entrySet()) {
                            Log.d(TAG, entry.getKey() + ": " + entry.getValue().toString());
                        }
                    }
                }
        );
    }
}
