package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.room.Appdb;
import com.example.myapplication.room.Point;
import com.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private MOTION preMotion,currMotion;
    private long prevTimeStamp,currTimeStamp;


    private volatile  int noneThreeshould=1;
    private volatile int lowThreeshold=4;
    private volatile int normalThreedhold=8;
    public static final int GYRO=1;


    List<String[]> data = new ArrayList<String[]>();
    private TextView x;
    private TextView x2;

    private EditText none,low,normal;
    private SensorManager sensorManager;
    private Sensor sensor, sensorAcc;
    private ExecutorService executor;
    long timer = 0;
    long timer2 = 0;
    ArrayList<Point> lastSamples = new ArrayList<Point>();
    ArrayList<Point> lastAccSamples = new ArrayList<Point>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data.add(new String[]{"Accelerometer X","Accelerometer Y","Accelerometer Z"});

        AudioManager mobilemode = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
//   int streamMaxVolume = mobilemode.getStreamMaxVolume(AudioManager.STREAM_RING);
        switch (mobilemode.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                Log.i("MyApp","Silent mode");

                mobilemode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                //mobilemode.setStreamVolume (AudioManager.STREAM_MUSIC,mobilemode.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
                mobilemode.setStreamVolume(AudioManager.STREAM_RING,mobilemode.getStreamMaxVolume(AudioManager.STREAM_RING),0);

                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                Log.i("MyApp","Vibrate mode");
                mobilemode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                //mobilemode.setStreamVolume (AudioManager.STREAM_MUSIC,mobilemode.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
                mobilemode.setStreamVolume(AudioManager.STREAM_RING,mobilemode.getStreamMaxVolume(AudioManager.STREAM_RING),0);

                break;
            case AudioManager.RINGER_MODE_NORMAL:
                Log.i("MyApp","Normal mode");
                break;
        }



        setContentView(R.layout.activity_main);

        Button saveData = (Button) findViewById(R.id.saveButton);
        saveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveCSV();
            }
        });

        x = findViewById(R.id.textViewx);
        x2 = findViewById(R.id.textViewx2);

        none=findViewById(R.id.noneThreshold);
        low=findViewById(R.id.lowThreshold);
        normal=findViewById(R.id.mediumThredhold);
      //  high=findViewById(R.id.highThreeshold);


        executor= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        none.setText(noneThreeshould+"");
        low.setText(lowThreeshold+"");
        normal.setText(normalThreedhold+"");
       // high.setText(highThreeshold+"");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SensorEventListener sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                timer++;
                //x2.setText("" + (Math.abs(event.values[0]) + Math.abs(event.values[1]) + Math.abs(event.values[2])));
                //Log.e("value : ", "" + event.values[1]);
                //lastsample // list -> 50 values
                lastSamples.add(new Point(event.values[0],event.values[1],event.values[2]));
                if(timer % 25 ==0) {///...25..25...25...25
                    boolean value = determineShakness(lastSamples);
                    if (value)
                        x2.setText(x2.getText() + " SHAKY Motion");

                }
                processMotion(determineMotion(Math.abs(event.values[0])+
                        Math.abs(event.values[1])+
                        Math.abs(event.values[2])),event.timestamp);

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };


        sensorManager.registerListener(sensorEventListener,sensor,SensorManager.SENSOR_DELAY_NORMAL);

        SensorEventListener sensorAccEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                //Log.e("value : ", "" + event.values[1]);
                String[] values = {""+event.values[0],""+event.values[1],""+event.values[2]};
                data.add(values);
                lastAccSamples.add(new Point(event.values[0],event.values[1],event.values[2]));
                //x2.setText("" + event.values[2]);
                timer2++;
                if(timer2 % 30 == 0) {
                    boolean value2 = determinePickUp(lastAccSamples);
                    if (value2)
                        x2.setText(x2.getText() + " Pick Up Motion");

                }


            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };


        sensorManager.registerListener(sensorAccEventListener, sensorAcc, SensorManager.SENSOR_DELAY_NORMAL);


    }


    public void onclick(View view) {
        if(view.getId()==R.id.button)
        {
            printAllEvents();

            if(none.getText()!=null&&none.getText().length()>0)
                noneThreeshould=Integer.parseInt(none.getText().toString());

            if(low.getText()!=null&&low.getText().length()>0)
                lowThreeshold=Integer.parseInt(low.getText().toString());

            if(normal.getText()!=null&&normal.getText().length()>0)
                normalThreedhold=Integer.parseInt(normal.getText().toString());

//            if(high.getText()!=null&&high.getText().length()>0)
//                highThreeshold=Integer.parseInt(high.getText().toString());

            Toast.makeText(getApplicationContext(),"Changed",Toast.LENGTH_SHORT).show();
        }

    }

    private void printAllEvents(){
        new Thread(() -> {
          List<com.example.myapplication.room.Sensor> sensors= Appdb.getInstance(MainActivity.this)
                    .sensorsDao().getSessions(0);

          for(com.example.myapplication.room.Sensor sensor:sensors)
             sensor.print();


        }).start();
    }

    enum MOTION{
        NONE,
        LOW,
        NORMAL,
        HIGH
    }

    private boolean determinePickUp(ArrayList<Point> list) {
        int startDirection = 0;// -1 searching for min, 1 searching for max
        ArrayList<Point> minis = new ArrayList<Point>();
        ArrayList<Point> maxis = new ArrayList<Point>();
        float Score = 0;
        float globalMax = -10000, globalMin = 10000;
        int gMaxIndex = 0, gMinIndex = 0;
        if(list.size() >= 50) {
            while (list.size() > 50) {
                list.remove(0);
            }
            lastAccSamples = list;
            for(int i=0; i < list.size(); i++ ) {
                float val = list.get(i).getZ();
                if( val > globalMax) {
                    globalMax = val;
                    gMaxIndex = i;
                }
                if (val < globalMin) {
                    globalMin = val;
                    gMinIndex = i;
                }

            }
        }

        if(globalMin < -3 + 9.8 && globalMax > 4 + 9.8)
            if(gMinIndex > gMaxIndex) {
                return true;
        }

        return false;
    }
    private boolean determineShakness(ArrayList<Point> list) {
        int startDirection = 0;// -1 searching for min, 1 searching for max
        ArrayList<Point> minis = new ArrayList<Point>();
        ArrayList<Point> maxis = new ArrayList<Point>();
        float Score = 0;
        if(list.size() >= 50){
            while(list.size() > 50) {
                list.remove(0);
            }
            lastSamples = list;
            float first = list.get(0).getY();
            float second = list.get(1).getY();
            float maximum = -10000;
            float minimum = 10000;
            startDirection = (second - first > 0) ? 1 : -1;
            for(int i=0; i < list.size(); i++ ) {
                float val = list.get(i).getY();
                if(startDirection == 1) {
                    if(val > maximum) {
                        maximum = val;
                    }
                    else {
                        maxis.add(list.get(i-1));
                        startDirection *= -1;
                        maximum = -10000;
                        i-=1;
                    }
                } else {
                    if(val < minimum) {
                        minimum = val;
                    }
                    else {
                        minis.add(list.get(i-1));
                        startDirection *= -1;
                        minimum = 10000;
                        i-=1;
                    }
                }

            }
            int countMax = 0;
            int countMin = 0;
            for (Point p : maxis) {
                float val = p.getY();
                if(val > 0 && val >= 0.5 && val <= 2) {
                    countMax++;
                }
            }
            for (Point p : minis) {
                float val = p.getY();
                if(val < 0 && val <= -0.5 && val >= -2) {
                    countMin++;
                }
            }
            if((float)((float)countMax / maxis.size()) >= 0.4) {
                if((float)((float)countMin / minis.size()) >= 0.4) {
                    Score+= (float)((float)countMax / maxis.size()) * (float) countMax / 9;
                    Score+= (float)((float)countMin / minis.size()) * (float) countMax / 9;

                }
            }

        }

        ArrayList<Point> minisZ = new ArrayList<Point>();
        ArrayList<Point> maxisZ = new ArrayList<Point>();
        if(list.size() >= 50){
            while(list.size() > 50) {
                list.remove(0);
            }
            float first = list.get(0).getZ();
            float second = list.get(1).getZ();
            float maximum = -10000;
            float minimum = 10000;
            startDirection = (second - first > 0) ? 1 : -1;
            for(int i=0; i < list.size(); i++ ) {
                float val = list.get(i).getZ();
                if(startDirection == 1) {
                    if(val > maximum) {
                        maximum = val;
                    }
                    else {
                        maxisZ.add(list.get(i-1));
                        startDirection *= -1;
                        maximum = -10000;
                        i-=1;
                    }
                } else {
                    if(val < minimum) {
                        minimum = val;
                    }
                    else {
                        minisZ.add(list.get(i-1));
                        startDirection *= -1;
                        minimum = 10000;
                        i-=1;
                    }
                }

            }
            int countMax = 0;
            int countMin = 0;
            for (Point p : maxisZ) {
                float val = p.getZ();
                if(val >= 0 && val >= 0 && val <= 0.5) {
                    countMax++;
                }
            }
            for (Point p : minisZ) {
                float val = p.getZ();
                if(val <= 0 && val <= 0 && val >= -0.5) {
                    countMin++;
                }
            }
            if((float)((float)countMax / maxisZ.size()) >= 0.4) {
                if((float)((float)countMin / minisZ.size()) >= 0.4) {
                    Score+= (float)((float)countMax / maxisZ.size());
                    Score+= (float)((float)countMin / minisZ.size());
                }
            }

        }
        if(Score > 2.2)
            return true;
        return false;

    }
    private MOTION determineMotion(double magnitude){
        //Log.e("asmarmag",magnitude+"");
        if(magnitude<noneThreeshould)
            return MOTION.NONE;
        if(magnitude<lowThreeshold)
            return MOTION.LOW;
        if(magnitude<normalThreedhold)
            return MOTION.NORMAL;
        return MOTION.HIGH;
    }


    public class Test {

         long MinThreshold = 7 * 1000;
         long MaxThreshold = 12 * 1000;
        //sorted order
        //values are stopped when there is none
        public void processAndSaveInDb(List<SensorVal> magnitudeList){

            long first = magnitudeList.get(0).getTime();
            long last = magnitudeList.get(magnitudeList.size() - 1 ).getTime();
            long totalTime = last - first;
            if(totalTime < MinThreshold) {
                return;// there is no activity to consider below 7 seconds

            }
            if(totalTime < MaxThreshold) {
                double sum = 0;
                //we need to save the whole list as one activity
                for (int i = 0; i < magnitudeList.size(); i++) {
                    sum += Math.abs(magnitudeList.get(i).magnitude);
                }
                double value = sum / magnitudeList.size();
                //Check Value Activity******

                return;// no need to continue we got 1 activity
            }
            int size = (int)(totalTime / (1000 * 12));
            ActivityBlock[] results = new ActivityBlock[size];
            int i = 0;
            double sum = 0;
            long startAc = first;
            int count = 0;
            double value = 0;
            for (SensorVal sample : magnitudeList) {
                if(count == 0)
                    startAc = sample.getTime();
                sum += sample.magnitude;
                count++;
                if(sample.getTime() - startAc >= MaxThreshold || i == size - 1) { // if it is below the threshold or
                    // if it is the last Activity so we need to continue merging
                    value = sum / count;
                    int type = 0;// need to Check Value Activity ********
                    results[i] = new ActivityBlock(type, startAc, sample.getTime());
                    count = 0;
                    sum = 0;
                    i++;

                }

            }
            value = sum / count;
            int type = 0;// need to Check Value Activity ********
            results[i] = new ActivityBlock(type, startAc, last);
            //count = 0;
            //sum = 0;
            // now we have an array that has all the activities inside


            //save

        }





    }
    class ActivityBlock {
        int Type;
        long startTime;
        long endTime;

        public ActivityBlock(int type, long startTime, long endTime) {
            Type = type;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public int getType() {
            return Type;
        }

        public void setType(int type) {
            Type = type;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }
    }
    class SensorVal{
        double magnitude;
        long time;

        public void setMagnitude(double magnitude) {
            this.magnitude = magnitude;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public double getMagnitude() {
            return magnitude;
        }

        public long getTime() {
            return time;
        }
    }







    private void processMotion(MOTION motion,long timeStamp){
        fillView(motion);
        timeStamp=timeStamp/1000000;
        if(preMotion==null){
            preMotion=motion;
            prevTimeStamp=timeStamp;

        }else if(currMotion==null){
            currMotion=motion;
            currTimeStamp=timeStamp;

        }else if(preMotion.equals(currMotion)){
            prevTimeStamp=currTimeStamp;
            currMotion=motion;
            currTimeStamp=timeStamp;

            //removing jitter
        }else if(preMotion.equals(motion)&&timeStamp-prevTimeStamp<1000){
            currMotion=null;
            currTimeStamp=0;

        }else if(currMotion.equals(motion)){


        }else{
          saveInDb(new com.example.myapplication.room.Sensor(GYRO,
                  preMotion.ordinal(),prevTimeStamp,currTimeStamp-1
                  ));
          preMotion=currMotion;
          prevTimeStamp=currTimeStamp;
          currMotion=motion;
          currTimeStamp=timeStamp;
        }

    }

    private void fillView(MOTION motion){
        x.setText(motion.toString());
    }


    private void saveInDb(com.example.myapplication.room.Sensor sensor){
        if(sensor.getActivityType()==0)return;
        executor.execute(() -> putSensor(MainActivity.this,sensor));
    }

    private static synchronized void putSensor(@NonNull Context context, com.example.myapplication.room.Sensor sensor){
        Appdb.getInstance(context)
                .sensorsDao().insert(sensor);

    }

    public void SaveCSV() {
        String csv;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            csv= "/storage/emulated/0/Download" + "/MyCsvAccFile.csv";
        }
        else
        {
            csv= Environment.getExternalStorageDirectory().toString() + "/MyCsvAccFile.csv";
        }
        //csv = (Environment.getExternalStorageDirectory() + "/MyCsvAccFile.csv"); // Here csv file name is MyCsvAccFile.csv

        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(csv));



            writer.writeAll(data); // data is adding to csv

            writer.close();
            //callRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
