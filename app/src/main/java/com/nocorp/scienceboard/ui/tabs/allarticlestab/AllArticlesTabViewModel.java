package com.nocorp.scienceboard.ui.tabs.allarticlestab;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.repository.ArticleRepository;
import com.nocorp.scienceboard.repository.SourceRepository;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.rss.DomRssParser;

import java.util.Arrays;
import java.util.List;

public class AllArticlesTabViewModel extends AndroidViewModel {
    private MutableLiveData<List<ListItem>> articlesList;
    private ArticleRepository articleRepository;
    private final List<String> mainCategories = Arrays.asList("space", "physics", "tech", "medicine", "biology");
    private static List<Source> targetSources;
    private static boolean taskIsRunning;



    //------------------------------------------------------------ CONSTRUCTORS

    public AllArticlesTabViewModel(Application application) {
        super(application);
        articlesList = new MutableLiveData<>();
        articleRepository = new ArticleRepository(new DomRssParser());
    }



    //------------------------------------------------------------ GETTERS/SETTERS

    public LiveData<List<ListItem>> getObservableArticlesList() {
        return articlesList;
    }

    public void setArticlesList(List<ListItem> articlesList) {
        this.articlesList.postValue(articlesList);
    }



    //------------------------------------------------------------ METHODS

    public void downloadArticles(List<Source> givenSources, int limit, boolean forced) {
        Runnable task = () -> {
            if( ! taskIsRunning) {
                taskIsRunning = true;
                // pick sources for ALL tab, only once
                if(targetSources==null || targetSources.size()<=0) {
                    targetSources = SourceRepository.getAsourceForEachMainCategory_randomly(givenSources, mainCategories);// TODO this should not be static
                }
                List<ListItem> articles = articleRepository.getArticles(targetSources, limit, forced, getApplication());

                taskIsRunning = false;

                // publish results
                setArticlesList(articles);
            }
        };

        ThreadManager threadManager = ThreadManager.getInstance();
        threadManager.runTask(task);
    }
}// end AllArticlesTabViewModel