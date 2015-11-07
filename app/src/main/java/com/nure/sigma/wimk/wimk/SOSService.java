package com.nure.sigma.wimk.wimk;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;
import android.os.Handler;

import com.nure.sigma.wimk.wimk.logic.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



public class SOSService extends IntentService {


    private static final String TAG = "skornyakov";

    Location locationPASSIVE = null;
    Location locationGPS = null;
    Location locationNETWORK = null;
    public static int idChild;
    Handler mHandler;
    String serverURL = "http://blockverify.cloudapp.net:8080/wimk/mobile_get_point";


    public SOSService() {
            super(SOSService.class.getName());
        }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences settings = getSharedPreferences("password",0);
        idChild = settings.getInt("idChild",0);
        if (idChild != 0) {
            Log.i(TAG, "Service Started!");
            LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
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
            int batteryLevel = Util.getBatteryLevel(getApplicationContext());
            if (locationGPS != null) {
                Util.logRecord("GPS location" + Util.formatLocation(locationGPS));
            }
            if (locationNETWORK != null) {
                Util.logRecord("NETWORK location" + Util.formatLocation(locationNETWORK));
            }
            if (locationPASSIVE != null) {
                Util.logRecord("Passive location" + Util.formatLocation(locationPASSIVE));
            } else {
                Util.logRecord("All location is null.");
            }
            Util.logRecord(Util.formatBatteryLevel(batteryLevel));
            Util.logRecord("-------------------------------------------------------------");
            Util.logRecord("idChild = " + idChild);
            List<Pair<String, String>> pairs = new ArrayList<>();
            pairs.add(new Pair<>("idChild", String.valueOf(idChild)));
            if (Util.isGPSMoreAccuracyLocation(locationGPS, locationNETWORK, locationPASSIVE)) {
                pairs.add(new Pair<>("longitude", String.valueOf(locationGPS.getLongitude())));
                pairs.add(new Pair<>("latitude", String.valueOf(locationGPS.getLatitude())));
                pairs.add(new Pair<>("time", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(locationGPS.getTime()))));
            } else if (Util.isNetworkMoreAccuracyLocation(locationGPS, locationNETWORK, locationPASSIVE)) {
                pairs.add(new Pair<>("longitude", String.valueOf(locationNETWORK.getLongitude())));
                pairs.add(new Pair<>("latitude", String.valueOf(locationNETWORK.getLatitude())));
                pairs.add(new Pair<>("time", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(locationNETWORK.getTime()))));
            } else {
                pairs.add(new Pair<>("longitude", String.valueOf(locationPASSIVE.getLongitude())));
                pairs.add(new Pair<>("latitude", String.valueOf(locationPASSIVE.getLatitude())));
                pairs.add(new Pair<>("time", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(locationPASSIVE.getTime()))));
            }
            pairs.add(new Pair<>("battery_level", String.valueOf(batteryLevel)));
            pairs.add(new Pair<>("point_type", "sos"));
            Util.HttpPostRequest(serverURL, pairs);
            Log.i(TAG, "Service Stopping!");
            stopSelf();
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Cann`t find ID of child. Please, log in in application first.", Toast.LENGTH_LONG).show();
                }
            });        }
    }


}
