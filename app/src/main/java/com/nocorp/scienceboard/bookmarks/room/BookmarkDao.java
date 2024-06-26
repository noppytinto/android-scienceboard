package com.nocorp.scienceboard.bookmarks.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nocorp.scienceboard.bookmarks.model.BookmarkArticle;

import java.util.List;

@Dao
public interface BookmarkDao {
    @Query("SELECT * FROM BookmarkArticle ORDER BY saved_date DESC")
    List<BookmarkArticle> selectAll();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(BookmarkArticle article);

    @Query("DELETE FROM BookmarkArticle WHERE id = :givenIdentifier")
    void delete(String givenIdentifier);

    @Query("SELECT 1 FROM BookmarkArticle WHERE id = :givenIdentifier")
    boolean checkDuplication(String givenIdentifier);

    @Query("SELECT EXISTS(SELECT * FROM bookmarkarticle WHERE id = :articleId)")
    public boolean isInBookmarks(String articleId);
}
