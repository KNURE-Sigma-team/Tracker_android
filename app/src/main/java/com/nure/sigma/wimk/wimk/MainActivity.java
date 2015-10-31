package com.nure.sigma.wimk.wimk;


import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    private Button stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button start = (Button) findViewById(R.id.start_button);
        Button stop = (Button) findViewById(R.id.stop_button);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences  settings = getSharedPreferences("password", 0);
                SharedPreferences.Editor e = settings.edit();
                e.putBoolean("runable", true);
                e.apply();
                startService(new Intent(getApplicationContext(), BackgroundService.class));
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences  settings = getSharedPreferences("password", 0);
                SharedPreferences.Editor e = settings.edit();
                e.putBoolean("runable",false );
                e.apply();
                stopService(new Intent(getApplicationContext(), BackgroundService.class));
            }
        });
    }
}


