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
    private long savedDate;


    public BookmarkedArticle() {
        setItemType(MyValues.ItemType.BOOKMARKED_ARTICLE);
    }

    public BookmarkedArticle(Article article) {
        setItemType(MyValues.ItemType.BOOKMARKED_ARTICLE);
        setId(article.getId());
        setTitle(article.getTitle());
        setThumbnailUrl(article.getThumbnailUrl());
        setWebpageUrl(article.getWebpageUrl());
        setPubDate(article.getPubDate());
        setSourceRealName(article.getSourceRealName());
        setSourceWebsiteUrl(article.getSourceWebsiteUrl());
        setSourceId(article.getSourceId());

    }

    @NonNull
    public long getSavedDate() {
        return savedDate;
    }

    public void setSavedDate(@NonNull long savedDate) {
        this.savedDate = savedDate;
    }
}// end BookmarkedArticle
