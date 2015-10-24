package com.nure.sigma.wimk.wimk;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends Activity {

    private static final String EMPTY_STRING = "";

    SharedPreferences settings;

    private Context context;
    EditText usernameEditText;
    EditText passwordEditText;

    String serverURL = "http://178.165.37.203:8080/wimk/mobile_authorization";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        context = this;
        settings = getSharedPreferences("password", 0);

        super.onCreate(savedInstanceState);

        //if(settings.getBoolean("isFirstEnter", true)){
            // Login if first app enter.
            setContentView(R.layout.activity_login);

            usernameEditText = (EditText) findViewById(R.id.username_editText);
            passwordEditText = (EditText) findViewById(R.id.password_editText);
            Button loginButton = (Button) findViewById(R.id.login_btn);

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new LoginAsyncTask().execute();
                    //login(usernameEditText.getText().toString(), passwordEditText.getText().toString());
                }
            });
        //}
       // else{
        //    moveToMainActivity();
        //}
    }

    private void login(String username, String password){
        LoginAsyncTask loginAsyncTask = new LoginAsyncTask(username, password);
        loginAsyncTask.execute();
    }

    private void afterLoginBehaviour(boolean loginSuccess){
        if(loginSuccess){
            SharedPreferences.Editor e = settings.edit();
            e.putBoolean("isFirstEnter", false);
            e.commit();

            //TODO save username and password to preferences somehow

            moveToMainActivity();
        }
        else{
            usernameEditText.setText(EMPTY_STRING);
            passwordEditText.setText(EMPTY_STRING);
        }
    }

    private void moveToMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    private class LoginAsyncTask extends AsyncTask<Void, Void, Boolean>{

        private String username;
        private String password;
        private ProgressDialog loadingDialog;

        public LoginAsyncTask() {

        }
        public LoginAsyncTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            this.loadingDialog = new ProgressDialog(context);
            this.loadingDialog.setMessage(context.getString(R.string.loading));
            this.loadingDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            URL obj = null;
            try {
                obj = new URL(serverURL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) obj.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                connection.setRequestMethod("POST");
            } catch (java.net.ProtocolException e) {
                e.printStackTrace();
            }

            StringBuilder urlParameters = new StringBuilder();

            urlParameters.append("loginParent");
            urlParameters.append("=");
            urlParameters.append("alik");
            urlParameters.append("&");
            urlParameters.append("loginChild");
            urlParameters.append("=");
            urlParameters.append("test1");
            urlParameters.append("&");
            urlParameters.append("password");
            urlParameters.append("=");
            urlParameters.append("test1");

            connection.setDoOutput(true);

            DataOutputStream wr = null;
            try {
                wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(urlParameters.toString());
                wr.flush();
                wr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            String response;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while((line = reader.readLine()) != null)
                {
                    // Append server response in string
                    sb.append(line + "\n");
                }
                response = sb.toString();
                Log.i("SERVICE", response);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(aBoolean){
                // Success
                //TODO Toast
            }
            else{
                //TODO Toast
            }
            this.loadingDialog.dismiss();

            afterLoginBehaviour(aBoolean);
        }
    }
}
