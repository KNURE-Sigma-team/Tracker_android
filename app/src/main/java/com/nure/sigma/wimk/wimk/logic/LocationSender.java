package com.nure.sigma.wimk.wimk.logic;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.util.Pair;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class LocationSender {

    public static final String SERVICE_TAG = "SERVICE";

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

    public MyHttpResponse sendLocation() {
        Log.i(SERVICE_TAG, "Service Started!");
        LocationManager locationManager = (LocationManager) context
                .getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if((!gpsEnabled) && (!networkEnabled)){
            // Both locations are switched off in settings!!!
            MyNotification.showNotificationOfSwitchedOffGeolocation(context);
            return sendDropGeolocation();
        }
        MyNotification.cancelNotificationOfSwitchedOffGeolocation(context);
        // Geolocation is switched on.
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

        batteryLevel = Info.getInstance().getBatteryLevel(context.getApplicationContext());

        if (locationGPS != null) {
            Util.logRecord(Util.formatLocation(locationGPS));
        } else if (locationNETWORK != null) {
            Util.logRecord(Util.formatLocation(locationNETWORK));
        } else if (locationPASSIVE != null) {
            Util.logRecord(Util.formatLocation(locationPASSIVE));
        } else {
            Util.logRecord("All locations are null.");
            return sendDropGeolocation();
        }
        Util.logRecord(Util.formatBatteryLevel(batteryLevel));
        Util.logRecord("-------------------------------------------------------------");


        List<Pair<String, String>> pairs = Info.getInstance().getParentAndChildLoginsListForHttp();

        // Choose, which location to sendLocation.
        Location resultLocation;
        if (Util.isGPSMoreAccurateLocation(locationGPS, locationNETWORK, locationPASSIVE)) {
            resultLocation = locationGPS;
        } else if (Util.isNetworkMoreAccurateLocation(locationGPS, locationNETWORK, locationPASSIVE)) {
            resultLocation = locationNETWORK;
        } else {
            resultLocation = locationPASSIVE;
        }

        pairs.add(new Pair<>(Info.LONGITUDE, String.valueOf(resultLocation.getLongitude())));
        pairs.add(new Pair<>(Info.LATITUDE, String.valueOf(resultLocation.getLatitude())));
        pairs.add(new Pair<>(Info.TIME, (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                .format(new Date(resultLocation.getTime()))));

        pairs.add(new Pair<>(Info.BATTERY_LEVEL, String.valueOf(batteryLevel)));
        pairs.add(new Pair<>(Info.POINT_TYPE, pointType));

        //Sending
        DataSender dataSender = new DataSender();
        MyHttpResponse myHttpResponse = dataSender.httpPostQuery(Info.MOBILE_GET_POINT_URL, pairs, Info.WAIT_TIME);
        if (myHttpResponse.getErrorCode() != MyHttpResponse.OK) {
            Util.addToFileList(new Pair<Location, String>(resultLocation,
                    String.valueOf(batteryLevel)), context);
        }
        return myHttpResponse;
    }

    private MyHttpResponse sendDropGeolocation(){
        DataSender dataSender = new DataSender();
        return dataSender.httpPostQuery(Info.DROP_GEO_SERVER_URL,
                Info.getInstance().getParentAndChildLoginsListForHttp(), Info.WAIT_TIME);
    }
}
