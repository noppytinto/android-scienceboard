package com.nocorp.scienceboard.ui.tabs.allarticlestab;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.repository.ArticleRepository;
import com.nocorp.scienceboard.repository.ArticlesFetcher;
import com.nocorp.scienceboard.repository.SourceRepository;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.ui.viewholder.ListItem;

import java.util.Arrays;
import java.util.List;

public class AllArticlesTabViewModel extends ViewModel {
    private MutableLiveData<List<ListItem>> articlesList;
    private ArticleRepository articleRepository;
    private final List<String> mainCategories = Arrays.asList("space", "physics", "tech", "medicine", "biology");
    private static List<Source> targetSources;

    public AllArticlesTabViewModel() {
        articlesList = new MutableLiveData<>();
        articleRepository = ArticleRepository.getInstance();
    }

    public LiveData<List<ListItem>> getObservableArticlesList() {
        return articlesList;
    }

    public void downloadArticles(List<Source> givenSources, int limit, boolean forced) {
        Runnable task = () -> {
            // pick sources for ALL tab, obly once
            if(targetSources==null || targetSources.size()<=0) {
                targetSources = SourceRepository.getAsourceForEachMainCategory_randomly(givenSources, mainCategories);
            }
            List<ListItem> articles = articleRepository.getArticles(targetSources, limit, forced);

            // publish results
            setArticlesList(articles);
        };

        ThreadManager threadManager = ThreadManager.getInstance();
        threadManager.runTask(task);
    }

    public void setArticlesList(List<ListItem> articlesList) {
        this.articlesList.postValue(articlesList);
    }


}// end AllArticlesTabViewModel