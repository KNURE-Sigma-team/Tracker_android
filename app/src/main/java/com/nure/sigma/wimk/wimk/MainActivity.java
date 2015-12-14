package com.nure.sigma.wimk.wimk;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.nure.sigma.wimk.wimk.gcm.QuickstartPreferences;
import com.nure.sigma.wimk.wimk.gcm.RegistrationIntentService;
import com.nure.sigma.wimk.wimk.logic.Info;


public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private static final int REQUEST_SWITCHING_ON_LOCATION = 2;

    private Button startButton;
    private Button stopButton;
    private TextView childNameTextView;
    private Info info = Info.getInstance();

    //GCM
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    //private ProgressBar mRegistrationProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            finish();
        }
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.start_button);
        stopButton = (Button) findViewById(R.id.stop_button);
        childNameTextView = (TextView) findViewById(R.id.childNameTextView);

        String childLogin = getSharedPreferences(Info.PASSWORD, 0).getString(Info.CHILD_LOGIN, null);
        childNameTextView.setText(childLogin);


        // GCM
        //mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Log.e("andstepko", "gcm_send_message");
                } else {
                    Log.e("andstepko", "token_error_message");
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
            Toast.makeText(this, getString(R.string.play_services_disabled),
                    Toast.LENGTH_LONG).show();
        }

        // Android API 23 permissions query
        suggestTurnOnPermissionsIfNecessary();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (info.startBackgroundServiceAndInformServer(getApplicationContext())) {
                    drawStartStopButtons(true);
                    Toast.makeText(MainActivity.this, R.string.service_started, Toast.LENGTH_LONG).show();
                }
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(info.stopBackgroundServiceAndInformServer(getApplicationContext())){
                    drawStartStopButtons(false);
                    Toast.makeText(MainActivity.this, R.string.service_stoped, Toast.LENGTH_LONG).show();
                }
                info.setFirstSending(true);
            }
        });
        drawStartStopButtons(info.isBackgroundserviceRunning(this));

//        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().
//                getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
//        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
//            startActivity(
//
//        }
    }

    private void drawStartStopButtons(boolean backgroundServiceIsRunning) {
        if (backgroundServiceIsRunning) {
            startButton.setVisibility(View.GONE);
            stopButton.setVisibility(View.VISIBLE);
        }
        else{
            startButton.setVisibility(View.VISIBLE);
            stopButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    suggestTurnOnLocationIfNecessary();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    showAppWontWorkWithoutPermissionsDialog();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_SWITCHING_ON_LOCATION: {
                Log.e("andstepko", "onActivityResult. requestCode == REQUEST_SWITCHING_ON_LOCATION");
                if (!isLocationSwitchedOn()) {
                    showAppWontWorkWithoutPermissionsDialog();
                }
                break;
            }
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

    private void suggestTurnOnLocationIfNecessary(){
        if(!isLocationSwitchedOn()){
            // Both locations are switched off in settings!!!
            startActivityForResult(
                    new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_SWITCHING_ON_LOCATION);
        }
    }

    private boolean isLocationSwitchedOn(){
        LocationManager locationManager = (LocationManager) this
                .getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return (gpsEnabled) || (networkEnabled);
    }

    private void suggestTurnOnPermissionsIfNecessary(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is switched off.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                Log.e("andstepko", "should show rationale");

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );

                // MY_PERMISSIONS_REQUEST_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else{
            // Permission is switched on.
            suggestTurnOnLocationIfNecessary();
        }
    }

    private void showAppWontWorkWithoutPermissionsDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(R.string.are_you_sure);
        alertDialogBuilder.setMessage(R.string.are_you_sure_message);
        alertDialogBuilder.setPositiveButton(R.string.continue_anywhay, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.back_to_permissions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                suggestTurnOnPermissionsIfNecessary();
            }
        });

        alertDialogBuilder.setCancelable(false);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
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
}
