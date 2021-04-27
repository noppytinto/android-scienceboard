package com.nocorp.scienceboard.topics.repository;

import android.content.Context;
import android.util.Log;

import com.nocorp.scienceboard.topics.model.Topic;
import com.nocorp.scienceboard.rss.room.ScienceBoardRoomDatabase;
import com.nocorp.scienceboard.topics.room.TopicDao;
import com.nocorp.scienceboard.system.ThreadManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TopicRepository {
    private final String TAG = this.getClass().getSimpleName();
    private static List<Topic> cachedAllTopics;
    private static List<Topic> cachedFollowedTopics;



    //----------------------------------------------------------------------------------------- CONSTRUCTORS

    public TopicRepository() {
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
        cachedAllTopics = buildTopics_eventually();

        //
        saveTopicsInRoom(cachedAllTopics, context, listener);
    }


    /**
     * NOTE:
     * will always fetch some topics,
     * because in case of Room fail, will fallback in cachedTopics,
     * cached topics are at least populated on the first app launch by buildTopics_eventually()
     */
    public void fetchTopics(Context context, OnTopicsFetchedListener listener) {
        getTopicsFromRoom(context, listener);
    }

    /**
     * NOTE:
     * will create topics only if
     * the first time the app is launched,*
     */
    private List<Topic> buildTopics_eventually() {
        List<Topic> result = new ArrayList<>();
        result.add(buildTopic("space", "Space"));
        result.add(buildTopic("tech", "Tech"));
        result.add(buildTopic("physics", "Physics"));
        result.add(buildTopic("medicine", "Medicine"));
        result.add(buildTopic("biology", "Biology"));

        return result;
    }



    private Topic buildTopic(String id, String name) {
        return new Topic(id, name);
    }

    private void saveTopicsInRoom(@NotNull List<Topic> topics, Context context, OnTopicRepositoryInitilizedListener listener) {
        Runnable task = () -> {
            try {
                // NOTE: if a topic extist, will be ignored
                TopicDao dao = getTopicDao(context);
                dao.insertAll(topics);
                listener.onComplete();
            } catch (Exception e) {
                Log.e(TAG, "SCIENCE_BOARD - saveTopicsInRoom: cannot save in Room, cause:" + e.getMessage());
                // always try getting topics from room in case of insertAll fail
                listener.onFailded("cannot save in Room, cause: " + e.getMessage());
            }

        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - saveTopicsInRoom: cannot start thread " + e.getMessage());
            listener.onFailded("cannot save in Room, cause: " + e.getMessage());
        }
    }


    /**
     * NOTE:
     * will always fetch some topics,
     * because in case of Room fail, will fallback in cachedTopics,
     * cached topics are at least populated on the first app launch by buildTopics_eventually()
     */
    private void getTopicsFromRoom(Context context, OnTopicsFetchedListener listener) {
        Runnable task = () -> {
            try {
                // NOTE: if a topic extist, will be ignored
                TopicDao dao = getTopicDao(context);
                cachedAllTopics = dao.selectAll();
                listener.onComplete(cachedAllTopics);
            } catch (Exception e) {
                Log.e(TAG, "SCIENCE_BOARD - getTopicsFromRoom: cannot get topics from Room, cause:" + e.getMessage());
                listener.onFailed(e.getMessage(), cachedAllTopics);
            }
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - getTopicsFromRoom: cannot start thread " + e.getMessage());
            listener.onFailed(e.getMessage(), cachedAllTopics);
        }
    }

    private TopicDao getTopicDao(Context context) {
        ScienceBoardRoomDatabase roomDatabase = ScienceBoardRoomDatabase.getInstance(context);
        return roomDatabase.getTopicDao();
    }

    public static List<Topic> getCachedAllTopics() {
        return cachedAllTopics;
    }

    public void updateAll(List<Topic> topicsToUpdate, Context context, OnTopicRepositoryUpdatedListener listener) {
        TopicDao dao = getTopicDao(context);

        Runnable task = () -> {
            try {
                dao.updateAll(topicsToUpdate);

                // update cached topics
                cachedAllTopics = dao.selectAll();

                //
                listener.onComplete(cachedAllTopics);
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
