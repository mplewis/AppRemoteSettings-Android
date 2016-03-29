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

    public static final String TAG = "AppRemoteSettingsClient";

    /**
     * Updates a SharedPreferences object with values from an AppRemoteSettings server. Any
     * existing keys that collide with keys in the AppRemoteSettings data will be replaced with the
     * values from AppRemoteSettings.
     *
     * This method should be called as soon as possible in app execution so your settings are
     * up-to-date quickly. You should probably call it in your main Activity's onCreate method.
     *
     * This method will never block or throw an exception.
     *
     * @param context       The {@link Context} to use for creating an HTTP connection
     * @param endpointAPIv1 The URL for the AppRemoteSettings server's v1 API endpoint
     * @param preferences   The {@link SharedPreferences} object to be updated with
     *                      AppRemoteSettings data
     */
    public static void updatePreferencesWithAppRemoteSettings(
            Context context, String endpointAPIv1, final SharedPreferences preferences) {
        updatePreferencesWithAppRemoteSettings(context, endpointAPIv1, preferences, null);
    }

    /**
     * Updates a SharedPreferences object with values from an AppRemoteSettings server. Any
     * existing keys that collide with keys in the AppRemoteSettings data will be replaced with the
     * values from AppRemoteSettings.
     *
     * This method should be called as soon as possible in app execution so your settings are
     * up-to-date quickly. You should probably call it in your main Activity's onCreate method.
     *
     * This method will never block or throw an exception.
     *
     * @param context       The {@link Context} to use for creating an HTTP connection
     * @param endpointAPIv1 The URL for the AppRemoteSettings server's v1 API endpoint
     * @param preferences   The {@link SharedPreferences} object to be updated with
     *                      AppRemoteSettings data
     * @param onSuccess     The {@link Handler} to be called with a Map of {key: type} pairs for the
     *                      keys received from AppRemoteSettings. This is only called if the
     *                      operation succeeds. If it fails, nothing happens.
     */
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

    /**
     * Fetch raw JSON from AppRemoteSettings' V1 endpoint. If the request is successful, handler
     * is called with the response body. If it fails, nothing happens.
     *
     * @param context       The {@link Context} to use for creating an HTTP connection
     * @param endpointAPIv1 The URL for the AppRemoteSettings server's v1 API endpoint
     * @param handler       The {@link Handler} to call with response body on success
     */
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

    /**
     * Parse the raw JSON from AppRemoteSettings' V1 endpoint into typed objects. Store them in
     * a {@link SharedPreferences} object and commit them to disk.
     *
     * @param rawJson       The JSON body from AppRemoteSettings, annotated with types
     * @param preferences   The {@link SharedPreferences} object to be updated using
     *                      AppRemoteSettings data
     * @return
     */
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

    /**
     * Acts as a callback to return results asynchronously to the caller of
     * updatePreferencesWithAppRemoteSettings when SharedPreferences are successfully updated.
     * @param <T> the type of result to return
     */
    public abstract static class Handler<T> {
        public abstract void onResult(T t);
    }

    /**
     * Contains the data necessary to run an HTTP POST request with a JSON body.
     */
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

    /**
     * The task used to run an asynchronous HTTP POST action, validate the response, and do
     * something with the response body.
     */
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

                // Write the output stream to the HTTP connection
                OutputStream outputStream = connection.getOutputStream();
                writer = new BufferedWriter(new OutputStreamWriter(outputStream, "utf-8"));
                writer.write(requestHandler.body);
                writer.close();

                // Get the input stream
                InputStream inputStream;
                int status = connection.getResponseCode();
                if (status >= 400 && status <= 599) {
                    // Error occurred - we have to check getErrorStream instead of getInputStream
                    // http://stackoverflow.com/a/5379364/254187
                    inputStream = new BufferedInputStream(connection.getErrorStream());
                } else {
                    inputStream = new BufferedInputStream(connection.getInputStream());
                }

                // Read the input stream to a string
                reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String respBody = stringBuilder.toString();

                // Verify the status code was 200 OK
                if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                    Log.e(TAG, "Response code was not 200: got " + connection.getResponseCode());
                    Log.e(TAG, "Response body:");
                    Log.e(TAG, "    " + respBody);
                    return null;
                }

                // Return the response body as a string
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

}
