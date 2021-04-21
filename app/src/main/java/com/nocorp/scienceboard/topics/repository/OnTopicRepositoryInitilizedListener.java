package com.nocorp.scienceboard.topics.repository;

public interface OnTopicRepositoryInitilizedListener {
    void onComplete();
    void onFailded(String message);
}
