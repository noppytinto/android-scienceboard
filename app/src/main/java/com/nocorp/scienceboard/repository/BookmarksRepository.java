package com.nocorp.scienceboard.repository;

import android.content.Context;
import android.util.Log;

import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.BookmarkedArticle;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.room.BookmarkDao;
import com.nocorp.scienceboard.utility.room.ScienceBoardRoomDatabase;

import java.util.ArrayList;
import java.util.List;

public class BookmarksRepository {
    private final String TAG = this.getClass().getSimpleName();
    private BookmarkRepositoryListener listener;



    //------------------------------------------------------------ CONSTRUCTORS
    public BookmarksRepository(Context context) {

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
            List<BookmarkedArticle> temp = dao.selectAll();
            List<ListItem> result = null;

            if(temp!=null && temp.size()>0) {
                result = new ArrayList<>();
                for(Article article : temp) {
                    result.add((BookmarkedArticle) article);
                }
            }

            listener.onBookmarksFetchCompleted(result);
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "SCIENCE_BOARD - getHistoryFromRoom: cannot start thread " + e.getMessage());
        }
    }

    private BookmarkDao getBookmarkDao(Context context) {
        ScienceBoardRoomDatabase roomDatabase = ScienceBoardRoomDatabase.getInstance(context);
        return roomDatabase.getBookmarkDao();
    }



}// end BookmarksRepository
