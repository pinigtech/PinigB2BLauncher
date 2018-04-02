package com.pinig.launcher;


import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by varun on 5/3/18.
 */

public class AllCalls {
    final String TAG = "AllCalls";
    public String BaseUrl;
    Logger log;
    public AllCalls(String BaseUrl){
        log = LoggerFactory.getLogger(AllCalls.class);
        log.info(TAG,"AllCalls object created");
        this.BaseUrl = BaseUrl;
    }

    public String[] registerTab(String strSerialNumber,String strDeviceId){
        log.info("registerTab called");
        HttpHandler handler = new HttpHandler();
        String Url = BaseUrl+"/registerTab";
        JSONObject parameterToSend = new JSONObject();
        try {
            parameterToSend.accumulate("strSerialNumber", strSerialNumber);
            parameterToSend.accumulate("strDeviceId", strDeviceId);
        }
        catch(Exception e){
            log.error(e.getMessage());
        }
        String[] response = handler.postCall(Url,parameterToSend);
        log.info("Response Code from registerTab "+response[0]);
        log.info("Response Message from registerTab"+response[1]);
        return response;
    }

    public String[] getUpdates(String strDeviceId) {
        log.info("getUpdates called");
        String strWhatToUpdate = null;
        String[] allCallResponses =null;
        HttpHandler sh = new HttpHandler();
        String Url = BaseUrl + "/getUpdates";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.accumulate("strDeviceId", strDeviceId);
        }
        catch(Exception e){
            log.error(e.getMessage());
        }
        String[] jsonStr = sh.postCall(Url,jsonObject);
        if (jsonStr[1] != null) {
            try {
                JSONObject response = new JSONObject(jsonStr[1]);
                strWhatToUpdate = response.getString("strWhatToUpdate");
                log.info("strWhatToUpdate:"+strWhatToUpdate);
            } catch (JSONException e) {
                log.error(e.getMessage());
            }
            allCallResponses = makeNecessaryCalls(strDeviceId, strWhatToUpdate);
        }
        return allCallResponses;
    }

    public String getAPKs(String strDeviceId){
        log.info("getAPKs called");
        String strApkUrl = null;
        HttpHandler sh = new HttpHandler();
        String Url = BaseUrl + "/getAPKs";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.accumulate("strDeviceId", strDeviceId);
        }
        catch(Exception e){
            log.error(e.getMessage());
        }
        String[] jsonStr = sh.postCall(Url,jsonObject);
        if (jsonStr[1] != null) {
            try {
                JSONObject response = new JSONObject(jsonStr[1]);
                strApkUrl = response.getString("data");
                log.info(strApkUrl);
            } catch (JSONException e) {
                log.error(e.getMessage());
            }
        }
        return strApkUrl;
    }

    public String getAppsToRemove(String strDeviceId){
        log.info("getAppsToRemove called");
        String strAppName = null;
        HttpHandler sh = new HttpHandler();
        String Url = BaseUrl + "/getAppsToRemove";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.accumulate("strDeviceId", strDeviceId);
        }
        catch(Exception e){
            log.error(e.getMessage());
        }
        String[] jsonStr = sh.postCall(Url,jsonObject);
        if (jsonStr[1] != null) {
            try {
                JSONObject response = new JSONObject(jsonStr[1]);
                strAppName = response.getString("strAppName");
                log.info(strAppName);
            } catch (JSONException e) {
                log.error(e.getMessage());
            }
        }
        return strAppName;
    }
    public String getAddRemoveFromHomeScreen(String strDeviceId){
        log.info("getAddRemoveFromHomeScreen called");
        String strAppName = null;
        HttpHandler sh = new HttpHandler();
        String Url = BaseUrl + "/getAddRemoveFromHomeScreen";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.accumulate("strDeviceId", strDeviceId);
        }
        catch(Exception e){
            log.error(e.getMessage());
        }
        String[] jsonStr = sh.postCall(Url,jsonObject);
        if (jsonStr[1] != null) {
            try {
                JSONObject response = new JSONObject(jsonStr[1]);
                strAppName = response.getString("data");
                log.info(strAppName);
            } catch (JSONException e) {
                log.error(e.getMessage());
            }
        }
        return strAppName;
    }

    public String getWallpaper(String strDeviceId){
        log.info("getWallpaper called");
        String strWallpaperUrl = null;
        HttpHandler sh = new HttpHandler();
        String Url = BaseUrl + "/getWallpaper";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.accumulate("strDeviceId", strDeviceId);
        }
        catch(Exception e){
            log.error(e.getMessage());
        }
        String[] jsonStr = sh.postCall(Url,jsonObject);
        if (jsonStr[1] != null) {
            try {
                JSONObject response = new JSONObject(jsonStr[1]);
                strWallpaperUrl = response.getString("strWallpaperUrl");
                log.info(strWallpaperUrl);
            } catch (JSONException e) {
                log.error(e.getMessage());
            }
        }
        return strWallpaperUrl;
    }
    /*public String[] getDBTables(String strDeviceId){

    }
    public String[] getDataToInstert(String strDeviceId){

    }
    public String[] getDataToUpdate(String strDeviceId){

    }
    public String[] getDataToDelete(String strDeviceId){

    }
    */
    public void sendListOfApps(){}
    public void sendDeviceHealth(){}
    public void getBootAnimation(){}
    public void getBootLogo(){}

    public String[] makeNecessaryCalls(String strDeviceId,String strWhatToUpdate){
        log.info("makeNecessaryCalls called");
        String[] allCallResponses = new String[20];
        if (Integer.parseInt(Character.toString(strWhatToUpdate.charAt(0)))!=0) {
            if (Integer.parseInt(Character.toString(strWhatToUpdate.charAt(1))) == 1) {
                String x = getAPKs(strDeviceId);
                allCallResponses[0] = x;
            }
            if (Integer.parseInt(Character.toString(strWhatToUpdate.charAt(2))) == 1) {
                String x = getAppsToRemove(strDeviceId);
                allCallResponses[1]=x;
            }
            if(Integer.parseInt(Character.toString(strWhatToUpdate.charAt(3))) == 1){
                String x = getAddRemoveFromHomeScreen(strDeviceId);
                allCallResponses[2] = x;
            }
            if (Integer.parseInt(Character.toString(strWhatToUpdate.charAt(4))) == 1) {
                String x = getWallpaper(strDeviceId);
                allCallResponses[3] = x;
            }
            /*if(Integer.parseInt(Character.toString(strWhatToUpdate.charAt(5)))==1){
                getDBTables(strSerialNumber);
            }
            if(Integer.parseInt(Character.toString(strWhatToUpdate.charAt(6)))==1){
                getDataToInstert(strSerialNumber);
            }
            if(Integer.parseInt(Character.toString(strWhatToUpdate.charAt(7)))==1){
                getDataToUpdate(strSerialNumber);
            }
            if(Integer.parseInt(Character.toString(strWhatToUpdate.charAt(8)))==1){
                getDataToDelete(strSerialNumber);
            }
            */
            /*if (Integer.parseInt(Character.toString(strWhatToUpdate.charAt(9))) == 1) {
                sendListOfApps();
            }
            if (Integer.parseInt(Character.toString(strWhatToUpdate.charAt(10))) == 1) {
                sendDeviceHealth();
            }
            if (Integer.parseInt(Character.toString(strWhatToUpdate.charAt(11))) == 1) {
                getBootAnimation();
            }
            if (Integer.parseInt(Character.toString(strWhatToUpdate.charAt(12))) == 1) {
                getBootLogo();
            }
            */
        }
        log.info("Returned allCallResponses "+ Arrays.toString(allCallResponses));
        return allCallResponses;
    }
}
