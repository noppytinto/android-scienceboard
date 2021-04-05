package com.nocorp.scienceboard.repository;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.utility.HttpUtilities;
import com.nocorp.scienceboard.utility.MyOkHttpClient;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

    private List<Source> sources;
    private List<String> sourceUrls;
    private SourceRepositoryListener listener;
    private FirebaseFirestore db;
    private static boolean firestoreFetchCompleted;



    //--------------------------------------------------------------------------- CONSTRUCTORS

    public SourceRepository (SourceRepositoryListener listener) {
        this.listener = listener;
        db = FirebaseFirestore.getInstance();
    }



    //--------------------------------------------------------------------------- METHODS

    public void loadSources() {
        loadSourcesBasicInfoFromRemoteDb();


//        if( ! firestoreFetchCompleted)
//            loadSourcesBasicInfoFromRemoteDb();
//        else
//            listener.onSourcesFetchCompleted(sources);
    }


    private void loadSourcesBasicInfoFromRemoteDb() {
        sources = new ArrayList<>();
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
                                if(source!=null) sources.add(source);
                            }
                        }

                        listener.onSourcesFetchCompleted(sources);
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


    private void loadTestUrls() {
        final String feedTag = "https://www.theverge.com/rss/index.xml";
        final String rdfTag = "https://www.nature.com/nmat.rss"; // unsecure (HTTP)
        final String rssTag = "https://home.cern/api/news/news/feed.rss";
        final String malformedRss = "https://www.theverge.com/";

        // space
        final String esa_italy = "https://www.esa.int/rssfeed/Italy";
        final String nytimes_space = "https://rss.nytimes.com/services/xml/rss/nyt/Space.xml";
        final String cern = "https://home.cern/api/news/news/feed.rss";
        final String spacenews = "https://spacenews.com/feed/";
        final String space_com = "https://www.space.com/feeds/all";
        final String phys_org_space = "https://phys.org/rss-feed/space-news/";
        final String newscientist_space = "https://www.newscientist.com/subject/space/feed/";
        final String esa_space_news = "https://www.esa.int/rssfeed/Our_Activities/Space_News";
        final String nasa = "https://www.nasa.gov/rss/dyn/breaking_news.rss";

        // tech
        final String wired = "https://www.wired.com/feed/rss";
        final String nvidiaBlog = "https://feeds.feedburner.com/nvidiablog";
        final String hdblog = "https://www.hdblog.it/feed/";
        final String theverge = "https://www.theverge.com/rss/index.xml";

        // science
        final String nature = "http://feeds.nature.com/nature/rss/current";
        final String livescience = "https://www.livescience.com/feeds/all";


        sourceUrls.add(esa_italy);
        sourceUrls.add(nytimes_space);
        sourceUrls.add(cern);
        sourceUrls.add(space_com);
//        sourceUrls.add(newscientist_space); // no thumbnails
        sourceUrls.add(esa_space_news);
        sourceUrls.add(hdblog);
        sourceUrls.add(theverge);
//        sourceUrls.add(nasa); // not https



//        // ----------------- slow
        sourceUrls.add(spacenews);
//        sourceUrls.add(phys_org_space);
//        sourceUrls.add(wired);
//        sourceUrls.add(nvidiaBlog); // not https
//        sourceUrls.add(nature); // no thumbnails, images not https
        sourceUrls.add(livescience);
    }

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
