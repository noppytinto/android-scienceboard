package com.nocorp.scienceboard.ui.tabs.techtab;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.model.VisitedArticle;
import com.nocorp.scienceboard.repository.ArticleRepository;
import com.nocorp.scienceboard.repository.SourceRepository;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.room.HistoryDao;
import com.nocorp.scienceboard.utility.room.ScienceBoardRoomDatabase;
import com.nocorp.scienceboard.utility.rss.DomRssParser;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TechTabViewModel extends AndroidViewModel {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<List<ListItem>> articlesList;
    private ArticleRepository articleRepository;
    private final List<String> mainCategories = Arrays.asList("tech");
    private final String TECH_CATEGORY = "tech";
    private static List<Source> targetSources;
    private static boolean taskIsRunning;
    private static boolean saveInHistoryTaskIsRunning;
    private static String lastVisitedArticleId;
    private static Date oldVisitedDate;



    //------------------------------------------------------------ CONSTRUCTORS

    public TechTabViewModel(Application application) {
        super(application);
        articlesList = new MutableLiveData<>();
        articleRepository = new ArticleRepository(new DomRssParser());
    }


    //------------------------------------------------------------ GETTERS/SETTERS

    public LiveData<List<ListItem>> getObservableArticlesList() {
        return articlesList;
    }

    public void setArticlesList(List<ListItem> articlesList) {
        this.articlesList.postValue(articlesList);
    }



    //------------------------------------------------------------ METHODS

    public void downloadArticles(List<Source> givenSources, int limit, boolean forced) {
        Runnable task = () -> {
            if( ! taskIsRunning) {
                taskIsRunning = true;
                // pick sources for ALL tab, only once
                if(targetSources==null || targetSources.size()<=0) {
                    targetSources = SourceRepository.getAllSourcesOfThisCategory(givenSources, TECH_CATEGORY);// TODO this should not be static
                }
                List<ListItem> articles = articleRepository.getTechArticles(targetSources, limit, forced, getApplication());

                taskIsRunning = false;

                // publish results
                setArticlesList(articles);
            }
        };

        ThreadManager threadManager = ThreadManager.getInstance();
        threadManager.runTask(task);
    }

    public void smartSaveInHistory(@NotNull Article givenArticle) {
        if(lastVisitedArticleId==null || lastVisitedArticleId.isEmpty()) {
            saveInHistory(givenArticle);
        }
        else if(lastVisitedArticleId.equals(givenArticle.getIdentifier())){
            updateHistory(givenArticle);
        }
        else {
            saveInHistory(givenArticle);
        }
        // TODO: improve this branching
    }

    private void updateHistory(@NotNull Article givenArticle) {
        HistoryDao dao = getHistoryDao(getApplication());

        // TODO: prevent multiple thread spawning?
        Runnable task = () -> {
            // TODO null checks
            long millis=System.currentTimeMillis();
            java.util.Date newDate=new java.util.Date(millis);

            VisitedArticle visitedArticle = new VisitedArticle(givenArticle);
            visitedArticle.setVisitedDate(newDate);
            int result = dao.update(newDate, lastVisitedArticleId, oldVisitedDate);

            if(result>0) {
                oldVisitedDate = newDate;
                Log.d(TAG, "SCIENCE_BOARD - updateHistory: updateed to the latest vidited date \n" + lastVisitedArticleId + "\n" + millis);
            }
            else {
                Log.d(TAG, "SCIENCE_BOARD - updateHistory: cannot update history \n" + lastVisitedArticleId + "\n" + millis);
            }
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "SCIENCE_BOARD - updateHistory: cannot start thread " + e.getMessage());
        }
    }

    private void saveInHistory(@NotNull Article givenArticle) {
        HistoryDao dao = getHistoryDao(getApplication());

        Runnable task = () -> {
            // TODO null checks
            saveInHistoryTaskIsRunning = true;
            long millis=System.currentTimeMillis();
            java.util.Date date=new java.util.Date(millis);

            VisitedArticle visitedArticle = new VisitedArticle(givenArticle);
            visitedArticle.setVisitedDate(date);
            dao.insert(visitedArticle);
            lastVisitedArticleId = givenArticle.getIdentifier();
            oldVisitedDate = date;
            saveInHistoryTaskIsRunning = false;
        };

        if( ! saveInHistoryTaskIsRunning) {
            ThreadManager t = ThreadManager.getInstance();
            try {
                t.runTask(task);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "SCIENCE_BOARD - saveInHistory: cannot start thread " + e.getMessage());
            }
        }
    }

    private HistoryDao getHistoryDao(Context context) {
        ScienceBoardRoomDatabase roomDatabase = ScienceBoardRoomDatabase.getInstance(context);
        return roomDatabase.getHistoryDao();
    }











}// end TechTabViewModel