package com.nocorp.scienceboard.ui.topics;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nocorp.scienceboard.model.Topic;
import com.nocorp.scienceboard.rss.repository.TopicRepository;
import com.nocorp.scienceboard.rss.repository.TopicRepositoryListener;
import com.nocorp.scienceboard.ui.viewholder.ListItem;

import java.util.List;

public class TopicsViewModel extends AndroidViewModel implements TopicRepositoryListener {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<List<Topic>> topicsList;
    private TopicRepository topicRepository;

    //
    private static boolean taskIsRunning;
    private static List<Topic> cachedTopics;



    //-------------------------------------------------------------------------------------------- CONSTRUCTORS

    public TopicsViewModel(Application application) {
        super(application);
        topicsList = new MutableLiveData<>();
        topicRepository = new TopicRepository(this);
    }



    //-------------------------------------------------------------------------------------------- GETTERS/SETTERS

    public LiveData<List<Topic>> getObservableTopicsList() {
        return topicsList;
    }

    public void setTopicsList(List<Topic> topicsList) {
        this.topicsList.postValue(topicsList);
    }




    //-------------------------------------------------------------------------------------------- METHODS

    public void fetchTopics() {
        topicRepository.fetchTopics(getApplication());
    }

    @Override
    public void onTopicsFetchCompleted(List<Topic> topics) {
        setTopicsList(topics);
    }

    @Override
    public void onTopicsFetchFailed(String cause) {
        Log.e(TAG, "SCIENCE_BOARD - onTopicsFetchFailed: cannot fetch topics, cause:" + cause);
        setTopicsList(null);
    }


    public void follow(String topicName) {
        topicRepository.follow(topicName, getApplication());
    }

    public void unfollow(String topicName) {
        topicRepository.unfollow(topicName, getApplication());
    }

    public void updateTopicsFollowStatus(List<Topic> topicsToUpdate) {
        if(topicsToUpdate!=null && !topicsToUpdate.isEmpty()) {
            topicRepository.updateAll(topicsToUpdate, getApplication());
        }
    }
}// end TopicsViewModel