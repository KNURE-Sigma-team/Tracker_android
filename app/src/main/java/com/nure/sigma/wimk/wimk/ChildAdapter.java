package com.nure.sigma.wimk.wimk;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nure.sigma.wimk.wimk.logic.Child;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andstepko on 22.11.15.
 */
public class ChildAdapter extends BaseAdapter {

    private List<Child> list;
    private Activity activity;
    private boolean flag;

    public ChildAdapter(List<Child> list, Activity activity){
        this.list = new ArrayList<Child>();
        for(Child child : list){
            this.list.add(child);
        }
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        if((i < list.size()) && (i >= 0)){
            return list.get(i);
        }
        else{
            return null;
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view==null)
        {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.child_list_item, viewGroup,false);
        }

        TextView childNameTextView = (TextView)view.findViewById(R.id.childItemTextView);

        Child child = list.get(i);
        childNameTextView.setText(child.getName());

        return view;
    }
}
