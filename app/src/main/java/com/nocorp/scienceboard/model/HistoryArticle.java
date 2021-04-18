package com.nocorp.scienceboard.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import com.nocorp.scienceboard.utility.MyValues;


@Entity(primaryKeys = {"id", "visited_date"})
public
class HistoryArticle extends Article {
    @ColumnInfo(name = "visited_date")
    @NonNull
    private long visitedDate;


    public HistoryArticle() {
        setItemType(MyValues.ItemType.HISTORY_ARTICLE);
    }

    public HistoryArticle(Article article) {
        setItemType(MyValues.ItemType.HISTORY_ARTICLE);
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


    public long getVisitedDate() {
        return visitedDate;
    }

    public void setVisitedDate(long visitedDate) {
        this.visitedDate = visitedDate;
    }



}// end VisitedArticle
