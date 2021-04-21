package com.nocorp.scienceboard.topics.repository;

import com.nocorp.scienceboard.topics.model.Topic;

import java.util.List;

public interface TopicRepositoryListener {
    void onTopicsFetchCompleted(List<Topic> topics);
    void onTopicsFetchFailed(String cause);
    void onTopicsUpdateCompleted(List<Topic> topics);
    void onTopicsUpdateFailed(String cause);
}
