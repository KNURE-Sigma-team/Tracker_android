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

import com.nure.sigma.wimk.wimk.logic.Child;
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

    //String serverURL = "http://blockverify.cloudapp.net:8080/wimk/mobile_authorization";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        settings = getSharedPreferences(Info.PASSWORD, 0);

        setContentView(R.layout.activity_login);
        usernameEditText = (EditText) findViewById(R.id.username_edit_text);
        passwordEditText = (EditText) findViewById(R.id.password_editText);
        //childNameEditText = (EditText) findViewById(R.id.loginChild_editText);
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

    private void afterFailedLogin() {
            //usernameEditText.setText(info.EMPTY_STRING);
            passwordEditText.setText(info.EMPTY_STRING);
    }

    private void afterSuccessfulLogin(String serverResponse){
        SharedPreferences.Editor e = settings.edit();
        e.putBoolean(Info.IS_FIRST_ENTER, false);
        e.apply();
        //TODO save username and password to preferences somehow

        List<Child> children = Child.parseChildrenList(serverResponse);
        if(children.size() == 0){
            Toast.makeText(this, getString(R.string.no_children_error), Toast.LENGTH_SHORT).show();
            return;
        }
        if(children.size() == 1){
            moveToMainActivity(children.get(0));
        }
        else{
            // Have many children
            moveToChooseChildActivity(children);
        }
    }

    private void moveToMainActivity(Child child) {
        SharedPreferences.Editor e = settings.edit();
        e.putInt(Info.ID_CHILD, child.getId());
        e.commit();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void moveToChooseChildActivity(List<Child> childList) {
        Info.getInstance().setChildList(childList);

        Intent intent = new Intent(this, ChooseChildActivity.class);
        startActivity(intent);
    }

    private class LoginAsyncTask extends AsyncTask<Void, Void, Boolean> {

        public static final String SERVER_ERROR_RESPONSE = "Exception\n";

        private String response;

        private String username;
        private String password;

        private ProgressDialog loadingDialog;

        //public LoginAsyncTask(String username, String password, String loginChild) {
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
            this.loadingDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            List<Pair<String, String>> pairs = new ArrayList<>();
            pairs.add(new Pair<String, String>("loginParent", username));
            //pairs.add(new Pair<String, String>("loginChild", loginChild));
            pairs.add(new Pair<String, String>("password", password));

            int id;
            DataSender dataSender = new DataSender();
            MyHttpResponse myHttpResponse = dataSender.HttpPostQuery(AUTH_URL, pairs, WAIT_TIME);
            int errorCode = myHttpResponse.getErrorCode();

            Log.e("andstepko", "errorCode from Server==>" + errorCode);

            if (errorCode == MyHttpResponse.OK) {

                response = myHttpResponse.getResponse();

                Log.e("andstepko", "login response==>" + response);
                Log.i("SERVICE", "got errorCode==>" + String.valueOf(myHttpResponse.getErrorCode()));

                if(response.equals(SERVER_ERROR_RESPONSE)){
                    return false;
                }
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                Toast.makeText(context, context.getString(R.string.login_successful), Toast.LENGTH_SHORT).show();
                afterSuccessfulLogin(response);
            } else {
                Toast.makeText(context, context.getString(R.string.login_unsuccessful), Toast.LENGTH_SHORT).show();
                afterFailedLogin();
            }

            this.loadingDialog.dismiss();
        }
    }
}
