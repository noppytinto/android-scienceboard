package com.nocorp.scienceboard.repository;

import com.nocorp.scienceboard.model.Source;
import java.util.List;

public interface SourcesFetcher {
    public void onSourcesFetchCompleted(List<Source> sources);
    public void onSourcesFetchFailed(String cause);
}
