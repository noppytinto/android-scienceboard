package com.nocorp.scienceboard.ui.tabs.alltab;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
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
    private static long oldVisitedDate;
    private SourceRepository sourceRepository;
    private static List<ListItem> cachedArticles;
    private static List<DocumentSnapshot> oldestArticlesBySource;



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

    public void fetchArticles(List<Source> givenSources, int numArticlesForEachSource, boolean forced) {
        if(forced) {
            downloadArticles(givenSources, numArticlesForEachSource);
        }
        else {
            smartArticlesDownload(givenSources, numArticlesForEachSource);
        }
    }

    private void downloadArticles(List<Source> givenSources, int numArticlesForEachSource) {
        if( ! taskIsRunning) {
            Runnable task = () -> {
                cachedArticles = new ArrayList<>();
                taskIsRunning = true;
                // pick sources for ALL tab, only once
                if(targetSources==null || targetSources.size()<=0) {
                    targetSources = sourceRepository.getAsourceForEachMainCategory_randomly(givenSources, mainCategories);
                }
                articleRepository.getArticles(targetSources, numArticlesForEachSource, getApplication());
            };

            ThreadManager threadManager = ThreadManager.getInstance();
            threadManager.runTask(task);
        }
    }

    private void smartArticlesDownload(List<Source> givenSources, int numArticlesForEachSource) {
        if(cachedArticles == null) {
            downloadArticles(givenSources, numArticlesForEachSource);
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
            VisitedArticle visitedArticle = new VisitedArticle(givenArticle);
            visitedArticle.setVisitedDate(millis);
            int result = dao.update(millis, lastVisitedArticleId, oldVisitedDate);

            if(result>0) {
                oldVisitedDate = millis;
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
            try {
                // TODO null checks
                saveInHistoryTaskIsRunning = true;
                long millis=System.currentTimeMillis();
                VisitedArticle visitedArticle = new VisitedArticle(givenArticle);
                visitedArticle.setVisitedDate(millis);
                dao.insert(visitedArticle);
                lastVisitedArticleId = givenArticle.getId();
                oldVisitedDate = millis;
                saveInHistoryTaskIsRunning = false;
            } catch (Exception e) {
                Log.e(TAG, "SCIENCE_BOARD - saveInHistory: cannot save in history, " + e.getMessage());
            }
        };

        if( ! saveInHistoryTaskIsRunning) {
            ThreadManager t = ThreadManager.getInstance();
            try {
                t.runTask(task);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "SCIENCE_BOARD - saveInHistory: cannot start thread " + e.getMessage());
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
        cachedArticles = articles;
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