package com.nocorp.scienceboard.utility.room;

import androidx.room.TypeConverter;

import java.util.Date;

public class DateConverter {

    @TypeConverter
    public Date toDate(long millis) {
        return new Date(millis);
    }

    @TypeConverter
    public long fromDate(Date date) {
        return date == null ? 0 : date.getTime();
    }
}
