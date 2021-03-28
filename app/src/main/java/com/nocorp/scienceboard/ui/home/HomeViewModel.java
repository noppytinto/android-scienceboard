package com.nocorp.scienceboard.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.repository.ArticleDownloader;
import com.nocorp.scienceboard.repository.SourceRepository;

import java.util.List;

public class HomeViewModel extends ViewModel implements ArticleDownloader {
    private MutableLiveData<List<Article>> articlesList;
    private SourceRepository sourceRepository;

    public HomeViewModel() {
        articlesList = new MutableLiveData<>();
        sourceRepository = SourceRepository.getInstance();
        sourceRepository.setArticlesListener(this);
    }

    public LiveData<List<Article>> getObservableArticlesList() {
        return articlesList;
    }

    public void fetchArticles(String rssUrl) {
        sourceRepository.getArticles(rssUrl, 20);
    }

    public void setArticlesList(List<Article> articlesList) {
        this.articlesList.postValue(articlesList);
    }

    @Override
    public void onArticlesDownloaded(List<Article> articles) {
        setArticlesList(articles);
    }

    @Override
    public void onArticlesDownloadFailed(String cause) {

    }
}