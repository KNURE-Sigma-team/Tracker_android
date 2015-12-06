package com.nure.sigma.wimk.wimk;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nure.sigma.wimk.wimk.logic.Child;
import com.nure.sigma.wimk.wimk.logic.DataSender;
import com.nure.sigma.wimk.wimk.logic.Info;
import com.nure.sigma.wimk.wimk.logic.MyHttpResponse;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {

    private Info info = Info.getInstance();

    private Context context;
    private EditText usernameEditText;
    private EditText passwordEditText;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        info.setSettings(getSharedPreferences(Info.PASSWORD, 0));

        setContentView(R.layout.activity_login);
        usernameEditText = (EditText) findViewById(R.id.username_edit_text);
        passwordEditText = (EditText) findViewById(R.id.password_editText);
        Button loginButton = (Button) findViewById(R.id.login_btn);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(usernameEditText.getText().toString(), passwordEditText.getText().toString());
            }
        });
    }

    private void login(String username, String password) {
        LoginAsyncTask loginAsyncTask = new LoginAsyncTask(username, password);
        loginAsyncTask.execute();
    }

    private void afterSuccessfulLogin(String serverResponse){
        SharedPreferences.Editor e = info.getSettings().edit();
        e.putBoolean(Info.IS_FIRST_ENTER, false);
        e.apply();

        //TODO save username and password to preferences somehow

        if(!info.isBackgroundserviceRunning(LoginActivity.this)) {
            // Background service is switched off.
            if(info.setRunning(false)) {
                // Running was set to false successfully, means this value was true.
                new InformStoppedTrackingTask().execute();
            }
        }

        List<Child> children = Child.parseChildrenList(serverResponse);
        if(children.size() == 0){
            Toast.makeText(this, getString(R.string.no_children_error), Toast.LENGTH_LONG).show();
            return;
        }
        if(children.size() == 1){
            Child child = children.get(0);

            // FIXME
            //info.moveToMainActivity(LoginActivity.this, child.getName(), child.getSendingFrequency());
            info.moveToMainActivity(LoginActivity.this, child.getName(), 1);
        }
        else{
            // Have many children
            moveToChooseChildActivity(children);
        }
    }

    private void moveToChooseChildActivity(List<Child> childList) {
        Info.getInstance().setChildList(childList);

        Intent intent = new Intent(this, ChooseChildActivity.class);
        startActivity(intent);
    }

    private class LoginAsyncTask extends AsyncTask<Void, Void, String> {

        public static final String SERVER_ERROR_RESPONSE = "Exception\n";

        private String response;

        private String username;
        private String password;

        private ProgressDialog loadingDialog;

        public LoginAsyncTask(String username, String password) {
            this.username = username;
            Log.i("SERVICE", "username==>" + username);
            this.password = password;
            Log.i("SERVICE", "password==>" + password);
//            this.loginChild = loginChild;
//            Log.i("SERVICE", "loginChild==>" + loginChild);
        }

        @Override
        protected void onPreExecute() {
            this.loadingDialog = new ProgressDialog(context);
            this.loadingDialog.setMessage(context.getString(R.string.loading));
            LoginActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            this.loadingDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            List<Pair<String, String>> pairs = new ArrayList<>();
            pairs.add(new Pair<>("loginParent", username));
            pairs.add(new Pair<>(Info.PASSWORD, password));

            DataSender dataSender = new DataSender();
            MyHttpResponse myHttpResponse = dataSender.httpPostQuery(
                    Info.AUTH_SERVER_URL, pairs, Info.WAIT_TIME);
            int errorCode = myHttpResponse.getErrorCode();

            Log.e("andstepko", "errorCode from Server==>" + errorCode);

            if (errorCode == MyHttpResponse.OK) {

                response = myHttpResponse.getResponse();

                Log.e("andstepko", "login response==>" + response);
                Log.i("SERVICE", "got errorCode==>" + String.valueOf(myHttpResponse.getErrorCode()));

                if(response.equals(SERVER_ERROR_RESPONSE)){
                    return context.getString(R.string.login_unsuccessful);
                }
                return context.getString(R.string.login_successful);
            } else {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().
                getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnected()) {
                    return context.getString(R.string.login_unsuccessful);
                }
                else{
                    return context.getString(R.string.no_connection);
                }
            }
        }

        @Override
        protected void onPostExecute(String aString) {

            Toast.makeText(context, aString, Toast.LENGTH_SHORT).show();

            if (aString.equals(context.getString(R.string.login_successful))) {
                SharedPreferences.Editor e = info.getSettings().edit();
                e.putString(Info.PARENT_LOGIN, username);
                e.commit();

                afterSuccessfulLogin(response);
            }

            passwordEditText.setText(info.EMPTY_STRING);
            this.loadingDialog.dismiss();
            LoginActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}
