package com.nocorp.scienceboard.rss.repository;

import com.google.firebase.firestore.DocumentSnapshot;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.ui.viewholder.ListItem;

import java.util.List;

public interface ArticlesRepositoryListener {
    public void onArticlesFetchCompleted(List<ListItem> articles, List<DocumentSnapshot> oldestArticles);
    public void onArticlesFetchFailed(String cause);
    public void onNextArticlesFetchCompleted(List<ListItem> articles, List<DocumentSnapshot> oldestArticles);
    public void onNextArticlesFetchFailed(String cause);
}
