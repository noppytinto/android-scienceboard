package com.nocorp.scienceboard.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.repository.ArticlesFetcher;
import com.nocorp.scienceboard.repository.SourceRepository;
import com.nocorp.scienceboard.ui.viewholder.ListItem;

import java.util.List;

public class HomeViewModel extends ViewModel implements ArticlesFetcher {
    private MutableLiveData<List<ListItem>> articlesList;
    private SourceRepository sourceRepository;

    public HomeViewModel() {
        articlesList = new MutableLiveData<>();
        sourceRepository = SourceRepository.getInstance();
        sourceRepository.setArticlesListener(this);


    }

    public LiveData<List<ListItem>> getObservableArticlesList() {
        return articlesList;
    }

    public void fetchArticles(String rssUrl) {
        sourceRepository.getArticles(rssUrl, 20);
    }

    public void setArticlesList(List<ListItem> articlesList) {
        this.articlesList.postValue(articlesList);
    }

    @Override
    public void onFetchCompleted(List<ListItem> articles) {
        setArticlesList(articles);
    }

    @Override
    public void onFetchFailed(String cause) {

    }
}