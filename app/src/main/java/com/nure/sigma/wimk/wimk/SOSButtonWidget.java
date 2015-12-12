package com.nure.sigma.wimk.wimk;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Arrays;


public class SOSButtonWidget extends AppWidgetProvider {

    final static String LOG_TAG = "myLogs";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        Log.d(LOG_TAG, "onUpdate " + Arrays.toString(appWidgetIds));
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);

        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        Log.d(LOG_TAG, "onDeleted " + Arrays.toString(appWidgetIds));
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        super.onEnabled(context);
        Log.d(LOG_TAG, "onEnabled");

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        super.onDisabled(context);
        Log.d(LOG_TAG, "onDisabled");
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Log.d(LOG_TAG, "updateWidget " + appWidgetId);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.sosbutton_widget);
        Intent serviceIntent = new Intent(context, SOSService.class);
        serviceIntent.setAction("com.nure.sigma.wimk.wimk.WIDGET_CLICK");
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pIntent = PendingIntent.getService(context, appWidgetId,
                serviceIntent, 0);
        views.setOnClickPendingIntent(R.id.imageButton, pIntent);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equalsIgnoreCase("com.nure.sigma.wimk.wimk.WIDGET_CLICK")) {
            Intent i = new Intent(context.getApplicationContext(), SOSService.class);
            context.getApplicationContext().startService(i);
        }
    }
}

