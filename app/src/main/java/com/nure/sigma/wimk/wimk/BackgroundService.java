package com.nure.sigma.wimk.wimk;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.nure.sigma.wimk.wimk.logic.Info;
import com.nure.sigma.wimk.wimk.logic.LocationSender;
import com.nure.sigma.wimk.wimk.logic.MyHttpResponse;


import java.util.concurrent.TimeUnit;

public class BackgroundService extends IntentService {

    boolean running;

    public BackgroundService() {
        super(BackgroundService.class.getName());
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences temp = getSharedPreferences(Info.PASSWORD, 0);
        running = temp.getBoolean(Info.RUNNING, false);

        if (running) {
            LocationSender locationSender = new LocationSender(Info.COMMON, this);
            MyHttpResponse myHttpResponse = locationSender.sendLocation();

            Log.i(Info.SERVICE_TAG, "Service Stopping!");

            //Creating notification for starting IntentService in Foreground.
            Intent notificationIntent = new Intent(this, BackgroundService.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("WimK")
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();
            startForeground(300, notification);
            try {
                TimeUnit.SECONDS.sleep(30);
            } catch (Exception e) {
                Log.i(Info.SERVICE_TAG, e.toString());
            }
            getApplicationContext().startService(new Intent(getApplicationContext(), BackgroundService.class));
        }
        else{
            // If not running.
            stopSelf();
        }
    }
}