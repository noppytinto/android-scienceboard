package com.nocorp.scienceboard.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.MyValues;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
public class Article extends ListItem implements Comparable<Article>, Serializable {
    @PrimaryKey
    @NonNull
    private String id;
    private String title;
    @ColumnInfo(name = "thumbnail_url")
    private String thumbnailUrl;
    @ColumnInfo(name = "webpage_url")
    private String webpageUrl;
    @ColumnInfo(name = "pub_date")
    private Long pubDate;
    @ColumnInfo(name = "source_id")
    private String sourceId;
    @ColumnInfo(name = "source_website_url")
    private String sourceWebsiteUrl;
    @ColumnInfo(name = "source_real_name")
    private String sourceRealName;
    private List<String> keywords;
    private boolean visited;
    private boolean bookmarked;



    public Article() {
        setItemType(MyValues.ItemType.ARTICLE);
    }


//    Date publishDate;
//    String overview;
//    String fullText;
//    Source source;
//    List<String> imagesUrl;
//    boolean read;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getWebpageUrl() {
        return webpageUrl;
    }

    public void setWebpageUrl(String webpageUrl) {
        this.webpageUrl = webpageUrl;
    }

//    public SyndEntry getSyndEntry() {
//        return syndEntry;
//    }
//
//    public void setSyndEntry(SyndEntry syndEntry) {
//        this.syndEntry = syndEntry;
//    }

    public Long getPubDate() {
        return pubDate;
    }

    public void setPubDate(Long pubDate) {
        this.pubDate = pubDate;
    }

    @Override
    public int compareTo(Article another) {
        if(another==null) return 0;
        if(another.pubDate==null) return 0;
        if(this.pubDate==null) return 0;

        if(this.pubDate > another.pubDate)
            return -1;
        else if(this.pubDate < another.pubDate)
            return 1;

        return 0;
    }

    public String getSourceRealName() {
        return sourceRealName;
    }

    public void setSourceRealName(String sourceRealName) {
        this.sourceRealName = sourceRealName;
    }

    public String getSourceWebsiteUrl() {
        return sourceWebsiteUrl;
    }

    public void setSourceWebsiteUrl(String sourceWebsiteUrl) {
        this.sourceWebsiteUrl = sourceWebsiteUrl;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }


}// end Article
