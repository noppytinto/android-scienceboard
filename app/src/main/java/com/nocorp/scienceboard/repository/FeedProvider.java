package com.nocorp.scienceboard.repository;

import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.utility.HttpUtilities;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndImage;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
        sourceUrls.add(spacenews);
        sourceUrls.add(space);
        sourceUrls.add(phys_org_space);
        sourceUrls.add(newscientist_space);
        sourceUrls.add(esa_space_news);
        sourceUrls.add(wired);
        sourceUrls.add(nvidiaBlog);
        sourceUrls.add(hdblog);
        sourceUrls.add(theverge);
        sourceUrls.add(nature);
        sourceUrls.add(livescience);

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



    private Source downloadFeed(String rssUrl) {
        Source source = null;

        try {
            String sanitizedUrl = HttpUtilities.sanitizeUrl(rssUrl);
            SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(sanitizedUrl)));
            if (feed!=null) {
                source = buildSource(feed);
            }

        } catch (FeedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
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
            source.setArticles(articles);
            source.setEntries(entries);
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




}// end SourceProvider
