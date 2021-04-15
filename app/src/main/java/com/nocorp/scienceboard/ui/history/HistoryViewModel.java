package com.nocorp.scienceboard.ui.history;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nocorp.scienceboard.rss.repository.HistoryRepository;
import com.nocorp.scienceboard.rss.repository.HistoryRepositoryListener;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.ui.viewholder.ListItem;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel implements HistoryRepositoryListener {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<List<ListItem>> articlesList;
    private HistoryRepository historyRepository;
    private static boolean taskIsRunning;


    //------------------------------------------------------------ CONSTRUCTORS

    public HistoryViewModel(Application application) {
        super(application);
        articlesList = new MutableLiveData<>();
        historyRepository = new HistoryRepository(this);
    }



    //------------------------------------------------------------ GETTERS/SETTERS

    public LiveData<List<ListItem>> getObservableArticlesList() {
        return articlesList;
    }

    public void setArticlesList(List<ListItem> articlesList) {
        this.articlesList.postValue(articlesList);
    }


    //------------------------------------------------------------ METHODS

    public void fetchHistory(int limit) {
        // TODO: implement limit?
        Runnable task = () -> {
            taskIsRunning = true;
            // pick sources for ALL tab, only once
            historyRepository.fetchArticles(limit, getApplication());
        };

        if( ! taskIsRunning) {
            ThreadManager threadManager = ThreadManager.getInstance();
            threadManager.runTask(task);
        }

    }


    @Override
    public void onHistoryFetchCompleted(List<ListItem> articles) {
        taskIsRunning = false;
        if(articles != null && articles.size()>0) {
            setArticlesList(articles);
            Log.d(TAG, "SCIENCE_BOARD - onHistoryFetchCompleted: articles fetched from ROOM");
        }
        else {
            setArticlesList(null);
            Log.d(TAG, "SCIENCE_BOARD - onHistoryFetchCompleted: article list is empty");
        }
    }

    @Override
    public void onHistoryFetchFailed(String cause) {
        taskIsRunning = false;
        Log.d(TAG, "SCIENCE_BOARD - onHistoryFetchFailed: articles fetching failed" + cause);
    }

    @Override
    public void onHistoryNuked(boolean nuked) {
        setArticlesList(null);
    }


}// end HistoryViewModel