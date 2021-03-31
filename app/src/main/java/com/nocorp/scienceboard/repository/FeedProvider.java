package com.nocorp.scienceboard.repository;

import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.utility.HttpUtilities;
import com.nocorp.scienceboard.utility.MyOkHttpClient;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndImage;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FeedProvider {
    private List<Source> sources;
    private List<String> sourceUrls;
    private OnFeedsDownloadedListener listener;
    public interface OnFeedsDownloadedListener {
        public void onFeedsDownloadCompleted(List<Source> sources);
        public void onFeedsDownloadFailed(String cause);
    }

    public FeedProvider(OnFeedsDownloadedListener listener) {
        this.listener = listener;
        sourceUrls = new ArrayList<>();
        fetchSourcesUrl();
    }





    private void fetchSourcesUrl() {
        final String feedTag = "https://www.theverge.com/rss/index.xml";
        final String rdfTag = "https://www.nature.com/nmat.rss"; // unsecure (HTTP)
        final String rssTag = "https://home.cern/api/news/news/feed.rss";
        final String malformedRss = "https://www.theverge.com/";

        // space
        final String esa_italy = "https://www.esa.int/rssfeed/Italy";
        final String nytimes_space = "https://rss.nytimes.com/services/xml/rss/nyt/Space.xml";
        final String cern = "https://home.cern/api/news/news/feed.rss";
        final String spacenews = "https://spacenews.com/feed/";
        final String space = "https://www.space.com/feeds/all";
        final String phys_org_space = "https://phys.org/rss-feed/space-news/";
        final String newscientist_space = "https://www.newscientist.com/subject/space/feed/";
        final String esa_space_news = "https://www.esa.int/rssfeed/Our_Activities/Space_News";

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
//        sourceUrls.add(spacenews);
        sourceUrls.add(space);
//        sourceUrls.add(phys_org_space);
        sourceUrls.add(newscientist_space);
        sourceUrls.add(esa_space_news);
        sourceUrls.add(wired);
//        sourceUrls.add(nvidiaBlog);
        sourceUrls.add(hdblog);
        sourceUrls.add(theverge);
//        sourceUrls.add(nature);
//        sourceUrls.add(livescience);

    }



    public List<Source> downloadFeeds() {
        sources = new ArrayList<>();
        if(sourceUrls==null || sourceUrls.size()<=0) {
            listener.onFeedsDownloadFailed("url list is empty/null");
            return sources;
        }

        Runnable task = () -> {
            try {
                for(String url : sourceUrls) {
                    Source source = downloadFeed(url);
                    if(source!=null) sources.add(source);
                }

                //
                listener.onFeedsDownloadCompleted(sources);

            } catch (Exception e) {
                e.printStackTrace();
                listener.onFeedsDownloadFailed(e.getMessage());
            }
        };

        ThreadManager threadManager = ThreadManager.getInstance();
        threadManager.runTask(task);

        return sources;
    }



    private Source downloadFeed(String url) {
        Source source = null;
        Response response = null;
        boolean result = false;

        try {
            String sanitizedUrl = HttpUtilities.sanitizeUrl(url);
//            SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(sanitizedUrl)));


            // build httpurl and request for remote db
            HttpUrl httpUrl = buildHttpURL(url);
            final OkHttpClient httpClient = MyOkHttpClient.getClient();
            Request request = buildRequest(httpUrl);

            // performing request
            response = httpClient.newCall(request).execute();

            // check response
            if (response.isSuccessful()) {
                try (ResponseBody responseBody = response.body()) {
                    InputStream is = responseBody.byteStream();
                    InputSource inputSource = new InputSource(is);
                    SyndFeedInput input = new SyndFeedInput();
                    SyndFeed feed = input.build(inputSource);
                    if (feed!=null) {
                        source = buildSource(feed);
                    }
                }
            }
            // if the response is unsuccesfull
            else result = false;

        } catch (FeedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
        }

        return source;
    }



    private Article preBuildArticle(SyndEntry entry) {
        Article article = new Article();
        article.setPublishDate(entry.getPublishedDate());
        article.setSyndEntry(entry);
        return article;
    }


    private Source buildSource(SyndFeed feed) {
        Source source = null;

        try {
            source = new Source();
            String logoUrl = getLogoUrl(feed);
            String name = feed.getTitle();
            String websiteUrl = feed.getLink();
            List<SyndEntry> entries = feed.getEntries();
            List<Article> articles = preDownloadArticles(feed);

            source.setLogoUrl(logoUrl);
            source.setName(name);
            source.setWebsiteUrl(websiteUrl);
            source.setEntries(entries);
            source.setArticles(articles);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return source;
    }

    private List<Article> preDownloadArticles(SyndFeed feed) {
        List<Article> articleList = new ArrayList<>();

        List<SyndEntry> entries = feed.getEntries();
        for(SyndEntry entry : entries) {
            Article article = preBuildArticle(entry);
            if(article!=null) articleList.add(article);
        }

        return articleList;
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

    public static HttpUrl buildHttpURL(String url) {
        HttpUrl httpUrl = HttpUrl.get(url);

        return httpUrl;
    }

    public static Request buildRequest(HttpUrl httpUrl) {
        Request request = null;
        try {
            request = new Request.Builder()
                    .url(httpUrl)
                    .header("User-Agent", "OkHttp Headers.java")
                    .addHeader("Accept", "application/json; q=0.5")
                    .addHeader("Accept", "application/vnd.github.v3+json")
                    .head()
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return request;
    }




}// end SourceProvider
