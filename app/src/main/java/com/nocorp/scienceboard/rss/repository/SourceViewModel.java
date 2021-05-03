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
    private MutableLiveData<List<Source>> allSources;
    private SourceRepository sourceRepository;


    //------------------------------------------------------------ CONSTRUCTORS

    public SourceViewModel(Application application) {
        super(application);
        allSources = new MutableLiveData<>();
        sourceRepository = new SourceRepository();
    }



    //------------------------------------------------------------ GETTERS/SETTERS

    public LiveData<List<Source>> getObservableAllSources() {
        return allSources;
    }

    public void setAllSources(List<Source> allSources) {
        this.allSources.postValue(allSources);
    }






    //------------------------------------------------------------ METHODS

    public void loadSourcesFromRemoteDb() {
        sourceRepository.fetchSources(getApplication(), new OnSourcesFetchedListener() {
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
