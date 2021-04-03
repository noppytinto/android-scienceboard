package com.nocorp.scienceboard.model.xml;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.nocorp.scienceboard.model.Source;

import java.util.Date;

@Entity
public class Entry implements Comparable<Entry> {
    @PrimaryKey
    @ColumnInfo(name = "webpage_url")
    private String webpageUrl;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "description")
    private String description;
    @ColumnInfo(name = "content")
    private String content;
    @ColumnInfo(name = "thumbnail_url")
    private String thumbnailUrl;
    @ColumnInfo(name = "pub_date")
    private Date pubDate;
    private Channel channel;

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
}// end Entry
