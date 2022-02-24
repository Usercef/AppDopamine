package com.example.myapplication.room;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;



/**
 * this is the abstract class used to create room database
 * For more information please look at Room library referenece guide in android
 */


@Database(entities = {Sensor.class}, version = 1)
public abstract class Appdb extends RoomDatabase {
    public static final String DATABASE_NAME="unlockeentdb";

//initailizing again and again is not a good practise so making instance static
    private static Appdb INSTANCE=null;
    public static Appdb getInstance(Context context) {





        try {

            if (INSTANCE == null)
                INSTANCE = Room.databaseBuilder(context, Appdb.class, DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .build();


        }catch (Exception e){
            INSTANCE = Room.databaseBuilder(context, Appdb.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();

        }
        return INSTANCE;

    }

    public abstract SensorsDao sensorsDao();


}
