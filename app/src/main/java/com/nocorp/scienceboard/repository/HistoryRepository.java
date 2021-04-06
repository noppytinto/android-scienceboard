package com.nocorp.scienceboard.repository;

import android.content.Context;
import android.util.Log;

import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.VisitedArticle;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.room.HistoryDao;
import com.nocorp.scienceboard.utility.room.ScienceBoardRoomDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class HistoryRepository{
    private final String TAG = this.getClass().getSimpleName();
    private HistoryRepositoryListener listener;




    //------------------------------------------------------------ CONSTRUCTORS

    public HistoryRepository(Context context) {

    }

    public HistoryRepository(HistoryRepositoryListener listener) {
        this.listener = listener;
    }



    //------------------------------------------------------------ GETTERS/SETTERS



    //------------------------------------------------------------ METHODS

    public void fetchArticles(int limit, Context context) {
        getHistoryFromRoom(context);
    }

    private void getHistoryFromRoom(Context context) {
        HistoryDao dao = getHistoryDao(context);

        Runnable task = () -> {
            List<VisitedArticle> temp = dao.selectAll();
            List<ListItem> result = null;

            if(temp!=null && temp.size()>0) {
                result = new ArrayList<>();
                for(Article article : temp) {
                    result.add((VisitedArticle) article);
                }
            }

            listener.onHistoryFetchCompleted(result);
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "SCIENCE_BOARD - getHistoryFromRoom: cannot start thread " + e.getMessage());
        }
    }

    private HistoryDao getHistoryDao(Context context) {
        ScienceBoardRoomDatabase roomDatabase = ScienceBoardRoomDatabase.getInstance(context);
        return roomDatabase.getHistoryDao();
    }


}// end HistoryRepository
