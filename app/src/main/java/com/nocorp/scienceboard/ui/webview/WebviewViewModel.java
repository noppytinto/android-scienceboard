package com.nocorp.scienceboard.ui.webview;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nocorp.scienceboard.bookmarks.repository.AddToBookmarksListener;
import com.nocorp.scienceboard.bookmarks.repository.BookmarksListOnChangedListener;
import com.nocorp.scienceboard.bookmarks.repository.BookmarksRepository;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.bookmarks.model.BookmarkArticle;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.bookmarks.room.BookmarkDao;
import com.nocorp.scienceboard.rss.room.ScienceBoardRoomDatabase;

import org.jetbrains.annotations.NotNull;


public class WebviewViewModel extends AndroidViewModel {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<Article> article;
    private static boolean checkingIsInBookmarksTaskIsRunning;
    private MutableLiveData<Boolean> addToBookmarksResponse;
    private MutableLiveData<Boolean> markAsVisitedResponse;
    private MutableLiveData<Boolean> bookmarkDuplicationResponse;
    private static boolean addingToBookmarksTaskIsRunning;
    private MutableLiveData<Boolean> removedFromBookmarksResponse;
    private static boolean removeFromBookmarksTaskIsRunning;
    private BookmarksRepository bookmarksRepository;
    private BookmarksListOnChangedListener bookmarksListOnChangedListener;



    //------------------------------------------------------------ CONSTRUCTORS

    public WebviewViewModel(@NonNull Application application) {
        super(application);
        article = new MutableLiveData<>();
        addToBookmarksResponse = new MutableLiveData<>();
        markAsVisitedResponse = new MutableLiveData<>();
        bookmarkDuplicationResponse = new MutableLiveData<>();
        removedFromBookmarksResponse = new MutableLiveData<>();
        bookmarksRepository = new BookmarksRepository();
    }





    //------------------------------------------------------------ GETTERS/SETTERS

    public LiveData<Boolean> getObservableAddToBookmarksResponse() {
        return addToBookmarksResponse;
    }

    public void setAddToBookmarksResponse(boolean response) {
        this.addToBookmarksResponse.postValue(response);
    }

    public LiveData<Boolean> getObservableRemovedFromBookmarksResponse() {
        return removedFromBookmarksResponse;
    }

    public void setRemovedFromBookmarksResponse(boolean response) {
        this.removedFromBookmarksResponse.postValue(response);
    }

    public LiveData<Boolean> getObservableMarkAsVisitedResponse() {
        return markAsVisitedResponse;
    }

    public void setMarkAsVisitedResponse(boolean response) {
        this.markAsVisitedResponse.postValue(response);
    }


    public LiveData<Boolean> getObservableBookmarkDuplicationResponse() {
        return bookmarkDuplicationResponse;
    }

    public void setBookmarkDuplicationResponse(boolean response) {
        this.bookmarkDuplicationResponse.postValue(response);
    }

    public BookmarksListOnChangedListener getBookmarksListOnChangedListener() {
        return bookmarksListOnChangedListener;
    }

    public void setBookmarksListOnChangedListener(BookmarksListOnChangedListener bookmarksListOnChangedListener) {
        this.bookmarksListOnChangedListener = bookmarksListOnChangedListener;
    }

    //------------------------------------------------------------ METHODS

    public void checkIsInBookmarks(Article givenArticle) {
        BookmarkDao dao = getBookmarkDao(getApplication());

        Runnable task = () -> {
            checkingIsInBookmarksTaskIsRunning = true;
            boolean result = dao.checkDuplication(givenArticle.getId());
            setBookmarkDuplicationResponse(result);
            checkingIsInBookmarksTaskIsRunning = false;
        };

        if( !checkingIsInBookmarksTaskIsRunning) {
            ThreadManager t = ThreadManager.getInstance();
            try {
                t.runTask(task);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "SCIENCE_BOARD - checkIsInBookmarks: cannot start thread " + e.getMessage());
            }
        }
    }

    public void addToBookmarks(@NotNull Article givenArticle) {
        Runnable task = () -> {
            // TODO null checks
            try {
                addingToBookmarksTaskIsRunning = true;
                bookmarksRepository.addToBookmarks_sync(givenArticle, getApplication(), () -> {
                    setAddToBookmarksResponse(true);
                    bookmarksListOnChangedListener.onBookmarksListChanged();
                });
            } catch (Exception e) {
                Log.e(TAG, "SCEINCE_BOARD - addToBookmarks: cannot insert in bookmarks " + e.getMessage());
                setAddToBookmarksResponse(false);
            }
            addingToBookmarksTaskIsRunning = false;
        };

        if( ! addingToBookmarksTaskIsRunning) {
            ThreadManager t = ThreadManager.getInstance();
            try {
                t.runTask(task);
            } catch (Exception e) {
                Log.e(TAG, "SCIENCE_BOARD - saveInBookmarks: cannot start thread " + e.getMessage());
            }
        }

    }

    public void removeFromBookmarks(@NotNull Article givenArticle) {
        Runnable task = () -> {
            // TODO null checks
            try {
                removeFromBookmarksTaskIsRunning = true;
                bookmarksRepository.removeFromBookmarks_sync(givenArticle, getApplication(), () -> {
                    setRemovedFromBookmarksResponse(true);
                    bookmarksListOnChangedListener.onBookmarksListChanged();
                });
            } catch (Exception e) {
                Log.e(TAG, "SCIENCE_BOARD - removeFromBookmarks: cannot remove from bookmarks, cause: " + e.getMessage());
                setRemovedFromBookmarksResponse(false);
            }
            removeFromBookmarksTaskIsRunning = false;
        };

        if( ! removeFromBookmarksTaskIsRunning) {
            ThreadManager t = ThreadManager.getInstance();
            try {
                t.runTask(task);
            } catch (Exception e) {
                Log.e(TAG, "SCIENCE_BOARD - removeFromBookmarks: cannot start thread, cause: " + e.getMessage());
                setRemovedFromBookmarksResponse(false);
            }
        }
    }


    private BookmarkDao getBookmarkDao(Context context) {
        ScienceBoardRoomDatabase roomDatabase = ScienceBoardRoomDatabase.getInstance(context);
        return roomDatabase.getBookmarkDao();
    }




}// end WebviewViewModel
