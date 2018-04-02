package com.pinig.launcher;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.lang.Long;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pinig.pinigb2blauncher.R;


public class MainActivity extends AppCompatActivity {
    DatabaseHandler dbHandler;
    AllCalls caller;
    String BaseUrl;
    PackageManager myPackageManager;
    GridView gridView;
    File files;
    Logger log;
    long updateTime;
    List<ResolveInfo> appIntentList;
    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        log = LoggerFactory.getLogger(MainActivity.class);
        log.info("MainActivity Started");
        myPackageManager = getPackageManager();
        files = new File(Environment.getExternalStorageDirectory().getPath()+"/Pinig/Downloads");
        dbHandler = new DatabaseHandler(this, "Pinig.db", null, 1);
        try{
            BaseUrl = Util.getProperty("BaseURL",getApplicationContext());
            updateTime = Long.parseLong(Util.getProperty("updateTime",getApplicationContext()));
        }
        catch(Exception e)
        {
            log.error("Error Retrieving Data from config.properties");
            log.error(e.getMessage());
        }
        caller = new AllCalls(BaseUrl);
        String deviceId = readFromFile(getApplicationContext());
        gridView = (GridView)findViewById(R.id.gridView);
        String[] apps = dbHandler.appsOnHomeScreen();
        appIntentList = getAppList(apps);
        gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        gridView.setAdapter(new AppAdapter(this, appIntentList));
        setContentView(gridView);
        refreshHomeScreen();
        callAsynchronousTask(updateTime,deviceId);
    }

    public String readFromFile(Context context) {
        log.info("readFromFile called");
        String ret = null;

        try {
            InputStream inputStream = context.openFileInput("Dingo.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (Exception e) {
            log.error("Error reading strDeviceId from Dingo.txt");
            log.error(e.getMessage());
        }
        log.info("readFromFile ended");
        return ret;
    }
    public List<ResolveInfo> getAppList(String[] appNames) throws IllegalArgumentException {
        log.info("getAppList called");
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps=new ArrayList<>();
        for(int i=0;i<appNames.length;i++)
        {
            for ( ResolveInfo info : getPackageManager().queryIntentActivities( mainIntent, 0) ) {
                if ( info.loadLabel(getPackageManager()).equals(appNames[i]) ) {
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(info.activityInfo.applicationInfo.packageName);
                    apps.add(getPackageManager().resolveActivity(launchIntent,0));
                }
            }
        }
        log.info("getAppList ended");
        return apps;
    }

    private class Update extends AsyncTask<String,Void,String[]> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            refreshHomeScreen();
        }
        protected String[] doInBackground(String... strings) {
            log.info("Update started");
            String[] allCallResponses = new String[20];
            int count = 0;
            String[] failed = new String[20];
            try{
                allCallResponses = caller.getUpdates(strings[0]);
                if(allCallResponses.length!=0)
                {
                    if(allCallResponses[0]!=null) {
                        JSONArray applist = new JSONArray(allCallResponses[0]);
                        failed = new String[applist.length()];
                        int z=0;
                        for (int i = 0; i < applist.length(); i++) {
                            JSONObject app = applist.getJSONObject(i);
                            if(!dbHandler.isAppOnDevice(app.getString("strAppName"),app.getString("strVersion"))) {
                                String link = app.getString("strApkUrl");
                                File apk = new File(files.getPath() + "/app" + String.valueOf(count) + ".apk");
                                boolean flag = downloadFile(link, apk);
                                if(flag) {
                                    dbHandler.addApp(app.getString("strAppName"), app.getString("strVersion"));
                                }
                                else{
                                    failed[z]=String.valueOf(count);
                                    z++;
                                }
                                count++;
                            }
                        }
                    }
                    if(allCallResponses[1]!=null) {
                        JSONArray removeList = new JSONArray(allCallResponses[1]);
                        for (int i = 0; i < removeList.length(); i++) {
                            dbHandler.removeApp(removeList.getString(i));
                        }
                    }
                    if(allCallResponses[2]!=null) {
                        JSONArray homeScreenList = new JSONArray(allCallResponses[2]);
                        for(int i=0;i<homeScreenList.length();i++){
                            JSONObject app = homeScreenList.getJSONObject(i);
                            int addOrRemove = app.getInt("strAddToHomeScreen");
                            dbHandler.addOnHomeScreen(app.getString("strAppName"),addOrRemove);
                        }
                    }
                    if(allCallResponses[3]!=null){
                        String wallpaperLink = allCallResponses[3];
                        File wppr = new File(files.getPath()+"/wppr.png");
                        downloadFile(wallpaperLink,wppr);
                    }
                    List<String> failedList = Arrays.asList(failed);
                    for(int i = 0;i<count;i++) {
                        if(!failedList.contains(String.valueOf(i))) {
                            File apk = new File(files.getPath() + "/app" + String.valueOf(i) + ".apk");
                            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                            intent.setData(Uri.fromFile(apk));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }
                    if(allCallResponses[1]!=null) {
                        try {
                            JSONArray appsToRemove = new JSONArray(allCallResponses[1]);
                            for(int i=0;i<appsToRemove.length();i++){
                                String appName = appsToRemove.getString(i);
                                uninstall(appName);
                                dbHandler.removeApp(appName);
                            }
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    }
                }
                String[] y = {String.valueOf(count),allCallResponses[1]};
                return y;
            }
            catch (Exception e){
                log.error(e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(final String[] result) {
            super.onPostExecute(result);
            log.info("Update ended");
            refreshHomeScreen();
        }
    }

    private boolean downloadFile(String url, File outputFile) {
        log.info("downloadFile called");
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            int contentLength = conn.getContentLength();

            DataInputStream stream = new DataInputStream(u.openStream());

            byte[] buffer = new byte[contentLength];
            stream.readFully(buffer);
            stream.close();

            DataOutputStream fos = new DataOutputStream(new FileOutputStream(outputFile));
            fos.write(buffer);
            fos.flush();
            fos.close();
            log.info("downloadFile ended");
            return true;
        } catch(Exception e){
            log.error("downloadFile failed");
            log.error(e.getMessage());
            return false;
        }
    }



    public void callAsynchronousTask(long time, final String deviceId) {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            if(isNetworkAvailable()) {
                                Update performBackgroundTask = new Update();
                                performBackgroundTask.execute(deviceId);
                            }
                            else{
                                Toast.makeText(MainActivity.this, "Please Connect to Internet to refresh Device", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, time);
    }

    public void uninstall(String appName) throws IllegalArgumentException {
        log.info("uninstalling app "+appName);
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        for ( ResolveInfo info : getPackageManager().queryIntentActivities( mainIntent, 0) ) {
            if ( info.loadLabel(getPackageManager()).equals(appName) ) {
                Intent intent = new Intent(Intent.ACTION_DELETE);
                intent.setData(Uri.parse("package:"+info.activityInfo.packageName));
                startActivity(intent);
            }
        }
        log.info("uninstall ended");
    }
    private boolean isNetworkAvailable() {
        log.info("Checking if Network Is Available");
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean response =  activeNetworkInfo != null && activeNetworkInfo.isConnected();
        log.info(String.valueOf(response));
        return response;
    }
    public void refreshHomeScreen(){
        File wprfl = new File(files.getPath()+"/wppr.png");
        if(wprfl!=null) {
            log.info("Valid Wall Paper found");
            Bitmap wppr = BitmapFactory.decodeFile(wprfl.getPath());
            BitmapDrawable wallpaper = new BitmapDrawable(wppr);
            gridView.setBackground(wallpaper);
        }
        else{
            log.info("Wall Paper found null");
        }
        String[] apps = dbHandler.appsOnHomeScreen();
        if(apps!=null) {
            appIntentList = getAppList(apps);
            gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
            gridView.setAdapter(new AppAdapter(this, appIntentList));
            setContentView(gridView);
        }
        else{
            log.info("No apps from DB");
        }
    }
}


