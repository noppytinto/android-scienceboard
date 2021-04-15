package com.nocorp.scienceboard.rss.room;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.nocorp.scienceboard.model.VisitedArticle;

import java.util.Date;
import java.util.List;

@Dao
public interface HistoryDao {
    @Query("SELECT * FROM visitedarticle ORDER BY visited_date DESC")
    List<VisitedArticle> selectAll();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(VisitedArticle article);

    @Query("UPDATE VisitedArticle " +
            "SET visited_date = :newVisitedDate " +
            "WHERE id = :targetIdentifier AND visited_date = :oldVisitedDate")
    int update(long newVisitedDate, String targetIdentifier, long oldVisitedDate);

    @Query("DELETE FROM VisitedArticle")
    public void nukeTable();

}
