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

import com.nure.sigma.wimk.wimk.logic.DataSender;
import com.nure.sigma.wimk.wimk.logic.Info;
import com.nure.sigma.wimk.wimk.logic.MyHttpResponse;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {

    public static final String AUTH_URL = Info.getInstance().SERVER_URL + "mobile_authorization";
    public static final int WAIT_TIME = Info.getInstance().WAIT_TIME;

    SharedPreferences settings;
    Info info = Info.getInstance();

    private Context context;
    EditText usernameEditText;
    EditText passwordEditText;
    EditText childNameEditText;

    protected void onCreate(Bundle savedInstanceState) {

        context = this;
        settings = getSharedPreferences("password", 0);

        super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);
            usernameEditText = (EditText) findViewById(R.id.username_edit_text);
            passwordEditText = (EditText) findViewById(R.id.password_editText);
            childNameEditText = (EditText) findViewById(R.id.loginChild_editText);
            Button loginButton = (Button) findViewById(R.id.login_btn);

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    login(usernameEditText.getText().toString(), passwordEditText.getText().toString(),
                            childNameEditText.getText().toString());
                }
            });
    }

    private void login(String username, String password, String childName){
        LoginAsyncTask loginAsyncTask = new LoginAsyncTask(username, password, childName);
        loginAsyncTask.execute();
    }

    private void afterLoginBehaviour(boolean loginSuccess){
        if(loginSuccess){
            SharedPreferences.Editor e = settings.edit();
            e.putBoolean("isFirstEnter", false);
            e.apply();
            //TODO save username and password to preferences somehow
            moveToMainActivity();
        }
        else{
            usernameEditText.setText(info.EMPTY_STRING);
            passwordEditText.setText(info.EMPTY_STRING);
            childNameEditText.setText(info.EMPTY_STRING);
        }
    }

    private void moveToMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    private class LoginAsyncTask extends AsyncTask<Void, Void, Boolean>{

        private String username;
        private String password;
        private String loginChild;
        private ProgressDialog loadingDialog;

        public LoginAsyncTask(String username, String password, String loginChild) {
            this.username = username;
            Log.i("SERVICE", "username==>" + username);
            this.password = password;
            Log.i("SERVICE", "password==>" + password);
            this.loginChild = loginChild;
            Log.i("SERVICE", "loginChild==>" + loginChild);
        }

        @Override
        protected void onPreExecute() {
            this.loadingDialog = new ProgressDialog(context);
            this.loadingDialog.setMessage(context.getString(R.string.loading));
            this.loadingDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            List<Pair<String,String>> pairs = new ArrayList<>();
            pairs.add(new Pair<String, String>("loginParent",username));
            pairs.add(new Pair<String, String>("loginChild",loginChild));
            pairs.add(new Pair<String, String>("password", password));

            int id;
            DataSender dataSender = new DataSender();
            MyHttpResponse myHttpResponse = dataSender.HttpPostQuery(AUTH_URL, pairs, WAIT_TIME);
            int errorCode = myHttpResponse.getErrorCode();

            Log.e("andstepko", "errorCode from Server==>" + errorCode);

            if (errorCode == MyHttpResponse.OK){

                Log.e("andstepko", "login response==>" + myHttpResponse.getResponse());

                int i;
                try {
                    double d = Double.valueOf(myHttpResponse.getResponse());
                    i = (int) d;
                } catch (Exception e) {
                    i = -1;
                }

                Log.i("SERVICE", "got errorCode==>" + String.valueOf(myHttpResponse.getErrorCode()));

                if(i != -1) {
                    SharedPreferences.Editor e = settings.edit();
                    e.putInt("idChild", i);
                    e.apply();
                    return true;
                }
                else{
                    // Server returned error.
                    return false;
                }
            }
            else{
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(aBoolean){
                Toast.makeText(context,"Login successful", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(context,"Login unsuccessful", Toast.LENGTH_SHORT).show();
            }

            this.loadingDialog.dismiss();
            afterLoginBehaviour(aBoolean);
        }
    }
}
