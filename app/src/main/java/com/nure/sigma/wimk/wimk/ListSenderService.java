package com.nure.sigma.wimk.wimk;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Pair;

import com.nure.sigma.wimk.wimk.logic.DataSender;
import com.nure.sigma.wimk.wimk.logic.Info;
import com.nure.sigma.wimk.wimk.logic.MyHttpResponse;
import com.nure.sigma.wimk.wimk.logic.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Дмитрий on 08.11.2015.
 */
public class ListSenderService extends IntentService {

    boolean running;
    public static final String MOBILE_GET_POINT_URL = Info.getInstance().SERVER_URL + "mobile_get_point";

    public ListSenderService() {
        super(ListSenderService.class.getName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(Info.PASSWORD, 0);
        int idChild = settings.getInt(Info.ID_CHILD, 0);
        Util.logRecord(Info.ID_CHILD + " = " + idChild);
        //  Util.fillFileList(this);
        List<Pair<String, String>> pairs = new ArrayList<>();
        for (Pair<Location, String> pair : Info.FILE_LIST) {
            pairs.add(new Pair<>(Info.ID_CHILD, String.valueOf(idChild)));

            pairs.add(new Pair<>(Info.LONGITUDE, String.valueOf(pair.first.getLongitude())));
            pairs.add(new Pair<>(Info.LATITUDE, String.valueOf(pair.first.getLatitude())));
            pairs.add(new Pair<>(Info.TIME, (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                    .format(new Date(pair.first.getTime()))));

            pairs.add(new Pair<>(Info.BATTERY_LEVEL, pair.second));
            pairs.add(new Pair<>(Info.POINT_TYPE, Info.COMMON));
            //Sending
            DataSender dataSender = new DataSender();
            MyHttpResponse myHttpResponse = dataSender.HttpPostQuery(MOBILE_GET_POINT_URL, pairs, Info.WAIT_TIME);
            /*if (myHttpResponse.getErrorCode() != MyHttpResponse.OK) {
                Util.addToFileList(new Pair<Location, String>(pair.first, pair.second),this);
            }*/
        }
    }
}