package com.nure.sigma.wimk.wimk.logic;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.BatteryManager;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.Map;


public class Util {

    //Getting battery level.
    public static int getBatteryLevel(Context context) {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if(level == -1 || scale == -1) {
            return  50;
        }

        return (int)(((float) level / (float) scale) * 100.0f);
    }

    public static String formatLocation(Location location) {
        if (location == null) {
            return "";
        }
        return String.format(
                "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",
                location.getLatitude(), location.getLongitude(), new Date(
                        location.getTime()));
    }

    public static String formatBatteryLevel(float level) {
        if (level == 0)
            return "";
        return String.format("Battery level = %1$.2f",level);
    }

    public static void log(String record){
        Log.e("andstepko", record);
    }

    public static void logRecord(String record){
        Log.i(Info.getInstance().SERVICE_TAG, record);
    }

    public static boolean isGPSMoreAccurateLocation(Location locationGPS,
                                                    Location locationNETWORK, Location locationPASSIVE){
        try {
            return (locationGPS.getAccuracy() > locationNETWORK.getAccuracy())
                    && (locationGPS.getAccuracy()>locationPASSIVE.getAccuracy());
        }
        catch (NullPointerException e){
            return false;
        }
    }

    public static boolean isNetworkMoreAccurateLocation(Location locationGPS,
                                                        Location locationNETWORK, Location locationPASSIVE){
        try {
            return (locationNETWORK.getAccuracy() > locationGPS.getAccuracy())
                    && (locationNETWORK.getAccuracy()>locationPASSIVE.getAccuracy());
        }
        catch (NullPointerException e){
            return false;
        }
    }
}
