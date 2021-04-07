package com.nocorp.scienceboard.utility.room;


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
            "SET visited_date = :lastVisitedDate " +
            "WHERE identifier = :targetIdentifier AND visited_date = :oldVisitedDate")
    int update(Date lastVisitedDate, String targetIdentifier, Date oldVisitedDate);

}
