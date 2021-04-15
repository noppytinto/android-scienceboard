package com.nocorp.scienceboard.rss.repository;


import android.content.Context;
import android.os.ConditionVariable;
import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.rss.room.ArticleDao;
import com.nocorp.scienceboard.rss.room.ScienceBoardRoomDatabase;
import com.nocorp.scienceboard.rss.room.SourceDao;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class ArticleRepository {
    private final String TAG = this.getClass().getSimpleName();
    private final String ARTICLES_COLLECTION_NAME = "articles";
    private final String PUB_DATE = "pub_date";
    private final String SOURCE_ID = "source_id";
    private final String SOURCE_REAL_NAME = "source_real_name";
    private final String SOURCE_WEBSITE_URL = "source_website_url";
    private final String TITLE = "title";
    private final String THUMBNAIL_URL = "thumbnail_url";
    private final String WEBPAGE_URL = "webpage_url";
    private List<ListItem> cachedArticles;
    private FirebaseFirestore db;
    private ArticlesRepositoryListener listener;
    private int sourcesFetched;
    private int sourcesToFetch;
    private final ConditionVariable releaseThread = new ConditionVariable();



    //----------------------------------------------------------- CONSTRUCTORS

    public ArticleRepository(ArticlesRepositoryListener listener) {
        this.listener = listener;
        db = FirebaseFirestore.getInstance();
    }




    //----------------------------------------------------------- PUBLIC METHODS

    // DOM strategy
    public void getArticles(List<Source> givenSources, int numArticlesForEachSource, Context context) {
        if(givenSources==null || givenSources.size()<=0) return;
        downloadArticlesFromRemoteDb(givenSources, numArticlesForEachSource, context);
    }// end getArticles()







    //----------------------------------------------------------- PRIVATE METHODS


    /**
     * TODO implemente real download limit
     * since this is a fake limit, because alla rticles are always downloaded regardless
     */
    private void downloadArticlesFromRemoteDb(List<Source> givenSources, int numArticlesForEachSource, Context context) {
        // download source data
        cachedArticles = new ArrayList<>();
        sourcesToFetch = givenSources.size();
        sourcesFetched = 0;
        Log.d(TAG, "SCIENCE_BOARD - sources to fetch: " + sourcesToFetch);
        for(Source currentSource: givenSources) {
            downloadArticlesFromRemoteDb_companion(currentSource.getId(), numArticlesForEachSource, context);
        }
        Log.d(TAG, "SCIENCE_BOARD - blocking on this thread until sources are all fetched");
        releaseThread.block();
        Log.d(TAG, "SCIENCE_BOARD - thread released");
        listener.onArticlesFetchCompleted(cachedArticles);
    }

    // get the latest <limit> articles
    private void downloadArticlesFromRemoteDb_companion(String sourceName, int limit, Context context) {
        List<Article> result = new ArrayList<>();
        db.collection(ARTICLES_COLLECTION_NAME).whereEqualTo(SOURCE_ID, (String)sourceName).orderBy(PUB_DATE, Query.Direction.DESCENDING).limit(limit)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Article article = buildArticle(document);
                            if(article!=null)  {
                                result.add(article);
                            }
                        }

                        //
                        onArticlesFetchCompleted(result, context);

                    } else {
                        Log.e(TAG, "SCIENCE_BOARD - Error getting articles.", task.getException());
//                        listener.onArticlesFetchFailed("Error getting articles." + task.getException().getMessage());
                    }
                });
    }

    private void onArticlesFetchCompleted(List<Article> articles, Context context) {
        if(articles==null || articles.size()<=0) return;
        cachedArticles.addAll(articles);
        saveArticlesInRoom(articles, context);
        sourcesFetched++;
        Log.d(TAG, "SCIENCE_BOARD - sources fetched: " + sourcesFetched);
        if(sourcesFetched >= sourcesToFetch) {
            releaseThread.open();
            Log.d(TAG, "SCIENCE_BOARD - condition verified, releasing thread");
        }
    }

    // DOM strategy
    private List<Article> combineArticles(List<Source> sources) {
        List<Article> result = null;
        if(sources==null || sources.size()<=0) return result;

        result = new ArrayList<>();
        for(Source currentSource: sources) {
            List<Article> temp = currentSource.getArticles();
            if(temp!=null && temp.size()>0) {
                result.addAll(temp);
            }
        }

        return result;
    }

    private Article buildArticle(QueryDocumentSnapshot document) {
        Article article = null;
        if(document==null) return article;

        article = new Article();
        article.setTitle((String) document.get(TITLE));
        article.setWebpageUrl((String) document.get(WEBPAGE_URL));
        article.setPubDate((long) document.get(PUB_DATE));
        article.setThumbnailUrl((String) document.get(THUMBNAIL_URL));
        article.setId((String) document.getId());
        article.setSourceId((String) document.get(SOURCE_ID));
        article.setSourceRealName((String) document.get(SOURCE_REAL_NAME));
        article.setSourceWebsiteUrl((String) document.get(SOURCE_WEBSITE_URL));

        return article;
    }




    private void saveArticlesInRoom(@NotNull List<Article> articles, Context context) {
        ArticleDao articleDao = getArticleDao(context);

        Runnable task = () -> {
            try {
                articleDao.insertAll(articles);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "SCIENCE_BOARD - saveArticlesInRoom: cannot insert articles " + e.getMessage());
            }
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "SCIENCE_BOARD - saveArticlesInRoom: cannot start thread " + e.getMessage());
        }
    }

    private ArticleDao getArticleDao(Context context) {
        ScienceBoardRoomDatabase roomDatabase = ScienceBoardRoomDatabase.getInstance(context);
        return roomDatabase.getArticleDao();
    }

}// end ArticleRepository
