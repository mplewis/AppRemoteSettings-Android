package com.kesdev.appremotesettingsexample;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class AppRemoteSettingsClient {

    private abstract static class Handler {
        public abstract void onResult(String s);
    }

    private static class JsonPostRequestHandler {
        HttpsURLConnection connection;
        String body;
        Handler handler;

        public JsonPostRequestHandler(String url, JSONObject data, Handler handler)
                throws IOException {

            connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            body = data.toString();
            this.handler = handler;
        }
    }

    private static class HttpPostTask extends AsyncTask<JsonPostRequestHandler, Void, String> {
        JsonPostRequestHandler requestHandler;

        @Override
        protected String doInBackground(JsonPostRequestHandler... params) {
            requestHandler = params[0];
            BufferedReader reader = null;
            BufferedWriter writer = null;

            //noinspection TryFinallyCanBeTryWithResources
            try {
                HttpsURLConnection connection = requestHandler.connection;

                OutputStream outputStream = connection.getOutputStream();
                writer = new BufferedWriter(new OutputStreamWriter(outputStream, "utf-8"));
                writer.write(requestHandler.body);
                writer.close();

                InputStream inputStream;
                int status = connection.getResponseCode();
                if (status >= 400 && status <= 599) {
                    // Error occurred - we have to check getErrorStream instead of getInputStream
                    // http://stackoverflow.com/a/5379364/254187
                    inputStream = new BufferedInputStream(connection.getErrorStream());
                } else {
                    inputStream = new BufferedInputStream(connection.getInputStream());
                }

                reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String respBody = stringBuilder.toString();

                if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                    Log.e(TAG, "Response code was not 200: got " + connection.getResponseCode());
                    Log.e(TAG, "Response body:");
                    Log.e(TAG, "    " + respBody);
                    return null;
                }

                return respBody;

            } catch (Exception e) {
                Log.e(TAG, "Error fetching JSON");
                e.printStackTrace();
                return null;

            } finally {
                try {
                    if (writer != null) writer.close();
                    if (reader != null) reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (requestHandler.handler != null) requestHandler.handler.onResult(s);
        }
    }

    public static final String TAG = "AppRemoteSettingsClient";

    public static void fetchRawJsonFromAppRemoteSettings(
            Context context, String endpointAPIv1, Handler handler) {

        try {
            JSONObject data = new JSONObject();
            data.put("app_id", context.getPackageName());
            data.put("format", "json_annotated");

            JsonPostRequestHandler requestHandler =
                    new JsonPostRequestHandler(endpointAPIv1, data, handler);
            new HttpPostTask().execute(requestHandler);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseRawJsonIntoSharedPreferences(SharedPreferences preferences) {

    }

    public static void updatePreferencesWithAppRemoteSettings(
            Context context, String endpointAPIv1, SharedPreferences preferences) {

        fetchRawJsonFromAppRemoteSettings(context, endpointAPIv1, new Handler() {
            @Override
            public void onResult(String s) {
                Log.d(TAG, s);
            }
        });
    }

}
