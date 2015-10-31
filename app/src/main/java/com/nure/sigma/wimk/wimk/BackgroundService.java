package com.nure.sigma.wimk.wimk;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.util.Pair;

import com.nure.sigma.wimk.wimk.logic.Util;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BackgroundService extends IntentService {

    private static final String TAG = "SERVICE";

    private int batteryLevel;
    Location locationPASSIVE = null;
    Location locationGPS = null;
    Location locationNETWORK = null;
    public static int idChild;
    String serverURL = "http://blockverify.cloudapp.net:8080/wimk/mobile_get_point";
    boolean runable;

    public BackgroundService() {
        super(BackgroundService.class.getName());
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences temp = getSharedPreferences("password", 0);
        runable = temp.getBoolean("runable", false);
        if (runable) {
            Log.i(TAG, "Service Started!");
            LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

            //Try to get locations from every provider.
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
            batteryLevel = Util.getBatteryLevel(getApplicationContext());

            //Logging getting locations and battery level.
            if (locationGPS != null) {
                Util.logRecord(Util.formatLocation(locationGPS));
            } else if (locationNETWORK != null) {
                Util.logRecord(Util.formatLocation(locationNETWORK));
            } else if (locationPASSIVE != null) {
                Util.logRecord(Util.formatLocation(locationPASSIVE));
            } else {
                Util.logRecord("All location is null.");
            }
            Util.logRecord(Util.formatBatteryLevel(batteryLevel));
            Util.logRecord("-------------------------------------------------------------");

            //Getting idChild, which setting in LoginActivity.
            SharedPreferences settings = getSharedPreferences("password", 0);
            idChild = settings.getInt("idChild", 0);
            Util.logRecord("idChild = " + idChild);

        //Making request`s parametrs and finding the most accuracy provider.
        List<Pair<String,String>> pairs = new ArrayList<>();
        pairs.add(new Pair<>("idChild", String.valueOf(idChild)));
        if (isGPSMoreAccuracyLocation()) {
            pairs.add(new Pair<>("longitude", String.valueOf(locationGPS.getLongitude())));
            pairs.add(new Pair<>("latitude", String.valueOf(locationGPS.getLatitude())));
            pairs.add(new Pair<>("time",(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(locationGPS.getTime()))));
        }
        else if (isNetworkMoreAccuracyLocation()) {
            pairs.add(new Pair<>("longitude", String.valueOf(locationNETWORK.getLongitude())));
            pairs.add(new Pair<>("latitude", String.valueOf(locationNETWORK.getLatitude())));
            pairs.add(new Pair<>("time",(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(locationNETWORK.getTime()))));
        }
        else {
            pairs.add(new Pair<>("longitude", String.valueOf(locationPASSIVE.getLongitude())));
            pairs.add(new Pair<>("latitude", String.valueOf(locationPASSIVE.getLatitude())));
            pairs.add(new Pair<>("time",(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(locationPASSIVE.getTime()))));
        }
        pairs.add(new Pair<>("battery_level",String.valueOf(batteryLevel)));
        pairs.add(new Pair<>("point_type","common"));

        //Post request to server.
        Util.HttpPostRequest(serverURL, pairs);
        Log.i(TAG, "Service Stopping!");

            //Creating notification for starting IntentService in Foreground.
            Intent notificationIntent = new Intent(this, BackgroundService.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("WimK")
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();
            startForeground(300, notification);
            try {
                TimeUnit.SECONDS.sleep(30);
            } catch (Exception e) {
                Log.i(TAG, e.toString());
            }
            getApplicationContext().startService(new Intent(getApplicationContext(), BackgroundService.class));
        }
        else{
            stopSelf();
        }
    }


    public boolean isGPSMoreAccuracyLocation(){
        try {
            return locationGPS.getAccuracy() > locationNETWORK.getAccuracy()  &&  locationGPS.getAccuracy()>locationPASSIVE.getAccuracy();
        }
        catch (NullPointerException e){
            return false;
        }
    }
    public boolean isNetworkMoreAccuracyLocation(){
        try {
            return locationNETWORK.getAccuracy() > locationGPS.getAccuracy()  &&  locationNETWORK.getAccuracy()>locationPASSIVE.getAccuracy();
        }
        catch (NullPointerException e){
            return false;
        }
    }
}