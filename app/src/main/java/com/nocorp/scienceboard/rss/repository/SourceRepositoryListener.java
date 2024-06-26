package com.nocorp.scienceboard.rss.repository;

import com.nocorp.scienceboard.model.Source;
import java.util.List;

public interface SourceRepositoryListener {
    public void onAllSourcesFetchCompleted(List<Source> sources);
    public void onAllSourcesFetchFailed(String cause);
}
