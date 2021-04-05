package com.nocorp.scienceboard.repository;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.utility.HttpUtilities;
import com.nocorp.scienceboard.utility.MyOkHttpClient;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SourceRepository {
    private final String TAG = this.getClass().getSimpleName();
    private final String SOURCES_COLLECTION_NAME = "sources";
    private final String NAME = "name";
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



    //--------------------------------------------------------------------------- CONSTRUCTORS

    public SourceRepository () {
        db = FirebaseFirestore.getInstance();
    }

    public SourceRepository (SourceRepositoryListener listener) {
        this.listener = listener;
        db = FirebaseFirestore.getInstance();
    }



    //--------------------------------------------------------------------------- METHODS

    public List<Source> getSources() {
        return cachedSources;
    }

    public void loadSources(Context context) {
//        loadSourcesBasicInfoFromRemoteDb();
        if( ! firestoreFetchCompleted)
            loadSourcesBasicInfoFromRemoteDb();
        else
            listener.onSourcesFetchCompleted(cachedSources);
    }


    private void loadSourcesBasicInfoFromRemoteDb() {
        cachedSources = new ArrayList<>();
        db.collection(SOURCES_COLLECTION_NAME)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.get(NAME));
                            Log.d(TAG, document.getId() + " => " + document.get(WEBSITE_URL));
                            Log.d(TAG, document.getId() + " => " + document.get(LANGUAGE));
                            Log.d(TAG, document.getId() + " => " + document.get(RSS_URL));
                            Log.d(TAG, document.getId() + " => " + document.get(CATEGORY));
                            Log.d(TAG, document.getId() + " => " + document.get(ENABLED));

                            Boolean enabled = (Boolean) document.get(ENABLED);
                            if(enabled!=null && enabled) {
                                Source source = buildBasicSourceObject(document);
                                if(source!=null)  {
                                    cachedSources.add(source);
                                }
                            }
                        }

                        Collections.shuffle(cachedSources); // randomize collection
                        listener.onSourcesFetchCompleted(cachedSources);
                        firestoreFetchCompleted = true;
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        listener.onSourcesFetchFailed("Error getting documents." + task.getException().getMessage());
                    }
                });
    }


    private Source buildBasicSourceObject(QueryDocumentSnapshot document) {
        Source source = null;
        if(document==null) return source;

        source = new Source();
        source.setName((String) document.get(NAME));
        source.setWebsiteUrl((String) document.get(WEBSITE_URL));
        source.setRssUrl((String) document.get(RSS_URL));
        source.setLanguage((String) document.get(LANGUAGE));

        List<String> categories = (List<String>) document.get(CATEGORY);
        if(categories!=null)
            source.setCategories(categories);


        return source;
    }

    private void downloadXmlCode(List<Source> sources) {
        Runnable task = () -> loadSourcesBasicInfoFromRemoteDb();
        ThreadManager threadManager = ThreadManager.getInstance();
        threadManager.runTask(task);
    }



    private void downloadXmlCode(String rssUrl) {
        Runnable task = () -> loadSourcesBasicInfoFromRemoteDb();
        ThreadManager threadManager = ThreadManager.getInstance();
        threadManager.runTask(task);
    }

    public String getXmlCode(String rssUrl) {
        String result = null;
        if (rssUrl==null || rssUrl.isEmpty()) return result;

        Response response = null;
        try {
            final OkHttpClient httpClient = MyOkHttpClient.getClient();
            String sanitizedUrl = HttpUtilities.sanitizeUrl(rssUrl);
            HttpUrl httpUrl = buildHttpURL(sanitizedUrl);
            Request request = buildRequest(httpUrl);

            // performing request
            response = httpClient.newCall(request).execute();

            // check response
            if (response.isSuccessful()) {
                try (ResponseBody responseBody = response.body()) {
                    if(responseBody!=null) {
                        InputStream inputStream = responseBody.byteStream();
                        result = convrtInputStreamToString(inputStream);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "getXmlCode: " + e.getCause());
        } finally {
            if (response != null) {
                response.close();
            }
        }

        return result;
    }

    public void isEnabled() {

    }


    public List<Source> getAsourceForEachMainCategory_randomly(List<String> categories) {
        List<Source> result = null;
        List<Source> allSources = new ArrayList<>(this.cachedSources);

        if(allSources==null || allSources.size()<=0) return result;
        if(categories==null || categories.size()<=0) return result;

        for(Source source: allSources) {
            for(String category: categories) {
                if(sourcefallInThisCategory(source, category)) {
                    result.add(source);
                    allSources.remove(source);
                    categories.remove(category);
                    break;
                }
            }
        }

        return result;
    }



    public List<Source> getAsourceForEachMainCategory_randomly() {
        List<Source> result = null;
        List<Source> allSources = new ArrayList<>(this.cachedSources);
        List<String> mainCategories = new ArrayList<>(this.mainCategories);

        if(allSources==null || allSources.size()<=0) return result;
        if(mainCategories==null || mainCategories.size()<=0) return result;

        for(Source source: allSources) {
            for(String category: mainCategories) {
                if(sourcefallInThisCategory(source, category)) {
                    result.add(source);
                    allSources.remove(source);
                    mainCategories.remove(category);
                    break;
                }
            }
        }

        return result;
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



    private Source getTheFirstSourceFallingInThisCategory(List<Source> sources, String category) {
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



    private boolean sourcefallInThisCategory(Source source, String category) {
        boolean result = false;
        if(source==null) return result;
        if(category==null || category.isEmpty()) return result;

        List<String> sourceCategories = source.getCategories();
        result = sourceCategories.contains(category);

        return result;
    }


//    private void loadTestUrls() {
//        final String feedTag = "https://www.theverge.com/rss/index.xml";
//        final String rdfTag = "https://www.nature.com/nmat.rss"; // unsecure (HTTP)
//        final String rssTag = "https://home.cern/api/news/news/feed.rss";
//        final String malformedRss = "https://www.theverge.com/";
//
//        // space
//        final String esa_italy = "https://www.esa.int/rssfeed/Italy";
//        final String nytimes_space = "https://rss.nytimes.com/services/xml/rss/nyt/Space.xml";
//        final String cern = "https://home.cern/api/news/news/feed.rss";
//        final String spacenews = "https://spacenews.com/feed/";
//        final String space_com = "https://www.space.com/feeds/all";
//        final String phys_org_space = "https://phys.org/rss-feed/space-news/";
//        final String newscientist_space = "https://www.newscientist.com/subject/space/feed/";
//        final String esa_space_news = "https://www.esa.int/rssfeed/Our_Activities/Space_News";
//        final String nasa = "https://www.nasa.gov/rss/dyn/breaking_news.rss";
//
//        // tech
//        final String wired = "https://www.wired.com/feed/rss";
//        final String nvidiaBlog = "https://feeds.feedburner.com/nvidiablog";
//        final String hdblog = "https://www.hdblog.it/feed/";
//        final String theverge = "https://www.theverge.com/rss/index.xml";
//
//        // science
//        final String nature = "http://feeds.nature.com/nature/rss/current";
//        final String livescience = "https://www.livescience.com/feeds/all";
//
//
//        sourceUrls.add(esa_italy);
//        sourceUrls.add(nytimes_space);
//        sourceUrls.add(cern);
//        sourceUrls.add(space_com);
////        sourceUrls.add(newscientist_space); // no thumbnails
//        sourceUrls.add(esa_space_news);
//        sourceUrls.add(hdblog);
//        sourceUrls.add(theverge);
////        sourceUrls.add(nasa); // not https
//
//
//
////        // ----------------- slow
//        sourceUrls.add(spacenews);
////        sourceUrls.add(phys_org_space);
////        sourceUrls.add(wired);
////        sourceUrls.add(nvidiaBlog); // not https
////        sourceUrls.add(nature); // no thumbnails, images not https
//        sourceUrls.add(livescience);
//    }

    private static HttpUrl buildHttpURL(String url) {
        HttpUrl httpUrl = HttpUrl.get(url);

        return httpUrl;
    }

    private static Request buildRequest(HttpUrl httpUrl) {
        Request request = null;
        try {
            request = new Request.Builder()
                    .url(httpUrl)
                    .header("User-Agent", "OkHttp Headers.java")
                    .addHeader("Accept", "application/json; q=0.5")
                    .addHeader("Accept", "application/vnd.github.v3+json")
                    .get()
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return request;
    }

    private String convrtInputStreamToString(InputStream inputStream) throws IOException {
        String result = null;
        if(inputStream==null) return result;
        result = org.apache.commons.io.IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        return result;
    }

}// end SourceRepository
