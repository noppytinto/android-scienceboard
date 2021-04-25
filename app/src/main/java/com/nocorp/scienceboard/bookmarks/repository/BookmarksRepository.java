package com.nocorp.scienceboard.bookmarks.repository;

import android.content.Context;
import android.util.Log;

import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.bookmarks.model.BookmarkArticle;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.bookmarks.room.BookmarkDao;
import com.nocorp.scienceboard.rss.room.ScienceBoardRoomDatabase;
import com.nocorp.scienceboard.utility.MyValues;

import java.util.ArrayList;
import java.util.List;

public class BookmarksRepository {
    private final String TAG = this.getClass().getSimpleName();
    private BookmarkRepositoryListener listener;



    //------------------------------------------------------------ CONSTRUCTORS
    public BookmarksRepository() {

    }

    public BookmarksRepository(BookmarkRepositoryListener listener) {
        this.listener = listener;
    }



    //------------------------------------------------------------ GETTERS/SETTERS




    //------------------------------------------------------------ METHODS

    public void fetchArticles(int limit, Context context) {
        getFromRoom(context);
    }

    private void getFromRoom(Context context) {
        BookmarkDao dao = getBookmarkDao(context);

        Runnable task = () -> {
            List<BookmarkArticle> temp = dao.selectAll();
            List<ListItem> result = null;

            if(temp!=null && temp.size()>0) {
                result = new ArrayList<>();
                for(Article article : temp) {
                    article.setBookmarked(true);
                    result.add((BookmarkArticle) article);
                }
            }

            listener.onBookmarksFetchCompleted(result);
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - getHistoryFromRoom: cannot start thread " + e.getMessage());
        }
    }

    private BookmarkDao getBookmarkDao(Context context) {
        ScienceBoardRoomDatabase roomDatabase = ScienceBoardRoomDatabase.getInstance(context);
        return roomDatabase.getBookmarkDao();
    }

    public void bookmarksCheck(List<ListItem> articles, Context context) {
        BookmarkDao dao = getBookmarkDao(context);
        for(ListItem article: articles) {
            if(article.getItemType() == MyValues.ItemType.ARTICLE) {
                if(dao.isInBookmarks(((Article)article).getId()))
                    ((Article)article).setBookmarked(true);
                else
                    ((Article)article).setBookmarked(false);
            }
        }
    }

    public void addToBookmarks_async(Article givenArticle, Context context) {
        Runnable task = () -> {
            // TODO null checks
            try {
                BookmarkDao dao = getBookmarkDao(context);
                long millis=System.currentTimeMillis();
                BookmarkArticle bookmarkArticle = new BookmarkArticle(givenArticle);
                bookmarkArticle.setSavedDate(millis);
                dao.insert(bookmarkArticle);
            } catch (Exception e) {
                Log.e(TAG, "SCIENCE_BOARD - addToBookmarks_async: cannot insert in bookmarks, cause: " + e.getMessage());
            }
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - addToBookmarks_async: cannot start thread, cause: " + e.getMessage());
        }
    }

    public void addToBookmarks_sync(Article givenArticle, Context context, AddToBookmarksListener addToBookmarksListener) {
        try {
            BookmarkDao dao = getBookmarkDao(context);
            long millis=System.currentTimeMillis();
            BookmarkArticle bookmarkArticle = new BookmarkArticle(givenArticle);
            bookmarkArticle.setSavedDate(millis);
            dao.insert(bookmarkArticle);
            addToBookmarksListener.onAddToBookmarksCompleted();
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - addToBookmarks_sync: cannot insert in bookmarks, cause: " + e.getMessage());
//            addToBookmarksListener.onAddToBookmarksCompleted();
        }
    }

    public void removeFromBookmarks_async(Article article, Context context) {
        Runnable task = () -> {
            // TODO null checks
            try {
                BookmarkDao dao = getBookmarkDao(context);
                dao.delete(article.getId());
            } catch (Exception e) {
                Log.e(TAG, "SCIENCE_BOARD - removeFromBookmarks_async: cannot remove from bookmarks, cause: " + e.getMessage());
            }
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - removeFromBookmarks_async: cannot start thread, cause: " + e.getMessage());
        }
    }

    public void removeFromBookmarks_sync(Article article, Context context, RemoveFromBookmarksListener removeFromBookmarksListener) {
        // TODO null checks
        try {
            BookmarkDao dao = getBookmarkDao(context);
            dao.delete(article.getId());
            removeFromBookmarksListener.onRemoveFromBookmarksCompleted();
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - removeFromBookmarks_async: cannot remove from bookmarks, cause: " + e.getMessage());
        }
    }


    public void removeArticlesFromBookmark(List<Article> articlesToRemove,
                                           Context context,
                                           BookmarksListOnChangedListener bookmarksListOnChangedListener) {
        if(articlesToRemove==null) return;

        Runnable task = () -> {
            // TODO null checks
            try {
                BookmarkDao dao = getBookmarkDao(context);

                for(Article article: articlesToRemove) {
                    dao.delete(article.getId());
                }

                bookmarksListOnChangedListener.onBookmarksListChanged();
            } catch (Exception e) {
                Log.e(TAG, "SCIENCE_BOARD - removeArticlesFromBookmark: cannot remove from bookmarks, cause: " + e.getMessage());
            }
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            Log.e(TAG, "SCIENCE_BOARD - removeArticlesFromBookmark: cannot start thread, cause: " + e.getMessage());
        }
    }


}// end BookmarksRepository
