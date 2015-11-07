package com.nure.sigma.wimk.wimk.logic;

/**
 * Created by andstepko on 31.10.15.
 */
public class Info {

    public static final String SERVER_URL = "http://blockverify.cloudapp.net:8080/wimk/";
    public static final String EMPTY_STRING = "";
    public static final String SERVICE_TAG = "SERVICE";

    public static final String LONGITUDE  = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String TIME = "time";
    public static final String BATTERY_LEVEL = "battery_level";
    public static final String POINT_TYPE = "point_type";

    public static final String COMMON = "common";
    public static final String SOS = "sos";

    public static final String RUNNING = "running";
    //Settings
    public static final String PASSWORD = "password";
    public static final String ID_CHILD = "idChild";

    public static final int WAIT_TIME = 2000;

    private static Info ourInstance = new Info();

    public static Info getInstance() {
        return ourInstance;
    }

    private Info() {
    }
}
