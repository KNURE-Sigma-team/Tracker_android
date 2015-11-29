package com.nure.sigma.wimk.wimk.logic;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.BatteryManager;
import android.util.Pair;

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
    public static final String STORAGED = "storaged";
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

    public List<Pair<String, String>> getLoginsListForHttp(Context context){
        SharedPreferences settings = context.getSharedPreferences(Info.PASSWORD, 0);
        String parentLogin = settings.getString(Info.PARENT_LOGIN, null);
        String childLogin = settings.getString(Info.CHILD_LOGIN, null);

        ArrayList<Pair<String, String>> result = new ArrayList<>();
        result.add(new Pair<String, String>(PARENT_LOGIN, parentLogin));
        result.add(new Pair<String, String>(CHILD_LOGIN, childLogin));

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
}
