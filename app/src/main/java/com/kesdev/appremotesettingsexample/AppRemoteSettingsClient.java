package com.kesdev.appremotesettingsexample;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class AppRemoteSettingsClient {

    public abstract static class Handler<T> {
        public abstract void onResult(T t);
    }

    private static class JsonPostRequestHandler {
        HttpsURLConnection connection;
        String body;
        Handler<String> handler;

        public JsonPostRequestHandler(String url, JSONObject data, Handler<String> handler)
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
            if (requestHandler.handler != null && s != null) requestHandler.handler.onResult(s);
        }
    }

    public static final String TAG = "AppRemoteSettingsClient";

    private static void fetchRawJsonFromAppRemoteSettings(
            Context context, String endpointAPIv1, Handler<String> handler) {

        try {
            JSONObject data = new JSONObject();
            data.put("app_id", context.getPackageName());
            data.put("format", "json_annotated");
            new HttpPostTask().execute(new JsonPostRequestHandler(endpointAPIv1, data, handler));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> parseRawJsonIntoSharedPreferences(
            String rawJson, SharedPreferences preferences) {

        SharedPreferences.Editor editor = preferences.edit();
        Map<String, String> added = new HashMap<>();

        try {
            JSONObject parsed = new JSONObject(rawJson);
            JSONObject types = parsed.getJSONObject("types");
            JSONObject values = parsed.getJSONObject("values");
            Iterator<String> keys = types.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String type = types.getString(key);

                if (type.equals("bool")) {
                    editor.putBoolean(key, values.getBoolean(key));
                } else if (type.equals("int")) {
                    editor.putLong(key, values.getLong(key));
                } else if (type.equals("float")) {
                    editor.putFloat(key, (float) values.getDouble(key));
                } else if (type.equals("string")) {
                    editor.putString(key, values.getString(key));
                }

                added.put(key, type);
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSON processing failed");
            e.printStackTrace();
            return null;
        }

        boolean success = editor.commit();
        if (!success) {
            Log.e(TAG, "Failed to commit preferences");
            return null;
        }

        return added;
    }

    public static void updatePreferencesWithAppRemoteSettings(
            Context context, String endpointAPIv1, final SharedPreferences preferences,
            final Handler<Map<String, String>> onSuccess) {

        fetchRawJsonFromAppRemoteSettings(context, endpointAPIv1, new Handler<String>() {
            @Override
            public void onResult(String rawJson) {
                Map<String, String> added = parseRawJsonIntoSharedPreferences(rawJson, preferences);
                if (onSuccess != null) onSuccess.onResult(added);
            }
        });
    }

    public static void updatePreferencesWithAppRemoteSettings(
            Context context, String endpointAPIv1, final SharedPreferences preferences) {
        updatePreferencesWithAppRemoteSettings(context, endpointAPIv1, preferences, null);
    }

}
