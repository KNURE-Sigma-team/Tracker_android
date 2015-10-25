package com.nure.sigma.wimk.wimk;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.nure.sigma.wimk.wimk.logic.Util;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {

    private static final String EMPTY_STRING = "";

    SharedPreferences settings;

    private Context context;
    EditText usernameEditText;
    EditText passwordEditText;
    EditText loginChildEditText;

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
            loginChildEditText = (EditText) findViewById(R.id.loginChild_editText);
            Button loginButton = (Button) findViewById(R.id.login_btn);

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    login(usernameEditText.getText().toString(),passwordEditText.getText().toString(),loginChildEditText.getText().toString());
                }
            });
        //}
       // else{
        //    moveToMainActivity();
        //}
    }

    private void login(String username, String password, String loginChild){
        LoginAsyncTask loginAsyncTask = new LoginAsyncTask(username, password, loginChild);
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
        private String loginChild;
        private ProgressDialog loadingDialog;

        public LoginAsyncTask(String username, String password, String loginChild) {
            this.username = username;
            this.password = password;
            this.loginChild = loginChild;
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
            id = Util.HttpPostRequest(serverURL, pairs);
            if (id != -1){
            SharedPreferences.Editor e = settings.edit();
            e.putInt("idChild", id);
            e.apply();
            return true;
            }
            else{
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(aBoolean){
            Toast.makeText(context,"Login succesful", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(context,"Login unsuccesful", Toast.LENGTH_SHORT).show();
            }
            this.loadingDialog.dismiss();

            afterLoginBehaviour(aBoolean);
        }
    }
}
