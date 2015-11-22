package com.nure.sigma.wimk.wimk;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.NotificationCompat;
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
import java.util.concurrent.TimeUnit;

public class BackgroundService extends IntentService {


    boolean running;

    public BackgroundService() {
        super(BackgroundService.class.getName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        SharedPreferences temp = getSharedPreferences(Info.PASSWORD, 0);
        running = temp.getBoolean(Info.RUNNING, false);
        int frequency = temp.getInt(Info.FREQUENCY, 30);
        int idChild = temp.getInt(Info.ID_CHILD, 0);
        if (running) {
            LocationSender locationSender = new LocationSender(Info.COMMON, this);
            MyHttpResponse myHttpResponse = locationSender.sendLocation();
            Util.logRecord(String.valueOf(myHttpResponse.getErrorCode()));
//            Util.fillFileList(this);
//            ArrayList<Pair<Location, String>> tempList = new ArrayList<>();
            /*if (Info.FAILED_LOCATIONS_LIST != null && !Info.FAILED_LOCATIONS_LIST.isEmpty() && myHttpResponse.getErrorCode() == MyHttpResponse.OK) {
                Util.logRecord("Sending failed locations");
                Util.logRecord(Info.ID_CHILD + " = " + idChild);
                for (Pair<Location, String> pair : Info.FAILED_LOCATIONS_LIST) {

                    List<Pair<String, String>> pairs = new ArrayList<>();

                    pairs.add(new Pair<>(Info.ID_CHILD, String.valueOf(idChild)));
                    pairs.add(new Pair<>(Info.LONGITUDE, String.valueOf(pair.first.getLongitude())));
                    pairs.add(new Pair<>(Info.LATITUDE, String.valueOf(pair.first.getLatitude())));
                    pairs.add(new Pair<>(Info.TIME, (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                            .format(new Date(pair.first.getTime()))));
                    pairs.add(new Pair<>(Info.BATTERY_LEVEL, pair.second));
                    pairs.add(new Pair<>(Info.POINT_TYPE, Info.COMMON));
                    DataSender dataSender = new DataSender();
                    myHttpResponse = dataSender.HttpPostQuery(MOBILE_GET_POINT_URL, pairs, Info.WAIT_TIME);
                    if (myHttpResponse.getErrorCode() != MyHttpResponse.OK) {
                        tempList.add(new Pair<Location, String>(pair.first, pair.second));
                    }
                }
                Info.FAILED_LOCATIONS_LIST = new ArrayList<>();
                for (Pair<Location, String> pair : tempList) {
                    Util.addToFileList(pair, getApplicationContext());
                }
                Util.logRecord("Stop sending failed locations");
            }*/

            Log.i(Info.SERVICE_TAG, "Service Stopping!");

            //Creating notification for starting IntentService in Foreground.
            Intent notificationIntent = new Intent(this, BackgroundService.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("WimK")
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .setContentText("Where is my Kid?")
                    .setSubText("Some text")
                    .build();
            startForeground(300, notification);
            try {
                TimeUnit.SECONDS.sleep(frequency);
            } catch (Exception e) {
                Log.i(Info.SERVICE_TAG, e.toString());
            }
            getApplicationContext().startService(new Intent(getApplicationContext(), BackgroundService.class));
        } else {
            // If not running.
            stopSelf();
        }
    }
}