package com.nocorp.scienceboard.utility.room;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nocorp.scienceboard.model.Source;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface SourceDao {
    @Query("SELECT * FROM source")
    List<Source> selectAll();

    @Query("SELECT * FROM source WHERE name = :givenValue")
    List<Source> selectByName(String givenValue);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Source> sources);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Source source);

    @Delete
    void delete(Source source);
}
