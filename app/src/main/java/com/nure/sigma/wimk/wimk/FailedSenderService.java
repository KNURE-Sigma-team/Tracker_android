package com.nure.sigma.wimk.wimk;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FailedSenderService extends IntentService {

    private Info info = Info.getInstance();

    public FailedSenderService() {
        super("FailedSenderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Util.logRecord(networkInfo.getTypeName());
            Util.fillFileList(getApplicationContext());

            Util.logRecord(String.valueOf(info.getFailedLocations().size()));
            if (info.getFailedLocations() != null && !info.getFailedLocations().isEmpty()) {
                Util.logRecord("Sending failed locations");
                Util.logRecord(networkInfo.getTypeName());

                SharedPreferences temp = getApplicationContext().getSharedPreferences(Info.PASSWORD, 0);
                ArrayList<Pair<Location, String>> tempList = new ArrayList<>();

                for (Pair<Location, String> pair : info.getFailedLocations()) {

                    List<Pair<String, String>> pairs = info.getLoginsListForHttp(getApplicationContext());
                    pairs.add(new Pair<>(Info.LONGITUDE, String.valueOf(pair.first.getLongitude())));
                    pairs.add(new Pair<>(Info.LATITUDE, String.valueOf(pair.first.getLatitude())));
                    pairs.add(new Pair<>(Info.TIME, (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                            .format(new Date(pair.first.getTime()))));
                    pairs.add(new Pair<>(Info.BATTERY_LEVEL, pair.second));
                    pairs.add(new Pair<>(Info.POINT_TYPE, Info.COMMON));
                    DataSender dataSender = new DataSender();
                    MyHttpResponse myHttpResponse = dataSender.httpPostQuery(
                            Info.MOBILE_GET_POINT_URL, pairs, Info.WAIT_TIME);
                    Util.logRecord(String.valueOf(myHttpResponse.getErrorCode()));
                    if (myHttpResponse.getErrorCode() != MyHttpResponse.OK) {
                        tempList.add(new Pair<Location, String>(pair.first, pair.second));
                    }
                }

                info.clearFailedLocations();
                for (Pair<Location, String> pair : tempList) {
                    Util.addToFileList(pair, getApplicationContext());
                }
                Util.logRecord("Stop sending failed locations");
            }
        }
    }


}
