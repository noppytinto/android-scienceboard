package com.nocorp.scienceboard.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.nocorp.scienceboard.utility.MyValues;

import java.util.Date;

@Entity
public class BookmarkedArticle extends Article {
    @ColumnInfo(name = "saved_date")
    @NonNull
    private Date savedDate;


    public BookmarkedArticle() {
        setItemType(MyValues.ItemType.BOOKMARKED_ARTICLE);
    }

    public BookmarkedArticle(Article article) {
        setItemType(MyValues.ItemType.BOOKMARKED_ARTICLE);
        setIdentifier(article.getIdentifier());
        setTitle(article.getTitle());
        setDescription(article.getDescription());
        setContent(article.getContent());
        setThumbnailUrl(article.getThumbnailUrl());
        setWebpageUrl(article.getWebpageUrl());
        setPubDate(article.getPubDate());
        setSourceName(article.getSourceName());
        setSourceUrl(article.getSourceUrl());
    }

    @NonNull
    public Date getSavedDate() {
        return savedDate;
    }

    public void setSavedDate(@NonNull Date savedDate) {
        this.savedDate = savedDate;
    }
}// end BookmarkedArticle
