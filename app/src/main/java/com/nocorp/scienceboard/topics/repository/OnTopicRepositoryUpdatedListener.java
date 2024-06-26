package com.nocorp.scienceboard.topics.repository;

import com.nocorp.scienceboard.topics.model.Topic;

import java.util.List;

public interface OnTopicRepositoryUpdatedListener {
    void onComplete(List<Topic> newTopicsList);
    void onFailed(String cause);
}
