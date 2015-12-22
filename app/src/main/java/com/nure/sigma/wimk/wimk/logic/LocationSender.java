package com.nure.sigma.wimk.wimk.logic;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class LocationSender {

    public static final String SERVICE_TAG = "SERVICE";
    public static final int MIN_UPDATING_TIME = 1000;
    public static final long TOTAL_WAIT_TIME = MIN_UPDATING_TIME * 10;
    public static final long TOTAL_ON_DEMAND_WAIT_TIME = MIN_UPDATING_TIME * 5;

    private int batteryLevel;
    private Location locationPASSIVE = null;
    private Location locationGPS = null;
    private Location locationNETWORK = null;
    LocationListener emptyLocationListener;

    private String pointType;
    private Context context;

    public LocationSender(String pointType, Context context) {
        this.pointType = pointType;
        this.context = context;
    }

    public MyHttpResponse gainAndSendLocation() {
        Log.i(SERVICE_TAG, "Service Started!");
        final LocationManager locationManager = (LocationManager) context
                .getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if ((!gpsEnabled) && (!networkEnabled)) {
            Log.e("andstepko", "// Both locations are switched off in settings!!!");
            MyNotification.showNotificationOfSwitchedOffGeolocation(context);
            return sendDropGeolocation();
        }

        // Geolocation is switched on.
        MyNotification.cancelNotificationOfSwitchedOffGeolocation(context);

        batteryLevel = Info.getInstance().getBatteryLevel(context.getApplicationContext());

        if(!pointType.equals(Info.ON_DEMAND)) {
            emptyLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            gainGPSLocation(locationManager);
            gainNETWORKLocation(locationManager);

            try {
                Thread.currentThread().sleep(TOTAL_WAIT_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else{
            // On demand.
            class UserLocationThread extends Thread {
                public void run() {
                    try {
                        Looper.prepare();
                        emptyLocationListener = new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {

                            }

                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {

                            }

                            @Override
                            public void onProviderEnabled(String provider) {

                            }

                            @Override
                            public void onProviderDisabled(String provider) {

                            }
                        };
                        gainGPSLocation(locationManager);
                        gainNETWORKLocation(locationManager);
                        //Looper.loop();
                    } catch (Exception e) {
                        //...
                    }
                }
            }
            UserLocationThread userLocationThread = new UserLocationThread();
            userLocationThread.run();

            Log.e("andstepko", "On demand. Thread started sleeping");
            try {
                Thread.currentThread().sleep(TOTAL_ON_DEMAND_WAIT_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            userLocationThread.interrupt();
        }

        getLastKnownLocation(locationManager);

        // Unbind listeners
        try {
            locationManager.removeUpdates(emptyLocationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
            Util.logRecord("SECURITY_EXCEPTION");
        }

        return chooseProperAndSend(locationManager);
    }

    private void gainGPSLocation(LocationManager locationManager){
        try {
            //locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_UPDATING_TIME, 1,
                    emptyLocationListener);
        } catch (SecurityException se) {
            Util.logRecord("SECURITY_EXCEPTION");
        }
        catch (IllegalArgumentException iaE){
            Util.logRecord("No such location provider (GPS) !");
        }
    }

    private void gainNETWORKLocation(LocationManager locationManager){
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_UPDATING_TIME, 1,
                    emptyLocationListener);
        } catch (SecurityException se) {
            Util.logRecord("SECURITY_EXCEPTION");
        }
        catch (IllegalArgumentException iaE){
            Util.logRecord("No such location provider (NETWORK) !");
        }
    }

    private void getLastKnownLocation(LocationManager locationManager){

        Log.e("andstepko", "getLastKnownLocation");
        try {
            locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(locationGPS != null) {
                Log.e("andstepko", "locationGPS.getTime()==>" + locationGPS.getTime());
            }
        }
        catch (SecurityException e){
            e.printStackTrace();
            Util.logRecord("SECURITY_EXCEPTION");
        }
        try {
            locationNETWORK = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(locationNETWORK != null) {
                Log.e("andstepko", "locationNETWORK.getTime()==>" + locationNETWORK.getTime());
            }
        }
        catch (SecurityException e){
            e.printStackTrace();
            Util.logRecord("SECURITY_EXCEPTION");
        }
        try {
            locationPASSIVE = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }
        catch (SecurityException e){
            e.printStackTrace();
            Util.logRecord("SECURITY_EXCEPTION");
        }
    }

    private MyHttpResponse chooseProperAndSend(LocationManager locationManager){
        // Choose, which location to gainAndSendLocation.

//        Log.e("andstepko", "chooseProperAndSend");
//        Log.e("andstepko", "locationGPS.getAccuracy()==>" + locationGPS.getAccuracy());
//        Log.e("andstepko", "locationNETWORK.getAccuracy()==>" + locationNETWORK.getAccuracy());

        Location resultLocation;
        if (isFirstLocationMostAccurate(locationGPS, locationNETWORK, locationPASSIVE) &&
                ((locationGPS.getTime() - locationNETWORK.getTime()) > -TOTAL_WAIT_TIME)) {
            Log.e("andstepko", "Chose GPS location");
            resultLocation = locationGPS;
        //} else if (isFirstLocationMostAccurate(locationNETWORK, locationGPS, locationPASSIVE)) {
        } else if (locationNETWORK != null) {
            Log.e("andstepko", "Do not get GPS location, but NETWORK");
            resultLocation = locationNETWORK;
        } else {
            Log.e("andstepko", "Do not get GPS or NETWORK location, but PASSIVE");
            resultLocation = locationPASSIVE;
        }

        if(resultLocation != null) {
            return send(resultLocation);
        }
        else{
            Log.e("andstepko", "resultLocation == null");
            return sendDropGeolocation();
        }
    }

    private static boolean isFirstLocationMostAccurate(Location locationFirst,
                                                      Location locationSecond, Location locationThird) {
        try {

            return (locationFirst.getAccuracy() <= locationSecond.getAccuracy())
                    && (locationFirst.getAccuracy() <= locationThird.getAccuracy());
        } catch (NullPointerException e) {
            if (locationFirst != null) {
                return true;
            } else {
                return false;
            }
        }
    }

    private MyHttpResponse send(Location resultLocation){
        List<Pair<String, String>> pairs = Info.getInstance().getParentAndChildLoginsListForHttp();

        pairs.add(new Pair<>(Info.LONGITUDE, String.valueOf(resultLocation.getLongitude())));
        pairs.add(new Pair<>(Info.LATITUDE, String.valueOf(resultLocation.getLatitude())));
        pairs.add(new Pair<>(Info.TIME, (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                //.format(new Date(resultLocation.getTime()))));
                .format(new Date())));

        pairs.add(new Pair<>(Info.BATTERY_LEVEL, String.valueOf(batteryLevel)));
        pairs.add(new Pair<>(Info.POINT_TYPE, pointType));

        for(Pair<String, String> pair : pairs){
            Log.e("andstepko", "LocationSender.name==>" + pair.first + " value==>" + pair.second);
        }

        //Sending
        DataSender dataSender = new DataSender();
        MyHttpResponse myHttpResponse = dataSender.httpPostQuery(Info.MOBILE_GET_POINT_URL, pairs, Info.WAIT_TIME);
        if (myHttpResponse.getErrorCode() != MyHttpResponse.OK) {
            // Store
            resultLocation.setTime(new Date().getTime());
            Util.addToFileList(new Pair<>(resultLocation,
                    String.valueOf(batteryLevel)), context);
        }
        return myHttpResponse;
    }

    private MyHttpResponse sendDropGeolocation() {

        Log.e("andstepko", "sendDropGeolocation");

        DataSender dataSender = new DataSender();
        return dataSender.httpPostQuery(Info.   DROP_GEO_SERVER_URL,
                Info.getInstance().getParentAndChildLoginsListForHttp(), Info.WAIT_TIME);
    }
}
