package com.nocorp.scienceboard.ui.home;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.nocorp.scienceboard.bookmarks.repository.BookmarksRepository;
import com.nocorp.scienceboard.bookmarks.repository.OnBookmarksCheckedListener;
import com.nocorp.scienceboard.history.repository.HistoryRepository;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.CustomizeMyTopicsButton;
import com.nocorp.scienceboard.model.MyTopicsItem;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.repository.GeneralRepository;
import com.nocorp.scienceboard.rss.repository.ArticleRepository;
import com.nocorp.scienceboard.rss.repository.ArticlesRepositoryListener;
import com.nocorp.scienceboard.rss.repository.SourceRepository;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.topics.model.Topic;
import com.nocorp.scienceboard.topics.repository.TopicRepository;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.MyValues;
import com.nocorp.scienceboard.utility.ad.admob.AdProvider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeViewModel extends AndroidViewModel implements
        ArticlesRepositoryListener {
    private final String TAG = getClass().getSimpleName();
    private final String APP_NAME = "NOPPYS_BOARD - ";
    private MutableLiveData<List<ListItem>> articlesList;
    private MutableLiveData<List<ListItem>> nextArticlesList;
    private static List<Source> pickedSources;
    private static boolean taskIsRunning;
    private static boolean bookmarksChecksTaskIsRunning;
    private SourceRepository sourceRepository;
    private static List<ListItem> cachedArticles;
    private static long lastFetchDate;
    private static List<DocumentSnapshot> oldestArticlesSnapshots;

    // repos
    private HistoryRepository historyRepository;
    private BookmarksRepository bookmarksRepository;
    private GeneralRepository generalRepository;
    private ArticleRepository articleRepository;
    private TopicRepository topicRepository;
//    private final int FETCH_INTERVAL = 15; // in minutes


    //
    private AdProvider adProvider;


    //-------------------------------------------------------------------------------------------- CONSTRUCTORS

    public HomeViewModel(Application application) {
        super(application);
        articlesList = new MutableLiveData<>();
        nextArticlesList = new MutableLiveData<>();
        articleRepository = new ArticleRepository(this);
        sourceRepository = new SourceRepository();
        historyRepository = new HistoryRepository();
        bookmarksRepository = new BookmarksRepository();
        generalRepository = new GeneralRepository();
        topicRepository = new TopicRepository();

        //
        adProvider = AdProvider.getInstance(); // is not guaranteed that
    }




    //-------------------------------------------------------------------------------------------- GETTERS/SETTERS

    public LiveData<List<ListItem>> getObservableArticlesList() {
        return articlesList;
    }

    public void setArticlesList(List<ListItem> articlesList) {
        this.articlesList.postValue(articlesList);
    }

    public LiveData<List<ListItem>> getObservableNextArticlesList() {
        return nextArticlesList;
    }

    public void setNextArticlesList(List<ListItem> articlesList) {
        this.nextArticlesList.postValue(articlesList);
    }

    public List<Source> getPickedSources() {
        return pickedSources;
    }



    //-------------------------------------------------------------------------------------------- METHODS


    //-------------------------------------------------------------- FETCH ARTICLES

    public void fetchArticles(List<Source> givenSources,
                              long startingDateInMillis,
                              boolean forced) {
        Log.d(TAG, APP_NAME + "fetchArticles: called, forced:" + forced);

        if( ! taskIsRunning) {
            Runnable task = () -> {
                taskIsRunning = true;

                if (forced) {
                    Log.d(TAG, APP_NAME + "fetchArticles: FORCED: fetching from remote");
                    downloadArticlesFromFollowedTopics( givenSources,
                                                        topicRepository.getFollowedTopics_sync(getApplication()),
                                                        startingDateInMillis);
                } else {
                    Log.d(TAG, APP_NAME + "fetchArticles: NOT FORCED: trying fetching from cache");
                    tryCachedArticles( givenSources,
                                       topicRepository.getFollowedTopics_sync(getApplication()),
                                       startingDateInMillis);
                }
            };

            // starting task
            ThreadManager threadManager = ThreadManager.getInstance();
            threadManager.runTask(task);
        }

            //        // if the request is within 15 mins
//        // then use cached sources from local variable or Room
//            Log.d(TAG, "SCIENCE_BOARD - fetchArticles: lastArticlesFetchDate: " + lastFetchDate);

//            if(MyUtilities.isWithin_minutes(FETCH_INTERVAL, lastFetchDate)) {
//                Log.d(TAG, "SCIENCE_BOARD - fetchArticles: fetching from cache (within 15 mins)");
//                tryCachedArticles(givenSources,
//                        numArticlesForEachSource,
//                        startingDateInMillis);
//            }
//            else {
//                Log.d(TAG, "SCIENCE_BOARD - fetchArticles: NOT FORCED: fetching from cache");
//                tryCachedArticles(givenSources,
//                        numArticlesForEachSource,
//                        startingDateInMillis);
//            }

    }// end fetchArticles

    private void downloadArticlesFromFollowedTopics(List<Source> givenSources,
                                                    List<Topic> followedTopics,
                                                    long startingDateInMillis) {
        Log.d(TAG, APP_NAME + "downloadArticlesFromFollowedTopics: called");
        cachedArticles = new ArrayList<>();
        int maxSourcesToGet = 1;
        int numArticlesToFetchForEachSource = 2;
        final int numFollowedTopics = followedTopics.size();

        // deciding how many sources to take in consideration
        // and how many articles to fetch for each source
        switch (numFollowedTopics) {
            case 1: {
                maxSourcesToGet = 5;
                numArticlesToFetchForEachSource = 1;
                Log.d(TAG, APP_NAME + "downloadArticlesFromFollowedTopics: case 1, " + maxSourcesToGet + " sources, " + numArticlesToFetchForEachSource + " articles for each source");
            }
            break;
            case 2: {
                maxSourcesToGet = 3;
                numArticlesToFetchForEachSource = 2;
                Log.d(TAG, APP_NAME + "downloadArticlesFromFollowedTopics: case 2, " + maxSourcesToGet + " sources, " + numArticlesToFetchForEachSource + " articles for each source");
            }
            break;
            case 3: {
                maxSourcesToGet = 2;
                numArticlesToFetchForEachSource = 3;
                Log.d(TAG, APP_NAME + "downloadArticlesFromFollowedTopics: case 3, " + maxSourcesToGet + " sources, " + numArticlesToFetchForEachSource + " articles for each source");
            }
            break;
//            default: {
//                maxSourcesToGet = 1;
//                numArticlesToFetchForEachSource = 2;
//                Log.d(TAG, APP_NAME + "downloadArticlesFromFollowedTopics: default case, " + maxSourcesToGet + " sources, " + numArticlesToFetchForEachSource + " articles for each source");
//            }
        }


        // picking sources
        pickedSources =
                sourceRepository.getNsourcesForEachFollowedTopic_randomly( givenSources,
                                                                           followedTopics,
                                                                           maxSourcesToGet);
//                pickedSources = sourceRepository.getAsourceForEachFollowedCategory_randomly(givenSources, TopicRepository.getAllEnabledTopics_cached());

        // fetching articles
        articleRepository.fetchArticles(pickedSources,
                                        numArticlesToFetchForEachSource,
                                        startingDateInMillis,
                                        getApplication());
    }

    private void tryCachedArticles(List<Source> givenSources,
                                   List<Topic> followedTopics,
                                   long startingDateinMillis) {
        if(cachedArticles == null) {
            Log.d(TAG, APP_NAME + "tryCachedArticles: fetched from remote");
            downloadArticlesFromFollowedTopics(givenSources,
                                               followedTopics,
                                               startingDateinMillis);
        }
        else {
            Log.d(TAG, APP_NAME + "tryCachedArticles: fetched from cache");
            setArticlesList(cachedArticles);
            taskIsRunning = false;
        }
    }

    @Override
    public void onArticlesFetchCompleted(List<ListItem> resultArticles, List<DocumentSnapshot> oldestArticles) {
        if(resultArticles==null) {
            // TODO: null is returned only in case of errors
        }
        else {
//            lastFetchDate = System.currentTimeMillis();
            oldestArticlesSnapshots = oldestArticles;


            // setting up followed topics items
            setTopicsThumbnails(resultArticles,
                    TopicRepository.getFollowedTopics(),
                    getPickedSources());

            populateHomeWithMyFollowedTopics(
                    TopicRepository.getFollowedTopics(),
                    resultArticles);


            // publish results
            cachedArticles = resultArticles;
            historyAndBookmarksCheck(resultArticles);

            //
            setArticlesList(resultArticles);
        }

        taskIsRunning = false;
    }



    @Override
    public void onArticlesFetchFailed(String cause) {
        taskIsRunning = false;
//        lastFetchDate = System.currentTimeMillis();

        // TODO
    }

    private void populateHomeWithMyFollowedTopics(List<Topic> topics, List<ListItem> elementsToDisplayInHome) {
        MyTopicsItem myTopicsItem = new MyTopicsItem();

        // convert Topic --> to ListItem, for recycler list
        List<ListItem> convertedList = new ArrayList<>();
        if(topics!=null) {
            convertedList = new ArrayList<>(topics);
            // add customize button to the end
            convertedList.add(new CustomizeMyTopicsButton());
        }

        myTopicsItem.setMyTopics(convertedList);
        elementsToDisplayInHome.add(0, myTopicsItem); // add topics as first element
    }



    private void setTopicsThumbnails(List<ListItem> elementsToDisplayInHome,
                                     List<Topic> givenTopics,
                                     List<Source> givenSources) {
        if(givenTopics==null || givenTopics.isEmpty()) return;
        if(elementsToDisplayInHome==null || elementsToDisplayInHome.isEmpty()) return;
        if(givenSources==null || givenSources.isEmpty()) return;

        List<Article> articles = filterArticles(elementsToDisplayInHome);
        if(articles==null || articles.isEmpty()) return;

//        SourceRepository sourceRepository = new SourceRepository();
//        List<Source> sourcesTarget = sourceRepository.getAsourceForEachFollowedCategory_randomly(givenSources, givenTopics);
//        for(Source currentSource: sourcesTarget) {
//
//        }

        for(Topic currentTopic: givenTopics) { // for each topic...
            String currentTopicId = currentTopic.getId();
            for(Source currentSource: givenSources) { // ...get a source...
                if(currentSource.getCategories().contains(currentTopicId)) {
                    String currentSourceId = currentSource.getId();
                    Collections.shuffle(articles); // ... randomize articles...
                    for(Article currentArticle: articles) { //...get an articles with a thumbnail ...
                        if(currentSourceId.equals(currentArticle.getSourceId())) {
                            String thumbnailUrl = currentArticle.getThumbnailUrl();
                            if(thumbnailUrl!=null) {
                                currentTopic.setThumbnailUrl(thumbnailUrl);
                                break;
                            }
                        }
                    }// end most inner for
                    break;
                }
            }// end middle for
        }// end outer for
    }

    private List<Article> filterArticles(List<ListItem> elementsToDisplayInHome) {
        List<Article> result = new ArrayList<>();

        for(ListItem listItem: elementsToDisplayInHome) {
            if(listItem.getItemType() == MyValues.ItemType.ARTICLE) {
                Article article = ((Article) listItem);
                result.add(article);
            }
        }

        return result;
    }



    //-------------------------------------------------------------- FETCH NEXT ARTICLES

    public void fetchNextArticles(int numArticlesForEachSource) {
        if( ! taskIsRunning) {
            Runnable task = () -> {
//                sleepforNseconds(1);
                Log.d(TAG, APP_NAME + "fetchNextArticles: fetching new articles");
                articleRepository.fetchNextArticles(oldestArticlesSnapshots, numArticlesForEachSource, getApplication());
            };

            ThreadManager threadManager = ThreadManager.getInstance();
            threadManager.runTask(task);
        }
    }

    @Override
    public void onNextArticlesFetchCompleted(List<ListItem> newArticles, List<DocumentSnapshot> oldestArticles) {
        oldestArticlesSnapshots = new ArrayList<>(oldestArticles);

        // publish results
        historyAndBookmarksCheck(newArticles);
        cachedArticles.addAll(newArticles);
        setNextArticlesList(cachedArticles);
        taskIsRunning = false;
    }

    @Override
    public void onNextArticlesFetchFailed(String cause) {
        setNextArticlesList(null);
        Log.e(TAG, APP_NAME + "onNextArticlesFetchFailed: " + cause);
        taskIsRunning = false;
    }




    //-------------------------------------------------------------- HISTORY/BOOKMARKS

    private void historyCheck(List<ListItem> articles) {
        historyRepository.historyCheck(articles, getApplication());
    }

    private void bookmarksCheck(List<ListItem> articles) {
        bookmarksRepository.bookmarksCheck(articles, getApplication());
    }

    private void historyAndBookmarksCheck(List<ListItem> articles) {
        if(articles==null) return;
        generalRepository.historyAndBookmarksCheck_sync(articles, getApplication());
    }

    public void asyncBookmarksCheck(List<ListItem> articles, OnBookmarksCheckedListener listener) {
        if(articles == null || articles.isEmpty()) return;

        if(!bookmarksChecksTaskIsRunning) {

            Runnable task = () -> {
                bookmarksRepository.bookmarksCheck(articles, getApplication());
                bookmarksChecksTaskIsRunning = false;
                listener.onComplete();
            };

            ThreadManager threadManager = ThreadManager.getInstance();
            threadManager.runTask(task);
        }
    }

    public void saveInHistory(@NotNull Article givenArticle) {
        historyRepository.saveInHistory(givenArticle, getApplication());
    }

    public void addToBookmarks(Article givenArticle) {
        bookmarksRepository.addToBookmarks_async(givenArticle, getApplication());
    }

    public void removeFromBookmarks(Article givenArticle) {
        bookmarksRepository.removeFromBookmarks_async(givenArticle, getApplication());
    }

    private void sleepforNseconds(long seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}// end HomeViewModel