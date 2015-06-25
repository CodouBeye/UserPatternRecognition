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
    public  static final String NUM ="num";
    public static final String  ROT_X= "rot_x";
    public static final String  ROT_Y= "rot_y";
    public static final String  ROT_Z= "rot_z";
    public static final String  TRANS_X= "trans_x";
    public static final String  TRANS_Y= "trans_y";
    public static final String  TRANS_Z= "trans_z";
    public static final String  TIME= "time";
    public static final String TABLE_NAME="user";
    public static final String DATA_BASE_NAME="patterndb";
    public static final int DATA_BASE_VERSION=1;
    public static final String DATABASE_CREATE="create table user(id text not null, num integer , time text not null,trans_x float,trans_y float,trans_z float, rot_x float,rot_y float,rot_z float);";

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

    public long insertData(String id,int num,String time, float transx ,float transy,float transz,float rotx,float roty,float rotz){
        ContentValues content=  new ContentValues();
        content.put(ID,id);
        content.put(NUM,num);
        content.put(TIME,time);
        content.put(TRANS_X,transx);
        content.put(TRANS_Y,transy);
        content.put(TRANS_Z,transz);
        content.put(ROT_X,rotx);
        content.put(ROT_Y,roty);
        content.put(ROT_Z, rotz);
        return db.insertOrThrow(TABLE_NAME,null,content);
    }


    public Cursor returnData(){
        return  db.query(TABLE_NAME, new String[]{ID, NUM, TIME, TRANS_X, TRANS_Y, TRANS_Z, ROT_X, ROT_Y, ROT_Z}, null, null, null, null, null);
    }

    public Cursor select(String id){
        return db.rawQuery("SELECT * FROM user where id=?", new String[]{id});
    }

    public Cursor selectMaxNum(String id){
        return db.rawQuery("SELECT MAX(num) FROM user where id=?",new String[]{id});
    }

   /* public Cursor delete(String id ,int num){
        return db.rawQuery("DELETE FROM user where id=? and num=?",new String[]{id,String.valueOf(num)});
    }*/

    public void deleteRes(String id, int num){
        db.delete(TABLE_NAME,"id=? and num=?",new String[]{id,String.valueOf(num)});
    }
}
