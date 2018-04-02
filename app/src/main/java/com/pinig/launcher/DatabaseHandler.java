package com.pinig.launcher;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;


public class DatabaseHandler extends SQLiteOpenHelper {
    Logger log;
    private static final int DATABASE_VERSION=1;
    public static final String TABLE_Device_Configuration="Device_Configuration";
    public static final String TABLE_Device_App="Device_App";
    public static final String COLUMN_iDeviceConfigurationID="iDeviceConfigurationID";
    public static final String COLUMN_strKey="strKey";
    public static final String COLUMN_strValue = "strValue";
    public static final String COLUMN_isDeleted="isDeleted";
    public static final String COLUMN_dtCreateDate="dtCreateDate";
    public static final String COLUMN_dtUpdateDate="dtUpdateDate";
    public static final String COLUMN_iDeviceAppID="iDeviceAppID";
    public static final String COLUMN_strAppName="strAppName";
    public static final String COLUMN_strVersion="strVersion";
    public static final String COLUMN_strOnHomeScreen="strOnHomeScreen";


    public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, DATABASE_VERSION);
        log = LoggerFactory.getLogger(DatabaseHandler.class);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query1 = "CREATE TABLE " + TABLE_Device_Configuration + " ( " + COLUMN_iDeviceConfigurationID + "  INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_strKey +" TEXT, "+COLUMN_strValue +" TEXT, "+COLUMN_isDeleted+" INTEGER, "+COLUMN_dtCreateDate+" INTEGER, "+COLUMN_dtUpdateDate+" INTEGER );";
        String query2 = "CREATE TABLE " + TABLE_Device_App + " ( " + COLUMN_iDeviceAppID + "  INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_strAppName +" TEXT, "+COLUMN_strVersion +" TEXT, "+COLUMN_strOnHomeScreen+" INTEGER DEFAULT 0, "+COLUMN_isDeleted+" INTEGER DEFAULT 0, "+COLUMN_dtCreateDate+" INTEGER, "+COLUMN_dtUpdateDate+" INTEGER );";
        db.execSQL(query1);
        db.execSQL(query2);
        log.info("Database Created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_Device_Configuration);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_Device_App);
        onCreate(db);
        log.info("Database Upgraded");
    }

    public void addApp(String appName,String version) {
        log.info("About to add the following app to Database "+appName);
        ContentValues values = new ContentValues();
        values.put(COLUMN_strAppName, appName);
        values.put(COLUMN_strVersion, version);
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_Device_App, null, values);
        db.close();
        log.info("App added to Database");
    }
    public void removeApp(String appName){
        log.info("About to set isDeleted 1 for the following app "+appName);
        SQLiteDatabase db = getWritableDatabase();
        String query = "UPDATE "+TABLE_Device_App+" SET "+ COLUMN_isDeleted+" = 1 WHERE "+COLUMN_strAppName+ " = '"+appName+"'";
        db.execSQL(query);
        db.close();
        log.info("isDeleted Updated");
    }
    public void addOnHomeScreen(String appName,int intAddOrRemove){
        log.info("About to set strOnHomeScreen "+String.valueOf(intAddOrRemove)+" for the following app "+appName);
        SQLiteDatabase db = getWritableDatabase();
        String query = "UPDATE "+TABLE_Device_App+" SET "+ COLUMN_strOnHomeScreen+" = "+ String.valueOf(intAddOrRemove)+ " WHERE "+COLUMN_strAppName+ " = '"+appName+"'";
        db.execSQL(query);
        db.close();
        log.info("strOnHomeScreen Updated");
    }
    public String[] appsOnHomeScreen(){
        log.info("Getting all apps to put on Home Screen");
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT "+COLUMN_strAppName+" FROM "+TABLE_Device_App+" WHERE "+COLUMN_strOnHomeScreen+"=1 AND "+COLUMN_isDeleted+"=0 ORDER BY "+COLUMN_strAppName;
        Cursor c= db.rawQuery(query,null);
        c.moveToFirst();
        String[] array = new String[c.getCount()];
        for(int i=0;i<c.getCount();i++){
            array[i]=c.getString(0);
            c.moveToNext();
        }
        c.close();
        db.close();
        log.info(Arrays.toString(array)+" Returned");
        return array;
    }
    public boolean isAppOnDevice(String appName,String appVersion){
        log.info("Checking if the following app is installed on Device "+appName);
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM "+TABLE_Device_App+" WHERE "+COLUMN_strAppName+"='"+appName+"' AND "+COLUMN_strVersion+"='"+appVersion+"' AND "+COLUMN_isDeleted+"=0";
        Cursor c = db.rawQuery(query,null);
        if(c.getCount()==0){
            c.close();
            db.close();
            log.info("False");
            return false;
        }
        c.close();
        db.close();
        log.info("True");
        return true;
    }
}
