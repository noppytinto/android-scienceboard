package com.nocorp.scienceboard.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;


import com.nocorp.scienceboard.utility.MyValues;

import java.util.Date;

@Entity(primaryKeys = {"id", "visited_date"})
public class VisitedArticle extends Article {
    @ColumnInfo(name = "visited_date")
    @NonNull
    private Date visitedDate;


    public VisitedArticle() {
        setItemType(MyValues.ItemType.VISITED_ARTICLE);
    }

    public VisitedArticle(Article article) {
        setItemType(MyValues.ItemType.VISITED_ARTICLE);
        setId(article.getId());
        setTitle(article.getTitle());
        setThumbnailUrl(article.getThumbnailUrl());
        setWebpageUrl(article.getWebpageUrl());
        setPubDate(article.getPubDate());
        setSourceRealName(article.getSourceRealName());
        setSourceWebsiteUrl(article.getSourceWebsiteUrl());
        setSourceId(article.getSourceId());
    }


    public Date getVisitedDate() {
        return visitedDate;
    }

    public void setVisitedDate(Date visitedDate) {
        this.visitedDate = visitedDate;
    }



}// end VisitedArticle
