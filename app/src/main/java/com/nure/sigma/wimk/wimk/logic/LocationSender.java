package com.nure.sigma.wimk.wimk.logic;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.util.Pair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by andstepko on 07.11.15.
 */
public class LocationSender {

    public static final String MOBILE_GET_POINT_URL = Info.getInstance().SERVER_URL + "mobile_get_point";
    public static final String SERVICE_TAG = "SERVICE";

    public static int idChild;

    private int batteryLevel;
    private Location locationPASSIVE = null;
    private Location locationGPS = null;
    private Location locationNETWORK = null;

    private String pointType;
    private Context context;

    public LocationSender(String pointType, Context context) {
        this.pointType = pointType;
        this.context = context;
    }

    public MyHttpResponse sendLocation(){
        Log.i(SERVICE_TAG, "Service Started!");
        LocationManager locationManager = (LocationManager) context
                .getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        try {
            locationNETWORK = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        } catch (SecurityException se) {
            Util.logRecord("SECURITY_EXCEPTION");
        }
        try {
            locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException se) {
            Util.logRecord("SECURITY_EXCEPTION");
        }
        try {
            locationPASSIVE = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        } catch (SecurityException se) {
            Util.logRecord("SECURITY_EXCEPTION");
        }

        batteryLevel = Util.getBatteryLevel(context.getApplicationContext());

        if (locationGPS != null) {
            Util.logRecord(Util.formatLocation(locationGPS));
        }
        else if (locationNETWORK != null){
            Util.logRecord(Util.formatLocation(locationNETWORK));
        }
        else if (locationPASSIVE != null){
            Util.logRecord(Util.formatLocation(locationPASSIVE));
        }
        else {
            Util.logRecord("All locations are null.");
        }
        Util.logRecord(Util.formatBatteryLevel(batteryLevel));
        Util.logRecord("-------------------------------------------------------------");

        SharedPreferences settings = context.getSharedPreferences(Info.PASSWORD, 0);
        idChild = settings.getInt(Info.ID_CHILD, 0);
        Util.logRecord(Info.ID_CHILD + " = " + idChild);

        List<Pair<String,String>> pairs = new ArrayList<>();
        pairs.add(new Pair<>(Info.ID_CHILD, String.valueOf(idChild)));
        // Choose, which location to sendLocation.
        Location resultLocation;
        if (Util.isGPSMoreAccurateLocation(locationGPS, locationNETWORK, locationPASSIVE)) {
            resultLocation = locationGPS;
        }else if (Util.isNetworkMoreAccurateLocation(locationGPS, locationNETWORK, locationPASSIVE)) {
            resultLocation = locationNETWORK;
        } else{
            resultLocation = locationPASSIVE;
        }

        pairs.add(new Pair<>(Info.LONGITUDE, String.valueOf(resultLocation.getLongitude())));
        pairs.add(new Pair<>(Info.LATITUDE, String.valueOf(resultLocation.getLatitude())));
        pairs.add(new Pair<>(Info.TIME,(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                .format(new Date(resultLocation.getTime()))));

        pairs.add(new Pair<>(Info.BATTERY_LEVEL, String.valueOf(batteryLevel)));
        pairs.add(new Pair<>(Info.POINT_TYPE, pointType));
        //Sending
        DataSender dataSender = new DataSender();
        MyHttpResponse myHttpResponse = dataSender.HttpPostQuery(MOBILE_GET_POINT_URL, pairs, Info.WAIT_TIME);
        if (myHttpResponse.getErrorCode()!=MyHttpResponse.OK){
            Util.addToFileList(new Pair<Location, String>(resultLocation,String.valueOf(batteryLevel)),context);
        }
        return myHttpResponse;
    }
}
