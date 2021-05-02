package com.nocorp.scienceboard.topics.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nocorp.scienceboard.topics.model.Topic;

import java.util.List;

@Dao
public interface TopicDao {
    @Query("SELECT * FROM topic WHERE enabled = 1")
    List<Topic> selectAll();

    @Query("SELECT * FROM topic WHERE followed = 1  AND enabled = 1")
    List<Topic> selectFollowedTopics();

    @Query("SELECT * FROM topic WHERE id = :givenValue AND enabled = 1")
    List<Topic> selectById(String givenValue);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Topic> topics);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void updateAll(List<Topic> topics);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Topic topic);

    @Delete
    void delete(Topic topic);

    @Query("UPDATE Topic " +
            "SET followed = :value " +
            "WHERE id = :topicName")
    int updateFollowStatus(boolean value, String topicName);

    @Query("UPDATE Topic " +
            "SET enabled = :value " +
            "WHERE id = :topicName")
    int updateEnabledStatus(boolean value, String topicName);
}
