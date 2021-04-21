package com.nocorp.scienceboard.rss.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.nocorp.scienceboard.model.Source;

import java.util.List;

public class SourceViewModel extends AndroidViewModel {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<List<String>> rssUrls;
    private MutableLiveData<List<Source>> allSources;
    private SourceRepository sourceRepository;
    private MutableLiveData<List<Source>> techSources;


    //------------------------------------------------------------ CONSTRUCTORS

    public SourceViewModel(Application application) {
        super(application);
        rssUrls = new MutableLiveData<>();
        allSources = new MutableLiveData<>();
        techSources = new MutableLiveData<>();
        sourceRepository = new SourceRepository();
    }



    //------------------------------------------------------------ GETTERS/SETTERS

    public LiveData<List<String>> getObservableRssUrls() {
        return rssUrls;
    }


    public void setRssUrls(List<String> rssUrls) {
        this.rssUrls.postValue(rssUrls);
    }

    public LiveData<List<Source>> getObservableAllSources() {
        return allSources;
    }

    public void setAllSources(List<Source> allSources) {
        this.allSources.postValue(allSources);
    }

    public LiveData<List<Source>> getObservableTechSources() {
        return techSources;
    }

    public void setTechSources(List<Source> allSources) {
        this.techSources.postValue(allSources);
    }






    //------------------------------------------------------------ METHODS

    public void loadSourcesFromRemoteDb() {
        sourceRepository.loadSources(getApplication(), new OnSourcesFetchedListener() {
            @Override
            public void onComplete(List<Source> fetchedSources) {
                setAllSources(fetchedSources);
                Log.d(TAG, "SCIENCE_BOARD - loadSourcesFromRemoteDb: sources fetched from remote db");
            }

            @Override
            public void onFailded(String message) {
                setAllSources(null);
                Log.d(TAG, "SCIENCE_BOARD - loadSourcesFromRemoteDb: sources list is empty");
            }
        });
    }






}// end SourceViewModel
