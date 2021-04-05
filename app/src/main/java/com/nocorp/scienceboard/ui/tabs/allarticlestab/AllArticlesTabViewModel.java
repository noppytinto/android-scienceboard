package com.nocorp.scienceboard.ui.tabs.allarticlestab;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.repository.ArticleRepository;
import com.nocorp.scienceboard.repository.ArticlesFetcher;
import com.nocorp.scienceboard.repository.SourceRepository;
import com.nocorp.scienceboard.ui.viewholder.ListItem;

import java.util.Arrays;
import java.util.List;

public class AllArticlesTabViewModel extends ViewModel implements ArticlesFetcher {
    private MutableLiveData<List<ListItem>> articlesList;
    private ArticleRepository articleRepository;
    private final List<String> mainCategories = Arrays.asList("space", "physics", "tech", "medicine", "biology");
    private SourceRepository sourceRepository;
    private static boolean randomMainCategoriesLoaded;
    private static List<Source> mySources;


    public AllArticlesTabViewModel() {
        articlesList = new MutableLiveData<>();
        articleRepository = ArticleRepository.getInstance();
        articleRepository.setArticlesListener(this);
        sourceRepository = new SourceRepository();
    }

    public LiveData<List<ListItem>> getObservableArticlesList() {
        return articlesList;
    }

    public void downloadArticles(List<Source> givenSources) {
        articleRepository.getArticles_dom(givenSources, 20);
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

    public void loadSources() {

    }




}// end AllArticlesTabViewModel