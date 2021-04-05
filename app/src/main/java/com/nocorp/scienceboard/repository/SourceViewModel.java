package com.nocorp.scienceboard.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.nocorp.scienceboard.model.Source;

import java.util.List;

public class SourceViewModel extends ViewModel implements SourceRepositoryListener {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<List<String>> rssUrls;
    private MutableLiveData<List<Source>> sources;
    private SourceRepository sourceRepository;


    //------------------------------------------------------------ CONSTRUCTORS

    public SourceViewModel() {
        rssUrls = new MutableLiveData<>();
        sources = new MutableLiveData<>();
        sourceRepository = new SourceRepository(this);
    }



    //------------------------------------------------------------ GETTERS/SETTERS

    public LiveData<List<String>> getObservableRssUrls() {
        return rssUrls;
    }

    public LiveData<List<Source>> getObservableSources() {
        return sources;
    }

    public void setRssUrls(List<String> rssUrls) {
        this.rssUrls.postValue(rssUrls);
    }

    public void setSources(List<Source> sources) {
        this.sources.postValue(sources);
    }





    //------------------------------------------------------------ METHODS


    @Override
    public void onSourcesFetchCompleted(List<Source> sources) {
        if(sources != null && sources.size()>0) {
            setSources(sources);
            Log.d(TAG, "SCIENCE_BOARD - onSourcesFetchCompleted: sources fetched from remote db");
        }
        else {
            setSources(null);
            Log.d(TAG, "SCIENCE_BOARD - onSourcesFetchCompleted: sources list is empty");
        }
    }

    @Override
    public void onSourcesFetchFailed(String cause) {
        setSources(null);
        Log.d(TAG, "SCIENCE_BOARD - onSourcesFetchFailed: sources fetching failed" + cause);
    }


    public void loadSourcesFromRemoteDb() {
        sourceRepository.loadSources();
    }



}// end SourceViewModel
