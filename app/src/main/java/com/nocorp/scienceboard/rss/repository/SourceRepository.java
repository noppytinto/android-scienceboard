package com.nocorp.scienceboard.rss.repository;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.rss.room.ScienceBoardRoomDatabase;
import com.nocorp.scienceboard.rss.room.SourceDao;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SourceRepository {
    private final String TAG = this.getClass().getSimpleName();
    private final String SOURCES_COLLECTION_NAME = "sources";
    private final String NAME = "name";
    private final String REAL_NAME = "real_name";
    private final String RSS_URL = "rss_url";
    private final String WEBSITE_URL = "website_url";
    private final String LANGUAGE = "language";
    private final String CATEGORY = "categories";
    private final String ENABLED = "enabled";

    private static List<Source> cachedSources;
    private List<String> sourceUrls;
    private SourceRepositoryListener listener;
    private FirebaseFirestore db;
    private static boolean firestoreFetchCompleted;
    private final List<String> mainCategories = Arrays.asList("space", "physics", "tech", "medicine", "biology");
    private static boolean taskIsRunning;



    //--------------------------------------------------------------------------- CONSTRUCTORS

    public SourceRepository () {
        db = FirebaseFirestore.getInstance();
    }

    public SourceRepository (SourceRepositoryListener listener) {
        db = FirebaseFirestore.getInstance();
        this.listener = listener;
    }





    //--------------------------------------------------------------------------- PUBLIC METHODS

    public void loadSources(Context context) {
        if( ! taskIsRunning) {
            taskIsRunning = true;
            if(cachedSources==null) {
                loadSourcesFromRemoteDb(context);
            }
            else {
                taskIsRunning = false;
                listener.onAllSourcesFetchCompleted(cachedSources);
            }
        }
    }

    public List<Source> getAsourceForEachMainCategory_randomly(List<Source> givenSources, List<String> givenCategories) {
        List<Source> result = null;
        if(givenSources==null || givenSources.size()<=0) return result;
        if(givenCategories==null || givenCategories.size()<=0) return result;

        List<Source> allSources = new ArrayList<>(givenSources);
        List<String> mainCategories = new ArrayList<>(givenCategories);

        result = new ArrayList<>();
        for(int i=0; i < mainCategories.size(); i++) {
            Source source = getTheFirstSourceFallingInThisCategory(allSources, mainCategories.get(i));
            if(source!=null) {
                result.add(source);
                allSources.remove(source);
            }
        }

        return result;
    }

    public List<Source> getCachedSources() {
        return cachedSources;
    }

    public static List<Source> getAllSourcesOfThisCategory(List<Source> sources, String category) {
        List<Source> result = null;
        if(sources==null || sources.size()<=0) return result;
        if(category==null || category.isEmpty()) return result;

        result = new ArrayList<>();
        for(Source currentSource: sources) {
            if(sourcefallInThisCategory(currentSource, category)) {
                result.add(currentSource);
            }
        }

        return result;
    }






    //--------------------------------------------------------------------------- PRIVATE METHODS

    private void loadSourcesFromRemoteDb(Context context) {
        cachedSources = new ArrayList<>();
        db.collection(SOURCES_COLLECTION_NAME)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            Log.d(TAG, document.getId() + " => " + document.get(NAME));
//                            Log.d(TAG, document.getId() + " => " + document.get(WEBSITE_URL));
//                            Log.d(TAG, document.getId() + " => " + document.get(LANGUAGE));
//                            Log.d(TAG, document.getId() + " => " + document.get(RSS_URL));
//                            Log.d(TAG, document.getId() + " => " + document.get(CATEGORY));
//                            Log.d(TAG, document.getId() + " => " + document.get(ENABLED));

                            Boolean sourceEnabled = (Boolean) document.get(ENABLED);

                            // testing code
//                            String name = (String) document.get(NAME);
//                            String testname = "lifehacker";
//                            if(enabled!=null && enabled && name.equals(testname)) {
//                                Source source = buildBasicSourceObject(document);
//                                if(source!=null)  {
//                                    cachedSources.add(source);
//                                }
//                            }

                            if(sourceEnabled!=null && sourceEnabled) {
                                Source source = buildSource(document);
                                if(source!=null)  {
                                    cachedSources.add(source);
                                }
                            }
                        }

                        //
                        saveSourcesInRoom(cachedSources, context);

                        //
                        Collections.shuffle(cachedSources); // randomize collection
                        taskIsRunning = false;
                        listener.onAllSourcesFetchCompleted(cachedSources);
                    } else {
                        Log.w(TAG, "SCIENCE_BOARD - Error getting sources.", task.getException());
                        listener.onAllSourcesFetchFailed("Error getting sources." + task.getException().getMessage());
                    }
                });
    }

    private Source buildSource(QueryDocumentSnapshot document) {
        Source source = null;
        if(document==null) return source;

        source = new Source();
        source.setId((String) document.getId());
        source.setRealName((String) document.get(REAL_NAME));
        source.setWebsiteUrl((String) document.get(WEBSITE_URL));
        source.setRssUrl((String) document.get(RSS_URL));
        source.setLanguage((String) document.get(LANGUAGE));
        List<String> categories = (List<String>) document.get(CATEGORY);
        source.setCategories(categories);

        return source;
    }

    private static Source getTheFirstSourceFallingInThisCategory(List<Source> sources, String category) {
        Source result = null;
        if(sources==null || sources.size()<=0) return result;
        if(category==null || category.isEmpty()) return result;

        for(Source source: sources) {
            if(sourcefallInThisCategory(source, category)) {
                result = source;
                break;
            }
        }

        return result;
    }

    private static boolean sourcefallInThisCategory(Source source, String category) {
        boolean result = false;
        if(source==null) return result;
        if(category==null || category.isEmpty()) return result;

        List<String> sourceCategories = source.getCategories();
        result = sourceCategories.contains(category);

        return result;
    }

    private void saveSourcesInRoom(@NotNull List<Source> sources, Context context) {
        SourceDao sourceDao = getSourceDao(context);

        Runnable task = () -> {
            try {
                sourceDao.insertAll(sources);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "SCIENCE_BOARD - saveSourcesInRoom: cannot save sources in Room, cause:" + e.getMessage());
            }
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "SCIENCE_BOARD - saveSourcesInRoom: cannot start thread " + e.getMessage());
        }
    }

    private SourceDao getSourceDao(Context context) {
        ScienceBoardRoomDatabase roomDatabase = ScienceBoardRoomDatabase.getInstance(context);
        return roomDatabase.getSourceDao();
    }


    private void cacheSourceInRoom(Source source, Context context) {
        if(source==null) {
            Log.d(TAG, "SCIENCE_BOARD - cacheSourceInRoom: cannot ccahe source in Room, source is null");
        }
        SourceDao sourceDao = getSourceDao(context);

        Runnable task = () -> {
            sourceDao.insert(source);
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "SCIENCE_BOARD - cacheSourceInRoom: cannot start thread " + e.getMessage());
        }
    }


}// end SourceRepository
