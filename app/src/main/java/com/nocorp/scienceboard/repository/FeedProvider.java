package com.nocorp.scienceboard.repository;

import android.content.Context;

import com.nocorp.scienceboard.utility.rss.model.Channel;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.utility.rss.model.Entry;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.utility.rss.DomXmlParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FeedProvider {
    private final String TAG = this.getClass().getSimpleName();
    private final String SOURCES_COLLECTION_NAME = "sources";

    private List<Source> sources;
    private List<String> sourceUrls;
    private OnFeedsDownloadedListener listener;
    //
    private static List<Channel> channelsCache;
    private final List<String> mainCategories = Arrays.asList("space", "physics", "tech", "medicine", "biology");
    private SourceRepository sourceRepository;

    public interface OnFeedsDownloadedListener {
        public void onFeedsDownloadCompleted(List<Source> sources);
        public void onFeedsDownloadFailed(String cause);
    }


    //------------------------------------------------------------ CONSTRUCTOR

    public FeedProvider(OnFeedsDownloadedListener listener) {
        this.listener = listener;
        sourceUrls = new ArrayList<>();
//        loadRssUrls();
        sourceRepository = new SourceRepository();

    }



    //------------------------------------------------------------

    public List<Source> downloadSources(List<Source> givenSources, Context context) {
        sources = null;
        if(givenSources==null || givenSources.size()<=0) {
            listener.onFeedsDownloadFailed("url list is empty/null");
            return sources;
        }
        List<Source> sourceList = sourceRepository.getAsourceForEachMainCategory_randomly(givenSources, mainCategories);

        sources = new ArrayList<>();

        Runnable task = () -> {
            try {
                for(Source source : sourceList) {
                    Source temp = downloadSource(source.getRssUrl(), context);
                    if(temp!=null) {
                        // add additional info
                        temp.setName(source.getName());
                        temp.setWebsiteUrl(source.getWebsiteUrl());
                        temp.setRssUrl(source.getRssUrl());
                        temp.setLanguage(source.getLanguage());
                        temp.setCategories(source.getCategories());
                        sources.add(temp);
                    }
                }

                if(sources!=null && sources.size()>0)
                    listener.onFeedsDownloadCompleted(sources);
                else
                    listener.onFeedsDownloadFailed("");

            } catch (Exception e) {
                e.printStackTrace();
                listener.onFeedsDownloadFailed(e.getMessage());
            }
        };

        ThreadManager threadManager = ThreadManager.getInstance();
        threadManager.runTask(task);

        return sources;
    }

    private Source downloadSource(String url, Context context) {
        Source source = null;

        try {
            DomXmlParser domXmlParser = new DomXmlParser();
            Channel channel = domXmlParser.getChannel(url, context);
            if (channel!=null) {
                channel.setRssUrl(url);
                source = buildSource(channel);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return source;
    }


    private Source buildSource(Channel channel) {
        Source source = null;

        try {
            source = new Source();
//            String name = channel.getName();
//            String websiteUrl = channel.getWebsiteUrl();
            Date lastUpdate = channel.getLastUpdate();
            List<Entry> entries = channel.getEntries();
//            List<Article> articles = preDownloadArticles(channel);

//            source.setName(name);
//            source.setWebsiteUrl(websiteUrl);
            source.setLastUpdate(lastUpdate);
            source.setEntries(entries);

//            if(entries!=null && entries.size()>0) {
//                for(Entry entry: entries)
//                    entry.setSource(source);
//            }
//            source.setArticles(articles);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return source;
    }



















//    private Source downloadSource(String url) {
//        Source source = null;
//
//        try {
//            String inputStream = getInputStreamFromUrl(url);
//            InputSource inputSource = new InputSource(inputStream);
//            SyndFeedInput input = new SyndFeedInput();
//            SyndFeed feed = input.build(inputSource);
//            if (feed!=null) {
//                source = buildSource(feed);
//            }
//
//        } catch (FeedException e) {
//            e.printStackTrace();
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return source;
//    }






//    public List<Source> downloadRssSources() {
//        sources = new ArrayList<>();
//        if(sourceUrls==null || sourceUrls.size()<=0) {
//            listener.onFeedsDownloadFailed("url list is empty/null");
//            return sources;
//        }
//
//        Runnable task = () -> {
//            try {
//                for(String url : sourceUrls) {
//                    Source source = downloadSource(url);
//                    if(source!=null) sources.add(source);
//                }
//
//                //
//                listener.onFeedsDownloadCompleted(sources);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                listener.onFeedsDownloadFailed(e.getMessage());
//            }
//        };
//
//        ThreadManager threadManager = ThreadManager.getInstance();
//        threadManager.runTask(task);
//
//        return sources;
//    }
//
//
//    private String getInputStreamFromUrl(String url) {
//        String result = null;
//        InputStream inputStream = null;
//        Response response = null;
//
//        try {
//            String sanitizedUrl = HttpUtilities.sanitizeUrl(url);
//            HttpUrl httpUrl = buildHttpURL(sanitizedUrl);
//            final OkHttpClient httpClient = MyOkHttpClient.getClient();
//            Request request = buildRequest(httpUrl);
//
//            // performing request
//            response = httpClient.newCall(request).execute();
//
//            // check response
//            if (response.isSuccessful()) {
//                try (ResponseBody responseBody = response.body()) {
//                    if(responseBody!=null) {
//                        inputStream = responseBody.byteStream();
//                        result = inputStreamToString(inputStream);
//                    }
//
////                    InputSource inputSource = new InputSource(is);
//                }
//            }
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (response != null) {
//                response.close();
//            }
//        }
//
//
//        return result;
//    }
//
//    public String inputStreamToString(InputStream inputStream) throws IOException {
//        String result = org.apache.commons.io.IOUtils.toString(inputStream, StandardCharsets.UTF_8);
//
//        return result;
//    }
//
//    private Article preBuildArticle(SyndEntry entry) {
//        Article article = new Article();
//        article.setPublishDate(entry.getPublishedDate());
//        article.setSyndEntry(entry);
//        return article;
//    }
//
//    private Source buildSource_dom(Channel channel) {
//        Source source = null;
//
//        try {
//            source = new Source();
//            String name = channel.getName();
//            String websiteUrl = channel.getWebsiteUrl();
//            Date lastUpdate = channel.getLastUpdate();
//            List<Entry> entries = channel.getEntries();
////            List<Article> articles = preDownloadArticles(channel);
//
//            source.setName(name);
//            source.setWebsiteUrl(websiteUrl);
//            source.setLastUpdate(lastUpdate);
//            source.setEntries(entries);
//
////            if(entries!=null && entries.size()>0) {
////                for(Entry entry: entries)
////                    entry.setSource(source);
////            }
////            source.setArticles(articles);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return source;
//    }
//
//    private Source buildSource(SyndFeed feed) {
//        Source source = null;
//
//        try {
//            source = new Source();
//            String logoUrl = getLogoUrl(feed);
//            String name = feed.getTitle();
//            String websiteUrl = feed.getLink();
//            List<SyndEntry> entries = feed.getEntries();
//            List<Article> articles = preDownloadArticles(feed);
//
//            source.setLogoUrl(logoUrl);
//            source.setName(name);
//            source.setWebsiteUrl(websiteUrl);
////            source.setEntries(entries);
//            source.setArticles(articles);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return source;
//    }
//
//    private List<Article> preDownloadArticles(SyndFeed feed) {
//        List<Article> articleList = new ArrayList<>();
//
//        List<SyndEntry> entries = feed.getEntries();
//        for(SyndEntry entry : entries) {
//            Article article = preBuildArticle(entry);
//            if(article!=null) articleList.add(article);
//        }
//
//        return articleList;
//    }
//
//    private String getLogoUrl(SyndFeed feed) {
//        if(feed==null) return null;
//
//        String logo = null;
//        SyndImage syndImage = feed.getIcon();
//        if (syndImage!=null) {
//            logo = syndImage.getUrl();
//        }
//        else {
//            // notes: sometimes the logo is fetched via getImage()
//            syndImage = feed.getImage();
//            if (syndImage!=null) {
//                logo = syndImage.getUrl();
//            }
//        }
//        return logo;
//    }
//
//    public static HttpUrl buildHttpURL(String url) {
//        HttpUrl httpUrl = HttpUrl.get(url);
//
//        return httpUrl;
//    }
//
//    public static Request buildRequest(HttpUrl httpUrl) {
//        Request request = null;
//        try {
//            request = new Request.Builder()
//                    .url(httpUrl)
//                    .header("User-Agent", "OkHttp Headers.java")
//                    .addHeader("Accept", "application/json; q=0.5")
//                    .addHeader("Accept", "application/vnd.github.v3+json")
//                    .get()
//                    .build();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return request;
//    }

}// end FeedProvider
