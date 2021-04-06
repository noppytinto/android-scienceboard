package com.nocorp.scienceboard.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.util.Date;

@Entity(primaryKeys = {"identifier", "saved_date"})
public class BookmarkedArticle extends Article {
    @ColumnInfo(name = "saved_date")
    @NonNull
    private Date savedDate;


    public BookmarkedArticle() {
    }

    @NonNull
    public Date getSavedDate() {
        return savedDate;
    }

    public void setSavedDate(@NonNull Date savedDate) {
        this.savedDate = savedDate;
    }
}// end BookmarkedArticle
