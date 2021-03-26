package com.nocorp.scienceboard.repository;


import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.system.MyOkHttpClient;
import com.nocorp.scienceboard.utility.HttpUtilities;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndImage;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;

public class SourceRepository {
    private static final String RAWG_API_KEY = "5c84ab52d6a84d2785b6770d8e52b455&";
    private static final String SEARCH_GAMES_URL = "https://api.rawg.io/api/games?key=" + RAWG_API_KEY + "&page_size=10";
    private final String RAWG_BASE_URL = "https://api.rawg.io";
    private static SourceRepository singletonInstance;


    private SourceRepository() {

    }

    public static SourceRepository getInstance() {
        if(singletonInstance==null)
            return new SourceRepository();

        return singletonInstance;
    }

    private interface RetrofitAPI {
        @GET("feed/")
        Call<String> buildServiceDownloadRss();
    }

    private class RetrofitDownloadRssResult {
        private List<Article> results;
        public List<Article> getData() {
            return results;
        }
    }





    private List<Article> downloadArticles(String rssUrl) throws URISyntaxException, IOException, FeedException {
        String sanitizedUrl = HttpUtilities.sanitizeUrl(rssUrl);
        SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(sanitizedUrl)));

        List<Article> articlesList = new ArrayList<>();

        if (feed!=null) {
            List<SyndEntry> entries = feed.getEntries();
            String logo = getLogoUrl(feed);

        }







        return articlesList;
    }

    private Article buildArticle(SyndFeed feed) {


        return null;
    }


    private String getLogoUrl(SyndFeed feed) {
        if(feed==null) return null;

        String logo = null;
        SyndImage syndImage = feed.getIcon();
        if (syndImage!=null) {
            logo = syndImage.getUrl();
        }
        else {
            // notes: sometimes the logo is fetched via getImage()
            syndImage = feed.getImage();
            if (syndImage!=null) {
                logo = syndImage.getUrl();
            }
        }
        return logo;
    }





//    public String downloadRss(String urlString) {
//        String result = null;
//        final OkHttpClient client = MyOkHttpClient.getClient();
//        Request request = new Request.Builder()
//                .url(urlString)
//                .build();
//
//        // calling
//        try (okhttp3.Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) {
//                throw new IOException("Unexpected code " + response);
//            }
//
//            result = response.body().string();
//
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return result;
//    }// end getRawDataStringFromURL()

//    public List<Article> downloadRss(String baseUrl) {
//        List<Article> results = new ArrayList<>();
//        if(baseUrl==null || baseUrl.isEmpty())
//            return results;
//
//        final RetrofitAPI apiService = buildRetrofitService(baseUrl);
//
//        try {
//            Call<String> call = apiService.buildServiceDownloadRss();
//            Response<String> response = call.execute();
//
//            if(response.isSuccessful()) {
//                String temp = response.body();
////                results = temp.getData();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return results;
//    }// end getGamesByTitle()



//    private RetrofitAPI buildRetrofitService() {
//        final Retrofit retrofitClient = new Retrofit.Builder()
//                .client(MyOkHttpClient.getClient())
//                .addConverterFactory(JaxbConverterFactory.create())
//                .build();
//
//        return retrofitClient.create(RetrofitAPI.class);
//    }

    private RetrofitAPI buildRetrofitService(String baseUrl) {
        final Retrofit retrofitClient = new Retrofit.Builder()
                .baseUrl("https://www.hdblog.it/")
                .client(MyOkHttpClient.getClient())
                .build();

        return retrofitClient.create(RetrofitAPI.class);
    }


}
