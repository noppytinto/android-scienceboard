package com.nocorp.scienceboard.model;

import java.io.Serializable;

public class Source implements Serializable {
    String name;
    String websiteUrl;
    String rssUrl;
    String logoUrl;
    String rssType;

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
}
