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
import com.nure.sigma.wimk.wimk.logic.MyNotification;


public class SOSService extends IntentService {


    public SOSService() {
        super(SOSService.class.getName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
            Log.i(Info.SERVICE_TAG, "Service Starting!");
            LocationSender locationSender = new LocationSender(Info.SOS, this);
            MyHttpResponse myHttpResponse = locationSender.sendLocation();
            if (myHttpResponse.getErrorCode() == 0){
                MyNotification.showNotificationOfSuccessfulSOSMessageSending(this);
            }
            else {
                MyNotification.showNotificationOfFailedSOSMessageSending(this);
            }
            Log.i(Info.SERVICE_TAG, "Service Stopping!");
    }
}
