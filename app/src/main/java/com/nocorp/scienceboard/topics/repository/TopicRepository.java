package com.nocorp.scienceboard.topics.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.annotations.Since;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.topics.model.Topic;
import com.nocorp.scienceboard.rss.room.ScienceBoardRoomDatabase;
import com.nocorp.scienceboard.topics.room.TopicDao;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.utility.MyUtilities;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class TopicRepository {
    private final String TAG = this.getClass().getSimpleName();
    private final String APP_NAME = "NOPPYS_BOARD - ";
    private static List<Topic> cachedAllTopics_enabled;
    private static List<Topic> fallbackTopics;
    private static List<Topic> followedTopics;
    private static List<Topic> cachedFollowedTopics;
    private FirebaseFirestore db;
    private final String TOPICS_COLLECTION_NAME = "topics";
    private final String ENABLED = "enabled";
    private final String DISPLAY_NAME_ENG = "display_name_eng";
    private final int FETCH_INTERVAL = 1; // in days


    private static boolean repositoryInitilized = false;


    //----------------------------------------------------------------------------------------- CONSTRUCTORS

    public TopicRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static void setFollowedTopics(List<Topic> followedTopics) {
        TopicRepository.followedTopics = followedTopics;
    }

    public static List<Topic> getFollowedTopics() {
        return followedTopics;
    }

    public List<Topic> getFollowedTopics_sync(Context context) {
        List<Topic> result = new ArrayList<>();

        if(followedTopics==null) {
            try {
                // NOTE: if a topic extist, will be ignored...
                TopicDao dao = getTopicDao(context);
                result = dao.selectFollowedTopics();
            } catch (Exception e) {
                Log.e(TAG, APP_NAME + "getFollowedTopics_sync: cannot get followed topics from Room, cause:" + e.getMessage());
            }
        }
        else {
            result = followedTopics;
        }

        return result;
    }


    //--------------------------------------------------------------------- METHODS
    /**
     * NOTE:
     * will always create some topics,
     * because in case of Room fail, will fallback in cachedTopics,
     * cached topics are at least populated on the first app launch by buildTopics_eventually()
     */
    public void init(Context context, OnTopicRepositoryInitilizedListener listener) {
        if(repositoryInitilized) {
            // ignore
        }
        else {
            //
            repositoryInitilized = true;

            // (will be relevant only on the first app launch)
            fallbackTopics = buildFallbackTopicsList();

            // if the request is within 1 our
            // then use cached sources from local variable or Room
            long lastFetchDate = getFromSharedPref(context.getString(R.string.pref_last_topics_fetch_date), context);
            Log.d(TAG, "init topics list: lastFetchDate: " + lastFetchDate);
            if(MyUtilities.isWithin_days(FETCH_INTERVAL, lastFetchDate)) {
                fetchLocally_strategy(context, listener);
            }
            //
            else {
                fetchRemotely_strategy(context, listener);
            }
        }
    }













    //------------------------------------------------------------------------------------ RXJAVA

//    public Single<List<Topic>> fetchTopics_rxjava(Context context) {
//        return Single.create(emitter -> {
//            try {
//                // (will be relevant only on the first app launch)
//                fallbackTopics = buildFallbackTopicsList();
//
//                // if the request is within 1 our
//                // then use cached sources from local variable or Room
//                long lastFetchDate = getFromSharedPref(context.getString(R.string.pref_last_topics_fetch_date), context);
//                Log.d(TAG, "init topics list: lastFetchDate: " + lastFetchDate);
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




    public Single<List<Topic>> fetchLocally_strategy_rxjava(Context context) {
        return Single.create(emitter -> {
            List<Topic> result;
            if(cachedAllTopics_enabled==null) {
                Log.d(TAG, "fetchLocally_strategy_rxjava: loading topics from room");
                result = getTopicsFromRoom_sync_rxjava(context);
                cachedAllTopics_enabled = result;
                if(result==null || result.isEmpty()) {
                    emitter.onSuccess(new ArrayList<>());
                }
                else {
                    emitter.onSuccess(result);
                }
            }
            else {
                Log.d(TAG, "fetchLocally_strategy_rxjava: loading topics from cachedAllTopics_enabled");
                emitter.onSuccess(cachedAllTopics_enabled);
            }
        });
    }


    /**
     * NOTE:
     * will always fetch some topics,
     * because in case of Room fail, will fallback in cachedTopics,
     * cached topics are at least populated on the first app launch by buildTopics_eventually()
     */
    private List<Topic> getTopicsFromRoom_sync_rxjava(Context context) {
        List<Topic> result = new ArrayList<>();
        try {
            // NOTE: if a topic extist, will be ignored
            TopicDao dao = getTopicDao(context);
            result = dao.selectAll();
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - getTopicsFromRoom_sync_rxjava: cannot get topics from Room, cause:" + e.getMessage());
        }

        return result;
    }

    public Single<List<Topic>> fetchRemotely_strategy_rxjava(Context context) {
        return Single.create(emitter -> {
            Log.d(TAG, "fetchRemotely_strategy_rxjava: loading topics remotely");
            cachedAllTopics_enabled = new ArrayList<>();
            db.collection(TOPICS_COLLECTION_NAME)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Topic topic = buildTopic(document);
                                if (topic != null) cachedAllTopics_enabled.add(topic);
                            }

                            storeFetchDate(context);

                            Log.d(TAG, "getTopicsFromRemoteDb_rxjava: topics fetched from remote db");
                        }
                        else {
                            if(task.getException()!=null) {
                                Log.w(TAG, "SCIENCE_BOARD - getTopicsFromRemoteDb_rxjava: Error getting topics.", task.getException());
                            }
                            cachedAllTopics_enabled = fallbackTopics;
                        }
                        Log.d(TAG, "getTopicsFromRemoteDb_rxjava: completed");
                        emitter.onSuccess(cachedAllTopics_enabled);
                    });
        });
    }




    public Single<List<Topic>> saveFetchedTopicsInRoom_rxjava(List<Topic> topics, Context context) {
        return Single.create(emitter -> {
            try {
                Log.d(TAG, "saveTopicsInRoom_rxjava: called");
                saveTopicsInRoom_sync(topics, context);
                emitter.onSuccess(topics);
            } catch (Exception ex) {
                emitter.onError(ex);
            }
        });
    }


    public Single<List<Topic>> getUpdatedTopicsFromRoom_sync_rxjava(Context context) {
        return Single.create(emitter -> {
            try {
                Log.d(TAG, "getTopicsFromRoom_sync_rxjava: called");
                // NOTE: if a topic extist, will be ignored
                TopicDao dao = getTopicDao(context);
                cachedAllTopics_enabled = dao.selectAll();
                emitter.onSuccess(cachedAllTopics_enabled);
            } catch (Exception e) {
                Log.e(TAG, "SCIENCE_BOARD - getTopicsFromRoom_sync_rxjava: cannot get topics from Room, cause:" + e.getMessage());
                emitter.onError(e);
            }
        });
    }














    //------------------------------------------------------------------------------------



    private void fetchRemotely_strategy(Context context, OnTopicRepositoryInitilizedListener listener) {
        Log.d(TAG, "fetchRemotely_strategy: loading topics remotely");
        getTopicsFromRemoteDb(context, listener);
    }



    private void fetchLocally_strategy(Context context, OnTopicRepositoryInitilizedListener listener) {
        if(cachedAllTopics_enabled==null) {
            Log.d(TAG, "fetchLocally_strategy: loading topics from room");
            getTopicsFromRoom_async(context, listener);
        }
        else {
            Log.d(TAG, "fetchLocally_strategy: loading topics from cachedAllTopics_enabled");
            listener.onComplete();
        }
    }




    /**
     * NOTE:
     * will always fetch some topics,
     * because in case of Room fail, will fallback in cachedTopics,
     * cached topics are at least populated on the first app launch by buildTopics_eventually()
     */
    public void fetchTopics(Context context, OnTopicsFetchedListener listener) {
        getTopicsFromRoom_async(context, listener);
//        getTopicsFromRemoteDb(context, listener);
    }



    private void storeFetchDate(Context context) {
        long currentMillis = System.currentTimeMillis();
        saveInSharedPref(currentMillis, context.getString(R.string.pref_last_topics_fetch_date), context);
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

    private void getTopicsFromRemoteDb(Context context, OnTopicRepositoryInitilizedListener listener) {
        cachedAllTopics_enabled = new ArrayList<>();
        db.collection(TOPICS_COLLECTION_NAME)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Topic topic = buildTopic(document);
                            if (topic != null) cachedAllTopics_enabled.add(topic);
                        }

                        storeFetchDate(context);

                        Log.d(TAG, "getTopicsFromRemoteDb: topics fetched from remote db");
                    }
                    else {
                        if(task.getException()!=null) {
                            Log.w(TAG, "SCIENCE_BOARD - Error getting topics.", task.getException());
                            listener.onFailed("Error getting topics." + task.getException().getMessage());
                        }
                        cachedAllTopics_enabled = fallbackTopics;
                    }

                    saveTopicsInRoom_async(cachedAllTopics_enabled, context, listener);

                });
    }



    /**
     * NOTE:
     * will create topics only if
     * the first time the app is launched,*
     */
    private List<Topic> buildFallbackTopicsList() {
        List<Topic> result = new ArrayList<>();
        result.add(buildTopic("space", "Space"));
        result.add(buildTopic("tech", "Tech"));
        result.add(buildTopic("physics", "Physics"));
        result.add(buildTopic("medicine", "Medicine"));
        result.add(buildTopic("biology", "Biology"));
        result.add(buildTopic("fakenews", "Fake News"));
        result.add(buildTopic("economy", "Economy"));
        result.add(buildTopic("geology", "Geology"));
        result.add(buildTopic("gossip", "Gossip"));
        result.add(buildTopic("health", "Health"));
        result.add(buildTopic("medicine", "Medicine"));
        result.add(buildTopic("music", "Music"));
        result.add(buildTopic("news", "News"));
        result.add(buildTopic("politics", "Politics"));
        result.add(buildTopic("sport", "Sport"));
        return result;
    }



    private Topic buildTopic(String id, String name) {
        return new Topic(id, name);
    }


    private Topic buildTopic(DocumentSnapshot document) {
        Topic topic = null;
        if(document==null) return topic;

        try {
            topic = new Topic();
            topic.setId((String) document.getId());
            topic.setDisplayName((String) document.get(DISPLAY_NAME_ENG));
            Boolean enabled = (Boolean) document.get(ENABLED);
            if(enabled!=null) topic.setEnabled(enabled);
        } catch (Exception e) {
            Log.e(TAG, "buildTopic: ", e);
            topic = null;
        }

        return topic;
    }


    private void saveTopicsInRoom_async(@NotNull List<Topic> topics, Context context, OnTopicRepositoryInitilizedListener listener) {
        if(topics==null) return;
        Runnable task = () -> {
            try {
                // NOTE: if a topic extist, will be ignored...
                TopicDao dao = getTopicDao(context);
                dao.insertAll(topics);
                // ...but update enabled status
                for(Topic current: topics) {
                    dao.updateEnabledStatus(current.getEnabled(), current.getId());
                }

                listener.onComplete();
            } catch (Exception e) {
                Log.e(TAG, "SCIENCE_BOARD - saveTopicsInRoom: cannot save in Room, cause:" + e.getMessage());
                // always try getting topics from room in case of insertAll fail
                listener.onFailed("cannot save in Room, cause: " + e.getMessage());
            }

        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - saveTopicsInRoom: cannot start thread " + e.getMessage());
            listener.onFailed("cannot save in Room, cause: " + e.getMessage());
        }
    }


    private void saveTopicsInRoom_sync(List<Topic> topics, Context context) {
        if(topics==null) return;

        try {
            // NOTE: if a topic extist, will be ignored...
            TopicDao dao = getTopicDao(context);
            dao.insertAll(topics);
            // ...but update enabled status
            for(Topic current: topics) {
                dao.updateEnabledStatus(current.getEnabled(), current.getId());
            }
            Log.d(TAG, "saveTopicsInRoom_sync: topics saved in Room");
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - saveTopicsInRoom_sync: cannot save in Room, cause:" + e.getMessage());
            // always try getting topics from room in case of insertAll fail
        }
    }


    /**
     * NOTE:
     * will always fetch some topics,
     * because in case of Room fail, will fallback in cachedTopics,
     * cached topics are at least populated on the first app launch by buildTopics_eventually()
     */
    private void getTopicsFromRoom_async(Context context, OnTopicsFetchedListener listener) {
        Runnable task = () -> {
            try {
                // NOTE: if a topic extist, will be ignored
                TopicDao dao = getTopicDao(context);
                cachedAllTopics_enabled = dao.selectAll();
                listener.onComplete(cachedAllTopics_enabled);
            } catch (Exception e) {
                Log.e(TAG, "SCIENCE_BOARD - getTopicsFromRoom_async: cannot get topics from Room, cause:" + e.getMessage());
                listener.onFailed(e.getMessage(), cachedAllTopics_enabled);
            }
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - getTopicsFromRoom_async: cannot start thread " + e.getMessage());
            listener.onFailed(e.getMessage(), cachedAllTopics_enabled);
        }
    }

    /**
     * NOTE:
     * will always fetch some topics,
     * because in case of Room fail, will fallback in cachedTopics,
     * cached topics are at least populated on the first app launch by buildTopics_eventually()
     */
    private void getTopicsFromRoom_sync(Context context) {
        try {
            // NOTE: if a topic extist, will be ignored
            TopicDao dao = getTopicDao(context);
            cachedAllTopics_enabled = dao.selectAll();
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - getTopicsFromRoom_async: cannot get topics from Room, cause:" + e.getMessage());
        }
    }


    private void getTopicsFromRoom_async(Context context, OnTopicRepositoryInitilizedListener listener) {
        Runnable task = () -> {
            try {
                // NOTE: if a topic extist, will be ignored
                TopicDao dao = getTopicDao(context);
                cachedAllTopics_enabled = dao.selectAll();
                listener.onComplete();
            } catch (Exception e) {
                Log.e(TAG, "SCIENCE_BOARD - getTopicsFromRoom_async: cannot get topics from Room, cause:" + e.getMessage());
                listener.onFailed(e.getMessage());
            }
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - getTopicsFromRoom_async: cannot start thread " + e.getMessage());
            listener.onFailed(e.getMessage());
        }
    }


    private TopicDao getTopicDao(Context context) {
        ScienceBoardRoomDatabase roomDatabase = ScienceBoardRoomDatabase.getInstance(context);
        return roomDatabase.getTopicDao();
    }

    public static List<Topic> getAllEnabledTopics_cached() {
        return cachedAllTopics_enabled;
    }

    public static List<Topic> getFollowedTopics_fromCached() {
        List<Topic> result = null;
        if(cachedAllTopics_enabled==null || cachedAllTopics_enabled.isEmpty()) return result;

        result = new ArrayList<>();

        for (Topic topic: cachedAllTopics_enabled) {
            if(topic.getFollowed() == true && topic.getEnabled()) {
                result.add(topic);
            }
        }

        followedTopics = new ArrayList<>(result);

        return result;
    }


    public void updateAll(List<Topic> topicsToUpdate, Context context, OnTopicRepositoryUpdatedListener listener) {
        TopicDao dao = getTopicDao(context);

        Runnable task = () -> {
            try {
                dao.updateAll(topicsToUpdate);

                // update cached topics
                cachedAllTopics_enabled = dao.selectAll();

                //
                listener.onComplete(cachedAllTopics_enabled);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "SCIENCE_BOARD - follow: cannot update topics in Room, cause:" + e.getMessage());
                listener.onFailed(e.getMessage());
            }
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "SCIENCE_BOARD - follow: cannot start thread " + e.getMessage());
            listener.onFailed(e.getMessage());
        }
    }



}// end TopicRepository
