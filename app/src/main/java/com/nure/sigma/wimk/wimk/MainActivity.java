package com.nure.sigma.wimk.wimk;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;

import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.nure.sigma.wimk.wimk.gcm.QuickstartPreferences;
import com.nure.sigma.wimk.wimk.gcm.RegistrationIntentService;
import com.nure.sigma.wimk.wimk.logic.DataSender;
import com.nure.sigma.wimk.wimk.logic.Info;
import com.nure.sigma.wimk.wimk.logic.LocationSender;
import com.nure.sigma.wimk.wimk.logic.MyHttpResponse;
import com.nure.sigma.wimk.wimk.logic.SendLocationTask;
import com.nure.sigma.wimk.wimk.logic.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    //FIXME remove after debug
    private Button performButton;
    private Button cleanButton;
    private TextView logTextView;

    private Button startButton;
    private Button stopButton;
    private Info info = Info.getInstance();

    //GCM
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ProgressBar mRegistrationProgressBar;

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
                //gainLogSendCoordinates();
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


        // GCM
        mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    logRecord("gcm_send_message");
                } else {
                    logRecord("token_error_message");
                }
            }
        };

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            //Intent intent = new Intent(this, RegistrationIntentService.class);
            Intent intent = new Intent(getApplicationContext(), RegistrationIntentService.class);
            startService(intent);
        }
        else{
            Log.e("andstepko", "No Play services!!!");
            Toast.makeText(MainActivity.this, getString(R.string.play_services_diabled),
                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.e("andstepko", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }




    //FIXME remove after debug
    private LocationManager locationManager;

//    private void gainLogSendCoordinates() {
//        // Must happen in background every N seconds.
//
//        // Means enabling in settings.
//        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        logRecord("GPS==>" + gpsEnabled);
//        logRecord("NETWORK==>" + networkEnabled);
//
//        if (gpsEnabled || networkEnabled) {
//            // Hang listeners, specified below
//            try {
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                        0, 1, locationListener);
//                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
//                        0, 1, locationListener);
//            } catch (SecurityException se) {
//                // Almost never happens.
//                logRecord("SECURITY_EXCEPTION");
//            }
//        } else {
//            // TODO Report switching the geo-location off in settings to the mother.
//        }
//    }

    private void logRecord(String record) {
        logTextView.setText(logTextView.getText().toString() + "\n" + record);
    }

//    private String formatLocation(Location location) {
//        if (location == null)
//            return "";
//        return String.format(
//                "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",
//                location.getLatitude(), location.getLongitude(), new Date(
//                        location.getTime()));
//    }


//    private LocationListener locationListener = new LocationListener() {
//
//        @Override
//        public void onLocationChanged(Location location) {
//            Location locationGPS = null;
//            Location locationNETWORK = null;
//
//            try {
//                // Geo-location starts blinking, if it's
//                locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                locationNETWORK = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//            } catch (SecurityException se) {
//                logRecord("SECURITY_EXCEPTION");
//            }
//
//            if (locationGPS == null) {
//                // GPS is enabled in settings, but unable to get location.
//                logRecord("locationGPS == null");
//            } else {
//                logRecord(formatLocation(locationGPS));
//            }
//            if (locationNETWORK == null) {
//                // NETWORK location is enabled in settings, but unable to get location.
//                logRecord("locationNETWORK == null");
//            } else {
//                logRecord(formatLocation(locationNETWORK));
//            }
//            logRecord("-------------------------------------------------------------");
//
//            // Shut down!
//            try {
//                locationManager.removeUpdates(locationListener);
//            } catch (SecurityException se) {
//                logRecord("SECURITY_EXCEPTION");
//            }
//
//            // Choose which provider data to sendLocation
//            Location locationToSend = null;
//            if (locationGPS != null) {
//                locationToSend = locationGPS;
//            } else if (locationNETWORK != null) {
//                locationToSend = locationNETWORK;
//            }
//
//            // Sending.
//            SendLocationTask sendLocationTask = new SendLocationTask(locationToSend,
//                    Util.getBatteryLevel(getApplicationContext()), MainActivity.this);
//            sendLocationTask.execute();
//        }
//
//        @Override
//        public void onProviderDisabled(String provider) {
//            // Automatically happens, when provider has been disabled in settings or during linking of the listener.
//            logRecord(provider + " disabled!");
//            // TODO send notification to mother
//        }
//
//        @Override
//        public void onProviderEnabled(String provider) {
//            // Automatically happens, when provider has been enabled in settings or during linking of the listener
//            logRecord(provider + " enabled!");
//        }
//
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//            // Do nothing.
//        }
//    };
}
