package com.nocorp.scienceboard.ui.tabs.allarticlestab;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.repository.ArticleRepository;
import com.nocorp.scienceboard.repository.ArticlesFetcher;
import com.nocorp.scienceboard.ui.viewholder.ListItem;

import java.util.List;

public class AllArticlesTabViewModel extends ViewModel implements ArticlesFetcher {
    private MutableLiveData<List<ListItem>> articlesList;
    private ArticleRepository articleRepository;



    public AllArticlesTabViewModel() {
        articlesList = new MutableLiveData<>();
        articleRepository = ArticleRepository.getInstance();
        articleRepository.setArticlesListener(this);
    }

    public LiveData<List<ListItem>> getObservableArticlesList() {
        return articlesList;
    }

    public void fetchArticles(List<Source> sources) {
        articleRepository.getArticles_dom(sources, 20);
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


}// end AllArticlesTabViewModel