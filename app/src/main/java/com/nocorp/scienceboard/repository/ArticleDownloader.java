package com.nocorp.scienceboard.repository;

import com.nocorp.scienceboard.model.Article;

import java.util.List;

public interface ArticleDownloader {
    public void onArticlesDownloaded(List<Article> articles);
    public void onArticlesDownloadFailed(String cause);

}
