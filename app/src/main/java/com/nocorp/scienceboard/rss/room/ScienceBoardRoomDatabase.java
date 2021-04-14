package com.nocorp.scienceboard.rss.room;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.BookmarkedArticle;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.model.VisitedArticle;

@Database(
        entities = {Source.class, Article.class, VisitedArticle.class, BookmarkedArticle.class},
        version = 1,
        exportSchema = false)
@TypeConverters({DateConverter.class, StringListConverter.class})
public abstract class ScienceBoardRoomDatabase extends RoomDatabase {
    private static volatile ScienceBoardRoomDatabase singletonInstance;
    private static final String DB_NAME = "scienceboard-room-db";



    //--------------------------------- CONSTRUCTORS
    public ScienceBoardRoomDatabase() {}

    //--------------------------------- GETTERS/SETTERS
    public abstract SourceDao getSourceDao();
    public abstract ArticleDao getArticleDao();
    public abstract HistoryDao getHistoryDao();
    public abstract BookmarkDao getBookmarkDao();

    public static synchronized ScienceBoardRoomDatabase getInstance(Context context) {
        if (singletonInstance == null) {
            singletonInstance = create(context);
        }
        return singletonInstance;
    }


    //--------------------------------- METHODS

    private static ScienceBoardRoomDatabase create(final Context context) {
        return Room.databaseBuilder(context, ScienceBoardRoomDatabase.class, DB_NAME)
                .build();
    }

}// end ScienceBoardRoomDatabase
