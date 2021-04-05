package com.nocorp.scienceboard.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.MyValues;

import java.util.Date;

@Entity
public class Article extends ListItem implements Comparable<Article> {
    private String title;
    private String description;
    private String content;
    @ColumnInfo(name = "thumbnail_url")
    private String thumbnailUrl;
    @PrimaryKey
    @ColumnInfo(name = "webpage_url")
    @NonNull
    private String webpageUrl;
//    private SyndEntry syndEntry;
    @Ignore
    private Source source;
    @ColumnInfo(name = "pub_date")
    private Date pubDate;
    @ColumnInfo(name = "source_name")
    private String sourceName;
    @ColumnInfo(name = "source_url")
    private String sourceUrl;

    public Article() {
        super(MyValues.ItemType.ARTICLE);
    }

    @Ignore
    public Article(String title, String content, String thumbnailUrl) {
        this();
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
    }

    @Ignore
    public Article(String title, String thumbnailUrl) {
        this();
        this.title = title;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
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

    @Override
    public int compareTo(Article another) {
        if(this.pubDate.getTime() > another.pubDate.getTime())
            return -1;
        else if(this.pubDate.getTime() < another.pubDate.getTime())
            return 1;

        return 0;
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
}// end Article
