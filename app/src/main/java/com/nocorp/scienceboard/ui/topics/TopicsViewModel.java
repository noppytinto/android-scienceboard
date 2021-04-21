package com.nocorp.scienceboard.ui.topics;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nocorp.scienceboard.topics.model.Topic;
import com.nocorp.scienceboard.topics.repository.OnTopicsFetchedListener;
import com.nocorp.scienceboard.topics.repository.TopicRepository;
import com.nocorp.scienceboard.topics.repository.TopicRepositoryListener;

import java.util.List;

public class TopicsViewModel extends AndroidViewModel {
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
        topicRepository = new TopicRepository();
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
        topicRepository.fetchTopics(getApplication(), new OnTopicsFetchedListener() {
            @Override
            public void onComplete(List<Topic> fetchedTopics) {
                setTopicsList(fetchedTopics);
            }

            @Override
            public void onFailed(String message, List<Topic> cachedTopics) {
                setTopicsList(cachedTopics);
                Log.e(TAG, "SCIENCE_BOARD - fetchTopics: cannot fetch topics, cause:" + message);

            }
        });
    }


}// end TopicsViewModel