package com.nocorp.scienceboard.topics.room;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.nocorp.scienceboard.topics.model.TopicWithSubtopics;

import java.util.List;

@Dao
public interface TopicWithSubtopicsDao {
    @Transaction
    @Query("SELECT * FROM Topic")
    public List<TopicWithSubtopics> getTopicWithSubtopics();

}
