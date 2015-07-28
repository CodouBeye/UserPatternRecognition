package com.api.codou.patternrecognition;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by codou on 20/05/15.
 */
public class LoggedActivity extends Activity {
    TextView tv;
    String getid;
    float getTransx,getTransy,getTransz, getRotx,getRoty,getRotz;
    String id;
    int getNum;
    String getTime;


    private DataHandler dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logged);
        dataSource = new DataHandler(this);
        getid= (String) getIntent().getSerializableExtra("id");

      /*  dataSource.open();
        Cursor cursor = dataSource.returnData();
        tv = (TextView) findViewById(R.id.bdd);

        if (cursor.moveToFirst()) {
            do {
                getid = cursor.getInt(0);
                getTime = cursor.getString(1);
                getTrans = cursor.getFloat(2);
                getRot = cursor.getFloat(3);

            } while (cursor.moveToNext());
        }

        Toast.makeText(getBaseContext(), "id=" + getid + " time=" + getTime + " trans=" + getTrans + " rot=" + getRot, Toast.LENGTH_LONG).show();
        dataSource.close();
*/
        try {
            exportTheDB();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void onClickButton(View v) {
        startActivity(new Intent(LoggedActivity.this, DrawingActivity.class));
        LoggedActivity.this.finish();
    }

    public void onRetryButton(View v) {
        Bundle obj = new Bundle();
        obj.putInt("num",getNum+1);
        obj.putInt("first",1);
        String valStr = getid ;
        Intent intent = new Intent(LoggedActivity.this, SensorActivity.class);
        //Intent newIntent=new Intent(DrawingActivity.this,LoggedActivity.class);
        intent.putExtra("id", valStr);
        intent.putExtras(obj);
        // newIntent.putExtra("id",valStr);
        startActivity(intent);
        LoggedActivity.this.finish();
    }

    public void onCloseButton(View v){
        finish();
        System.exit(0);
    }


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

    private void exportTheDB() throws IOException {
        File myFile;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
        String TimeStampDB = sdf.format(cal.getTime());

        try {

            final File folder = new File(Environment.getExternalStorageDirectory() + "/DATAS");
            //MediaScannerConnection.scanFile(LoggedActivity.this, new String[]{folder.getAbsolutePath()},null,null);


            //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
          /*  final MediaScannerConnection conn = new MediaScannerConnection(LoggedActivity.this ,
                    new MediaScannerConnection.MediaScannerConnectionClient() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            if (path.equals(folder.getAbsolutePath())) {
                                Log.i("Scan Status", "Completed");
                                Log.i("uri: ", uri.toString());

                                conn.disconnect();
                            }
                        }
                        @Override
                        public void onMediaScannerConnected() {
                            // TODO Auto-generated method stub
                            conn.scanFile( folder .getAbsolutePath(), null);

                        }
                    });
            conn.connect();*/
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdir();

            }
            if (success) {


                myFile = new File(Environment.getExternalStorageDirectory() + "/DATAS/"+getid+ ".csv");
                // MediaScannerConnection.scanFile(LoggedActivity.this, new String[]{"/export_file/Export_" + TimeStampDB + ".csv"}, null, null);
                myFile.createNewFile();
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(myFile));
                sendBroadcast(intent);
                FileOutputStream fOut = new FileOutputStream(myFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append("ID,Num,Time,TransX,TransY,TransZ,RotX,RotY,RotZ");
                myOutWriter.append("\n");
                /*myOutWriter.append("ID,Translation,Rotation");
                myOutWriter.append("\n");*/
                dataSource.open();
                Cursor c = dataSource.select(getid);




                if (c != null) {
                    if (c.moveToFirst()) {
                        do {

                           // Toast.makeText(getBaseContext(),"id="+getid+" trouve="+c.getString(0),Toast.LENGTH_SHORT).show();
                            id = c.getString(0);
                            getNum= c.getInt(1);
                            getTime = c.getString(2);
                            getTransx = c.getFloat(3);
                            getTransy = c.getFloat(4);
                            getTransz = c.getFloat(5);
                            getRotx = c.getFloat(6);
                            getRoty = c.getFloat(7);
                            getRotz = c.getFloat(8);

                            myOutWriter.append(getid +  ","+ getNum+ "," +getTime+","+ getTransx + "," + getTransy+ "," +getTransz+"," + getRotx +","+ getRoty +","+ getRotz);
                            myOutWriter.append("\n");

                        }

                        while (c.moveToNext());
                    }

                    c.close();
                    myOutWriter.close();
                    fOut.close();
                    Toast.makeText(getBaseContext(), "file finished", Toast.LENGTH_LONG).show();

                }
            }
        } catch (SQLiteException se) {
            Log.e(getClass().getSimpleName(), "Could not create or Open the database");
        } finally {

            dataSource.close();

        }

    }

}