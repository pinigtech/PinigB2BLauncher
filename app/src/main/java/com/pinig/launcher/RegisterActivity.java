package com.pinig.launcher;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.test.mock.MockPackageManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import pinig.pinigb2blauncher.R;

public class RegisterActivity extends AppCompatActivity {
EditText serial;
    Button submit;
    AllCalls caller;
    String randomID;
    String BaseUrl;
    ProgressDialog pd;
    File downloadFoler;
    Logger log;
    DatabaseHandler dbHandler;
    private static final int REQUEST_CODE_PERMISSION = 2;
    String[] mPermission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log = LoggerFactory.getLogger(RegisterActivity.class);
        log.info("RegisterActivity Started");
        setContentView(R.layout.activity_register);
        dbHandler = new DatabaseHandler(this,"Pinig.db",null,1);
        try {
            if (ActivityCompat.checkSelfPermission(this, mPermission[0]) != MockPackageManager.PERMISSION_GRANTED )
            {

                ActivityCompat.requestPermissions(this, mPermission, REQUEST_CODE_PERMISSION);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        serial = (EditText)findViewById(R.id.serial);
        submit = (Button)findViewById(R.id.submit);
        randomID = UUID.randomUUID().toString();
        try{
            BaseUrl = Util.getProperty("BaseURL",getApplicationContext());
        }
        catch(Exception e)
        {
            log.error(e.getMessage());
        }
        caller = new AllCalls(BaseUrl);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log.info("Submit button clicked - lets register this tab");
                if(isNetworkAvailable()) {
                    if (!serial.getText().toString().equals("")) {
                        downloadFoler = new File(Environment.getExternalStorageDirectory().getPath() + "/Pinig/Downloads");
                        if (!downloadFoler.exists()) {
                            downloadFoler.mkdirs();
                        }
                        new Register().execute(serial.getText().toString(), randomID);
                    }
                }
                else{
                    Toast.makeText(RegisterActivity.this, "Please Connect to the Internet and try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class Register extends AsyncTask<String,Void,String[]> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            log.info("Register called - begining Pre Execute");
            pd = new ProgressDialog(RegisterActivity.this);
            pd.setMessage("Setting Up Device For First Use");
            pd.setCancelable(false);
            pd.show();
        }

        protected String[] doInBackground(String... strings) {
            log.info("Background action started - about to call register tab");
            String[] responseFromRegisterTab = caller.registerTab(strings[0],strings[1]);
            String[] allCallResponses = new String[20];
            JSONObject jsonObject;
            try{
                jsonObject = new JSONObject(responseFromRegisterTab[1]);
                String x = jsonObject.getString("MSG");
                int count = 0;
                String[] failed = new String[20];
                if(Integer.parseInt(responseFromRegisterTab[0])==200)
                {
                    if(readFromFile(getApplicationContext())==null) {
                        writeToFile(strings[1], getApplicationContext());
                    }
                    allCallResponses = caller.getUpdates(strings[1]);
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
                                    File apk = new File(downloadFoler.getPath() + "/app" + String.valueOf(count) + ".apk");
                                    boolean flag = downloadFile(link, apk);
                                    if(flag) {
                                        dbHandler.addApp(app.getString("strAppName"), app.getString("strVersion"));
                                        log.info("App downloaded succesfully and entered in DB" + app.getString("strAppName"));
                                    }
                                    else{
                                        failed[z]=String.valueOf(count);
                                        z++;
                                        log.info("Dont think it ever comes here");
                                    }
                                    count++;
                                }
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
                            File wppr = new File(downloadFoler.getPath()+"/wppr.png");
                            downloadFile(wallpaperLink,wppr);
                        }

                    }
                }
                String[] y = {responseFromRegisterTab[0],x,String.valueOf(count),allCallResponses[1],String.valueOf(failed)};
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
            log.info("Register ended");
            if(pd.isShowing()){
                pd.dismiss();
            }
            if(Integer.parseInt(result[0])==200) {
                Intent mainAct = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(mainAct);
                List<String> failed = Arrays.asList(result[4]);
                for(int i = 0;i<Integer.parseInt(result[2]);i++) {
                    if(!failed.contains(String.valueOf(i))) {
                        File apk = new File(downloadFoler.getPath() + "/app" + String.valueOf(i) + ".apk");
                        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                        intent.setData(Uri.fromFile(apk));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
                if(result[3]!=null) {
                    try {
                        JSONArray appsToRemove = new JSONArray(result[3]);
                        for(int i=0;i<appsToRemove.length();i++){
                            String appName = appsToRemove.getString(i);
                            uninstall(appName);
                            dbHandler.removeApp(appName);
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
                finish();
            }
            else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RegisterActivity.this, result[1], Toast.LENGTH_SHORT).show();
                    }
                });
            }
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
    private void writeToFile(String data,Context context) {
        log.info("writeToFile called");
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("Dingo.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            log.info("writeToFile ended");
        }
        catch (IOException e) {
            log.error("File write failed: " + e.toString());
            log.error(e.getMessage());
        }
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
            log.error("readFromFile failed");
            log.error(e.getMessage());
        }
        log.info("readFromFile ended");
        return ret;
    }
    private boolean isNetworkAvailable() {
        log.info("Checking if Network Is Available");
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean response = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        log.info(String.valueOf(response));
        return response;
    }
}
