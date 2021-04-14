package com.nocorp.scienceboard.rss.room;

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

    @Query("DELETE FROM bookmarkedarticle WHERE id = :givenIdentifier")
    void delete(String givenIdentifier);

    @Query("SELECT 1 FROM bookmarkedarticle WHERE id = :givenIdentifier")
    boolean checkDuplication(String givenIdentifier);
}
