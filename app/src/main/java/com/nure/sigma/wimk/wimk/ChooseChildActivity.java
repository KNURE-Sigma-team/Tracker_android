package com.nure.sigma.wimk.wimk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.nure.sigma.wimk.wimk.logic.Child;
import com.nure.sigma.wimk.wimk.logic.DataSender;
import com.nure.sigma.wimk.wimk.logic.Info;
import com.nure.sigma.wimk.wimk.logic.MyHttpResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andstepko on 22.11.15.
 */
public class ChooseChildActivity extends Activity {

    private ListView listView;
    SharedPreferences settings;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = getSharedPreferences(Info.PASSWORD, 0);
        editor = settings.edit();

        // Log out
        String parentLogin = settings.getString(Info.PARENT_LOGIN, null);
        String childLogin = settings.getString(Info.CHILD_LOGIN, null);
        if((parentLogin != null) && (childLogin != null)) {
            LogOutTask logOutTask = new LogOutTask(parentLogin, childLogin);
            logOutTask.execute();
        }

        // Clear data about the child from DB
        editor.putString(Info.CHILD_LOGIN, null);
        editor.putInt(Info.SENDING_FREQUENCY, Info.DEFAULT_SENDING_FREQUENCY);
        editor.commit();


        setContentView(R.layout.activity_choose_child);
        listView = (ListView)findViewById(R.id.childrenListView);

        initializeChildList(Info.getInstance().getChildList());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Child child = (Child)adapterView.getAdapter().getItem(i);
                editor.putString(Info.CHILD_LOGIN, child.getName());
                editor.putInt(Info.SENDING_FREQUENCY, child.getSendingFrequency());
                editor.commit();

                moveToMainActivity();
            }
        });
    }

    private void initializeChildList(List<Child> children){
        ChildAdapter childAdapter = new ChildAdapter(children, this);
        listView.setAdapter(childAdapter);
    }

    private void moveToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    private class LogOutTask extends AsyncTask<Void, Void, Void>{

        private String parentLogin;
        private String childLogin;

        public LogOutTask(String parentLogin, String childLogin) {
            this.parentLogin = parentLogin;
            this.childLogin = childLogin;
        }

        @Override
        protected Void doInBackground(Void... params) {
            DataSender dataSender = new DataSender();
            List<Pair<String, String>> pairs = new ArrayList<>();
            pairs.add(new Pair<>(Info.PARENT_LOGIN, parentLogin));
            pairs.add(new Pair<>(Info.CHILD_LOGIN, childLogin));

            MyHttpResponse myHttpResponse = dataSender.httpPostQuery
                    (Info.LOGOUT_SERVER_URL, pairs, Info.WAIT_TIME);
            return null;
        }
    }
}
