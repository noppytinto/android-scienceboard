package com.nocorp.scienceboard.model.xml;

import com.nocorp.scienceboard.model.Source;

import java.util.Date;

public class Entry implements Comparable<Entry> {
    private String webpageUrl;
    private String title;
    private String description;
    private String content;
    private String thumbnailUrl;
    private Date pubDate;
    private Source source;

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

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }
}// end Entry
