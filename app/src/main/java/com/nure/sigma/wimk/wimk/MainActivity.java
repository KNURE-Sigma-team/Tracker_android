package com.nure.sigma.wimk.wimk;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Button performButton;
    private TextView logTextView;

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        logTextView = (TextView) findViewById(R.id.logsTextView);
        performButton = (Button) findViewById(R.id.perform_btn);
        performButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(
//                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                gainAndLogCoordinates();
            }
        });
    }

    private boolean sendLocation(Location location){
        //TODO Send location ot server
        return false;
    }

    private void gainAndLogCoordinates(){
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 1, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    0, 1, locationListener);
        }
        catch (SecurityException se){
            logRecord("SECURITY_EXCEPTION");
        }

//        logRecord("GPS ==>" + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
//        logRecord("NETWORK ==>" + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));


    }

    private void logRecord(String record){
        logTextView.setText(logTextView.getText().toString() + "\n" + record);
    }

    private void logReplace(String message){
        logTextView.setText(message);
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
                locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locationNETWORK = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            catch (SecurityException se){
                logRecord("SECURITY_EXCEPTION");
            }

            if(locationGPS == null){
                logRecord("locationGPS == null");
            }
            else{
                logRecord(formatLocation(locationGPS));
            }

            if(locationNETWORK == null){
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
        }

        @Override
        public void onProviderDisabled(String provider) {
            logRecord(provider + "disabled!");
        }

        @Override
        public void onProviderEnabled(String provider) {
            logRecord(provider + "enabled!");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Do nothing.
        }
    };
}
