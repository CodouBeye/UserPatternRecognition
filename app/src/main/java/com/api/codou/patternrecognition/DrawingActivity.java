package com.api.codou.patternrecognition;


import android.app.ActionBar;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class DrawingActivity extends Activity {
    EditText userId;
    private  DataHandler dataSource;
    int num;
    String getid;
    float getTransx,getTransy,getTransz, getRotx,getRoty,getRotz;
    String id;
    int getNum;
    String getTime;
    Cursor ids,nums,maxNum;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_display);
        dataSource= new DataHandler(this);
        userId = (EditText) findViewById(R.id.userId);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("DrawInTheAir");
        mToolbar.setLogo(R.mipmap.ic_launcher);
        mToolbar.inflateMenu(R.menu.menu_main);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener(){
            @Override
            public  boolean onMenuItemClick(MenuItem menuItem){
                Toast.makeText(getBaseContext(),"Put an Id, click on the sign button and start draw in the air immediately",Toast.LENGTH_SHORT).show();
                return  true;
            }
        });
    }


/*
il s'agit ici de la premiere activite. en appuyant sur le bouton sign , l'appli doit se connecter à la bdd pour verifier s'il y a deja des enregistrement
et relever la valeur du compteur
Si le id existe deja dans la bdee, on va selectionner la valeur max du numero d'essai*/

    public void onClickButton(View v){
        String valStr = userId.getText().toString();
        dataSource.open();
        Cursor c = dataSource.selectMaxNum(userId.getText().toString());
        if(c.moveToFirst()) num= c.getInt(0);
        else num=0;
        Toast.makeText(getBaseContext(),"You tried "+ num+"time(s)",Toast.LENGTH_LONG).show();
        Bundle obj= new Bundle();
        obj.putInt("numero",num);
        Intent intent = new Intent(DrawingActivity.this, SensorActivity.class);
        //Intent newIntent=new Intent(DrawingActivity.this,LoggedActivity.class);
        intent.putExtra("id", valStr);
        intent.putExtras(obj);
       // newIntent.putExtra("id",valStr);
        startActivity(intent);
        DrawingActivity.this.finish();
    }

    public void exportData(View v) throws IOException {
        String valStr = userId.getText().toString();
        dataSource.open();
        int max;
        File myFile;
        Cursor c= dataSource.select(valStr);
        maxNum= dataSource.selectMaxNum(userId.getText().toString());
        if(maxNum.moveToFirst()) {max= maxNum.getInt(0);}
            else{ max=0;}
            Toast.makeText(getBaseContext(),"You tried "+ max +"time(s)",Toast.LENGTH_SHORT).show();
        if (c != null) {
            if (c.moveToFirst()) {
                do {



                    try {

                        final File folder = new File(Environment.getExternalStorageDirectory() + "/" + valStr);

                        boolean success = true;
                        if (!folder.exists()) {
                            success = folder.mkdir();

                        }
                        if (success) {

                            for (int i = 1; i < maxNum.getInt(0)+1; i++) {

                                myFile = new File(Environment.getExternalStorageDirectory() + "/" + valStr+ "/" + i + ".csv");
                                // MediaScannerConnection.scanFile(LoggedActivity.this, new String[]{"/export_file/Export_" + TimeStampDB + ".csv"}, null, null);
                                myFile.createNewFile();
                                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                intent.setData(Uri.fromFile(myFile));
                                sendBroadcast(intent);
                                FileOutputStream fOut = new FileOutputStream(myFile);
                                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                                myOutWriter.append("ID,Time,TransX,TransY,TransZ,RotX,RotY,RotZ");
                                myOutWriter.append("\n");
                                ids = dataSource.selectByIdNum(valStr, i);

                                if (ids != null) {
                                    if (ids.moveToFirst()) {
                                        do {
                                            id = ids.getString(0);
                                            getNum = ids.getInt(1);
                                            getTime = ids.getString(2);
                                            getTransx = ids.getFloat(3);
                                            getTransy = ids.getFloat(4);
                                            getTransz = ids.getFloat(5);
                                            getRotx =ids.getFloat(6);
                                            getRoty = ids.getFloat(7);
                                            getRotz = ids.getFloat(8);

                                            myOutWriter.append(id+","+getTime + "," + getTransx + "," + getTransy + "," + getTransz + "," + getRotx + "," + getRoty + "," + getRotz);
                                            myOutWriter.append("\n");
                                            Toast.makeText(getBaseContext(),"written",Toast.LENGTH_SHORT).show();

                                        }

                                        while (ids.moveToNext());
                                    }
                                    ids.close();
                                    myOutWriter.close();
                                    fOut.close();
                                }
                            }
                        }

                    }catch (SQLiteException se) {
                        Log.e(getClass().getSimpleName(), "Could not create or Open the database");
                    }



                }while (c.moveToNext());
            }
            c.close();
        }
        Toast.makeText(getBaseContext(),"finished",Toast.LENGTH_SHORT).show();
        onDestroy();
    }

    /*
    public void exportData(View v) throws IOException {
        dataSource.open();
        Cursor c= dataSource.selectIds();
        File myFile;
        int max;

        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    getid=c.getString(0);

                    maxNum= dataSource.selectMaxNum(getid);
                    if(maxNum.moveToFirst()) max= maxNum.getInt(0);
                    else max=0;
                    Toast.makeText(getBaseContext(),"id:::"+getid +" "+ max+" times",Toast.LENGTH_SHORT).show();



                        try {

                            final File folder = new File(Environment.getExternalStorageDirectory() + "/" + getid);

                            boolean success = true;
                            if (!folder.exists()) {
                                success = folder.mkdir();

                            }
                            if (success) {

                                for (int i = 0; i < max; i++) {

                                    myFile = new File(Environment.getExternalStorageDirectory() + "/" + getid + "/" + i + ".csv");
                                    // MediaScannerConnection.scanFile(LoggedActivity.this, new String[]{"/export_file/Export_" + TimeStampDB + ".csv"}, null, null);
                                    myFile.createNewFile();
                                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                    intent.setData(Uri.fromFile(myFile));
                                    sendBroadcast(intent);
                                    FileOutputStream fOut = new FileOutputStream(myFile);
                                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                                    myOutWriter.append("Time,TransX,TransY,TransZ,RotX,RotY,RotZ");
                                    myOutWriter.append("\n");
                                    ids = dataSource.selectByIdNum(getid, i);

                                    if (ids != null) {
                                        if (ids.moveToFirst()) {
                                            do {
                                                id = ids.getString(0);
                                                getNum = ids.getInt(1);
                                                getTime = ids.getString(2);
                                                getTransx = ids.getFloat(3);
                                                getTransy = ids.getFloat(4);
                                                getTransz = ids.getFloat(5);
                                                getRotx =ids.getFloat(6);
                                                getRoty = ids.getFloat(7);
                                                getRotz = ids.getFloat(8);

                                                myOutWriter.append(getTime + "," + getTransx + "," + getTransy + "," + getTransz + "," + getRotx + "," + getRoty + "," + getRotz);
                                                myOutWriter.append("\n");
                                                Toast.makeText(getBaseContext(),"written",Toast.LENGTH_SHORT).show();

                                            }

                                            while (ids.moveToNext());
                                        }
                                        ids.close();
                                        myOutWriter.close();
                                        fOut.close();
                                    }
                                }
                            }

                        }catch (SQLiteException se) {
                            Log.e(getClass().getSimpleName(), "Could not create or Open the database");
                        }


                }
                while (c.moveToNext());


            }
            c.close();
        }

        onDestroy();
    }
*/

    @Override
    protected void onResume() {

        dataSource.open();

        super.onResume();
    }

    @Override
    protected void onPause() {
        dataSource.close();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        dataSource.close();
        super.onDestroy();
    }

}