package com.nocorp.scienceboard.rss.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.topics.model.Topic;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.rss.room.ScienceBoardRoomDatabase;
import com.nocorp.scienceboard.rss.room.SourceDao;
import com.nocorp.scienceboard.topics.room.TopicDao;
import com.nocorp.scienceboard.utility.MyUtilities;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;

public class SourceRepository {
    private final String TAG = this.getClass().getSimpleName();
    private final String SOURCES_COLLECTION_NAME = "sources";
    private final String NAME = "name";
    private final String REAL_NAME = "real_name";
    private final String RSS_URL = "rss_url";
    private final String WEBSITE_URL = "website_url";
    private final String LANGUAGE = "language";
    private final String CATEGORY = "categories";
    private final String ENABLED = "enabled";
    private static List<Source> cachedSources;
    private OnSourcesFetchedListener fetchListener;
    private FirebaseFirestore db;
    private static boolean firestoreFetchCompleted;
    private static boolean taskIsRunning;
    private final int FETCH_INTERVAL = 0; // in days



    //--------------------------------------------------------------------------- CONSTRUCTORS

    public SourceRepository () {
        db = FirebaseFirestore.getInstance();
    }

    public SourceRepository (OnSourcesFetchedListener fetchListener) {
        db = FirebaseFirestore.getInstance();
        this.fetchListener = fetchListener;
    }

    public void setFetchListener(OnSourcesFetchedListener fetchListener) {
        this.fetchListener = fetchListener;
    }


    //--------------------------------------------------------------------------- PUBLIC METHODS









    //--------------------------------------------------------------------------- RXJAVA




//    public Single<List<Source>> fetchSources_rxjava(Context context) {
//        return Single.create(emitter -> {
//            try {
//                // if the request is within 1 our
//                // then use cached sources from local variable or Room
//                long lastFetchDate = getFromSharedPref(context.getString(R.string.pref_last_sources_fetch_date), context);
//                if(MyUtilities.isWithin_days(FETCH_INTERVAL, lastFetchDate)) {
//                    fetchLocally_strategy_rxjava(context, emitter);
//                }
//                //
//                else {
//                    fetchRemotely_strategy_rxjava(context, emitter);
//                }
//
//            } catch (Exception ex) {
//                emitter.onError(ex);
//            }
//        });
//    }

    public Single<Boolean> checkLastFetchDate(Context context) {
        return Single.create(emitter -> {
            long lastFetchDate = getFromSharedPref(context.getString(R.string.pref_last_topics_fetch_date), context);
            Log.d(TAG, "checkLastFetchDate: lastFetchDate: " + lastFetchDate);
            Boolean result = MyUtilities.isWithin_days(FETCH_INTERVAL, lastFetchDate);
            emitter.onSuccess(result);
        });
    }



    public Single<List<Source>> fetchLocally_strategy_rxjava(Context context) {
        return Single.create(emitter -> {
            List<Source> result;
            if(cachedSources==null) {
                Log.d(TAG, "fetchLocally_strategy: loading sources from room");
                result = getSourcesFromRoom_sync_rxjava(context);
                cachedSources = result;
                emitter.onSuccess(result);
            }
            else {
                Log.d(TAG, "fetchLocally_strategy: loading sources from cachedSources");
                emitter.onSuccess(cachedSources);
            }
        });
    }


    public Single<List<Source>> fetchRemotely_strategy_rxjava(Context context) {
        return Single.create(emitter -> {
            Log.d(TAG, "fetchRemotely_strategy: loading sources remotely");
            cachedSources = new ArrayList<>();
            db.collection(SOURCES_COLLECTION_NAME)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // testing code
//                            String name = (String) document.get(NAME);
//                            String testname = "lifehacker";
//                            if(enabled!=null && enabled && name.equals(testname)) {
//                                Source source = buildBasicSourceObject(document);
//                                if(source!=null)  {
//                                    cachedSources.add(source);
//                                }
//                            }

                                Source source = buildSource(document);
                                if(source!=null)  {
                                    cachedSources.add(source);
                                }
                            }

                            //
                            storeFetchDate(context);

                            //
                            Collections.shuffle(cachedSources); // randomize collection

                            //
                            emitter.onSuccess(cachedSources);
                        } else {
                            if(task.getException()!=null) {
                                Log.w(TAG, "SCIENCE_BOARD - Error getting sources.", task.getException());
                                emitter.onError(task.getException());
                            }
                        }
                    });
        });
    }

    public Single<List<Source>> saveFetchedSourcesInRoom_sync_rxjava(@NotNull List<Source> sources, Context context) {
        return Single.create(emitter -> {
            try {
                Log.d(TAG, "saveFetchedSourcesInRoom_sync_rxjava: called");
                saveSourcesInRoom_sync(sources, context);
                emitter.onSuccess(sources);
            } catch (Exception ex) {
                emitter.onError(ex);
            }
        });
    }

    private List<Source> getSourcesFromRoom_sync_rxjava(Context context) {
        List<Source> result = new ArrayList<>();
        try {
            SourceDao sourceDao = getSourceDao(context);
            result = sourceDao.selectAll();
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - getSourcesFromRoom_sync: cannot get sources from Room, cause:" + e.getMessage());
        }

        return result;
    }









    //---------------------------------------------------------------------------



    private void saveSourcesInRoom_sync(List<Source> sources, Context context) {
        try {
            Log.d(TAG, "saveSourcesInRoom_sync: called");
            SourceDao dao = getSourceDao(context);
            dao.insertAll(sources);
            for(Source current: sources) {
                dao.updateEnabledStatus(current.getEnabled(), current.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "SCIENCE_BOARD - saveSourcesInRoom_sync: cannot save sources in Room, cause:" + e.getMessage());
        }
    }



    public void fetchSources(Context context, OnSourcesFetchedListener listener) {
        if( ! taskIsRunning) {
            taskIsRunning = true;

            // if the request is within 1 our
            // then use cached sources from local variable or Room
            long lastFetchDate = getFromSharedPref(context.getString(R.string.pref_last_sources_fetch_date), context);
            Log.d(TAG, "loadSources: lastFetchDate: " + lastFetchDate);
            if(MyUtilities.isWithin_days(FETCH_INTERVAL, lastFetchDate)) {
                fetchLocally_strategy(context, listener);
            }
            //
            else {
                fetchRemotely_strategy(context, listener);
            }
        }
    }

    private void fetchRemotely_strategy(Context context, OnSourcesFetchedListener listener) {
        Log.d(TAG, "fetchRemotely_strategy: loading sources remotely");
        loadSourcesFromRemoteDb(context, listener);
    }

    private void fetchLocally_strategy(Context context, OnSourcesFetchedListener listener) {
        if(cachedSources==null) {
            Log.d(TAG, "fetchLocally_strategy: loading sources from room");
            loadSourcesFromRoom(context, listener);
            taskIsRunning = false;
        }
        else {
            Log.d(TAG, "fetchLocally_strategy: loading sources from cachedSources");
            taskIsRunning = false;
            listener.onSourcesFetchComplete(cachedSources);
        }
    }

    private void loadSourcesFromRoom(Context context, OnSourcesFetchedListener listener) {
        getSourcesFromRoom_async(context, listener);
    }

    private void storeFetchDate(Context context) {
        long currentMillis = System.currentTimeMillis();
        saveInSharedPref(currentMillis, context.getString(R.string.pref_last_sources_fetch_date), context);
        Log.d(TAG, "storeFetchDate: fetch date:" + currentMillis);
    }

    private void saveInSharedPref(long givenValue, String prefName, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                prefName,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(prefName, givenValue);
        editor.apply();
    }

    private long getFromSharedPref(String prefName, Context context) {
        final long defaultValue = -1;
        SharedPreferences sharedPref = context.getSharedPreferences(
                prefName,
                Context.MODE_PRIVATE);
        long result = sharedPref.getLong(prefName, defaultValue);
        return result;
    }

    private void loadSourcesFromRemoteDb(Context context, OnSourcesFetchedListener listener) {

        cachedSources = new ArrayList<>();
        db.collection(SOURCES_COLLECTION_NAME)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // testing code
//                            String name = (String) document.get(NAME);
//                            String testname = "lifehacker";
//                            if(enabled!=null && enabled && name.equals(testname)) {
//                                Source source = buildBasicSourceObject(document);
//                                if(source!=null)  {
//                                    cachedSources.add(source);
//                                }
//                            }

                            Source source = buildSource(document);
                            if(source!=null)  {
                                cachedSources.add(source);
                            }
                        }

                        //
                        storeFetchDate(context);
                        saveSourcesInRoom_async(cachedSources, context);

                        //
                        Collections.shuffle(cachedSources); // randomize collection
                        taskIsRunning = false;
                        listener.onSourcesFetchComplete(cachedSources);
                    } else {
                        if(task.getException()!=null) {
                            Log.w(TAG, "SCIENCE_BOARD - Error getting sources.", task.getException());
                            listener.onSourcesFetchFailded("Error getting sources." + task.getException().getMessage());
                        }
                    }
                });
    }

    private void saveSourcesInRoom_async(@NotNull List<Source> sources, Context context) {
        Runnable task = () -> {
            try {
                SourceDao dao = getSourceDao(context);
                dao.insertAll(sources);
                for(Source current: sources) {
                    dao.updateEnabledStatus(current.getEnabled(), current.getId());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "SCIENCE_BOARD - saveSourcesInRoom: cannot save sources in Room, cause:" + e.getMessage());
            }
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - saveSourcesInRoom: cannot start thread " + e.getMessage());
        }
    }

    private void getSourcesFromRoom_async(Context context, OnSourcesFetchedListener listener) {
        Runnable task = () -> {
            try {
                List<Source> result = null;
                SourceDao sourceDao = getSourceDao(context);
                result = sourceDao.selectAll();
                cachedSources = result;
                listener.onSourcesFetchComplete(result);
            } catch (Exception e) {
                Log.e(TAG, "SCIENCE_BOARD - getSourcesFromRoom_sync: cannot get sources from Room, cause:" + e.getMessage());
                listener.onSourcesFetchFailded("cannot get sources from Room, cause:" + e.getMessage());
            }
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - getSourcesFromRoom_sync: cannot start thread " + e.getMessage());
        }
    }

    public List<Source> getAsourceForEachFollowedCategory_randomly(List<Source> givenSources, List<Topic> topics) {
        List<Source> result = null;
        if(givenSources==null || givenSources.isEmpty()) return result;
        if(topics==null || topics.isEmpty()) return result;

        List<Source> allSources = new ArrayList<>(givenSources);
        Collections.shuffle(allSources);
        result = new ArrayList<>();
        for(int i=0; i < topics.size(); i++) {
            if(topics.get(i).getFollowed()) {
                Source source = extractTheFirstSourceBelongingToThisTopic(allSources, topics.get(i).getId());
                if(source!=null) {
                    result.add(source);
                    allSources.remove(source);
                }
            }
        }

        return result;
    }


    /**
     * NOTE:
     * if numSourcesToFetch > givenSources.size()
     * nothing will happen,
     * will be extracted only n<=givenSources.size() sources
     */
    public List<Source> getNsourcesForEachFollowedTopic_randomly(List<Source> givenSources,
                                                                 List<Topic> followedTopics,
                                                                 int numSourcesToFetch) {
        List<Source> result = null;
        if(givenSources==null || givenSources.isEmpty()) return null;
        if(followedTopics==null || followedTopics.isEmpty()) return null;
        if(numSourcesToFetch<=0) return null;

        // randomizing sources
        List<Source> randomizedSources = new ArrayList<>(givenSources);
        Collections.shuffle(randomizedSources);

        //
        result = new ArrayList<>();
        for(int i=0; i < followedTopics.size(); i++) { // for each topic, fetch N sources
            Topic currentTopic = followedTopics.get(i);
            for(int j=0; j<numSourcesToFetch; j++) { // counter to fetch n sources for each topic
                Source extractedSource = extractTheFirstSourceBelongingToThisTopic(randomizedSources, currentTopic.getId());
                if(extractedSource!=null) {
                    result.add(extractedSource);
                    // remove the source extracted to avoid duplications
                    randomizedSources.remove(extractedSource);
                }
            }
        }

        return result;
    }




    public List<Source> getCachedSources() {
        return cachedSources;
    }

    public List<Source> getAllSourcesOfThisCategory(List<Source> sources, String category) {
        List<Source> result = null;
        if(sources==null || sources.size()<=0) return result;
        if(category==null || category.isEmpty()) return result;

        result = new ArrayList<>();
        for(Source currentSource: sources) {
            if(sourcefallInThisCategory(currentSource, category)) {
                result.add(currentSource);
            }
        }

        return result;
    }






    //--------------------------------------------------------------------------- PRIVATE METHODS



    private Source buildSource(QueryDocumentSnapshot document) {
        Source source = null;
        if(document==null) return source;

        source = new Source();
        source.setId((String) document.getId());
        source.setRealName((String) document.get(REAL_NAME));
        source.setWebsiteUrl((String) document.get(WEBSITE_URL));
        source.setRssUrl((String) document.get(RSS_URL));
        source.setLanguage((String) document.get(LANGUAGE));
        List<String> categories = (List<String>) document.get(CATEGORY);
        source.setCategories(categories);
        Boolean sourceEnabled = (Boolean) document.get(ENABLED);
        if(sourceEnabled!=null) source.setEnabled(sourceEnabled);

        return source;
    }

    private static Source extractTheFirstSourceBelongingToThisTopic(List<Source> givenSources, String givenTopicName) {
        Source result = null;
        if(givenSources==null || givenSources.isEmpty()) return null;
        if(givenTopicName==null || givenTopicName.isEmpty()) return null;

        for(Source source: givenSources) {
            if(sourcefallInThisCategory(source, givenTopicName)) {
                result = source;
                break;
            }
        }

        return result;
    }

    private static boolean sourcefallInThisCategory(Source source, String category) {
        boolean result = false;
        if(source==null) return result;
        if(category==null || category.isEmpty()) return result;

        List<String> sourceCategories = source.getCategories();
        result = sourceCategories.contains(category);

        return result;
    }



    private SourceDao getSourceDao(Context context) {
        ScienceBoardRoomDatabase roomDatabase = ScienceBoardRoomDatabase.getInstance(context);
        return roomDatabase.getSourceDao();
    }


    public long getLastArticlesFetchDate_sync(String givenSourceId, Context context) {
        long result = 0;
        try {
            SourceDao sourceDao = getSourceDao(context);
            result = sourceDao.selectLastArticlesFetchDate(givenSourceId);
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - getLastArticlesFetchDate_sync: cannot get sources last articles fetch date from Room, cause:" + e.getMessage());
        }

        return result;
    }
}// end SourceRepository
