package com.nure.sigma.wimk.wimk;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity {

    private static final String EMPTY_STRING = "";

    SharedPreferences settings;

    private Context context;
    EditText usernameEditText;
    EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        settings = getSharedPreferences("password", 0);

        if(settings.getBoolean("isFirstEnter", true)){
            // Login if first app enter.
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);

            usernameEditText = (EditText) findViewById(R.id.username_editText);
            passwordEditText = (EditText) findViewById(R.id.password_editText);
            Button loginButton = (Button) findViewById(R.id.login_btn);

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    login(usernameEditText.getText().toString(), passwordEditText.getText().toString());
                }
            });
        }
        else{
            moveToMainActivity();
        }
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
            // Logins is unsccessfull.
            usernameEditText.setText(EMPTY_STRING);
            passwordEditText.setText(EMPTY_STRING);
        }
    }

    private void moveToMainActivity(){
        //TODO
    }


    private class LoginAsyncTask extends AsyncTask<Void, Void, Boolean>{

        private String username;
        private String password;
        private ProgressDialog loadingDialog;

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

            // TODO server request


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
