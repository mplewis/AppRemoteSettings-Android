package com.kesdev.appremotesettingsexample;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class AppRemoteSettingsClient {

    private abstract static class URLForStringResult {
        URL url;
        public abstract void onResult(String s);
    }

    private static class HttpPostTask extends AsyncTask<URLForStringResult, Void, String> {
        URLForStringResult urlAndResult;

        @Override
        protected String doInBackground(URLForStringResult... params) {
            urlAndResult = params[0];
            URL url = urlAndResult.url;

            InputStream inputStream = null;
            try {
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                if (connection.getResponseCode() != 200) {
                    Log.e(TAG, "Response code was not 200: got " + connection.getResponseCode());
                    return null;
                }

                inputStream = new BufferedInputStream(connection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                return stringBuilder.toString();

            } catch (Exception e) {
                Log.e(TAG, "Error fetching JSON");
                e.printStackTrace();
                return null;

            } finally {
                try {
                    if (inputStream != null) inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Couldn't close input stream");
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null) urlAndResult.onResult(s);
        }
    }

    public static final String TAG = "AppRemoteSettingsClient";

    public static void fetchJsonFromAppRemoteSettings(URL endpointV1API) {
        URLForStringResult urlForStringResult = new URLForStringResult() {
            @Override
            public void onResult(String s) {
                Log.d(TAG, s);
            }
        };
        urlForStringResult.url = endpointV1API;
        new HttpPostTask().execute(urlForStringResult);
    }

    private static void parseJsonIntoSharedPreferences(SharedPreferences preferences) {

    }

    public static void updatePreferencesWithAppRemoteSettings(SharedPreferences preferences,
                                                              URL endpointV1API) {

    }

}
