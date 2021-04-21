package com.nocorp.scienceboard.rss.repository;

import com.nocorp.scienceboard.model.Source;

import java.util.List;

public interface OnSourcesFetchedListener {
    void onComplete(List<Source> fetchedSources);
    void onFailded(String message);
}
