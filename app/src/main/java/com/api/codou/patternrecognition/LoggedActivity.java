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
    float getTrans, getRot;
    //String getTime;

    private DataHandler dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.database_view);
        dataSource = new DataHandler(this);
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

            final File folder = new File(Environment.getExternalStorageDirectory() + "/export_file");
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

                myFile = new File(Environment.getExternalStorageDirectory() + "/SensorData_"/* + TimeStampDB +*/ + ".csv");
                // MediaScannerConnection.scanFile(LoggedActivity.this, new String[]{"/export_file/Export_" + TimeStampDB + ".csv"}, null, null);
                myFile.createNewFile();
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(myFile));
                sendBroadcast(intent);
                FileOutputStream fOut = new FileOutputStream(myFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append("ID,Translation,Rotation");
                myOutWriter.append("\n");
                dataSource.open();

                Cursor c = dataSource.returnData();

                if (c != null) {
                    if (c.moveToFirst()) {
                        do {


                            getid = c.getString(0);
                            //getTime = c.getString(1);
                            getTrans = c.getFloat(2);
                            getRot = c.getFloat(3);

                            myOutWriter.append(getid +  "," + getTrans + "," + getRot);
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