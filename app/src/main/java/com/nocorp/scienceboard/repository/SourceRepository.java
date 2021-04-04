package com.nocorp.scienceboard.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nocorp.scienceboard.model.Source;

import java.util.ArrayList;
import java.util.List;

public class SourceRepository {
    private final String TAG = this.getClass().getSimpleName();
    private final String SOURCES_COLLECTION_NAME = "sources";
    private final String NAME = "name";
    private final String RSS_URL = "rss_url";
    private final String WEBSITE_URL = "website_url";
    private final String LANGUAGE = "language";
    private final String CATEGORY = "category";

    private List<Source> sources;
    private List<String> sourceUrls;

    private SourcesFetcher listener;




    //--------------------------------------------------------------------------- CONSTRUCTORS

    public SourceRepository (SourcesFetcher listener) {
        this.listener = listener;
        sources = new ArrayList<>();
    }



    //--------------------------------------------------------------------------- METHODS

    public void loadSources() {
        loadSourcesBasicInfoFromRemoteDb();
    }


    public void loadSourcesBasicInfoFromRemoteDb() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(SOURCES_COLLECTION_NAME)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.get(NAME));
                                Log.d(TAG, document.getId() + " => " + document.get(WEBSITE_URL));
                                Log.d(TAG, document.getId() + " => " + document.get(LANGUAGE));
                                Log.d(TAG, document.getId() + " => " + document.get(RSS_URL));
                                Log.d(TAG, document.getId() + " => " + document.get(CATEGORY));

                                Source source = buildBasicSourceObject(document);
                                if(source!=null) sources.add(source);
                            }

                            listener.onSourcesFetchCompleted(sources);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                            listener.onSourcesFetchFailed("Error getting documents." + task.getException().getCause());
                        }
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
        source.setCategory((String) document.get(CATEGORY));

        return source;
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



}// end SourceRepository
