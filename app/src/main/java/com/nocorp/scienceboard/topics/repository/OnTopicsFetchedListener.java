package com.nocorp.scienceboard.topics.repository;

import com.nocorp.scienceboard.topics.model.Topic;

import java.util.List;

public interface OnTopicsFetchedListener {
    void onComplete(List<Topic> fetchedTopics);
    void onFailed(String cause, List<Topic> cachedTopics);
}
