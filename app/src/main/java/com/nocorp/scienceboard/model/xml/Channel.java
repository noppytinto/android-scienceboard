package com.nocorp.scienceboard.model.xml;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.List;

@Entity
public class Channel {
    @PrimaryKey
    private String name;
    @ColumnInfo(name = "website_url")
    private String websiteUrl;
    @ColumnInfo(name = "rss_url")
    private String rssUrl;
    @ColumnInfo(name = "logo_url")
    private String logoUrl;
    private String language;
    @ColumnInfo(name = "last_update")
    private Date lastUpdate;
    @ColumnInfo(name = "pub_date")
    private Date pubDate;
    @Ignore
    private List<Entry> entries;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getRssUrl() {
        return rssUrl;
    }

    public void setRssUrl(String rssUrl) {
        this.rssUrl = rssUrl;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Date getLastUpdate() {
//        if(lastUpdate!=null)
//            return lastUpdate;
//        else if(entries!=null && entries.size()>0) // consider last article pub date as last update
//            return entries.get(0).getPubDate();
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Date getPubDate() {
        return pubDate;
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }
}
