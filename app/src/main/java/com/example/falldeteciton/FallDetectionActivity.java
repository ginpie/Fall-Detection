/**
 * Fall detection feature - User interface of data demonstration and testing
 *
 * Authors:         Jinpei Chen
 *                  Yuzhao Li
 *
 * Created data:    02/09/2019
 * Last modified:   03/09/2019
 */
package com.example.falldeteciton;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.LinkedList;

public class FallDetectionActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mySensorManager;
    private Sensor myAccelerometer;
    private Sensor myGyroscope;

    private final static String TAG = FallDetectionActivity.class.getSimpleName();
    private TextView xAcc, yAcc, zAcc, tAcc;
    private TextView xGyro, yGyro, zGyro, tGyro;

    private LineChart myChart;
    private Thread thread;
    private boolean plotData = true;

    private boolean bp = true;

    private int MAX_ENTRY = 100;

    /* A LinkedList is used to store collected data, each piece of data is
     * an array of 2 elements: value and timestamp
     */
    private LinkedList<Float> data = new LinkedList<>();
    private LinkedList<Float> time = new LinkedList<>();
    private LinkedList<Float> dtime = new LinkedList<>();

    /* Call when the activity is first created */
    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_falldetection);

        // get an instance of the SensorManager
        Log.d(TAG, "onCreate: Initializing sensor service.");
        mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // get an instance of the Accelerometer and Gyroscope
        Log.d(TAG, "onCreate: Initializing accelerometer.");
        myAccelerometer = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Log.d(TAG, "onCreate: Initializing gyroscope.");
        myGyroscope = mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // register the sensor listener
        mySensorManager.registerListener(FallDetectionActivity.this, myAccelerometer, mySensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG, "onCreate: Accelerometer listener is registered.");
        mySensorManager.registerListener(FallDetectionActivity.this, myGyroscope, mySensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG, "onCreate: Gyroscope listener is registered.");

        // assign TextViews
        xAcc = (TextView) findViewById(R.id.textViewX);
        yAcc = (TextView) findViewById(R.id.textViewY);
        zAcc = (TextView) findViewById(R.id.textViewZ);
        tAcc = (TextView) findViewById(R.id.textViewAcc);
        xGyro = (TextView) findViewById(R.id.textViewGyroX);
        yGyro = (TextView) findViewById(R.id.textViewGyroY);
        zGyro = (TextView) findViewById(R.id.textViewGyroZ);
        tGyro = (TextView) findViewById(R.id.textViewGyro);

        // initialize LineChart
        myChart = (LineChart) findViewById(R.id.lineChart);
        myChart.getDescription().setEnabled(true);
        myChart.getDescription().setText("Real Time Accelerometer Data");
        myChart.getDescription().setTextSize(15f);

        myChart.setTouchEnabled(false);
        myChart.setDragEnabled(false);
        myChart.setScaleEnabled(false);
        myChart.setDrawGridBackground(false);
        myChart.setPinchZoom(false);
        myChart.setBackgroundColor(Color.LTGRAY);

        LineData lineData = new LineData();
        lineData.setValueTextColor(Color.WHITE);
        myChart.setData(lineData);

        // get the legend
        Legend l = myChart.getLegend();
        // modify the legend
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.BLACK);

        XAxis xl = myChart.getXAxis();
        xl.setTextColor(Color.BLACK);
        xl.setTextSize(15f);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);

        YAxis yl = myChart.getAxisLeft();
        yl.setTextColor(Color.BLACK);
        yl.setTextSize(20f);
        yl.setDrawGridLines(false);
        yl.setAxisMaximum(10f);
        yl.setAxisMinimum(-10f);
        yl.setDrawGridLines(true);

        YAxis yr = myChart.getAxisRight();
        yr.setEnabled(false);

        myChart.getAxisLeft().setDrawGridLines(false);
        myChart.getXAxis().setDrawGridLines(false);
        myChart.setDrawBorders(false);

        // Start to plot
        startPlot();
    }

    private void startPlot(){
        if (thread != null){
            thread.interrupt();
        }

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    plotData = true;
                    try {
                        Thread.sleep(10);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float acc = 0f;
        float gyro = 0f;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            Log.d(TAG, "onSensorChanged: ||| X:" + event.values[0] + ", Y:" + event.values[1] + ", Z:" + event.values[2]);
            DecimalFormat df = new DecimalFormat("#.#");
            float xA = Float.valueOf(df.format(event.values[0]));
            float yA = Float.valueOf(df.format(event.values[1]));
            float zA = Float.valueOf(df.format(event.values[2]));
            acc = Float.valueOf(df.format((float) Math.sqrt(xA*xA + yA*yA + zA*zA)));

            xAcc.setText("X: " + xA);
            yAcc.setText("Y: " + yA);
            zAcc.setText("Z: " + zA);
            tAcc.setText("Acc: " + acc);

            // record data
            float t = System.nanoTime()/1000000;
            float d = acc;
            if (time.size()>0)  {
                dtime.add(t - time.peekLast());
            }else{
                dtime.add(t);
            }
            data.add(d);
            time.add(t);
            if (data.size()>20){
                data.pop();
                time.pop();
                dtime.pop();
            }

//            Log.d(TAG, "onSensorChanged: value: "+ data.toString() + "time: " + time.toString());
            System.out.println("onSensorChanged: value: "+ data.toString() + "time: " + time.toString() + " dtime: "+dtime.toString());

            // free fall event
            if (acc<1f && bp) {
                bp = false;
                beep();
                Toast.makeText(FallDetectionActivity.this, R.string.freefall,Toast.LENGTH_SHORT).show();
                openDialog();
            }
            if (acc>3f && !bp) {
                bp = true;
            }
        }

        // collect Gyroscope data
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
//            Log.d(TAG, "onSensorChanged: ||| X:" + event.values[0] + ", Y:" + event.values[1] + ", Z:" + event.values[2]);
            DecimalFormat df = new DecimalFormat("#.#");
            float xG = Float.valueOf(df.format(event.values[0]));
            float yG = Float.valueOf(df.format(event.values[1]));
            float zG = Float.valueOf(df.format(event.values[2]));
            gyro = Float.valueOf(df.format((float) Math.sqrt(xG*xG + yG*yG + zG*zG)));

            xGyro.setText("X: " + xG);
            yGyro.setText("Y: " + yG);
            zGyro.setText("Z: " + zG);
            tGyro.setText("Gyro: " + gyro);
        }

        if (plotData){
            addEntry(event);
            plotData = false;
        }
    }

    public void openDialog() {
        FallDialog fd = new FallDialog();
        fd.show(getSupportFragmentManager(), "alert dialog");
    }

    private void addEntry(SensorEvent event){
        LineData lineData = myChart.getData();

        if (lineData != null){
            LineDataSet set = (LineDataSet) lineData.getDataSetByIndex(0);

            if (set == null){
                set = createSet();
                lineData.addDataSet(set);
            }

            // add entries of xValues to the data
            lineData.addEntry(new Entry(set.getEntryCount(), event.values[0]), 0);
            if (set.getEntryCount() == MAX_ENTRY){
                set.removeFirst();
                for (Entry entry : set.getValues() )
                    entry.setX(entry.getX() - 1);
            }

            // enable the chart know when its data has changed
            lineData.notifyDataChanged();
            myChart.notifyDataSetChanged();
            myChart.setMaxVisibleValueCount(1500);
            myChart.moveViewToX(lineData.getEntryCount());
        }
    }

    private LineDataSet createSet(){
        LineDataSet set = new LineDataSet(null, "dynamic data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(4f);
        set.setColor(Color.DKGRAY);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircleHole(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    @Override
    protected void onDestroy() {
        mySensorManager.unregisterListener(FallDetectionActivity.this);
        thread.interrupt();
        super.onDestroy();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mySensorManager.registerListener(this, myAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (thread != null){
            thread.interrupt();
        }
        mySensorManager.unregisterListener(this);
    }

    protected void beep(){
        ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen.startTone(ToneGenerator.TONE_CDMA_PIP,150);
    }
}
