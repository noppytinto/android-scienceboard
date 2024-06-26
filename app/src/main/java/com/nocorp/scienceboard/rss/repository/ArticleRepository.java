package com.nocorp.scienceboard.rss.repository;


import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.rss.room.ArticleDao;
import com.nocorp.scienceboard.rss.room.ScienceBoardRoomDatabase;

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
    private final String KEYWORDS = "keywords";
    private List<ListItem> fetchedArticles;
    private FirebaseFirestore db;
    private ArticlesRepositoryListener listener;
    private int sourcesConsumed;
    private int sourcesToConsume;
    private List<DocumentSnapshot> oldestArticlesSnapshots;
    private SourceRepository sourceRepository;


    //--------------------------------------------------------------------------- CONSTRUCTORS

    public ArticleRepository(ArticlesRepositoryListener listener) {
        this.listener = listener;
        sourceRepository = new SourceRepository();
        db = FirebaseFirestore.getInstance();
    }




    //--------------------------------------------------------------------------- PUBLIC METHODS

//    public void getArticles(List<Source> givenSources,
//                            int numArticlesForEachSource,
//                            Context context) {
//        //TODO: givenSources==null should be returned only in case of errors
//        if(givenSources==null || givenSources.isEmpty()) {
//            listener.onArticlesFetchCompleted(new ArrayList<>(), new ArrayList<>());
//            return;
//        }
//
//        // server strategy
//        downloadArticlesFromServer(givenSources, numArticlesForEachSource, context);
//    }

    public void fetchArticles(List<Source> givenSources,
                              int numArticlesForEachSource,
                              long startingDate,
                              Context context) {
        //TODO: givenSources==null should be returned only in case of errors
        if(givenSources==null || givenSources.isEmpty()) {
            listener.onArticlesFetchCompleted(new ArrayList<>(), new ArrayList<>());
            return;
        }

        // server strategy
        downloadArticlesFromServer(givenSources, numArticlesForEachSource, context, startingDate);
    }

    public void fetchNextArticles(List<DocumentSnapshot> oldestArticles,
                                  int numArticlesForEachSource,
                                  Context context) {

        if(oldestArticles==null || oldestArticles.isEmpty()) {
            listener.onNextArticlesFetchFailed("no more articles");
            return;
        }

        // server strategy
        downloadNextArticlesFromServer(oldestArticles, numArticlesForEachSource, context);
    }





    //--------------------------------------------------------------------------- PRIVATE METHODS

//    /**
//     * TODO implemente real download limit
//     * since this is a fake limit, because alla rticles are always downloaded regardless
//     */
//    private void downloadArticlesFromServer(List<Source> givenSources,
//                                            int numArticlesForEachSource,
//                                            Context context) {
//        // download source articles
//        fetchedArticles = new ArrayList<>();
//        oldestArticlesSnapshots = new ArrayList<>();
//        sourcesToConsume = givenSources.size();
//        sourcesConsumed = 0;
//        Log.d(TAG, "SCIENCE_BOARD - sources to consume: " + sourcesToConsume);
//
//        //
//        for(Source currentSource: givenSources) {
//            downloadArticlesFromServer_companion(currentSource.getId(), numArticlesForEachSource, context);
//        }
//
//        //
//        if(sourcesConsumed >= sourcesToConsume) {
//            Log.d(TAG, "SCIENCE_BOARD - all articles fetched");
//            listener.onArticlesFetchCompleted(fetchedArticles, oldestArticlesSnapshots);
//        }
//    }

    private void downloadArticlesFromServer(List<Source> givenSources,
                                            int numArticlesForEachSource,
                                            Context context,
                                            long startingDate) {
        // download source articles
        fetchedArticles = new ArrayList<>();
        oldestArticlesSnapshots = new ArrayList<>();
        sourcesToConsume = givenSources.size();
        sourcesConsumed = 0;
        Log.d(TAG, "SCIENCE_BOARD - sources to consume: " + sourcesToConsume);

        //
        for(Source currentSource: givenSources) {
            downloadNewArticlesFromServer_companion(currentSource.getId(), numArticlesForEachSource, context, startingDate);
        }

        //
        if(sourcesConsumed >= sourcesToConsume) {
            Log.d(TAG, "SCIENCE_BOARD - all articles fetched");
            listener.onArticlesFetchCompleted(fetchedArticles, oldestArticlesSnapshots);
        }
    }

    private void fetchLocally_strategy(Source source, Context context) {
        List<Article> fetchedArticles = null;
        fetchedArticles = getArticlesByIdFromRoom(source, context);

    }

    private List<Article> getArticlesByIdFromRoom(Source source, Context context) {
        List<Article> result = null;

        try {
            ArticleDao articleDao = getArticleDao(context);
            result = articleDao.selectBySourceId(source.getId());

            // getting oldest document
            // note: now i points to the real(starting from 1) last element position
//            extractOldestArticle(documentSnapshots, i);
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - fetchLocally_strategy: cannot fetch articles from room, cause:" + e.getMessage());
        }

        return result;
    }


//    // get the latest <limit> articles
//    private void downloadArticlesFromServer_companion(String sourceName,
//                                                      int limit,
//                                                      Context context) {
//        List<Article> result = new ArrayList<>();
//        db.collection(ARTICLES_COLLECTION_NAME)
//                .whereEqualTo(SOURCE_ID, (String)sourceName)
//                .orderBy(PUB_DATE, Query.Direction.DESCENDING)
//                .limit(limit)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//
//                        List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
//
//                        if(documentSnapshots!=null && !documentSnapshots.isEmpty()) {
//                            int i=0;
//                            for ( /*ignore*/ ; i<documentSnapshots.size(); i++) {
//                                DocumentSnapshot document = documentSnapshots.get(i);
//                                Article article = buildArticle(document);
//                                if(article!=null)  {
//                                    result.add(article);
//                                }
//                            }
//                            // getting oldest document
//                            extractOldestArticles(documentSnapshots, i);
//                        }
//
//                        //
//                        onArticlesFetchCompleted(result, context);
//
//                    } else {
//                        Log.e(TAG, "SCIENCE_BOARD - Error getting articles.", task.getException());
////                        listener.onArticlesFetchFailed("Error getting articles." + task.getException().getMessage());
//                        sourcesConsumed++;
//                    }
//                });
//        Log.d(TAG, "SCIENCE_BOARD - blocking on this thread until articles are fetched");
//
//        synchronized (fetchedArticles) {
//            try {
//                fetchedArticles.wait();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        Log.d(TAG, "SCIENCE_BOARD - thread resumed");
//    }

    // get the latest <limit> articles
    private void downloadNewArticlesFromServer_companion(String sourceName,
                                                         int limit,
                                                         Context context, long startingDate) {
        List<Article> result = new ArrayList<>();
        db.collection(ARTICLES_COLLECTION_NAME)
                .whereEqualTo(SOURCE_ID, (String)sourceName)
                .whereLessThanOrEqualTo(PUB_DATE, startingDate)
                .orderBy(PUB_DATE, Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();

                        if(documentSnapshots!=null && !documentSnapshots.isEmpty()) {
                            int i=0;
                            for ( /*ignore*/ ; i<documentSnapshots.size(); i++) {
                                DocumentSnapshot document = documentSnapshots.get(i);
                                Article article = buildArticle(document);
                                if(article!=null)  {
                                    result.add(article);
                                }
                            }
                            // getting oldest document
                            // note: now i points to the real(starting from 1) last element position
                            extractOldestArticle(documentSnapshots, i);
                        }

                        //
                        onArticlesFetchCompleted(result, context);

                    } else {
                        Log.e(TAG, "SCIENCE_BOARD - Error getting articles.", task.getException());
//                        listener.onArticlesFetchFailed("Error getting articles." + task.getException().getMessage());
                        sourcesConsumed++;
                    }
                });
        Log.d(TAG, "SCIENCE_BOARD - blocking on this thread until articles are fetched");

        synchronized (fetchedArticles) {
            try {
                fetchedArticles.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "SCIENCE_BOARD - thread resumed");
    }

    private void downloadNextArticlesFromServer(List<DocumentSnapshot> startingArticles,
                                                int numArticlesForEachSource,
                                                Context context) {
        // download source data
        fetchedArticles = new ArrayList<>();
        oldestArticlesSnapshots = new ArrayList<>();
        sourcesToConsume = startingArticles.size();
        sourcesConsumed = 0;
        Log.d(TAG, "SCIENCE_BOARD - sources to fetch: " + sourcesToConsume);
        for(DocumentSnapshot currentDocument: startingArticles) {
            downloadNextArticlesFromServer_companion(currentDocument, numArticlesForEachSource, context);
        }
        if(sourcesConsumed >= sourcesToConsume) {
            Log.d(TAG, "SCIENCE_BOARD - all articles fetched");
            listener.onNextArticlesFetchCompleted(fetchedArticles, oldestArticlesSnapshots);
        }
    }

    private void downloadNextArticlesFromServer_companion(DocumentSnapshot startingArticle,
                                                          int givenLimit,
                                                          Context context) {
        List<Article> result = new ArrayList<>();

        Query query = db.collection(ARTICLES_COLLECTION_NAME)
                .orderBy(PUB_DATE, Query.Direction.DESCENDING)
                .startAfter(startingArticle)
                .whereEqualTo("source_id", startingArticle.get("source_id"))
                .limit(givenLimit);

        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();

                        if(documentSnapshots!=null && !documentSnapshots.isEmpty()) {
                            int i=0;
                            for ( /*ignore*/ ; i<documentSnapshots.size(); i++) {
                                DocumentSnapshot document = documentSnapshots.get(i);
                                Article article = buildArticle(document);
                                if(article!=null)  {
                                    result.add(article);
                                }
                            }
                            // getting oldest document
                            extractOldestArticle(documentSnapshots, i);
                        }

                        //
                        onArticlesFetchCompleted(result, context);

                    } else {
                        Log.e(TAG, "SCIENCE_BOARD - Error getting articles.", task.getException());
//                        listener.onArticlesFetchFailed("Error getting articles." + task.getException().getMessage());
                        sourcesConsumed++;
                    }
                });

        Log.d(TAG, "SCIENCE_BOARD - blocking on this thread until articles are fetched");
        synchronized (fetchedArticles) {
            try {
                fetchedArticles.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "SCIENCE_BOARD - thread resumed");
    }

    private Article buildArticle(DocumentSnapshot document) {
        Article article = null;
        if(document==null) return article;

        article = new Article();
        article.setTitle((String) document.get(TITLE));
        article.setWebpageUrl((String) document.get(WEBPAGE_URL));
        article.setPubDate((Long) document.get(PUB_DATE));
        article.setThumbnailUrl((String) document.get(THUMBNAIL_URL));
        article.setId((String) document.getId());
        article.setSourceId((String) document.get(SOURCE_ID));
        article.setSourceRealName((String) document.get(SOURCE_REAL_NAME));
        article.setSourceWebsiteUrl((String) document.get(SOURCE_WEBSITE_URL));
        List<String> keywords = (List<String>) document.get(KEYWORDS);
        article.setKeywords(keywords);

        return article;
    }

    private void onArticlesFetchCompleted(List<Article> articles, Context context) {
        if(articles==null || articles.isEmpty()) {
            // in case of no more articles to fetch
            sourcesConsumed = sourcesToConsume;
        }
        else {
            fetchedArticles.addAll(articles);
            saveArticlesInRoom_async(articles, context);
            sourcesConsumed++;
        }

        Log.d(TAG, "SCIENCE_BOARD - article fetched, resuming thread");
        synchronized(fetchedArticles){
            fetchedArticles.notify();
        }
    }

    private void extractOldestArticle(List<DocumentSnapshot> documentSnapshots, int lastRealElementPosition) {
        oldestArticlesSnapshots.add(documentSnapshots.get(lastRealElementPosition-1));
    }

//    private void extractOldestArticle(List<Article> givenArticles) {
//        oldestArticlesSnapshots.add(documentSnapshots.get(lastRealElementPosition-1));
//
//
//    }

    private void saveArticlesInRoom_async(@NotNull List<Article> articles, Context context) {
        Runnable task = () -> {
            try {
                ArticleDao articleDao = getArticleDao(context);
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

    //    private Query contains(Query query, String... keywords) {
//        for(String keyword: )
//        query.whereEqualTo("source_id", document.get("source_id"));
//        return query;
//    }

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

}// end ArticleRepository
