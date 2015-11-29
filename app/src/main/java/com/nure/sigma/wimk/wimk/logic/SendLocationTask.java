package com.nure.sigma.wimk.wimk.logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Pair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by andstepko on 28.11.15.
 */
public class SendLocationTask extends AsyncTask<Void, Void, Boolean> {

    private Location location;
    private int batteryLevel;
    private Context context;

    private String response;

    public SendLocationTask(Location location, int batteryLevel, Context context) {
        this.location = location;
        this.batteryLevel = batteryLevel;
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (location == null) {
            return false;
        }

        //Making request`s parameters and finding the most accurate provider.
        List<Pair<String, String>> pairs = Info.getInstance().getLoginsListForHttp(context);
        // Location data.
        pairs.add(new Pair<>(Info.LONGITUDE, String.valueOf(location.getLongitude())));
        pairs.add(new Pair<>(Info.LATITUDE, String.valueOf(location.getLatitude())));
        pairs.add(new Pair<>(Info.TIME, (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                .format(new Date(location.getTime()))));
        // Non-location data.
        pairs.add(new Pair<>(Info.BATTERY_LEVEL, String.valueOf(batteryLevel)));
        pairs.add(new Pair<>(Info.POINT_TYPE, Info.COMMON));

        //Post request to server.
        DataSender dataSender = new DataSender();
        MyHttpResponse myHttpResponse = dataSender.httpPostQuery(Info.MOBILE_GET_POINT_URL, pairs,
                Info.getInstance().WAIT_TIME);

        response = myHttpResponse.getResponse();
        if (myHttpResponse.getErrorCode() != MyHttpResponse.OK) {
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
//        logRecord("response==>" + response);
//
//        if (aBoolean) {
//            logRecord("Data was sent.");
//        } else {
//            logRecord("Error sending data!");
//        }

        // TODO show error toasts on context
    }
}