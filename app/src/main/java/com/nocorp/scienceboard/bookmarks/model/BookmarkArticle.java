package com.nocorp.scienceboard.bookmarks.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.utility.MyValues;

@Entity
public class BookmarkArticle extends Article {
    @ColumnInfo(name = "saved_date")
    @NonNull
    private long savedDate;


    public BookmarkArticle() {
        setItemType(MyValues.ItemType.BOOKMARK_ARTICLE);
    }

    public BookmarkArticle(Article article) {
        setItemType(MyValues.ItemType.BOOKMARK_ARTICLE);
        setId(article.getId());
        setTitle(article.getTitle());
        setThumbnailUrl(article.getThumbnailUrl());
        setWebpageUrl(article.getWebpageUrl());
        setPubDate(article.getPubDate());
        setSourceRealName(article.getSourceRealName());
        setSourceWebsiteUrl(article.getSourceWebsiteUrl());
        setSourceId(article.getSourceId());
        setKeywords(article.getKeywords());

    }

    @NonNull
    public long getSavedDate() {
        return savedDate;
    }

    public void setSavedDate(@NonNull long savedDate) {
        this.savedDate = savedDate;
    }
}// end BookmarkedArticle
