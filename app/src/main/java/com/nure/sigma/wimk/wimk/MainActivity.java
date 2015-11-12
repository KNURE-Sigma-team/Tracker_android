package com.nure.sigma.wimk.wimk;


import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;

import android.widget.TextView;

import com.nure.sigma.wimk.wimk.logic.DataSender;
import com.nure.sigma.wimk.wimk.logic.Info;
import com.nure.sigma.wimk.wimk.logic.MyHttpResponse;
import com.nure.sigma.wimk.wimk.logic.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static final String MOBILE_GET_POINT_URL = Info.getInstance().SERVER_URL + "mobile_get_point";

    //FIXME remove after debug
    private Button performButton;
    private Button cleanButton;
    private TextView logTextView;

    private Button startButton;
    private Button stopButton;
    private Info info = Info.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            finish();
        }
        setContentView(R.layout.activity_main);

        //FIXME removeme after debug
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        logTextView = (TextView) findViewById(R.id.logsTextView);
        performButton = (Button) findViewById(R.id.perform_btn);
        cleanButton = (Button) findViewById(R.id.clean_btn);

        performButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(
//                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                gainLogSendCoordinates();
            }
        });

        cleanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logTextView.setText("");
            }
        });
        //


        startButton = (Button) findViewById(R.id.start_button);
        stopButton = (Button) findViewById(R.id.stop_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences(info.PASSWORD, 0);
                SharedPreferences.Editor e = settings.edit();

                e.putBoolean(info.RUNNING, true);
                e.apply();

                startService(new Intent(getApplicationContext(), BackgroundService.class));
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences(info.PASSWORD, 0);
                SharedPreferences.Editor e = settings.edit();
                e.putBoolean(info.RUNNING, false);
                e.apply();

                stopService(new Intent(getApplicationContext(), BackgroundService.class));
            }
        });
    }


    //FIXME remove after debug
    private LocationManager locationManager;

    private void gainLogSendCoordinates(){
        // Must happen in background every N seconds.

        // Means enabling in settings.
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        logRecord("GPS==>" + gpsEnabled);
        logRecord("NETWORK==>" + networkEnabled);

        if(gpsEnabled || networkEnabled) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        0, 1, locationListener);
           locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        0, 1, locationListener);
            } catch (SecurityException se) {
                logRecord("SECURITY_EXCEPTION");
            }
        }
        else{
            // Report switching the geo-location off in settings.
        }
    }

    private void logRecord(String record){
        logTextView.setText(logTextView.getText().toString() + "\n" + record);
    }

    private String formatLocation(Location location) {
        if (location == null)
            return "";
        return String.format(
                "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",
                location.getLatitude(), location.getLongitude(), new Date(
                        location.getTime()));
    }


    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Location locationGPS = null;
            Location locationNETWORK = null;

            try {
                // Geo-location starts blinking, if it's
                locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locationNETWORK = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            catch (SecurityException se){
                logRecord("SECURITY_EXCEPTION");
            }

            if(locationGPS == null){
                // GPS is enabled in settings, but unable to get location.
                logRecord("locationGPS == null");
            }
            else{
                logRecord(formatLocation(locationGPS));
            }
            if(locationNETWORK == null){
                // NETWORK location is enabled in settings, but unable to get location.
                logRecord("locationNETWORK == null");
            }
            else{
                logRecord(formatLocation(locationNETWORK));
            }
            logRecord("-------------------------------------------------------------");

            // Shut down!
            try {
                locationManager.removeUpdates(locationListener);
            }
            catch (SecurityException se){
                logRecord("SECURITY_EXCEPTION");
            }

            // Choose which provider data to sendLocation
            Location locationToSend = null;
            if(locationGPS != null){
                locationToSend = locationGPS;
            }
            else if(locationNETWORK != null){
                locationToSend = locationNETWORK;
            }

            // Sending.
            SendLocationTask sendLocationTask = new SendLocationTask(locationToSend,
                    Util.getBatteryLevel(getApplicationContext()));
            sendLocationTask.execute();
        }

        @Override
        public void onProviderDisabled(String provider) {
            // Automatically happens, when provider has been disabled in settings or during linking of the listener.
            logRecord(provider + " disabled!");
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Automatically happens, when provider has been enabled in settings or during linking of the listener
            logRecord(provider + " enabled!");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Do nothing.
        }
    };


    private class SendLocationTask extends AsyncTask<Void, Void, Boolean>{

        private Location location;
        private int batteryLevel;

        private int idChild;
        private String response;

        public SendLocationTask(Location location, int batteryLevel) {
            this.location = location;
            this.batteryLevel = batteryLevel;
        }

        @Override
        protected void onPreExecute() {
            //Getting idChild, which setting in LoginActivity.
            SharedPreferences settings = getSharedPreferences(Info.getInstance().PASSWORD, 0);
            idChild = settings.getInt(info.ID_CHILD, 0);
            logRecord(info.ID_CHILD + "==>" + idChild);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if(location == null){
                return false;
            }

            if(idChild == -1){
                return false;
            }

            //Making request`s parameters and finding the most accurate provider.
            List<Pair<String,String>> pairs = new ArrayList<>();
            pairs.add(new Pair<>(info.ID_CHILD, String.valueOf(idChild)));
            // Location data.
            pairs.add(new Pair<>(info.LONGITUDE, String.valueOf(location.getLongitude())));
            pairs.add(new Pair<>(info.LATITUDE, String.valueOf(location.getLatitude())));
            pairs.add(new Pair<>(info.TIME,(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                    .format(new Date(location.getTime()))));
            // Non-location data.
            pairs.add(new Pair<>(info.BATTERY_LEVEL, String.valueOf(batteryLevel)));
            pairs.add(new Pair<>(info.POINT_TYPE, info.COMMON));

            //Post request to server.
            DataSender dataSender = new DataSender();
            MyHttpResponse myHttpResponse = dataSender.HttpPostQuery(MOBILE_GET_POINT_URL, pairs,
                    Info.getInstance().WAIT_TIME);

            response = myHttpResponse.getResponse();
            if(myHttpResponse.getErrorCode() != MyHttpResponse.OK){
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            logRecord("response==>" + response);

            if(aBoolean){
                logRecord("Data was sent.");
            }
            else{
                logRecord("Error sending data!");
            }
        }
    }
}
