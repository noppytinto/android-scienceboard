package com.nocorp.scienceboard.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
public class Source implements Serializable{
    @PrimaryKey
    @NonNull
    private String name;
    @ColumnInfo(name = "website_url")
    private String websiteUrl;
    @ColumnInfo(name = "rss_url")
    private String rssUrl;
    @Ignore
    private String logoUrl;
    private List<String> categories;
    @Ignore
    private List<Article> articles;
    @ColumnInfo(name = "last_update")
    private Date lastUpdate;
    private String language;
    @ColumnInfo(name = "xml_code")
    private String xmlCode;


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

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getXmlCode() {
        return xmlCode;
    }

    public void setXmlCode(String xmlCode) {
        this.xmlCode = xmlCode;
    }

}// end Source
