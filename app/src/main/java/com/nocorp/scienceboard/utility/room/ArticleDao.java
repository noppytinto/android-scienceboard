package com.nocorp.scienceboard.utility.room;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nocorp.scienceboard.model.Article;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface ArticleDao {
    @Query("SELECT * FROM article")
    List<Article> selectAll();

    @Query("SELECT * FROM article WHERE webpage_url = :givenValue")
    List<Article> selectByWebsiteUrl(String givenValue);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Article> articles);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Article article);

    @Delete
    void delete(Article article);
}
