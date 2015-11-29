package com.nure.sigma.wimk.wimk;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.nure.sigma.wimk.wimk.logic.Info;
import com.nure.sigma.wimk.wimk.logic.LocationSender;
import com.nure.sigma.wimk.wimk.logic.MyHttpResponse;


public class SOSService extends IntentService {

    private Handler handler;

    public SOSService() {
        super(SOSService.class.getName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler = new Handler();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
            Log.i(Info.SERVICE_TAG, "Service Starting!");
            LocationSender locationSender = new LocationSender(Info.SOS, this);
            MyHttpResponse myHttpResponse = locationSender.sendLocation();
            Log.i(Info.SERVICE_TAG, "Service Stopping!");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Send SOS message!", Toast.LENGTH_LONG).show();
                }
            });
    }
}
