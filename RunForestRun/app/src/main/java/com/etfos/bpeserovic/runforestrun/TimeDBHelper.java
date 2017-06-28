package com.etfos.bpeserovic.runforestrun;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import static android.support.v7.media.MediaItemMetadata.KEY_YEAR;

/**
 * Created by Bobo on 13.6.2017..
 */

public class TimeDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "RunTimes";
    private static final int DATABASE_VERSION = 7;
    private static final String TABLE_TIMES = "tableTimes";
    private static final String KEY_ID = "_id";
    private static final String KEY_TIME = "time";

    //singleton
    private static TimeDBHelper dbHelper = null;

    public TimeDBHelper(Context context){
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized TimeDBHelper getInstance(Context context){
        if(dbHelper == null){
            dbHelper = new TimeDBHelper(context);
        }
        return dbHelper;
    }

    SQLiteDatabase db;

    public void dajBaze(){
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //String CREATE_TIMES_TABLE = "CREATE TABLE " + TABLE_TIMES + "(" + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_TIME + "text " + ")";
        Log.d("BORIS","onCreate TimeDBHelper");
        db.execSQL("CREATE TABLE " + TABLE_TIMES + "(" + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_TIME + " text " + ")"); //PAZI NA RAZMAKEEEEEEEEEEEEEEEEE
        //db.execSQL("create table contacts " + "(id integer primary key, name text,phone text,email text, street text,place text)
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void addTime(Times time){
        Log.d("BORIS","add time TimeDBHelper");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TIME, time.getTime());
        db.insert(TABLE_TIMES, KEY_TIME, values);
        db.close();
    }

    public ArrayList<Times> getTimes(){
        ArrayList<Times> times = new ArrayList<Times>();
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_TIMES, null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    Times time = new Times(
                            cursor.getInt(0), cursor.getString(1));
                    times.add(time);
                } while (cursor.moveToNext());
            }
            db.close();
            cursor.close();
        return times;
    }

    public void deleteTime(Times time){
        int id = time.getId();
        String[] arg = new String[]{
                String.valueOf(id)
        };
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TIMES, KEY_ID + "=?", arg);
        db.close();
    }
}
