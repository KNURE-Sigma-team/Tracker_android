package com.nure.sigma.wimk.wimk;


import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity {

    private Button stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            finish();
        }
        setContentView(R.layout.activity_main);
        final EditText frequency = (EditText) findViewById(R.id.update_frequency);
        Button start = (Button) findViewById(R.id.start_button);
        Button stop = (Button) findViewById(R.id.stop_button);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences  settings = getSharedPreferences("password", 0);
                SharedPreferences.Editor e = settings.edit();
                e.putBoolean("runable", true);
                try {
                    e.putInt("frequency",Integer.parseInt(frequency.getText().toString()));
                }
                catch (Exception f){
                    e.putInt("frequency", 30);
                }
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

    private void moveToLoginActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}


