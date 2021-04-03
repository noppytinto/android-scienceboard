package com.nocorp.scienceboard.utility.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nocorp.scienceboard.utility.rss.model.Entry;

import java.util.List;

@Dao
public interface EntryDao {
    @Query("SELECT * FROM entry")
    List<Entry> selectAll();

    @Query("SELECT * FROM entry WHERE webpage_url = :givenValue")
    List<Entry> selectByName(String givenValue);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(Entry... entries);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Entry entry);


    @Delete
    void delete(Entry entry);

}// end EntryDao
