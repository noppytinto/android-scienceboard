package com.nocorp.scienceboard.repository;

import android.content.Context;
import android.util.Log;

import com.nocorp.scienceboard.bookmarks.room.BookmarkDao;
import com.nocorp.scienceboard.history.room.HistoryDao;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.rss.room.ScienceBoardRoomDatabase;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.MyValues;

import java.util.List;

public class GeneralRepository {
    private final String TAG = this.getClass().getSimpleName();




    //------------------------------------------------------------ CONSTRUCTORS


    //------------------------------------------------------------ GETTERS/SETTERS






    //------------------------------------------------------------ METHODS

    public void historyAndBookmarksCheck_sync(List<ListItem> articles, Context context) {
        if(articles==null) return;
        Log.d(TAG, "historyAndBookmarksCheck_sync: " + articles);
        BookmarkDao bookmarkDao = getBookmarkDao(context);
        HistoryDao historyDao = getHistoryDao(context);

        for(ListItem article: articles) {
            if(article.getItemType() == MyValues.ItemType.ARTICLE) {
                Article current = (Article)article;
                if(bookmarkDao.isInBookmarks(current.getId())) {
                    Log.d(TAG, "bookmarksCheck: " + current.getTitle() + " is in bookmarks");
                    current.setBookmarked(true);
                }
                else {
                    Log.d(TAG, "bookmarksCheck: " + current.getTitle() + " is in bookmarks");
                    current.setBookmarked(false);
                }

                if(historyDao.isInHistory(current.getId())){
                    Log.d(TAG, "historyCheck: " + current.getTitle() + " is in hisotry");
                    current.setVisited(true);
                }
                else {
                    Log.d(TAG, "historyCheck: " + current.getTitle() + " is NOT in hisotry");
                    current.setVisited(false);
                }

            }
        }
    }

    private BookmarkDao getBookmarkDao(Context context) {
        ScienceBoardRoomDatabase roomDatabase = ScienceBoardRoomDatabase.getInstance(context);
        return roomDatabase.getBookmarkDao();
    }

    private HistoryDao getHistoryDao(Context context) {
        ScienceBoardRoomDatabase roomDatabase = ScienceBoardRoomDatabase.getInstance(context);
        return roomDatabase.getHistoryDao();
    }




}// end GeneralRepository
