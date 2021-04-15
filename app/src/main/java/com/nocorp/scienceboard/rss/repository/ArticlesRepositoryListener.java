package com.nocorp.scienceboard.rss.repository;

import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.ui.viewholder.ListItem;

import java.util.List;

public interface ArticlesRepositoryListener {
    public void onArticlesFetchCompleted(List<ListItem> articles);
    public void onArticlesFetchFailed(String cause);
    public void onAllArticlesFetchCompleted(List<Article> articles);
    public void onAllArticlesFetchFailed(String cause);
    public void onTechArticlesFetchCompleted(List<Article> articles);
    public void onTechArticlesFetchFailed(String cause);
}