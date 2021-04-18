package com.nocorp.scienceboard.rss.room;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nocorp.scienceboard.model.HistoryArticle;

import java.util.List;

@Dao
public interface HistoryDao {
    @Query("SELECT * FROM HistoryArticle ORDER BY visited_date DESC")
    List<HistoryArticle> selectAll();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(HistoryArticle article);

    @Query("UPDATE HistoryArticle " +
            "SET visited_date = :newVisitedDate " +
            "WHERE id = :targetIdentifier AND visited_date = :oldVisitedDate")
    int update(long newVisitedDate, String targetIdentifier, long oldVisitedDate);

    @Query("DELETE FROM HistoryArticle")
    public void nukeTable();

}
