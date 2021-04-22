package com.nocorp.scienceboard.rss.repository;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.topics.model.Topic;
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

    public void loadSources(Context context, OnSourcesFetchedListener listener) {
        if( ! taskIsRunning) {
            taskIsRunning = true;
            if(cachedSources==null) {
                loadSourcesFromRemoteDb(context, listener);
            }
            else {
                taskIsRunning = false;
                listener.onComplete(cachedSources);
            }
        }
    }

    private void loadSourcesFromRemoteDb(Context context, OnSourcesFetchedListener listener) {
        cachedSources = new ArrayList<>();
        db.collection(SOURCES_COLLECTION_NAME)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
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
                        listener.onComplete(cachedSources);
                    } else {
                        if(task.getException()!=null) {
                            Log.w(TAG, "SCIENCE_BOARD - Error getting sources.", task.getException());
                            listener.onFailded("Error getting sources." + task.getException().getMessage());
                        }
                    }
                });
    }

    private void saveSourcesInRoom(@NotNull List<Source> sources, Context context) {
        Runnable task = () -> {
            try {
                SourceDao sourceDao = getSourceDao(context);
                sourceDao.insertAll(sources);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "SCIENCE_BOARD - saveSourcesInRoom: cannot save sources in Room, cause:" + e.getMessage());
            }
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "SCIENCE_BOARD - saveSourcesInRoom: cannot start thread " + e.getMessage());
        }
    }

    public List<Source> getAsourceForEachFollowedCategory_randomly(List<Source> givenSources, List<Topic> topics) {
        List<Source> result = null;
        if(givenSources==null || givenSources.isEmpty()) return result;
        if(topics==null || topics.isEmpty()) return result;

        List<Source> allSources = new ArrayList<>(givenSources);
        Collections.shuffle(allSources);
        result = new ArrayList<>();
        for(int i=0; i < topics.size(); i++) {
            if(topics.get(i).getFollowed()) {
                Source source = getTheFirstSourceFallingInThisCategory(allSources, topics.get(i).getId());
                if(source!=null) {
                    result.add(source);
                    allSources.remove(source);
                }
            }
        }

        return result;
    }

    public List<Source> getCachedSources() {
        return cachedSources;
    }

    public List<Source> getAllSourcesOfThisCategory(List<Source> sources, String category) {
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



    private SourceDao getSourceDao(Context context) {
        ScienceBoardRoomDatabase roomDatabase = ScienceBoardRoomDatabase.getInstance(context);
        return roomDatabase.getSourceDao();
    }


}// end SourceRepository
