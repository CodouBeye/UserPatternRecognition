package com.api.codou.patternrecognition;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by codou on 26/05/15.
 */
public class DataHandler {

    public  static final String ID= "id";
    public static final String  ROT_VECT= "rot_vect";
    public static final String  TRANS_VECT= "trans_vect";
    public static final String  TIME= "time";
    public static final String TABLE_NAME="user";
    public static final String DATA_BASE_NAME="patterndb";
    public static final int DATA_BASE_VERSION=1;
    public static final String DATABASE_CREATE="create table user(id text not null , time text not null,trans_vect float,rot_vect float);";

    DataBaseHelper dbHelper;
    Context ctx;
    SQLiteDatabase db;

    public DataHandler (Context ctx){
        this.ctx=ctx;
        dbHelper= new DataBaseHelper(ctx);
    }
    private static class DataBaseHelper extends SQLiteOpenHelper {


        public DataBaseHelper(Context ctx){
            super(ctx, DATA_BASE_NAME, null, DATA_BASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {

                db.execSQL(DATABASE_CREATE);
            }
            catch (android.database.SQLException e){
                e.printStackTrace();
            }

        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);

        }
    }

    public DataHandler open(){
        db= dbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        dbHelper.close();
    }

    public long insertData(String id,String time,float rotation, float translation){
        ContentValues content=  new ContentValues();
        content.put(ID,id);
        content.put(TIME,time);
        content.put(TRANS_VECT,translation);
        content.put(ROT_VECT,rotation);

        return db.insertOrThrow(TABLE_NAME,null,content);
    }


    public Cursor returnData(){
        return  db.query(TABLE_NAME,new String[]{ID,TIME,TRANS_VECT,ROT_VECT},null,null,null,null,null);
    }
}
