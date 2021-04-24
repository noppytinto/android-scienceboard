package com.nocorp.scienceboard.history.repository;

import android.content.Context;
import android.util.Log;

import com.nocorp.scienceboard.history.model.HistoryArticle;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.history.room.HistoryDao;
import com.nocorp.scienceboard.rss.room.ScienceBoardRoomDatabase;

import java.util.ArrayList;
import java.util.List;


public class HistoryRepository{
    private final String TAG = this.getClass().getSimpleName();
    private HistoryRepositoryListener listener;




    //------------------------------------------------------------ CONSTRUCTORS


    public HistoryRepository() {
    }

    public HistoryRepository(HistoryRepositoryListener listener) {
        this.listener = listener;
    }



    //------------------------------------------------------------ GETTERS/SETTERS



    //------------------------------------------------------------ METHODS

    public void saveInHistory(Article givenArticle, Context context) {
        if(givenArticle==null) return;

        Runnable task = () -> {
            try {
                HistoryDao dao = getHistoryDao(context);

                // checking
                HistoryArticle lastVisitedArticle = dao.getLastVisitedArticle();

                if(lastVisitedArticle==null) {
                    // this case should happens when there are no article in history
                    Log.e(TAG, "SCIENCE_BOARD - saveInHistory: the history is empty, saving new element");
                    storeArticle(givenArticle, dao);
                }
                else {
                    // inserting
                    if( ! articlesAreEquals(lastVisitedArticle, givenArticle)) {
                        storeArticle(givenArticle, dao);
                    }
                    else {
                        Log.e(TAG, "SCIENCE_BOARD - saveInHistory: cannot save in history, cause: " + "the last article matches the current one");
                    }
                }


            } catch (Exception e) {
                Log.e(TAG, "SCIENCE_BOARD - saveInHistory: cannot save in history, cause: " + e.getMessage());
            }
        };

        try {
            ThreadManager t = ThreadManager.getInstance();
            t.runTask(task);
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - saveInHistory: cannot start thread, cause: " + e.getMessage());
        }
    }

    private boolean articlesAreEquals(Article article_1, Article article_2) {
        String lastVisitedArticleId = article_1.getId();
        String givenArticleId = article_2.getId();
        return givenArticleId.equals(lastVisitedArticleId);
    }

    private void storeArticle(Article givenArticle, HistoryDao dao) {
        long millis=System.currentTimeMillis();
        HistoryArticle historyArticle = new HistoryArticle(givenArticle);
        historyArticle.setVisitedDate(millis);
        dao.insert(historyArticle);

        Log.d(TAG, "SCIENCE_BOARD - saveInHistory: articles saved in history: " + givenArticle.getTitle());
    }

    public void fetchLastVisitedArticle(Context context, OnHistoryItemFetchedListener listener) {
        getLastVisitedArticleFromRoom(context, listener);
    }

    private void getLastVisitedArticleFromRoom(Context context, OnHistoryItemFetchedListener listener) {
        Runnable task = () -> {
            HistoryDao dao = getHistoryDao(context);
            try {
                HistoryArticle result = dao.getLastVisitedArticle();
                listener.onComplete(result);
            } catch (Exception e) {
                Log.e(TAG, "SCIENCE_BOARD - getLastVisitedArticleFromRoom: cannot get last visited article" + e.getMessage());
                listener.onFailed(e.getMessage());
            }
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - getLastVisitedArticleFromRoom: cannot start thread " + e.getMessage());
        }
    }


    public void fetchArticles(int limit, Context context) {
        getArticlesFromRoom(context);
    }

    private void getArticlesFromRoom(Context context) {
        HistoryDao dao = getHistoryDao(context);

        Runnable task = () -> {
            List<HistoryArticle> temp = dao.selectAll();
            List<ListItem> result = null;

            if(temp!=null && temp.size()>0) {
                result = new ArrayList<>(temp);
            }

            listener.onHistoryFetchCompleted(result);
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "SCIENCE_BOARD - getFromRoom: cannot start thread " + e.getMessage());
        }
    }

    public void nukeHistory(Context context) {
        HistoryDao dao = getHistoryDao(context);

        Runnable task = () -> {
            dao.nukeTable();
            listener.onHistoryNuked(true);
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "SCIENCE_BOARD - nukeHistory: cannot start thread " + e.getMessage());
        }
    }

    private HistoryDao getHistoryDao(Context context) {
        ScienceBoardRoomDatabase roomDatabase = ScienceBoardRoomDatabase.getInstance(context);
        return roomDatabase.getHistoryDao();
    }


    public void historyCheck(List<ListItem> articles, Context context) {
        HistoryDao dao = getHistoryDao(context);
        for(ListItem article: articles) {
            if(dao.isInHistory(((Article)article).getId()))
                ((Article)article).setVisited(true);
            else
                ((Article)article).setVisited(false);
        }
    }
}// end HistoryRepository
