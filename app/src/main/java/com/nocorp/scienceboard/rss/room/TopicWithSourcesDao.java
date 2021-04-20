package com.nocorp.scienceboard.rss.room;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.nocorp.scienceboard.model.TopicWithSources;

import java.util.List;

@Dao
public interface TopicWithSourcesDao {
    @Transaction
    @Query("SELECT * FROM Topic")
    public List<TopicWithSources> getTopicWithSources();
}
