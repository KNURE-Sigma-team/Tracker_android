package com.nure.sigma.wimk.wimk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InternetConnectionReceiver extends BroadcastReceiver {


    public InternetConnectionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, FailedSenderService.class));

    }
}