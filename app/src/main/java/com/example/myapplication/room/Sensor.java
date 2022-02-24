package com.example.myapplication.room;


import android.util.Log;

import androidx.room.Entity;
import androidx.room.Ignore;


@Entity(primaryKeys = {"sensorType", "startTime","activityType"})
public class Sensor {

    private final int sensorType;
    private final int activityType;
    private final long startTime;
    private final long endTime;


    public Sensor(int sensorType,int activityType, long startTime, long endTime) {
        this.sensorType = sensorType;
        this.activityType=activityType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Sensor(Sensor sensor) {
        this.sensorType = sensor.getSensorType();
        this.activityType=sensor.getActivityType();
        this.startTime = sensor.getStartTime();
        this.endTime = sensor.getEndTime();
    }

    public int getActivityType() {
        return activityType;
    }

    public int getSensorType() {
        return sensorType;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    @Ignore
    public void print(){
        Log.e("asmarsensor",activityType+"    "+(endTime-startTime)/1000.0);

    }


    public boolean match(Sensor sensor){
        return sensor.activityType==this.activityType;
    }
}
