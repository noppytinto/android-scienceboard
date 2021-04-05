package com.nocorp.scienceboard.model;

import com.nocorp.scienceboard.utility.rss.model.Entry;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Source implements Serializable{
    private String name;
    private String websiteUrl;
    private String rssUrl;
    private String logoUrl;
    private List<String> categories;
    private List<Entry> entries;
    private List<Article> articles;
    private Date lastUpdate;
    private String language;
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

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
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
