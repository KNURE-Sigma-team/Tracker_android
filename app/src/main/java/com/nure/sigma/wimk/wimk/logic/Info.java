package com.nure.sigma.wimk.wimk.logic;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.util.Pair;

import com.nure.sigma.wimk.wimk.BackgroundService;
import com.nure.sigma.wimk.wimk.InformStoppedTrackingTask;
import com.nure.sigma.wimk.wimk.MainActivity;
import com.nure.sigma.wimk.wimk.R;
import com.nure.sigma.wimk.wimk.SOSService;
import com.nure.sigma.wimk.wimk.ShortcutActivity;

import java.util.ArrayList;
import java.util.List;


public class Info {

    private static Info ourInstance = new Info();

    private Info() {
    }

    public static Info getInstance() {
        return ourInstance;
    }

    // production
    public static final String SERVER_URL = "http://blockverify.cloudapp.net:8080/wimk/";
    //local
    //public static final String SERVER_URL = "http://178.165.37.203:8080/wimk/";

    public static final String AUTH_SERVER_URL = SERVER_URL + "mobile_authorization";
    public static final String TOKEN_SERVER_URL = SERVER_URL + "mobile_set_child_token";
    public static final String LOGIN_SERVER_URL = SERVER_URL + "mobile_login";
    public static final String LOGOUT_SERVER_URL = SERVER_URL + "mobile_logout";
    public static final String DROP_GEO_SERVER_URL = SERVER_URL + "mobile_drop_geo";
    public static final String MOBILE_GET_POINT_URL = SERVER_URL + "mobile_get_point";

    public static final String EMPTY_STRING = "";
    public static final String SERVICE_TAG = "SERVICE";

    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String TIME = "time";
    public static final String BATTERY_LEVEL = "battery_level";
    public static final String POINT_TYPE = "point_type";
    public static final String LOCATIONS_LIST = "locations_list";
    public static final String TOKEN = "token";
    public static final int ERROR_CHILD_ID = 404;
    public static final int DEFAULT_SENDING_FREQUENCY = 30;

    public static final String COMMON = "common";
    public static final String SOS = "sos";
    public static final String STORED = "storaged";
    public static final String ON_DEMAND = "on_demand";

    public static final String RUNNING = "running";
    //Settings
    public static final String PASSWORD = "password";
    public static final String PARENT_LOGIN = "parent_login";
    //public static final String ID_CHILD = "id_child";
    public static final String CHILD_LOGIN = "child_login";
    public static final String IS_FIRST_ENTER = "is_first_enter";
    public static final String SENDING_FREQUENCY = "sending_frequency";
    //Locations file
    public static final String LOCATIONS_FILE_NAME = "/failed.json";

    public static final int WAIT_TIME = 2000;

    // NON-STATIC
    private List<Pair<Location, String>> failedLocationsList = new ArrayList<>();
    private  List<Child> childList = new ArrayList<>();
    private SharedPreferences settings = null;

    public SharedPreferences getSettings() {
        return settings;
    }

    public void setSettings(SharedPreferences settings) {
        this.settings = settings;
    }

    //region child list

    private boolean firstSending = true;

    public boolean isFirstSending() {
        return firstSending;
    }

    public void setFirstSending(boolean firstSending) {
        this.firstSending = firstSending;
    }

    public List<Child> getChildList(){
        return childList;
    }

    public void setChildList( List<Child> childList){
        this.childList = childList;
    }

    public List<Pair<Location, String>> getFailedLocations(){
        return failedLocationsList;
    }

    public void addFailedLocation(Pair<Location, String> pair){
        failedLocationsList.add(pair);
    }

    public void clearFailedLocations(){
        failedLocationsList = new ArrayList<>();
    }

    public List<Pair<String, String>> getParentAndChildLoginsListForHttp(){
        String parentLogin = settings.getString(Info.PARENT_LOGIN, null);
        String childLogin = settings.getString(Info.CHILD_LOGIN, null);

        ArrayList<Pair<String, String>> result = new ArrayList<>();
        result.add(new Pair<>(PARENT_LOGIN, parentLogin));
        result.add(new Pair<>(CHILD_LOGIN, childLogin));

        return result;
    }

    public int getBatteryLevel(Context context) {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level == -1 || scale == -1) {
            return 50;
        }

        return (int) (((float) level / (float) scale) * 100.0f);
    }

    public void moveToMainActivity(Activity contextActivity, String childName, int sendingFrequency){
        stopBackgroundServiceAndInformServer(contextActivity);

        // Log out
        SharedPreferences.Editor editor = settings.edit();

        // Save new data to DB
        editor.putString(Info.CHILD_LOGIN, childName);
        editor.putInt(Info.SENDING_FREQUENCY, sendingFrequency);
        editor.commit();
        Intent intent = new Intent(contextActivity, MainActivity.class);
        contextActivity.startActivity(intent);
    }

    public boolean isBackgroundserviceRunning(Context context){
        return isMyServiceRunning(BackgroundService.class, context);
    }

    public boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // Can be called from main stream.
    public boolean startBackgroundServiceAndInformServer(Context context){

        if(setRunning(true)){
            context.startService(new Intent(context.getApplicationContext(), BackgroundService.class));
            new InformStartedTrackingTask().execute();
            return true;
        }
        else{
            return false;
        }
    }

    // Can be called from main stream.
    public boolean stopBackgroundServiceAndInformServer(Context context){
        if(setRunning(false)){
            context.stopService(new Intent(context, BackgroundService.class));

            InformStoppedTrackingTask informStoppedTrackingTask = new InformStoppedTrackingTask();
            informStoppedTrackingTask.execute();

            return true;
        }
        else{
            return false;
        }
    }

    /**
     *
     * @return Whether the value was changed or not.
     */
    public boolean setRunning(boolean newValue) {
        if (newValue) {
            // Setting to running
            if(!settings.getBoolean(RUNNING, false)) {
                SharedPreferences.Editor e = settings.edit();
                e.putBoolean(RUNNING, true);
                e.apply();
                return true;
            }
            return false;
        }
        else {
            // Setting to NOT running.
            if (settings.getBoolean(RUNNING, false)) {
                SharedPreferences.Editor e = settings.edit();
                e.putBoolean(RUNNING, false);
                e.apply();
                return true;
            }
            return false;
        }
    }

    public MyHttpResponse informStoppedTracking(){
        DataSender dataSender = new DataSender();
        List<Pair<String, String>> pairs = getParentAndChildLoginsListForHttp();

        if((pairs.get(0).second != null) && (pairs.get(1).second != null)) {
            // Values of parent name and child name are not null.
            return dataSender.httpPostQuery(Info.LOGOUT_SERVER_URL, pairs, Info.WAIT_TIME);
        }
        return null;
    }


    private class InformStartedTrackingTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            DataSender dataSender = new DataSender();
            List<Pair<String, String>> pairs = getParentAndChildLoginsListForHttp();

            if((pairs.get(0).second != null) && (pairs.get(1).second != null)) {
                MyHttpResponse myHttpResponse = dataSender.httpPostQuery(Info.LOGIN_SERVER_URL, pairs, Info.WAIT_TIME);
            }
            return null;
        }
    }
}
