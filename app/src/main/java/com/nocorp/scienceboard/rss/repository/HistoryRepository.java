package com.nocorp.scienceboard.rss.repository;

import android.content.Context;
import android.util.Log;

import com.nocorp.scienceboard.model.HistoryArticle;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.rss.room.HistoryDao;
import com.nocorp.scienceboard.rss.room.ScienceBoardRoomDatabase;

import java.util.ArrayList;
import java.util.List;


public class HistoryRepository{
    private final String TAG = this.getClass().getSimpleName();
    private HistoryRepositoryListener listener;




    //------------------------------------------------------------ CONSTRUCTORS

    public HistoryRepository(HistoryRepositoryListener listener) {
        this.listener = listener;
    }



    //------------------------------------------------------------ GETTERS/SETTERS



    //------------------------------------------------------------ METHODS

    public void fetchArticles(int limit, Context context) {
        getFromRoom(context);
    }

    private void getFromRoom(Context context) {
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
            Log.d(TAG, "SCIENCE_BOARD - getFromRoom: cannot start thread " + e.getMessage());
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
            Log.d(TAG, "SCIENCE_BOARD - nukeHistory: cannot start thread " + e.getMessage());
        }
    }

    private HistoryDao getHistoryDao(Context context) {
        ScienceBoardRoomDatabase roomDatabase = ScienceBoardRoomDatabase.getInstance(context);
        return roomDatabase.getHistoryDao();
    }



}// end HistoryRepository
