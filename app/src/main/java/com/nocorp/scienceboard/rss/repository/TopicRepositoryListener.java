package com.nocorp.scienceboard.rss.repository;

import com.nocorp.scienceboard.model.Topic;

import java.util.List;

public interface TopicRepositoryListener {
    void onTopicsFetchCompleted(List<Topic> topics);
    void onTopicsFetchFailed(String cause);

}
