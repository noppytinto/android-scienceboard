package com.nocorp.scienceboard.repository;


import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.system.MyOkHttpClient;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.utility.HttpUtilities;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndImage;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import org.jdom2.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
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
    private ArticleDownloader articlesListener;


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


    public void setArticlesListener(ArticleDownloader listener) {
        this.articlesListener = listener;
    }



    public void getArticles(String rssUrl) {
        List<Article> articlesList = new ArrayList<>();

        Runnable task = () -> {
            try {
                Source source = new Source();

                String sanitizedUrl = HttpUtilities.sanitizeUrl(rssUrl);
                SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(sanitizedUrl)));
                if (feed!=null) {
                    List<SyndEntry> entries = feed.getEntries();
                    String logoUrl = getLogoUrl(feed);
                    String title = feed.getTitle();
                    String websiteUrl = feed.getLink();
                    source.setLogoUrl(logoUrl);
                    source.setName(title);
                    source.setWebsiteUrl(websiteUrl);

                    for(SyndEntry entry : entries) {
                        String articleTitle = entry.getTitle();
                        String webpageUrl = entry.getLink();
                        String thumbnailUrl = getThumbnailUrl(entry);
                        Date publishDate = entry.getPublishedDate();

                        Article article = new Article();
                        article.setThumbnailUrl(thumbnailUrl);
                        article.setTitle(articleTitle);
                        article.setWebpageUrl(webpageUrl);
                        article.setSyndEntry(entry);
                        article.setSource(source);
                        article.setPublishDate(publishDate);

                        articlesList.add(article);
                    }

                    //
                }
                articlesListener.onArticlesDownloaded(articlesList);

            } catch (FeedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        };

        ThreadManager threadManager = ThreadManager.getInstance();
        threadManager.runTaskInPool(task);
    }

    private Article buildArticle(SyndFeed feed) {


        return null;
    }


    public String getThumbnailUrl(SyndEntry entry) {
        if(entry==null) return null;
        String thumbnailUrl = null;

        // media namespace strategy
        List<Element> elements = entry.getForeignMarkup();
        for(Element element: elements) {
            String namespace = element.getNamespacePrefix();
            if(namespace.equals("media")) {
                thumbnailUrl = element.getAttributeValue("url");
                break;
            }
        }


        return thumbnailUrl;
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
