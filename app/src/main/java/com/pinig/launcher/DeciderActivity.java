package com.pinig.launcher;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pinig.pinigb2blauncher.R;

public class DeciderActivity extends AppCompatActivity {
    Logger log;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log = LoggerFactory.getLogger(DeciderActivity.class);
        log.info("Creating Decider Activity");
        setContentView(R.layout.activity_decider);
        if (readFromFile(getApplicationContext())==null) {
            log.info("No Dingo = Time to register");
            Intent regAct = new Intent(DeciderActivity.this, RegisterActivity.class);
            startActivity(regAct);
            finish();
        } else {
            log.info("Dingo found lets move on to the main action!");
            Intent mainAct = new Intent(DeciderActivity.this, MainActivity.class);
            startActivity(mainAct);
            finish();
        }
    }
    public String readFromFile(Context context) {

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
            log.error(e.getMessage());
        }

        return ret;
    }
}
