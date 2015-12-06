package com.nure.sigma.wimk.wimk;

import android.os.AsyncTask;

import com.nure.sigma.wimk.wimk.logic.Info;

/**
 * Created by andstepko on 06.12.15.
 */
public class InformStoppedTrackingTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... params) {
        Info.getInstance().informStoppedTracking();
        return null;
    }
}
