package com.nocorp.scienceboard.utility.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nocorp.scienceboard.model.BookmarkedArticle;

import java.util.List;

@Dao
public interface BookmarkDao {
    @Query("SELECT * FROM bookmarkedarticle ORDER BY saved_date DESC")
    List<BookmarkedArticle> selectAll();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(BookmarkedArticle article);

    @Query("SELECT 1 FROM bookmarkedarticle WHERE identifier = :givenIdentifier")
    boolean checkDuplication(String givenIdentifier);
}
