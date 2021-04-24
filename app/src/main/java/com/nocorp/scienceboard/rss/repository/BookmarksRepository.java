package com.nocorp.scienceboard.rss.repository;

import android.content.Context;
import android.util.Log;

import com.nocorp.scienceboard.history.room.HistoryDao;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.BookmarkArticle;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.rss.room.BookmarkDao;
import com.nocorp.scienceboard.rss.room.ScienceBoardRoomDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BookmarksRepository {
    private final String TAG = this.getClass().getSimpleName();
    private BookmarkRepositoryListener listener;



    //------------------------------------------------------------ CONSTRUCTORS
    public BookmarksRepository() {

    }

    public BookmarksRepository(BookmarkRepositoryListener listener) {
        this.listener = listener;
    }



    //------------------------------------------------------------ GETTERS/SETTERS




    //------------------------------------------------------------ METHODS

    public void fetchArticles(int limit, Context context) {
        getFromRoom(context);
    }

    private void getFromRoom(Context context) {
        BookmarkDao dao = getBookmarkDao(context);

        Runnable task = () -> {
            List<BookmarkArticle> temp = dao.selectAll();
            List<ListItem> result = null;

            if(temp!=null && temp.size()>0) {
                result = new ArrayList<>();
                for(Article article : temp) {
                    result.add((BookmarkArticle) article);
                }
            }

            listener.onBookmarksFetchCompleted(result);
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - getHistoryFromRoom: cannot start thread " + e.getMessage());
        }
    }

    private BookmarkDao getBookmarkDao(Context context) {
        ScienceBoardRoomDatabase roomDatabase = ScienceBoardRoomDatabase.getInstance(context);
        return roomDatabase.getBookmarkDao();
    }

    public void bookmarksCheck(List<ListItem> articles, Context context) {
        BookmarkDao dao = getBookmarkDao(context);
        for(ListItem article: articles) {
            if(dao.isInBookmarks(((Article)article).getId()))
                ((Article)article).setBookmarked(true);
            else
                ((Article)article).setBookmarked(false);
        }
    }

    public void addToBookmarks(Article givenArticle, Context context) {
        Runnable task = () -> {
            // TODO null checks
            try {
                BookmarkDao dao = getBookmarkDao(context);
                long millis=System.currentTimeMillis();
                BookmarkArticle bookmarkArticle = new BookmarkArticle(givenArticle);
                bookmarkArticle.setSavedDate(millis);
                dao.insert(bookmarkArticle);
            } catch (Exception e) {
                Log.e(TAG, "SCEINCE_BOARD - addToBookmarks: cannot insert in bookmarks " + e.getMessage());
            }
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - saveInBookmarks: cannot start thread " + e.getMessage());
        }
    }

    public void removeFromBookmarks(Article article, Context context) {
        Runnable task = () -> {
            // TODO null checks
            try {
                BookmarkDao dao = getBookmarkDao(context);
                dao.delete(article.getId());
            } catch (Exception e) {
                Log.e(TAG, "SCEINCE_BOARD - removeFromBookmarks: cannot remove from bookmarks " + e.getMessage());
            }
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - removeFromBookmarks: cannot start thread " + e.getMessage());
        }
    }


}// end BookmarksRepository
