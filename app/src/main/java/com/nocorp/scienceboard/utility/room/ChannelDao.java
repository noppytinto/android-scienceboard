package com.nocorp.scienceboard.utility.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nocorp.scienceboard.utility.rss.model.Channel;

import java.util.List;

@Dao
public interface ChannelDao {

    @Query("SELECT * FROM channel")
    List<Channel> selectAll();

    @Query("SELECT * FROM channel WHERE name = :givenValue")
    List<Channel> selectByName(String givenValue);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(Channel... channels);

    @Delete
    void delete(Channel channel);

}// end ChannelDao
