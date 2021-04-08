package com.nocorp.scienceboard.repository;

import com.nocorp.scienceboard.model.Source;
import java.util.List;

public interface SourceRepositoryListener {
    public void onAllSourcesFetchCompleted(List<Source> sources);
    public void onAllSourcesFetchFailed(String cause);
    public void onTechSourcesFetchCompleted(List<Source> sources);
    public void onTechSourcesFetchFailed(String cause);
}
