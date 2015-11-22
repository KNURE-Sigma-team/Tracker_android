package com.nure.sigma.wimk.wimk.logic;

import android.location.Location;
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


    public static final String SERVER_URL = "http://blockverify.cloudapp.net:8080/wimk/";
    public static final String EMPTY_STRING = "";
    public static final String SERVICE_TAG = "SERVICE";

    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String TIME = "time";
    public static final String BATTERY_LEVEL = "battery_level";
    public static final String POINT_TYPE = "point_type";
    public static final String LOCATIONS_LIST = "locations_list";

    public static final String COMMON = "common";
    public static final String SOS = "sos";

    public static final String RUNNING = "running";
    //Settings
    public static final String PASSWORD = "password";
    public static final String ID_CHILD = "idChild";
    public static final String IS_FIRST_ENTER = "isFirstEnter";
    public static final String FREQUENCY = "frequency";
    //Locations file
    public static final String LOCATIONS_FILE_NAME = "/failed.json";

    public static final int WAIT_TIME = 2000;

    // NON-STATIC
    public ArrayList<Pair<Location, String>> FAILED_LOCATIONS_LIST = new ArrayList<>();

    private  List<Child> childList = new ArrayList<>();

    public List<Child> getChildList() {
        return childList;
    }

    public void setChildList(List<Child> childList) {
        this.childList = childList;
    }
}
