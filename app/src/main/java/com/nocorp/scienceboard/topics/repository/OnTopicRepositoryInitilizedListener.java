package com.nocorp.scienceboard.topics.repository;

public interface OnTopicRepositoryInitilizedListener {
    void onComplete();
    void onFailed(String message);
}
