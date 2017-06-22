package com.etfos.bpeserovic.runforestrun;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;


/**
 * Created by Bobo on 13.6.2017..
 */

public class MainActivity extends Activity implements View.OnClickListener {

    Button bStartRunning, bDatabase;
    public static TimeDBHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new TimeDBHelper(this);
        dbHelper.dajBaze();
        bStartRunning = (Button) findViewById(R.id.bMapActivity);
        bStartRunning.setOnClickListener(this);
        bDatabase = (Button) findViewById(R.id.bDatabaseActivity);
        bDatabase.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent();
        switch (v.getId()){
            case R.id.bMapActivity:
                i.setClass(this, MapActivity.class);
                break;
            case R.id.bDatabaseActivity:
                i.setClass(this, TimeDB.class);
                break;
        }
        startActivity(i);
    }
}
