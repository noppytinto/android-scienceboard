package com.nocorp.scienceboard.ui.home;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.bookmarks.repository.BookmarksRepository;
import com.nocorp.scienceboard.bookmarks.repository.OnBookmarksCheckedListener;
import com.nocorp.scienceboard.history.repository.HistoryRepository;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.repository.GeneralRepository;
import com.nocorp.scienceboard.rss.repository.ArticleRepository;
import com.nocorp.scienceboard.rss.repository.ArticlesRepositoryListener;
import com.nocorp.scienceboard.rss.repository.SourceRepository;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.topics.repository.TopicRepository;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.MyUtilities;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends AndroidViewModel implements
        ArticlesRepositoryListener {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<List<ListItem>> articlesList;
    private MutableLiveData<List<ListItem>> nextArticlesList;
    private ArticleRepository articleRepository;
    private static List<Source> pickedSources;
    private static boolean taskIsRunning;
    private static boolean bookmarksChecksTaskIsRunning;
    private SourceRepository sourceRepository;
    private static List<ListItem> cachedArticles;
    private static long lastFetchDate;
    private static List<DocumentSnapshot> oldestArticlesSnapshots;
    private HistoryRepository historyRepository;
    private BookmarksRepository bookmarksRepository;
    private GeneralRepository generalRepository;
    private final int FETCH_INTERVAL = 15; // in minutes



    //-------------------------------------------------------------------------------------------- CONSTRUCTORS

    public HomeViewModel(Application application) {
        super(application);
        articlesList = new MutableLiveData<>();
        nextArticlesList = new MutableLiveData<>();
        articleRepository = new ArticleRepository(this);
        sourceRepository = new SourceRepository();
        historyRepository = new HistoryRepository();
        bookmarksRepository = new BookmarksRepository();
        generalRepository = new GeneralRepository();
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

    public List<Source> getPickedSources() {
        return pickedSources;
    }



    //-------------------------------------------------------------------------------------------- METHODS


    //-------------------------------------------------------------- FETCH ARTICLES

    public void fetchArticles(List<Source> givenSources,
                              int numArticlesForEachSource,
                              boolean forced,
                              long startingDateInMillis) {
        Log.d(TAG, "fetchArticles: called, forced:" + forced);

        // if the request is within 15 mins
        // then use cached sources from local variable or Room
        Log.d(TAG, "SCIENCE_BOARD - fetchArticles: lastArticlesFetchDate: " + lastFetchDate);
        if(MyUtilities.isWithin_minutes(FETCH_INTERVAL, lastFetchDate)) {
            Log.d(TAG, "SCIENCE_BOARD - fetchArticles: fetching from cache (within 15 mins)");
            tryCachedArticles(givenSources,
                    numArticlesForEachSource,
                    startingDateInMillis);
        }
        //
        else {
            if(forced) {
                Log.d(TAG, "SCIENCE_BOARD - fetchArticles: FORCED: fetching from remote");
                downloadArticlesFromFollowedTopics(
                        givenSources,
                        numArticlesForEachSource,
                        startingDateInMillis);
            }
            else {
                Log.d(TAG, "SCIENCE_BOARD - fetchArticles: NOT FORCED: fetching from cache");
                tryCachedArticles(givenSources,
                        numArticlesForEachSource,
                        startingDateInMillis);
            }
        }

    }

    private void downloadArticlesFromFollowedTopics(List<Source> givenSources,
            int numArticlesForEachSource,
            long startingDateinMillis) {

        if( ! taskIsRunning) {
            Runnable task = () -> {
                cachedArticles = new ArrayList<>();
                taskIsRunning = true;

                // pick sources for ALL tab, only once
                pickedSources = sourceRepository.getAsourceForEachFollowedCategory_randomly(givenSources, TopicRepository.getCachedAllTopics_enabled());
                articleRepository.fetchArticles(
                        pickedSources,
                        numArticlesForEachSource,
                        getApplication(),
                        startingDateinMillis);
            };

            ThreadManager threadManager = ThreadManager.getInstance();
            threadManager.runTask(task);
        }
    }

    private void tryCachedArticles(List<Source> givenSources, int numArticlesForEachSource, long startingDateinMillis) {
        if(cachedArticles == null) {
            downloadArticlesFromFollowedTopics(givenSources, numArticlesForEachSource, startingDateinMillis);
        }
        else {
            setArticlesList(cachedArticles);
        }
    }

    @Override
    public void onArticlesFetchCompleted(List<ListItem> articles, List<DocumentSnapshot> oldestArticles) {
        taskIsRunning = false;

        if(articles==null) {
            // TODO: null is returned only in case of errors
        }
        else {
            lastFetchDate = System.currentTimeMillis();
            oldestArticlesSnapshots = oldestArticles;

            // publish results
            cachedArticles = articles;
            historyAndBookmarksCheck(articles);
//            historyCheck(cachedArticles);
//            bookmarksCheck(cachedArticles);
            setArticlesList(articles);
        }
    }

    @Override
    public void onArticlesFetchFailed(String cause) {
        taskIsRunning = false;
//        lastFetchDate = System.currentTimeMillis();

        // TODO
    }





    //-------------------------------------------------------------- FETCH NEXT ARTICLES

    public void fetchNextArticles(int numArticlesForEachSource) {
        if(!taskIsRunning) {
            Runnable task = () -> {
//                sleepforNseconds(1);
                Log.d(TAG, "SCIENCE_BOARD - fetchNextArticles: fetching new articles");
                articleRepository.fetchNextArticles(oldestArticlesSnapshots, numArticlesForEachSource, getApplication());
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
        historyAndBookmarksCheck(newArticles);
        cachedArticles.addAll(newArticles);
        setNextArticlesList(cachedArticles);
    }

    @Override
    public void onNextArticlesFetchFailed(String cause) {
        taskIsRunning = false;
        setNextArticlesList(null);
        Log.e(TAG, "SCIENCE_BOARD - onNextArticlesFetchFailed: " + cause);
    }




    //-------------------------------------------------------------- HISTORY/BOOKMARKS

    private void historyCheck(List<ListItem> articles) {
        historyRepository.historyCheck(articles, getApplication());
    }

    private void bookmarksCheck(List<ListItem> articles) {
        bookmarksRepository.bookmarksCheck(articles, getApplication());
    }

    private void historyAndBookmarksCheck(List<ListItem> articles) {
        if(articles==null) return;
        generalRepository.historyAndBookmarksCheck_sync(articles, getApplication());
    }

    public void asyncBookmarksCheck(List<ListItem> articles, OnBookmarksCheckedListener listener) {
        if(articles == null || articles.isEmpty()) return;

        if(!bookmarksChecksTaskIsRunning) {

            Runnable task = () -> {
                bookmarksRepository.bookmarksCheck(articles, getApplication());
                bookmarksChecksTaskIsRunning = false;
                listener.onComplete();
            };

            ThreadManager threadManager = ThreadManager.getInstance();
            threadManager.runTask(task);
        }
    }

    public void saveInHistory(@NotNull Article givenArticle) {
        historyRepository.saveInHistory(givenArticle, getApplication());
    }

    public void addToBookmarks(Article givenArticle) {
        bookmarksRepository.addToBookmarks_async(givenArticle, getApplication());
    }

    public void removeFromBookmarks(Article givenArticle) {
        bookmarksRepository.removeFromBookmarks_async(givenArticle, getApplication());
    }

    private void sleepforNseconds(long seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}// end HomeViewModel