package com.nocorp.scienceboard.rss.room;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.nocorp.scienceboard.model.Article;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface ArticleDao {
    @Query("SELECT * FROM article")
    List<Article> selectAll();

    @Query("SELECT * FROM article WHERE id = :givenValue")
    List<Article> selectById(String givenValue);

    @Query("SELECT * FROM article WHERE source_id = :givenSourceId")
    List<Article> selectBySourceId(String givenSourceId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Article> articles);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Article article);

    @Delete
    void delete(Article article);
}
