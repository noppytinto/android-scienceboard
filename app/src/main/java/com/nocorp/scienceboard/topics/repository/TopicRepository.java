package com.nocorp.scienceboard.topics.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.topics.model.Topic;
import com.nocorp.scienceboard.rss.room.ScienceBoardRoomDatabase;
import com.nocorp.scienceboard.topics.room.TopicDao;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.utility.MyUtilities;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TopicRepository {
    private final String TAG = this.getClass().getSimpleName();
    private static List<Topic> cachedAllTopics_enabled;
    private static List<Topic> fallbackTopics;
    private static List<Topic> followedTopics;
    private static List<Topic> cachedFollowedTopics;
    private FirebaseFirestore db;
    private final String TOPICS_COLLECTION_NAME = "topics";
    private final String ENABLED = "enabled";
    private final String DISPLAY_NAME_ENG = "display_name_eng";
    private final int FETCH_INTERVAL = 1; // in days



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


    //--------------------------------------------------------------------- METHODS
    /**
     * NOTE:
     * will always create some topics,
     * because in case of Room fail, will fallback in cachedTopics,
     * cached topics are at least populated on the first app launch by buildTopics_eventually()
     */
    public void init(Context context, OnTopicRepositoryInitilizedListener listener) {
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
        storeFetchDate(context);
        cachedAllTopics_enabled = new ArrayList<>();
        db.collection(TOPICS_COLLECTION_NAME)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Topic topic = buildTopic(document);
                            if (topic != null) cachedAllTopics_enabled.add(topic);
                        }

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

    public static List<Topic> getCachedAllTopics_enabled() {
        return cachedAllTopics_enabled;
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
