package com.nocorp.scienceboard.ui.webview;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.BookmarkedArticle;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.utility.room.BookmarkDao;
import com.nocorp.scienceboard.utility.room.ScienceBoardRoomDatabase;

import org.jetbrains.annotations.NotNull;


public class WebviewViewModel extends AndroidViewModel {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<Article> article;
    private static boolean checkingIsInBookmarksTaskIsRunning;
    private MutableLiveData<Boolean> addToBookmarksResponse;
    private MutableLiveData<Boolean> markAsVisitedResponse;
    private MutableLiveData<Boolean> bookmarkDuplicationResponse;
    private static boolean addingToBookmarksTaskIsRunning;




    //------------------------------------------------------------ CONSTRUCTORS

    public WebviewViewModel(@NonNull Application application) {
        super(application);
        article = new MutableLiveData<>();
        addToBookmarksResponse = new MutableLiveData<>();
        markAsVisitedResponse = new MutableLiveData<>();
        bookmarkDuplicationResponse = new MutableLiveData<>();

    }





    //------------------------------------------------------------ GETTERS/SETTERS

    public LiveData<Boolean> getObservableAddToBookmarksResponse() {
        return addToBookmarksResponse;
    }

    public void setAddToBookmarksResponse(boolean response) {
        this.addToBookmarksResponse.postValue(response);
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



    //------------------------------------------------------------ METHODS

    public void checkIsInBookmarks(Article givenArticle) {
        BookmarkDao dao = getBookmarkDao(getApplication());

        Runnable task = () -> {
            checkingIsInBookmarksTaskIsRunning = true;
            boolean result = dao.checkDuplication(givenArticle.getIdentifier());
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


    public void saveInBookmarks(@NotNull Article givenArticle) {
        BookmarkDao dao = getBookmarkDao(getApplication());

        Runnable task = () -> {
            // TODO null checks
            try {
                addingToBookmarksTaskIsRunning = true;
                long millis=System.currentTimeMillis();
                java.util.Date date=new java.util.Date(millis);

                BookmarkedArticle bookmarkedArticle = new BookmarkedArticle(givenArticle);
                bookmarkedArticle.setSavedDate(date);
                dao.insert(bookmarkedArticle);
                setAddToBookmarksResponse(true);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "saveInBookmarks: cannot insert in bookmarks " + e.getMessage());
                setAddToBookmarksResponse(false);
            }
            addingToBookmarksTaskIsRunning = false;
        };

        if( ! addingToBookmarksTaskIsRunning) {
            ThreadManager t = ThreadManager.getInstance();
            try {
                t.runTask(task);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "SCIENCE_BOARD - saveInBookmarks: cannot start thread " + e.getMessage());
            }
        }

    }

    private BookmarkDao getBookmarkDao(Context context) {
        ScienceBoardRoomDatabase roomDatabase = ScienceBoardRoomDatabase.getInstance(context);
        return roomDatabase.getBookmarkDao();
    }




}// end WebviewViewModel
