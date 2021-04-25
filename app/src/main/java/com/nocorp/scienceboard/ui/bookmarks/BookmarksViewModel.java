package com.nocorp.scienceboard.ui.bookmarks;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nocorp.scienceboard.bookmarks.repository.BookmarksListOnChangedListener;
import com.nocorp.scienceboard.history.repository.HistoryRepository;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.bookmarks.repository.BookmarkRepositoryListener;
import com.nocorp.scienceboard.bookmarks.repository.BookmarksRepository;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.ui.viewholder.ListItem;

import java.util.List;

public class BookmarksViewModel extends AndroidViewModel implements BookmarkRepositoryListener {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<List<ListItem>> articlesList;
    private BookmarksRepository bookmarksRepository;
    private static boolean taskIsRunning;
    private HistoryRepository historyRepository;
    private BookmarksListOnChangedListener bookmarksListOnChangedListener;


    //------------------------------------------------------------ CONSTRUCTORS


    public BookmarksViewModel(Application application) {
        super(application);
        articlesList = new MutableLiveData<>();
        bookmarksRepository = new BookmarksRepository(this);
        historyRepository = new HistoryRepository();
    }



    //------------------------------------------------------------ GETTERS/SETTERS

    public LiveData<List<ListItem>> getObservableArticlesList() {
        return articlesList;
    }

    public void setArticlesList(List<ListItem> articlesList) {
        this.articlesList.postValue(articlesList);
    }

    public void setBookmarksListOnChangedListener(BookmarksListOnChangedListener bookmarksListOnChangedListener) {
        this.bookmarksListOnChangedListener = bookmarksListOnChangedListener;
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
            historyCheck(articles);
            setArticlesList(articles);
            Log.d(TAG, "SCIENCE_BOARD - onBookmarksFetchCompleted: articles fetched from ROOM");
        }
        else {
            setArticlesList(null);
            Log.e(TAG, "SCIENCE_BOARD - onBookmarksFetchCompleted: article list is empty");
        }
    }

    private void historyCheck(List<ListItem> articles) {
        historyRepository.historyCheck(articles, getApplication());
    }

    @Override
    public void onBookmarksFetchFailed(String cause) {
        taskIsRunning = false;
        Log.d(TAG, "SCIENCE_BOARD - onBookmarksFetchFailed: articles fetching failed" + cause);
    }

    @Override
    public void onBookmarksDuplicationCheckCompleted(boolean result) {
        // ignore
    }

    @Override
    public void onBookmarksDuplicationCheckFailed(String cause) {
        // ignore
    }


    public void removeArticlesFromBookmarks(List<Article> articlesToRemove) {
        bookmarksRepository.removeArticlesFromBookmark(articlesToRemove, getApplication(), bookmarksListOnChangedListener);
    }


}// end BookmarksViewModel