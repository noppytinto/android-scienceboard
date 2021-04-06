package com.nocorp.scienceboard.repository;

import com.nocorp.scienceboard.ui.viewholder.ListItem;

import java.util.List;

public interface HistoryRepositoryListener {
    public void onHistoryFetchCompleted(List<ListItem> articles);
    public void onHistoryFetchFailed(String cause);
}
