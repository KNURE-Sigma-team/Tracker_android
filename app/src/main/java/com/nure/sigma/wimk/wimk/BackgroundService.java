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

    private LocationManager locationManager;

    private int batteryLevel;

    Location locationGPS = null;
    Location locationNETWORK = null;
    public static int idChild;
    String serverURL = "http://178.165.37.203:8080/wimk/mobile_get_point";


    public BackgroundService() {
        super(BackgroundService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i(TAG, "Service Started!");
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        try {
            locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            locationNETWORK = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (SecurityException se) {
            logRecord("SECURITY_EXCEPTION");
        }
        getBatteryLevel();
        if (locationGPS == null) {
            logRecord(formatLocation(locationNETWORK));
        } else {
            logRecord(formatLocation(locationGPS));
        }
        logRecord(formatBatteryLevel(batteryLevel));
        logRecord("-------------------------------------------------------------");

        SharedPreferences settings = getSharedPreferences("password",0);
        idChild = settings.getInt("idChild",0);
        List<Pair<String,String>> pairs = new ArrayList<>();
        pairs.add(new Pair<>("idChild",String.valueOf(idChild)));
        pairs.add(new Pair<>("longitude",String.valueOf(locationNETWORK.getLongitude())));
        pairs.add(new Pair<>("latitude",String.valueOf(locationNETWORK.getLatitude())));
        pairs.add(new Pair<>("battery_level",String.valueOf(batteryLevel)));
        pairs.add(new Pair<>("time",(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(locationNETWORK.getTime()))));

        Util.HttpPostRequest(serverURL,pairs);
        //Log.i(TAG, "Service Stopping!");

        Intent notificationIntent = new Intent(this, BackgroundService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("WimK")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        startForeground(300, notification);
        try {
            TimeUnit.SECONDS.sleep(60);
        }
        catch (Exception e)
        {
            Log.i(TAG, e.toString());
        }
        getApplicationContext().startService(new Intent(getApplicationContext(), BackgroundService.class));
    }

    public void getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if(level == -1 || scale == -1) {
            batteryLevel = 50;
        }

        batteryLevel =(int)(((float) level / (float) scale) * 100.0f);
    }

    private void logRecord(String record){
        Log.i(TAG, record);
    }


    private String formatLocation(Location location) {
        if (location == null)
            return "";
        return String.format(
                "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",
                location.getLatitude(), location.getLongitude(), new Date(
                        location.getTime()));
    }

    public String formatBatteryLevel(float level) {
        if (level == 0)
            return "";
        return String.format("Battery level = %1$.2f",level);
    }

}