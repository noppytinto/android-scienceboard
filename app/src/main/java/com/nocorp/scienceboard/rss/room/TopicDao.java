package com.nocorp.scienceboard.rss.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.Topic;

import java.util.List;

@Dao
public interface TopicDao {
    @Query("SELECT * FROM topic")
    List<Topic> selectAll();

    @Query("SELECT * FROM topic WHERE name = :givenValue")
    List<Topic> selectById(String givenValue);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Topic> topics);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Topic topic);

    @Delete
    void delete(Topic topic);

    @Query("UPDATE Topic " +
            "SET followed = :value " +
            "WHERE name = :topicName")
    int updateFollowStatus(boolean value, String topicName);
}
