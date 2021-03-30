package com.nocorp.scienceboard.model;

import com.rometools.rome.feed.synd.SyndEntry;

import java.io.Serializable;
import java.util.List;

public class Source implements Serializable{
    private String name;
    private String websiteUrl;
    private String rssUrl;
    private String logoUrl;
    private String rssType;
    private List<SyndEntry> entries;
    private List<Article> articles;


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

    public String getRssType() {
        return rssType;
    }

    public void setRssType(String rssType) {
        this.rssType = rssType;
    }

    public List<SyndEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<SyndEntry> entries) {
        this.entries = entries;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }
}
