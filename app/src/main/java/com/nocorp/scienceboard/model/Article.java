package com.nocorp.scienceboard.model;

public class Article {
    private String title;
    private String contentText;
    private String thumbnailUrl;


    public Article(String title, String contentText, String thumbnailUrl) {
        this.title = title;
        this.contentText = contentText;
        this.thumbnailUrl = thumbnailUrl;
    }

    public Article(String title, String thumbnailUrl) {
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

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
