package com.kesdev.appremotesettingsexample;

import android.app.Application;

import java.net.MalformedURLException;
import java.net.URL;

public class ExampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            AppRemoteSettingsClient.fetchJsonFromAppRemoteSettings(new URL("https://example.com"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
