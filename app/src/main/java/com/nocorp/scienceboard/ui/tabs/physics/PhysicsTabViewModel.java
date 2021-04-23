package com.nocorp.scienceboard.ui.tabs.physics;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.nocorp.scienceboard.history.repository.HistoryRepository;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.history.model.HistoryArticle;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.rss.repository.ArticleRepository;
import com.nocorp.scienceboard.rss.repository.ArticlesRepositoryListener;
import com.nocorp.scienceboard.rss.repository.SourceRepository;
import com.nocorp.scienceboard.history.room.HistoryDao;
import com.nocorp.scienceboard.rss.room.ScienceBoardRoomDatabase;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.ui.viewholder.ListItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PhysicsTabViewModel extends AndroidViewModel implements ArticlesRepositoryListener {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<List<ListItem>> articlesList;
    private MutableLiveData<List<ListItem>> nextArticlesList;
    private ArticleRepository articleRepository;
    private final String PHYSICS_CATEGORY = "physics";
    private static List<Source> pickedSources;
    private static boolean taskIsRunning;
    private static boolean saveInHistoryTaskIsRunning;
    private static String lastVisitedArticleId;
    private static long oldVisitedDate;
    private SourceRepository sourceRepository;
    private static List<ListItem> cachedArticles;
    private static List<DocumentSnapshot> oldestArticlesSnapshots;
    private HistoryRepository historyRepository;



    //-------------------------------------------------------------------------------------------- CONSTRUCTORS

    public PhysicsTabViewModel(Application application) {
        super(application);
        articlesList = new MutableLiveData<>();
        nextArticlesList = new MutableLiveData<>();
        articleRepository = new ArticleRepository(this);
        sourceRepository = new SourceRepository();
        historyRepository = new HistoryRepository();
    }




    //-------------------------------------------------------------------------------------------- GETTERS/SETTERS

    public LiveData<List<ListItem>> getObservableArticlesList() {
        return articlesList;
    }

    public void setArticlesList(List<ListItem> articlesList) {
        this.articlesList.postValue(articlesList);
    }

    public LiveData<List<ListItem>> getObservableNextArticlesList() {
        return nextArticlesList;
    }

    public void setNextArticlesList(List<ListItem> articlesList) {
        this.nextArticlesList.postValue(articlesList);
    }



    //-------------------------------------------------------------------------------------------- METHODS

    //-------------------------------------------------------------- FETCH ARTICLES

    public void fetchArticles(List<Source> givenSources, int numArticlesForEachSource, boolean forced) {
        if(forced) {
            downloadArticles(givenSources, numArticlesForEachSource);
        }
        else {
            tryCachedArticles(givenSources, numArticlesForEachSource);
        }
    }

    private void downloadArticles(List<Source> givenSources, int numArticlesForEachSource) {
        if( ! taskIsRunning) {
            Runnable task = () -> {
                cachedArticles = new ArrayList<>();
                taskIsRunning = true;
                // pick sources for ALL tab, only once
                if(pickedSources == null || pickedSources.isEmpty()) {
                    pickedSources = sourceRepository.getAllSourcesOfThisCategory(givenSources, PHYSICS_CATEGORY);
                }
                articleRepository.getArticles(pickedSources, numArticlesForEachSource, getApplication());
            };

            ThreadManager threadManager = ThreadManager.getInstance();
            threadManager.runTask(task);
        }
    }

    private void tryCachedArticles(List<Source> givenSources, int numArticlesForEachSource) {
        if(cachedArticles == null) {
            downloadArticles(givenSources, numArticlesForEachSource);
        }
        else {
            setArticlesList(cachedArticles);
        }
    }

    @Override
    public void onArticlesFetchCompleted(List<ListItem> articles, List<DocumentSnapshot> oldestArticles) {
        taskIsRunning = false;

        oldestArticlesSnapshots = oldestArticles;

        // publish results
        cachedArticles = articles;
        setArticlesList(articles);
    }

    @Override
    public void onArticlesFetchFailed(String cause) {
        taskIsRunning = false;

        // TODO
    }



    //-------------------------------------------------------------- FETCH NEXT ARTICLES

    public void fetchNextArticles(int numArticlesForEachSource) {
        if(!taskIsRunning) {
            Runnable task = () -> {
//                sleepforNseconds(1);
                Log.d(TAG, "SCIENCE_BOARD - fetchNextArticles: fetching new articles");

                articleRepository.getNextArticles(oldestArticlesSnapshots, numArticlesForEachSource, getApplication());
            };

            ThreadManager threadManager = ThreadManager.getInstance();
            threadManager.runTask(task);
        }
    }

    @Override
    public void onNextArticlesFetchCompleted(List<ListItem> newArticles, List<DocumentSnapshot> oldestArticles) {
        taskIsRunning = false;

        oldestArticlesSnapshots = new ArrayList<>(oldestArticles);

        // publish results
        cachedArticles.addAll(newArticles);
        setNextArticlesList(cachedArticles);
    }

    @Override
    public void onNextArticlesFetchFailed(String cause) {
        taskIsRunning = false;
        setNextArticlesList(null);
        Log.d(TAG, "SCIENCE_BOARD - onNextArticlesFetchFailed: " + cause);
    }




    //-------------------------------------------------------------- HISTORY

    public void saveInHistory(@NotNull Article givenArticle) {
        historyRepository.saveInHistory(givenArticle, getApplication());
    }

    private void sleepforNseconds(long seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }






}// end PhysicsTabViewModel