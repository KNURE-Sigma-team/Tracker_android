package com.nure.sigma.wimk.wimk;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.util.Pair;

import com.nure.sigma.wimk.wimk.logic.DataSender;
import com.nure.sigma.wimk.wimk.logic.Info;
import com.nure.sigma.wimk.wimk.logic.LocationSender;
import com.nure.sigma.wimk.wimk.logic.MyHttpResponse;
import com.nure.sigma.wimk.wimk.logic.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class SOSService extends IntentService {

    public static final String SERVICE_TAG = "SERVICE";

    public SOSService() {
        super(SOSService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        LocationSender locationSender = new LocationSender(Info.SOS, this);
        MyHttpResponse myHttpResponse = locationSender.sendLocation();

        Log.i(SERVICE_TAG, "Service Stopping!");
        stopSelf();
    }
}
