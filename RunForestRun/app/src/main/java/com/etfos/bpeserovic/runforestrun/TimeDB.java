package com.etfos.bpeserovic.runforestrun;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Bobo on 13.6.2017..
 */

public class TimeDB extends Activity implements AdapterView.OnItemLongClickListener {
    ListView lvTimes;
//    TimeDBHelper dbHelper = new TimeDBHelper(this);
    TimeDBHelper dbHelper;

    ArrayList<Times> myTimes;
    ArrayAdapter<Times> myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);
//        dbHelper = new TimeDBHelper(this);
        MainActivity.dbHelper = dbHelper;
        lvTimes = (ListView) findViewById(R.id.lvTimes);
        Log.d("bbbbbbbbbbbbbbbbbbbbb","bbbbbbbbbbbbbbbbbbbbbbbb");
        if (dbHelper != null)
        {
            myTimes = dbHelper.getTimes();
            myAdapter = new ArrayAdapter<Times>(this, android.R.layout.simple_list_item_1, myTimes);
            lvTimes.setAdapter(myAdapter);
            lvTimes.setOnItemLongClickListener(this);
        }
        else
        {
            Log.d("ddddddddddddd", "dddddddddddddddd");
        }
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        dbHelper.deleteTime(myAdapter.getItem(position));
        myTimes.remove(position);
        myAdapter.notifyDataSetChanged();
        return false;
    }
}
