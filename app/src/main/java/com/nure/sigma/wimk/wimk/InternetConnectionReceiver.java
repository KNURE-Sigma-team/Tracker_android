package com.nure.sigma.wimk.wimk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nure.sigma.wimk.wimk.logic.Info;

public class InternetConnectionReceiver extends BroadcastReceiver {


    public InternetConnectionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Info.getInstance().isMyServiceRunning(FailedSenderService.class, context))
        context.startService(new Intent(context, FailedSenderService.class));
    }
}