package com.nure.sigma.wimk.wimk;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.nure.sigma.wimk.wimk.logic.Info;
import com.nure.sigma.wimk.wimk.logic.LocationSender;
import com.nure.sigma.wimk.wimk.logic.MyHttpResponse;


public class SOSService extends IntentService {
    boolean running;

    public SOSService() {
        super(SOSService.class.getName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(Info.SERVICE_TAG, "Service Starting!");
        LocationSender locationSender = new LocationSender(Info.SOS, this);
        MyHttpResponse myHttpResponse = locationSender.sendLocation();
        Log.i(Info.SERVICE_TAG, "Service Stopping!");
    }
}
