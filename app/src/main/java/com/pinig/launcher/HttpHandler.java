package com.pinig.launcher;

/**
 * Created by varun on 15/12/16.
 */
import android.support.annotation.NonNull;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpHandler {

    private static final String TAG = HttpHandler.class.getSimpleName();

    public HttpHandler() {
    }

    public String getCall(String reqUrl) {
        String response = null;
        Log.i(TAG,"GET call made");
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int HttpResult = conn.getResponseCode();
            if (HttpResult==HttpURLConnection.HTTP_OK) {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                response = convertStreamToString(in);
            }
            else if (HttpResult==HttpURLConnection.HTTP_FORBIDDEN){
                response=null;
            }
        } catch (Exception e){
        Log.e(TAG, "Exception: " + e.getMessage());
        }
        return response;
    }

    public String[] postCall(String reqUrl, JSONObject jsonObject) {
        HttpResponse  response;
        String[] values = new String[2];
        try {

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(reqUrl);

            StringEntity input = new StringEntity(jsonObject.toString());
            input.setContentType("application/json");
            postRequest.setEntity(input);

            response = httpClient.execute(postRequest);

            HttpEntity httpEntity = response.getEntity();
            String apiOutput = EntityUtils.toString(httpEntity);
            JSONObject jsonObject2 = new JSONObject(apiOutput);
            values[0] = String.valueOf(response.getStatusLine().getStatusCode());
            values[1] = jsonObject2.toString();
            httpClient.getConnectionManager().shutdown();
            return values;

        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}