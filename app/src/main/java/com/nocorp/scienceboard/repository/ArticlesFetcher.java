package com.nocorp.scienceboard.repository;

import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.ui.viewholder.ListItem;

import java.util.List;

public interface ArticlesFetcher {
    public void onFetchCompleted(List<ListItem> articles);
    public void onFetchFailed(String cause);

}
