package com.nocorp.scienceboard.repository;

import com.nocorp.scienceboard.ui.viewholder.ListItem;

import java.util.List;

public interface BookmarkRepositoryListener {
    public void onBookmarksFetchCompleted(List<ListItem> articles);
    public void onBookmarksFetchFailed(String cause);
    public void onBookmarksDuplicationCheckCompleted(boolean result);
    public void onBookmarksDuplicationCheckFailed(String cause);

}
