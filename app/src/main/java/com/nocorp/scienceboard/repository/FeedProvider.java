package com.nocorp.scienceboard.repository;

import com.nocorp.scienceboard.utility.rss.model.Channel;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.utility.rss.model.Entry;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.utility.rss.DomXmlParser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FeedProvider {
    private List<Source> sources;
    private List<String> sourceUrls;
    private OnFeedsDownloadedListener listener;
    //
    private static List<Channel> channelsCache;

    public interface OnFeedsDownloadedListener {
        public void onFeedsDownloadCompleted(List<Source> sources);
        public void onFeedsDownloadFailed(String cause);
    }


    //------------------------------------------------------------ CONSTRUCTOR

    public FeedProvider(OnFeedsDownloadedListener listener) {
        this.listener = listener;
        sourceUrls = new ArrayList<>();
        loadRssUrls();
    }



    //------------------------------------------------------------

    private void loadRssUrls() {
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

    public List<Source> downloadRssSources_dom() {
        sources = new ArrayList<>();
        if(sourceUrls==null || sourceUrls.size()<=0) {
            listener.onFeedsDownloadFailed("url list is empty/null");
            return sources;
        }

        Runnable task = () -> {
            try {
                for(String url : sourceUrls) {
                    Source source = downloadSource_dom(url);
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

    private Source downloadSource_dom(String url) {
        Source source = null;

        try {
            DomXmlParser domXmlParser = new DomXmlParser();
            Channel channel = domXmlParser.getChannel(url);
            if (channel!=null) {
                channel.setRssUrl(url);
                source = buildSource_dom(channel);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return source;
    }


    private Source buildSource_dom(Channel channel) {
        Source source = null;

        try {
            source = new Source();
            String name = channel.getName();
            String websiteUrl = channel.getWebsiteUrl();
            Date lastUpdate = channel.getLastUpdate();
            List<Entry> entries = channel.getEntries();
//            List<Article> articles = preDownloadArticles(channel);

            source.setName(name);
            source.setWebsiteUrl(websiteUrl);
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
