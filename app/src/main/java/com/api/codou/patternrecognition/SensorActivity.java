package com.api.codou.patternrecognition;

import java.io.FileOutputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.view.View;
import android.content.Intent;

import java.io.IOException;
import java.io.BufferedWriter;

import android.widget.TextView;
import android.widget.Toast;

import java.lang.*;


public class SensorActivity extends Activity {

    private static final String TAG = "Accelerometer Graph";   // TAG for Logging and Debugging

    private float[] mCurrents = new float[3];  //array for storing current accelerometer readings
    private ConcurrentLinkedQueue<float[]> mHistory = new ConcurrentLinkedQueue<float[]>();
    //private TextView[] mAccValueViews = new TextView[3];
    private boolean[] mGraphs = { true, true, true,};
    private int[] mAxisColors = new int[3];

    private int mBGColor;
    private int mZeroLineColor;
    private int mStringColor;

    private GraphView mGraphView;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    private int mSensorDelay = SensorManager.SENSOR_DELAY_UI;
    private int mMaxHistorySize;
    private boolean mDrawLoop = true;
    private int mDrawDelay = 100;
    private int mLineWidth = 2;
    private int mGraphScale = 6;
    private int mZeroLineY = 230;
    private int mZeroLineYOffset = 0;
    public static final float EPSILON = 0.000000001f;
    BufferedWriter out;
    //private FileWriter writer;
    TextView  tvx, tvy, tvz,tvrot;	// declare TextView objects
    private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp;
    Calendar rightNow = Calendar.getInstance();
    public DataHandler dataSource;
    float netForce = 0;
    float omegaMagnitude=0;
    String userText;
    int id;

    public void onClickButton(View v){
        startActivity(new Intent(SensorActivity.this, LoggedActivity.class));
        SensorActivity.this.finish();
    }


    private SensorEventListener mSensorEventListener = new SensorEventListener() {

        		/*	SensorEventListener must implement two superclass methods:
        		 * 	onAccuracyChanged and on SensorChanged
        		 */

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.i(TAG, "mSensorEventListener.onAccuracyChanged()");
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            Log.i(TAG, "mSensorEventListener.onSensorChanged()");
            tvx = (TextView) findViewById(R.id.x_text);        // TextView for displaying x accelerations
            tvy = (TextView) findViewById(R.id.y_text);        // TextView for displaying y accelerations
            tvz = (TextView) findViewById(R.id.z_text);
            tvrot = (TextView) findViewById(R.id.gyroVector);
          //  long timeInMillis = (new Date()).getTime()  + (event.timestamp - System.nanoTime()) / 1000000L;
           // String time = Long.toString(timeInMillis);



            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                for (int axis = 0; axis < 3; axis++) {
                    float value = event.values[axis];
                    // put the current accelerometer data into an array
                    mCurrents[axis] = value;

                }

                tvx.setText(" " + "x=" + event.values[0]);
                tvy.setText(" " + "y=" + event.values[1]);
                tvz.setText(" " + "z=" + event.values[2]);

                //Total acceleration will be sqrt(x^2+y^2+z^2)
                netForce=event.values[0]*event.values[0];    //X axis
                netForce+=event.values[1]*event.values[1];    //Y axis
                netForce+=(event.values[2])*(event.values[2]);    //Z axis (upwards)

                netForce = (float)(Math.sqrt(netForce) - SensorManager.GRAVITY_EARTH);    //Take the square root, minus gravity


                synchronized (this) {
                                /* 	Synchronized thread: if the size of the mHistory concurrent linked queue is greater
                                 *  than the specified max history size, then .poll() gets the head item
                                 *  in the queue and removes it. Otherwise, add a clone of the current value
                                 *  to the the queue
                                */
                    if (mHistory.size() >= mMaxHistorySize) {
                        mHistory.poll();
                    }
                    mHistory.add(mCurrents.clone());

                }
                //insert data on database
                dataSource.insertData(userText.toString(),netForce, omegaMagnitude);
                //  Toast.makeText(getBaseContext(),"inserted time="+time+ " trans="+netForce+" rot="+omegaMagnitude, Toast.LENGTH_SHORT).show();

            }
            else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                timestamp = event.timestamp;

                // This timestep's delta rotation to be multiplied by the current rotation
                // after computing it from the gyro sample data.
                if (timestamp != 0) {
                    final float dT = (event.timestamp - timestamp) * NS2S;
                    // Axis of the rotation sample, not normalized yet.
                    float axisX = event.values[0];
                    float axisY = event.values[1];
                    float axisZ = event.values[2];

                    // Calculate the angular speed of the sample
                    omegaMagnitude = (float)Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

                    // Normalize the rotation vector if it's big enough to get the axis
                    if (omegaMagnitude > EPSILON) {
                        axisX /= omegaMagnitude;
                        axisY /= omegaMagnitude;
                        axisZ /= omegaMagnitude;
                    }

                }

                tvrot.setText(" " + "rot=" + omegaMagnitude);
                // insert data on database
                dataSource.insertData(userText.toString(),netForce, omegaMagnitude);
                //  Toast.makeText(getBaseContext(),"inserted time="+time+ " trans="+netForce+" rot="+omegaMagnitude, Toast.LENGTH_SHORT).show();



            }



        }
    };

    private void startGraph() {
        // Register a sensor listener
        if (mAccelerometer != null) {
            mSensorManager.registerListener(mSensorEventListener, mAccelerometer, mSensorDelay);}
        if(mGyroscope!=null){
            mSensorManager.registerListener(mSensorEventListener, mGyroscope, mSensorDelay);
        }

        if (!mDrawLoop) {
            // resumes painting the graph
            mDrawLoop = true;
            mGraphView.surfaceCreated(mGraphView.getHolder());
        }
    }

    private void stopGraph() {
        // remove the sensor listener
        mSensorManager.unregisterListener(mSensorEventListener);

        // Stop drawing the graph
        mDrawLoop = false;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "MainActivity.onCreate()");

        userText= (String) getIntent().getSerializableExtra("id");
        // id = Integer.valueOf(userText);

        dataSource = new DataHandler(this);
        dataSource.open();
        Log.i(TAG,"Database opened in sensorActivity");



        Window window = getWindow();


        // Stop the screen from sleeping
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Hide the app name/title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Force to use Landscape mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Inflate the main layout
        setContentView(R.layout.activity_drawing);

        // get the frame layout for plotting the graphs
        FrameLayout frame = (FrameLayout) findViewById(R.id.frame);

        // set the colours for plots and text displays
        Resources resources = getResources();
        mStringColor = resources.getColor(R.color.string);
        mBGColor = resources.getColor(R.color.background);
        mZeroLineColor = resources.getColor(R.color.zero_line);
        mAxisColors[0] = resources.getColor(R.color.acc_x);
        mAxisColors[1] = resources.getColor(R.color.acc_y);
        mAxisColors[2] = resources.getColor(R.color.acc_z);

        // put the graphview into the framelayout
        mGraphView = new GraphView(this);
        frame.addView(mGraphView, 0);

    }


    @Override
    protected void onStart() {
        Log.i(TAG, "MainActivity.onStart()");

        // Initialisation
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManager
                .getSensorList(Sensor.TYPE_ACCELEROMETER);
        List<Sensor> sensorsGy = mSensorManager
                .getSensorList(Sensor.TYPE_GYROSCOPE);
        if (sensors.size() > 0) {
            mAccelerometer = sensors.get(0);
        } else {
            Log.e(TAG, "No Accelerometer Present");
        }
        if (sensorsGy.size() > 0) {
            mGyroscope = sensorsGy.get(0);
        } else {
            Log.e(TAG, "No Gyroscope Present");
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "MainActivity.onResume()");
        super.onResume();
        startGraph();


    }

    public void onClickStopButton(View v){
        onPause();
    }

    public void onClickReset(View v){
        //onResume();
        onRestart();

    }

    public void onClickStartButton(View v){
        onResume();


    }
    public void onRestart() {
        super.onRestart();
        Intent intent=new Intent();
        intent.setClass(this, this.getClass());
        finish();
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "MainActivity.onPause()");
        super.onPause();
        stopGraph();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "MainActivity.onStop()");
        super.onStop();
        mSensorManager = null;
        mAccelerometer = null;
    }


    @Override
    protected void onDestroy() {
        Log.i(TAG, "MainActivity.onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    final public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private class GraphView extends SurfaceView implements
            SurfaceHolder.Callback, Runnable {

        private Thread mThread;
        private SurfaceHolder mHolder;

        public GraphView(Context context) {
            super(context);

            Log.i(TAG, "GraphView.GraphView()");

            mHolder = getHolder();
            mHolder.addCallback(this);

            setFocusable(true);
            requestFocus();
        }
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            Log.i(TAG, "GraphView.surfaceChanged()");
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i(TAG, "GraphView.surfaceCreated()");
            mDrawLoop = true;
            mThread = new Thread(this);
            mThread.start();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i(TAG, "GraphView.surfaceDestroyed()");
            mDrawLoop = false;
            boolean roop = true;
            while (roop) {
                try {
                    mThread.join();
                    roop = false;
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
            mThread = null;
        }
        @Override
        public void run() {
            Log.i(TAG, "GraphView.run()");
            int width = getWidth();
            mMaxHistorySize = (int) ((width - 20) / mLineWidth);
            Paint textPaint = new Paint();
            textPaint.setColor(mStringColor);
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(14);
            Paint zeroLinePaint = new Paint();
            zeroLinePaint.setColor(mZeroLineColor);
            zeroLinePaint.setAntiAlias(true);

            Paint[] linePaints = new Paint[3];
            for (int i = 0; i < 3; i++) {
                linePaints[i] = new Paint();
                linePaints[i].setColor(mAxisColors[i]);
                linePaints[i].setAntiAlias(true);
                linePaints[i].setStrokeWidth(2);
            }

            while (mDrawLoop) {
                Canvas canvas = mHolder.lockCanvas();

                if (canvas == null) {
                    break;
                }

                canvas.drawColor(mBGColor);

                float zeroLineY = mZeroLineY + mZeroLineYOffset;

                synchronized (mHolder) {
                    float twoLineY = zeroLineY - (20 * mGraphScale);
                    float oneLineY = zeroLineY - (10 * mGraphScale);
                    float minasOneLineY = zeroLineY + (10 * mGraphScale);
                    float minasTwoLineY = zeroLineY + (20 * mGraphScale);

                    canvas.drawText("2", 5, twoLineY + 5, zeroLinePaint);
                    canvas.drawLine(20, twoLineY, width, twoLineY,
                            zeroLinePaint);

                    canvas.drawText("1", 5, oneLineY + 5, zeroLinePaint);
                    canvas.drawLine(20, oneLineY, width, oneLineY,
                            zeroLinePaint);

                    canvas.drawText("0", 5, zeroLineY + 5, zeroLinePaint);
                    canvas.drawLine(20, zeroLineY, width, zeroLineY,
                            zeroLinePaint);

                    canvas.drawText("-1", 5, minasOneLineY + 5, zeroLinePaint);
                    canvas.drawLine(20, minasOneLineY, width, minasOneLineY,
                            zeroLinePaint);

                    canvas.drawText("-2", 5, minasTwoLineY + 5, zeroLinePaint);
                    canvas.drawLine(20, minasTwoLineY, width, minasTwoLineY,
                            zeroLinePaint);

                    if (mHistory.size() > 1) {
                        Iterator<float[]> iterator = mHistory.iterator();
                        float[] before = new float[3];
                        int x = width - mHistory.size() * mLineWidth;
                        int beforeX = x;
                        x += mLineWidth;

                        if (iterator.hasNext()) {
                            float[] history = iterator.next();
                            for (int axis = 0; axis < 3; axis++) {
                                before[axis] = zeroLineY
                                        - (history[axis] * mGraphScale);
                            }
                            while (iterator.hasNext()) {
                                history = iterator.next();
                                for (int axis = 0; axis < 3; axis++) {
                                    float startY = zeroLineY
                                            - (history[axis] * mGraphScale);
                                    float stopY = before[axis];
                                    if (mGraphs[axis]) {
                                        canvas.drawLine(x, startY, beforeX,
                                                stopY, linePaints[axis]);
                                    }
                                    before[axis] = startY;
                                }
                                beforeX = x;
                                x += mLineWidth;
                            }
                        }
                    }
                }

                mHolder.unlockCanvasAndPost(canvas);

                try {
                    Thread.sleep(mDrawDelay);
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }



}

