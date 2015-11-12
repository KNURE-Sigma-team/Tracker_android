package com.nure.sigma.wimk.wimk.logic;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.BatteryManager;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;


public class Util {

    public static void fillFileList(Context context){
        ObjectInputStream in = null;
        Info.FILE_LIST = new ArrayList<>();
        try {
            File file = new File(context.getFilesDir().getPath().toString() + Info.LOCATIONS_FILE);
            if (!file.exists()){
                file.createNewFile();
            }
            in = new ObjectInputStream(new BufferedInputStream(
                    new FileInputStream(file)));
            Info.FILE_LIST = (ArrayList<Pair<Location,String>>)in.readObject();
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }
    public static void writeListToFile(Context context){
        ObjectOutputStream out = null;
        try {
            File file = new File(context.getFilesDir().getPath().toString() + "/failed.ser");
            if (!file.exists()){
                file.createNewFile();
            }
            out = new ObjectOutputStream(new BufferedOutputStream(
                    new FileOutputStream(file)));
            out.writeObject(Info.FILE_LIST);
        } catch ( IOException ex ) {
            ex.printStackTrace();
        }
    }

    public static void addToFileList(Pair<Location,String> pair, Context context){
        fillFileList(context);
        Info.FILE_LIST.add(pair);
        writeListToFile(context);
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
            if (locationGPS!=null){
                return true;
            }
            else {
                return false;
            }
        }
    }

    public static boolean isNetworkMoreAccurateLocation(Location locationGPS,
                                                        Location locationNETWORK, Location locationPASSIVE){
        try {

            return (locationNETWORK.getAccuracy() > locationGPS.getAccuracy())
                    && (locationNETWORK.getAccuracy()>locationPASSIVE.getAccuracy());
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
