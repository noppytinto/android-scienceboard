package com.nocorp.scienceboard.ui.bookmarks;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nocorp.scienceboard.repository.BookmarkRepositoryListener;
import com.nocorp.scienceboard.repository.BookmarksRepository;
import com.nocorp.scienceboard.repository.HistoryRepository;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.ui.viewholder.ListItem;

import java.util.List;

public class BookmarksViewModel extends AndroidViewModel implements BookmarkRepositoryListener {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<List<ListItem>> articlesList;
    private BookmarksRepository bookmarksRepository;
    private static boolean taskIsRunning;


    //------------------------------------------------------------ CONSTRUCTORS


    public BookmarksViewModel(Application application) {
        super(application);
        articlesList = new MutableLiveData<>();
        bookmarksRepository = new BookmarksRepository(this);
    }



    //------------------------------------------------------------ GETTERS/SETTERS

    public LiveData<List<ListItem>> getObservableArticlesList() {
        return articlesList;
    }

    public void setArticlesList(List<ListItem> articlesList) {
        this.articlesList.postValue(articlesList);
    }


    //------------------------------------------------------------ METHODS

    public void fetchBookmarks(int limit) {
        // TODO: implement limit?
        Runnable task = () -> {
            taskIsRunning = true;
            // pick sources for ALL tab, only once
            bookmarksRepository.fetchArticles(limit, getApplication());
        };

        if( ! taskIsRunning) {
            ThreadManager threadManager = ThreadManager.getInstance();
            threadManager.runTask(task);
        }

    }


    @Override
    public void onBookmarksFetchCompleted(List<ListItem> articles) {
        taskIsRunning = false;
        if(articles != null && articles.size()>0) {
            setArticlesList(articles);
            Log.d(TAG, "SCIENCE_BOARD - onBookmarksFetchCompleted: articles fetched from ROOM");
        }
        else {
            setArticlesList(null);
            Log.d(TAG, "SCIENCE_BOARD - onBookmarksFetchCompleted: article list is empty");
        }
    }

    @Override
    public void onBookmarksFetchFailed(String cause) {
        taskIsRunning = false;
        Log.d(TAG, "SCIENCE_BOARD - onBookmarksFetchFailed: articles fetching failed" + cause);
    }




}// end BookmarksViewModel