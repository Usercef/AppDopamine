package com.example.myapplication.room;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SensorsDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Sensor> sensors);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Sensor sensor);

    //getting all session time by end time because some session starts but not end in timeframe
    @Query("SELECT * FROM Sensor WHERE endTime>:time ORDER BY endTime ASC")
    List<Sensor> getSessions(long time);

    @Query("Delete FROM Sensor")
    void deleteAllSensors();


}
