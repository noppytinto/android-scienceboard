package com.nocorp.scienceboard.ui.home;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nocorp.scienceboard.topics.model.Topic;
import com.nocorp.scienceboard.topics.repository.OnTopicsFetchedListener;
import com.nocorp.scienceboard.topics.repository.TopicRepository;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<List<Topic>> topicsList;
    private TopicRepository topicRepository;



    //-------------------------------------------------------------------------------------------- CONSTRUCTORS

    public HomeViewModel(Application application) {
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
                // fallback in cached topics if room in unavalaible
                setTopicsList(cachedTopics);
                Log.e(TAG, "SCIENCE_BOARD - fetchTopics: cannot fetch topics, cause:" + message);

            }
        });
    }




}// end HomeViewModel