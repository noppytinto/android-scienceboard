package com.nocorp.scienceboard.dao.room;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nocorp.scienceboard.model.xml.Channel;

import java.util.List;

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
