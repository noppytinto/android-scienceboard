package com.nocorp.scienceboard.dao.room;


import android.content.Context;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.nocorp.scienceboard.model.xml.Channel;
import com.nocorp.scienceboard.model.xml.Entry;

@Database(entities = {Channel.class, Entry.class}, version = 1, exportSchema = false)
public abstract class ScienceBoardRoomDatabase extends RoomDatabase {
    private static volatile ScienceBoardRoomDatabase singletonInstance;
    private static final String DB_NAME = "scienceboard-room-db";



    //--------------------------------- CONSTRUCTORS
    public ScienceBoardRoomDatabase() {}

    //--------------------------------- GETTERS/SETTERS

    public abstract ChannelDao getChannelDao();
    public abstract EntryDao getEntryDao();

    public static synchronized ScienceBoardRoomDatabase getInstance(Context context) {
        if (singletonInstance == null) {
            singletonInstance = create(context);
        }
        return singletonInstance;
    }


    //--------------------------------- METHODS

    private static ScienceBoardRoomDatabase create(final Context context) {
        return Room.databaseBuilder(context, ScienceBoardRoomDatabase.class, DB_NAME)
                .build();
    }

}// end ScienceBoardRoomDatabase
