package com.nocorp.scienceboard.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.nocorp.scienceboard.model.Source;

import java.util.List;

public class SourceViewModel extends ViewModel implements SourcesFetcher{
    private MutableLiveData<List<String>> rssUrls;
    private MutableLiveData<List<Source>> sources;


    //------------------------------------------------------------ CONSTRUCTORS

    public SourceViewModel() {
        rssUrls = new MutableLiveData<>();
        sources = new MutableLiveData<>();
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

    }

    @Override
    public void onSourcesFetchFailed(String cause) {

    }










}// end SourceViewModel
