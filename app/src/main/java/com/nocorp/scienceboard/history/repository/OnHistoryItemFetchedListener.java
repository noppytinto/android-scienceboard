package com.nocorp.scienceboard.history.repository;

import com.nocorp.scienceboard.history.model.HistoryArticle;

public interface OnHistoryItemFetchedListener {
    void onComplete(HistoryArticle historyArticle);
    void onFailed(String cause);
}
