package com.nure.sigma.wimk.wimk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.nure.sigma.wimk.wimk.logic.Child;
import com.nure.sigma.wimk.wimk.logic.Info;

import java.util.List;

/**
 * Created by andstepko on 22.11.15.
 */
public class ChooseChildActivity extends Activity {

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
                //TODO onCLick behaviour
                moveToMainActivity((Child)adapterView.getAdapter().getItem(i));
            }
        });
    }

    private void initializeChildList(List<Child> children){
        ChildAdapter childAdapter = new ChildAdapter(children, this);
        listView.setAdapter(childAdapter);
    }

    private void moveToMainActivity(Child child) {
        SharedPreferences settings = getSharedPreferences(Info.PASSWORD, 0);
        SharedPreferences.Editor e = settings.edit();
        e.putInt(Info.ID_CHILD, child.getId());
        e.commit();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
