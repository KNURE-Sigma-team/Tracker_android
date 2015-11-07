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

    public static final int OK = 0;
    public static final int BAD_URL = 101;
    public static final int OPEN_CONNECTION_FAIL = 102;
    public static final int NULL_CONNECTION = 103;
    public static final int SET_POST_FAIL = 104;
    public static final int OUTPUT_STREAM_FAIL = 105;
    public static final int GET_RESPONSE_FAIL = 106;

    private static final String TAG = "skornyakov";


    public static int HttpPostRequest(String serverUrl, Collection<Pair<String, String>> pairs) {
        URL obj = null;
        try {
            obj = new URL(serverUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) obj.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            connection.setRequestMethod("POST");
        } catch (java.net.ProtocolException e) {
            e.printStackTrace();
        }

        StringBuilder urlParameters = new StringBuilder();

        for (Pair<String, String> pair : pairs) {
            urlParameters.append(pair.first);
            urlParameters.append("=");
            urlParameters.append(pair.second);
            urlParameters.append("&");
        }

        // Delete last ampersand
        urlParameters.delete(urlParameters.length() - 1, urlParameters.length());

        // Send post request
        connection.setDoOutput(true);

        DataOutputStream wr = null;
        try {
            wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters.toString());
            wr.flush();
            wr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String response = null;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while ((line = reader.readLine()) != null) {
                // Append server response in string
                sb.append(line + "\n");
            }
            response = String.valueOf(sb.toString());
            Log.i("SERVICE","Response = " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int i;
        try {
            double d = Double.valueOf(response);
            i = (int) d;
            Log.i("SERVICE","Child ID = " + String.valueOf(i));
            return i;
        } catch (Exception e) {
            return -1;
        }

    }
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
        if (location == null)
            return "";
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

    public static void logRecord(String record){
        Log.i(TAG, record);
    }

    public static boolean isGPSMoreAccuracyLocation(Location locationGPS, Location locationNETWORK, Location locationPASSIVE){
        try {
            return locationGPS.getAccuracy() > locationNETWORK.getAccuracy();
        }
        catch (NullPointerException e){
            if (locationGPS!=null){
                return true;
            }
            else {
                return false;
            }
        }
    }
    public static boolean isNetworkMoreAccuracyLocation(Location locationGPS, Location locationNETWORK, Location locationPASSIVE){
        try {
            return locationNETWORK.getAccuracy() > locationGPS.getAccuracy();
        }
        catch (NullPointerException e){
            if (locationNETWORK!=null){
                return true;
            }
            else {
                return false;
            }
        }
    }
}
