package com.nocorp.scienceboard.dao.room;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nocorp.scienceboard.model.xml.Entry;

import java.util.List;

public interface EntryDao {
    @Query("SELECT * FROM entry")
    List<Entry> selectAll();

    @Query("SELECT * FROM entry WHERE webpage_url = :givenValue")
    List<Entry> selectByName(String givenValue);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(Entry... entries);

    @Delete
    void delete(Entry entry);

}// end EntryDao
