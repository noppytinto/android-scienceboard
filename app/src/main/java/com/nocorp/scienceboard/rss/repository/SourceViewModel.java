package com.nocorp.scienceboard.rss.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.nocorp.scienceboard.model.Source;

import java.util.List;

public class SourceViewModel extends AndroidViewModel implements SourceRepositoryListener {
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
        sourceRepository = new SourceRepository(this);
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

    public void onAllSourcesFetchCompleted(List<Source> sources) {
        if(sources != null && sources.size()>0) {
            setAllSources(sources);
            Log.d(TAG, "SCIENCE_BOARD - onAllSourcesFetchCompleted: sources fetched from remote db");
        }
        else {
            setAllSources(null);
            Log.d(TAG, "SCIENCE_BOARD - onAllSourcesFetchCompleted: sources list is empty");
        }
    }

    @Override
    public void onAllSourcesFetchFailed(String cause) {
        setAllSources(null);
        Log.d(TAG, "SCIENCE_BOARD - onAllSourcesFetchFailed: sources fetching failed" + cause);
    }




    public void loadSourcesFromRemoteDb() {
        sourceRepository.loadSources(getApplication());
    }






    @Override
    public void onTechSourcesFetchCompleted(List<Source> sources) {
        if(sources != null && sources.size()>0) {
            setAllSources(sources);
            Log.d(TAG, "SCIENCE_BOARD - onTechSourcesFetchCompleted: sources fetched from remote db");
        }
        else {
            setAllSources(null);
            Log.d(TAG, "SCIENCE_BOARD - onTechSourcesFetchCompleted: sources list is empty");
        }
    }

    @Override
    public void onTechSourcesFetchFailed(String cause) {
        setAllSources(null);
        Log.d(TAG, "SCIENCE_BOARD - onTechSourcesFetchFailed: sources fetching failed" + cause);
    }


}// end SourceViewModel
