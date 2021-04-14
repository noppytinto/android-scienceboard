package com.nocorp.scienceboard.ui.tabs.alltab;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.model.VisitedArticle;
import com.nocorp.scienceboard.rss.repository.ArticleRepository;
import com.nocorp.scienceboard.rss.repository.ArticlesRepositoryListener;
import com.nocorp.scienceboard.rss.repository.SourceRepository;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.rss.room.HistoryDao;
import com.nocorp.scienceboard.rss.room.ScienceBoardRoomDatabase;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AllTabViewModel extends AndroidViewModel implements ArticlesRepositoryListener {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<List<ListItem>> articlesList;
    private ArticleRepository articleRepository;
    private final List<String> mainCategories = Arrays.asList("space", "physics", "tech", "medicine", "biology");
    private static List<Source> targetSources;
    private static boolean taskIsRunning;
    private static boolean saveInHistoryTaskIsRunning;
    private static String lastVisitedArticleId;
    private static Date oldVisitedDate;
    private SourceRepository sourceRepository;
    private static List<ListItem> cachedArticles;



    //------------------------------------------------------------ CONSTRUCTORS

    public AllTabViewModel(Application application) {
        super(application);
        articlesList = new MutableLiveData<>();
        articleRepository = new ArticleRepository(this);
        sourceRepository = new SourceRepository();
    }



    //------------------------------------------------------------ GETTERS/SETTERS

    public LiveData<List<ListItem>> getObservableArticlesList() {
        return articlesList;
    }

    public void setArticlesList(List<ListItem> articlesList) {
        this.articlesList.postValue(articlesList);
    }



    //------------------------------------------------------------ METHODS

    public void downloadArticles(List<Source> givenSources, int numArticlesForEachSource, boolean forced) {
        if(forced) {
            Runnable task = () -> {
                if( ! taskIsRunning) {
                    cachedArticles = new ArrayList<>();
                    taskIsRunning = true;
                    // pick sources for ALL tab, only once
                    if(targetSources==null || targetSources.size()<=0) {
                        targetSources = sourceRepository.getAsourceForEachMainCategory_randomly(givenSources, mainCategories);
                    }
                    articleRepository.getArticles(targetSources, numArticlesForEachSource, getApplication());
                }
            };

            ThreadManager threadManager = ThreadManager.getInstance();
            threadManager.runTask(task);
        }
        else {
            smartArticlesDownload(givenSources, numArticlesForEachSource);
        }
    }

    private void smartArticlesDownload(List<Source> givenSources, int numArticlesForEachSource) {
        if(cachedArticles ==null) {
            if(targetSources==null || targetSources.size()<=0) {
                targetSources = sourceRepository.getAsourceForEachMainCategory_randomly(givenSources, mainCategories);
            }
            articleRepository.getArticles(targetSources, numArticlesForEachSource, getApplication());
        }
        else {
            setArticlesList(cachedArticles);
        }
    }

    public void smartSaveInHistory(@NotNull Article givenArticle) {
        if(lastVisitedArticleId==null || lastVisitedArticleId.isEmpty()) {
            saveInHistory(givenArticle);
        }
        else if(lastVisitedArticleId.equals(givenArticle.getId())){
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
            lastVisitedArticleId = givenArticle.getId();
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


    @Override
    public void onArticlesFetchCompleted(List<ListItem> articles) {
        taskIsRunning = false;

        // publish results
        setArticlesList(articles);
    }

    @Override
    public void onArticlesFetchFailed(String cause) {

    }

    @Override
    public void onAllArticlesFetchCompleted(List<Article> articles) {

    }

    @Override
    public void onAllArticlesFetchFailed(String cause) {

    }

    @Override
    public void onTechArticlesFetchCompleted(List<Article> articles) {

    }

    @Override
    public void onTechArticlesFetchFailed(String cause) {

    }
}// end AllArticlesTabViewModel