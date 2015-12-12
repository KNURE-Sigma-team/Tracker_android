package com.nure.sigma.wimk.wimk;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.nure.sigma.wimk.wimk.logic.Info;
import com.nure.sigma.wimk.wimk.logic.LocationSender;
import com.nure.sigma.wimk.wimk.logic.MyHttpResponse;
import com.nure.sigma.wimk.wimk.logic.MyNotification;
import com.nure.sigma.wimk.wimk.logic.Util;


import java.util.concurrent.TimeUnit;

public class BackgroundService extends IntentService {

    public BackgroundService() {
        super(BackgroundService.class.getName());
    }

    @Override
    public void onDestroy() {

        Log.e("andstepko", "Background service destroyed!");

        Info.getInstance().informStoppedTracking();

        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences temp = getSharedPreferences(Info.PASSWORD, 0);
        boolean running = temp.getBoolean(Info.RUNNING, false);
        int frequency = temp.getInt(Info.SENDING_FREQUENCY, Info.DEFAULT_SENDING_FREQUENCY);

        if (running) {
            LocationSender locationSender = new LocationSender(Info.COMMON, this);
            MyHttpResponse myHttpResponse = locationSender.sendLocation();
            if (myHttpResponse.getErrorCode() == 0) {
                if (Info.getInstance().isFirstSending()) {
                    startForeground(MyNotification.SENDING_NOTIFICATION_ID, MyNotification.getSuccessfulFirstSendingNotification(this));
                    Toast.makeText(this,"First sending was successful.",Toast.LENGTH_LONG).show();
                } else {
                    startForeground(MyNotification.SENDING_NOTIFICATION_ID, MyNotification.getSuccesBackgroundServiceNotification(this));
                }
            } else {
                if (Info.getInstance().isFirstSending()) {
                    startForeground(MyNotification.SENDING_NOTIFICATION_ID, MyNotification.getFailedFirstSendingNotification(this));
                    Toast.makeText(this,"First sending was failed!!!",Toast.LENGTH_LONG).show();
                } else {
                    startForeground(MyNotification.SENDING_NOTIFICATION_ID, MyNotification.getFailedBackgroundServiceNotification(this));
                }
            }
            Util.logRecord(String.valueOf(myHttpResponse.getErrorCode()));
            Log.i(Info.SERVICE_TAG, "Service Stopping!");

            try {
                TimeUnit.SECONDS.sleep(frequency * 60);
            } catch (Exception e) {
                Log.e(Info.SERVICE_TAG, e.toString());
            }
            Info.getInstance().setFirstSending(false);
            getApplicationContext().startService(new Intent(getApplicationContext(), BackgroundService.class));
        } else {
            stopSelf();
        }
    }
}