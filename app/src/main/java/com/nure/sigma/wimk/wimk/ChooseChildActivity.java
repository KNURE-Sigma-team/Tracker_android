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

    private Info info = Info.getInstance();

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_choose_child);
        listView = (ListView)findViewById(R.id.childrenListView);

        initializeChildList(Info.getInstance().getChildList());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Child child = (Child)adapterView.getAdapter().getItem(i);

                //FIXME !!!
//                Info.getInstance().moveToMainActivity(ChooseChildActivity.this,
//                        child.getName(), child.getSendingFrequency());
                Info.getInstance().moveToMainActivity(ChooseChildActivity.this,
                        child.getName(), 1);


            }
        });
    }

    private void initializeChildList(List<Child> children){
        ChildAdapter childAdapter = new ChildAdapter(children, this);
        listView.setAdapter(childAdapter);
    }
}
