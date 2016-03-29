package com.kesdev.appremotesettingsexample;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    // To use the same SharedPreferences across activities, call this.getSharedPreferences with
    // the same key in each activity. MODE_PRIVATE is the only valid access mode for
    // SharedPreferences.
    final SharedPreferences prefs = this.getSharedPreferences("APPLICATION", MODE_PRIVATE);

    // This is the demo instance of AppRemoteSettings. Point this URL to your own server.
    private static final String APP_REMOTE_SETTINGS_SERVER =
            "https://appremotesettings.herokuapp.com/api/v1/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Launch your app's main activity
        //
        // The first time your app is launched, settings will all be unset.
        //
        Log.i(TAG, "App launched");
        printPreferences(prefs);

        // 2. OPTIONAL: Set up a handler for AppRemoteSettings completion
        //
        // When AppRemoteSettingsClient successfully updates the local settings, it will call the
        // onResult method of the AppRemoteSettingsClient.Handler passed in.
        //
        // The object passed as a parameter is a map. Keys are the keys added to the
        // SharedPreferences, while values represent what type each key was
        // ("string", "bool", "int", "float").
        //
        AppRemoteSettingsClient.Handler<Map<String, String>> handler =
                new AppRemoteSettingsClient.Handler<Map<String, String>>() {
                    @Override
                    public void onResult(Map<String, String> addedToPrefs) {
                        Log.i(TAG, "Fetched remote app settings");
                        printPreferences(prefs);
                    }
                };

        // 3. Update your local settings from AppRemoteSettings
        //
        // Fetch settings from AppRemoteSettings and apply them to the SharedPreferences,
        // overwriting any keys that may already exist.
        //
        // Ints are stored as longs to avoid accidental truncation.
        //
        AppRemoteSettingsClient.updatePreferencesWithAppRemoteSettings(
                this,
                APP_REMOTE_SETTINGS_SERVER,
                prefs,
                handler  // If you don't care about the success handler, just omit this argument
        );

        // 4. You're done!
        //
        // When you develop your app, use the values from SharedPreferences instead of hardcoding
        // your variables. These values will be pulled from the server every time your user starts
        // their app.
        //
        // If the app can't connect to the internet, it will use the last retrieved values. If no
        // values have been retrieved yet, it will use the default values you define.
    }

    private void printPreferences(SharedPreferences prefs) {
        // Whenever you want to use a variable you're defining remotely,
        // request it from SharedPreferences:
        //
        // prefs.getTYPE("KEY_FOR_VALUE", DEFAULT_VALUE).
        //
        // The examples below retrieve a boolean, string, and integer (long) value from
        // SharedPreferences while also providing a default value for offline development.

        boolean enableRetroEncabulator = prefs.getBoolean("ENABLE_RETRO_ENCABULATOR", false);
        String encabulatorMode = prefs.getString("ENCABULATOR_MODE", "conservative");
        long panametricFanRpm = prefs.getLong("PANAMETRIC_FAN_RPM", 4200);

        Log.i(TAG, "SharedPreferences:");
        Log.i(TAG, String.format("    ENABLE_RETRO_ENCABULATOR: %s", enableRetroEncabulator));
        Log.i(TAG, String.format("    ENCABULATOR_MODE: %s", encabulatorMode));
        Log.i(TAG, String.format("    PANAMETRIC_FAN_RPM: %s", panametricFanRpm));
    }
}
