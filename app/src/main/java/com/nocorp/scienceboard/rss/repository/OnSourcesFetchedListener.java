package com.nocorp.scienceboard.rss.repository;

import com.nocorp.scienceboard.model.Source;

import java.util.List;

public interface OnSourcesFetchedListener {
    void onSourcesFetchComplete(List<Source> fetchedSources);
    void onSourcesFetchFailded(String message);
}
