package com.nocorp.scienceboard.model;

import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.MyValues;
import com.rometools.rome.feed.synd.SyndEntry;

import java.util.Date;

public class Article extends ListItem {
    private String title;
    private String description;
    private String content;
    private String thumbnailUrl;
    private String webpageUrl;
    private SyndEntry syndEntry;
    private Source source;
    private Date publishDate;


    public Article() {
        super(MyValues.ItemType.ARTICLE);
    }


    public Article(String title, String content, String thumbnailUrl) {
        this();
        this.title = title;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
    }

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

    public SyndEntry getSyndEntry() {
        return syndEntry;
    }

    public void setSyndEntry(SyndEntry syndEntry) {
        this.syndEntry = syndEntry;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
