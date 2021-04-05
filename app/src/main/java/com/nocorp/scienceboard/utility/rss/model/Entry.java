package com.nocorp.scienceboard.utility.rss.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class Entry implements Comparable<Entry> {
    @PrimaryKey
    @ColumnInfo(name = "webpage_url")
    @NonNull
    private String webpageUrl;
    private String title;
    private String description;
    private String content;
    @ColumnInfo(name = "thumbnail_url")
    private String thumbnailUrl;
    @ColumnInfo(name = "pub_date")
    private Date pubDate;
    @Ignore
    private Channel channel;
    @ColumnInfo(name = "source_name")
    private String sourceName;
    @ColumnInfo(name = "source_url")
    private String sourceUrl;

    public String getWebpageUrl() {
        return webpageUrl;
    }

    public void setWebpageUrl(String webpageUrl) {
        this.webpageUrl = webpageUrl;
    }

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

    public Date getPubDate() {
        return pubDate;
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public int compareTo(Entry another) {
        if(pubDate==null || another==null) return 0;

        if(this.pubDate.getTime() > another.pubDate.getTime())
            return -1;
        else if(this.pubDate.getTime() < another.pubDate.getTime())
            return 1;

        return 0;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }
}// end Entry
