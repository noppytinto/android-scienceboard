package com.nocorp.scienceboard.ui.topics;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nocorp.scienceboard.topics.model.Topic;
import com.nocorp.scienceboard.topics.repository.OnTopicRepositoryUpdatedListener;
import com.nocorp.scienceboard.topics.repository.OnTopicsFetchedListener;
import com.nocorp.scienceboard.topics.repository.TopicRepository;

import java.util.List;

public class TopicsViewModel extends AndroidViewModel {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<List<Topic>> topicsList;
    private MutableLiveData<Boolean> customizationStatus;

    // repository
    private TopicRepository topicRepository;

    //
    private static boolean taskIsRunning;
    private static List<Topic> cachedTopics;



    //-------------------------------------------------------------------------------------------- CONSTRUCTORS

    public TopicsViewModel(Application application) {
        super(application);
        topicsList = new MutableLiveData<>();
        customizationStatus = new MutableLiveData<>();
        topicRepository = new TopicRepository();
    }



    //-------------------------------------------------------------------------------------------- GETTERS/SETTERS

    public LiveData<List<Topic>> getObservableTopicsList() {
        return topicsList;
    }

    public void setTopicsList(List<Topic> topicsList) {
        this.topicsList.postValue(topicsList);
    }


    public LiveData<Boolean> getObservableCustomizationStatus() {
        return customizationStatus;
    }

    public void setCustomizationStatus(Boolean customizationStatus) {
        this.customizationStatus.postValue(customizationStatus);
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

    public void updateTopicsFollowStatus(List<Topic> topicsToUpdate) {
        if(topicsToUpdate==null) {
        }
        else if(topicsToUpdate.isEmpty()) {

        }
        else {
            topicRepository.updateAll(topicsToUpdate, getApplication(), new OnTopicRepositoryUpdatedListener() {
                @Override
                public void onComplete(List<Topic> newTopicsList) {
                    setCustomizationStatus(true);
                }

                @Override
                public void onFailed(String cause) {
                    setCustomizationStatus(false);

                }
            });
        }
    }

}// end TopicsViewModel