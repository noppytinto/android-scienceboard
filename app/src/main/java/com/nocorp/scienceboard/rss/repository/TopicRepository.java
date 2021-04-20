package com.nocorp.scienceboard.rss.repository;

import android.content.Context;
import android.util.Log;

import com.nocorp.scienceboard.model.Topic;
import com.nocorp.scienceboard.rss.room.ScienceBoardRoomDatabase;
import com.nocorp.scienceboard.rss.room.TopicDao;
import com.nocorp.scienceboard.system.ThreadManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TopicRepository {
    private final String TAG = this.getClass().getSimpleName();
    private TopicRepositoryListener listener;

    public TopicRepository(TopicRepositoryListener listener) {
        this.listener = listener;
    }



    public void fetchTopics(Context context) {
        buildTopics(context);
    }

    private void buildTopics(Context context) {
        List<Topic> result = new ArrayList<>();
        result.add(buildTopic("Space"));
        result.add(buildTopic("Tech"));
        result.add(buildTopic("Physics"));
        result.add(buildTopic("Medicine"));
        result.add(buildTopic("Biology"));

        saveTopicsInRoom(result, context);
    }


    private Topic buildTopic(String name) {
        Topic result = new Topic();
        result.setName(name);
        result.setFollowed(true);

        return result;
    }

    private void saveTopicsInRoom(@NotNull List<Topic> topics, Context context) {
        TopicDao dao = getTopicDao(context);

        Runnable task = () -> {
            try {
                dao.insertAll(topics);
                listener.onTopicsFetchCompleted(topics);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "SCIENCE_BOARD - saveTopicsInRoom: cannot save in Room, cause:" + e.getMessage());
                listener.onTopicsFetchFailed(e.getMessage());
            }
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "SCIENCE_BOARD - saveTopicsInRoom: cannot start thread " + e.getMessage());
        }
    }

    private TopicDao getTopicDao(Context context) {
        ScienceBoardRoomDatabase roomDatabase = ScienceBoardRoomDatabase.getInstance(context);
        return roomDatabase.getTopicDao();
    }

}// end TopicRepository
