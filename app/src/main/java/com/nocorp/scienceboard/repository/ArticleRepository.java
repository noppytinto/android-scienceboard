package com.nocorp.scienceboard.repository;


import com.chimbori.crux.articles.ArticleExtractor;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.model.Source;
import com.nocorp.scienceboard.model.xml.Entry;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.MyOkHttpClient;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndImage;

import org.jdom2.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;

public class ArticleRepository {
    private static final String RAWG_API_KEY = "5c84ab52d6a84d2785b6770d8e52b455&";
    private static final String SEARCH_GAMES_URL = "https://api.rawg.io/api/games?key=" + RAWG_API_KEY + "&page_size=10";
    private final String RAWG_BASE_URL = "https://api.rawg.io";
    private static ArticleRepository singletonInstance;
    private ArticlesFetcher articlesListener;


    private ArticleRepository() {

    }

    public static ArticleRepository getInstance() {
        if(singletonInstance==null)
            return new ArticleRepository();

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

    public void setArticlesListener(ArticlesFetcher listener) {
        this.articlesListener = listener;
    }




    // DOM strategy
    public void getArticles_dom(List<Source> sources, int limit) {
        List<ListItem> limitedArticlesList = new ArrayList<>();
        int counter = 0;

        List<Entry> fullList = combineEntries(sources);

        // sort articles by publication date
        Collections.sort(fullList);

        try {
            if (fullList!=null && fullList.size()>=0) {
                for(Entry entry : fullList) {
                    if(counter == limit) break;
                    Article article = buildArticle(entry);
                    if(article!=null) {
                        limitedArticlesList.add((Article)article);
                    }
                    counter++;
                }
            }

            // publish result
            articlesListener.onFetchCompleted(limitedArticlesList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DOM strategy
    private List<Entry> combineEntries(List<Source> sources) {
        List<Entry> result = new ArrayList<>();

        for(Source source: sources) {
            List<Entry> temp = source.getEntries();
            if(temp!=null && temp.size()>0) {
//                for(Entry entry: temp) entry.setSource(source);
                result.addAll(temp);
            }
        }

        return result;
    }

    // DOM strategy
    private Article buildArticle(Entry entry) {
        Article article = null;

        try {
            String title = entry.getTitle();
            String webpageUrl = entry.getWebpageUrl();
            String thumbnailUrl = entry.getThumbnailUrl();
            Date pubDate = entry.getPubDate();
            Source source = entry.getSource();

            article = new Article();
            article.setThumbnailUrl(thumbnailUrl);
            article.setTitle(title);
            article.setWebpageUrl(webpageUrl);
            article.setPublishDate(pubDate);
            article.setSource(source);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return article;
    }













    public void getArticles(List<Source> sources, int limit) {
        Runnable task = () -> {
            List<ListItem> limitedArticlesList = new ArrayList<>();
            int counter = 0;

            List<Article> fullList = combineArticles(sources);

            // sort articles by publication date
            Collections.sort(fullList);

            try {
                if (fullList!=null && fullList.size()>=0) {
                    for(Article partialArticle : fullList) {
                        if(counter == limit) break;
                        Article completeArticle = downloadAdditionalInformation(partialArticle, partialArticle.getSyndEntry());
                        if(completeArticle!=null) limitedArticlesList.add(completeArticle);
                        counter++;
                    }
                }

                // publish result
                articlesListener.onFetchCompleted(limitedArticlesList);

            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        ThreadManager threadManager = ThreadManager.getInstance();
        threadManager.runTask(task);
    }

    //TODO
    private List<Article> combineArticles(List<Source> sources) {
        return null;
    }




    @NotNull
    private Article downloadAdditionalInformation(Article partialArticle, SyndEntry entry) {
        Article article = null;

        try {
            String title = entry.getTitle();
            String webpageUrl = entry.getLink();
            String thumbnailUrl = getThumbnailUrl(entry);
            String content = getContent(entry);
            String description = getDescription(entry);

            article = new Article();
            article.setThumbnailUrl(thumbnailUrl);
            article.setTitle(title);
            article.setWebpageUrl(webpageUrl);
            article.setContent(content);
            article.setDescription(description);
            article.setSource(partialArticle.getSource());
            article.setPublishDate(partialArticle.getPublishDate());
            article.setSyndEntry(partialArticle.getSyndEntry());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return article;
    }



    private Source buildSource(SyndFeed feed) {
        Source source = null;

        try {
            source = new Source();
            String logoUrl = getLogoUrl(feed);
            String name = feed.getTitle();
            String websiteUrl = feed.getLink();

            source.setLogoUrl(logoUrl);
            source.setName(name);
            source.setWebsiteUrl(websiteUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return source;
    }


    public String getThumbnailUrl(SyndEntry entry) {
        if(entry==null) return null;
        String thumbnailUrl = null;

        // media namespace strategy
        if(hasForeignMarkup("media", "thumb", entry)) {
            thumbnailUrl = extractThumbnailFromForeignMarkup("media","thumb", entry);
        }
        else if(hasForeignMarkup("media", "content", entry)) {
            thumbnailUrl = extractThumbnailFromForeignMarkup("media","content", entry);
        }
//        else if(hasTag("thumb", entry)) {
////            thumbnailUrl = extractThumbnailFromTag("thumb", entry);
//        }
        else if (hasEnclosure(entry)) {
            thumbnailUrl = extractImageEnclosure(entry);
        }
        else {
            thumbnailUrl = extractFirstImageFromEntry(entry);
        }



        return thumbnailUrl;
    }

    /**
     * sometimes the thumbnail is contained in "enclosure" tags
     * @param entry
     * @return
     */
    private boolean hasEnclosure(SyndEntry entry) {
        boolean result = false;

        List<SyndEnclosure> enclosures = entry.getEnclosures();
        if(enclosures.size()!=0) {
            for(SyndEnclosure enclosure: enclosures) {
                String type = enclosure.getType();

                if(type.contains("image") || type.contains("img") || type.contains("thumb")) {
                    return true;
                }
            }
        }

        return result;
    }

    private String extractImageEnclosure(SyndEntry entry) {
        String result = null;

        List<SyndEnclosure> enclosures = entry.getEnclosures();
        if(enclosures.size()!=0) {
            for(SyndEnclosure enclosure: enclosures) {
                String type = enclosure.getType();
                String url = enclosure.getUrl();

                if(type.contains("image") || type.contains("img") || type.contains("thumb")) {
                    return url;
                }
            }
        }

        return result;
    }

    private boolean hasTag(String thumb, SyndEntry entry) {
        boolean result = false;



        return result;

    }


    private String extractFirstImageFromEntry(SyndEntry entry) {
        String imageUrl = null;

        List<String> images = extractImagesUrlFromEntry(entry);
        if(images!=null && images.size()>0)
            imageUrl = images.get(0);

        imageUrl = fixMissingProtocol(imageUrl);

//        if( ! isReachable(imageUrl)) {
//            imageUrl = extractFirstImageFromWebsite(entry.getLink());
//        }

        return imageUrl;
    }

    private String fixMissingProtocol(String imageUrl) {
        if(imageUrl==null) return null;
        try {
            URI uri = new URI(imageUrl);
            String protocol = uri.getScheme();

            if(protocol==null) {
                String host = uri.getHost();
                String sub = uri.getPath();
                String query = uri.getQuery();
                imageUrl = "https://" + host + sub + "?" + query;
            }

//            System.out.println(imageUrl);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return imageUrl;
    }

    private String extractFirstImageFromWebsite(String url) {
        String imageUrl = null;
        try {
            Document document = null;
            document = Jsoup.connect(url).get();
//            String cleanHtmlCode = Jsoup.clean(document.body().toString(), Whitelist.basicWithImages());

            HttpUrl httpUrl = HttpUrl.Companion.parse(url);
            com.chimbori.crux.articles.Article article = new ArticleExtractor(httpUrl, document.html())
                    .extractMetadata()
                    .extractContent()
                    .getArticle();
//
//            String a = article.getDescription();
//            String b = article.getTitle();
            List<com.chimbori.crux.articles.Article.Image> images = article.getImages();
//            HttpUrl c = article.getVideoUrl();
//            Document d = article.getDocument();
//            String e = article.getSiteName();

            if(images.size()>0)
                imageUrl = images.get(0).getSrcUrl().toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageUrl;
    }

    private String extractThumbnailFromForeignMarkup(String prefix, String name, SyndEntry entry) {
        String thumbnailUrl = null;
        List<Element> elements = entry.getForeignMarkup();
        if(elements.size()!=0) {
            for(Element element: elements) {
                String namespacePrefix = element.getNamespacePrefix();
                String namespaceName = element.getName();

                // in case image is in media:content namespace, but with "image" type
                if(namespacePrefix.contains(prefix) && name.contains("content")) {
                    String medium = element.getAttributeValue("medium");
                    if(medium!=null && (medium.contains("img") || medium.contains("image") || medium.contains("thumb"))) {
                        thumbnailUrl = element.getAttributeValue("url");

                    }
                }
                else if(namespacePrefix.contains(prefix) && namespaceName.contains(name)) {
                    thumbnailUrl = element.getAttributeValue("url");
                    break;
                }
            }
        }

        return thumbnailUrl;
    }

    private boolean hasForeignMarkup(String prefix, String name, SyndEntry entry) {
        boolean result = false;

        List<Element> elements = entry.getForeignMarkup();
        if(elements.size()!=0) {
            for(Element element: elements) {
                String namespacePrefix = element.getNamespacePrefix();
                String namespaceName = element.getName();

                if(namespacePrefix.contains(prefix) && namespaceName.contains(name)) {
                    return true;
                }
            }
        }

        return result;
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



    private List<String> extractImagesUrlFromEntry(SyndEntry entry) {
        if(entry==null) return null;
        List<String> imagesUrl = new ArrayList<>();

//        String htmlCode = getHtmlContent(entry);

        String description = getDescription(entry);
        String content = getContent(entry);

        // checking images in description
        if(description != null) {
            Document doc = Jsoup.parse(description);
            Elements urls = doc.select("img");

            for(org.jsoup.nodes.Element el: urls) {
                String url = el.attr("src");
                if(url!=null || url.isEmpty())
                    imagesUrl.addAll(Collections.singleton(url));
            }
        }

        // checking images in content
        if(content != null) {
            Document doc = Jsoup.parse(content);
            Elements urls = doc.select("img");

            for(org.jsoup.nodes.Element el: urls) {
                String url = el.attr("src");
                if(url!=null || url.isEmpty())
                    imagesUrl.addAll(Collections.singleton(url));
            }
        }

        return imagesUrl;
    }

    @Nullable
    private String getHtmlContent(SyndEntry entry) {
        if(entry==null) return null;

        String htmlCode = null;
        String contentType = null;
        SyndContent description = entry.getDescription();
        List<SyndContent> contents = entry.getContents();

        if(description!=null && contents.size()==0) {
            htmlCode = description.getValue();
        }
        else {
            for(SyndContent content: contents) {
                contentType = content.getType();
                if(contentType.equals("html")) {
                    htmlCode = content.getValue();
                    break;
                }
            }
        }
        return htmlCode;
    }

    private String getContent(SyndEntry entry) {
        if(entry==null) return null;

        String result = null;
        String contentType = null;
        List<SyndContent> contents = entry.getContents();

        if(contents!=null && contents.size()>0) {
            for(SyndContent content: contents) {
                if(content!=null) {
                    contentType = content.getType();
                    if(contentType!=null && contentType.equals("html")) {
                        result = content.getValue();
                        break;
                    }
                }
            }
        }

        return result;
    }


    private String getDescription(SyndEntry entry) {
        if(entry==null) return null;

        String result = null;
        SyndContent description = entry.getDescription();
        if(description!=null)
            result = description.getValue();

        return result;
    }

    private List<String> extractImagesUrlFromHtml(String htmlCode) {
        if(htmlCode==null || htmlCode.isEmpty()) return null;

        Document doc = Jsoup.parse(htmlCode);
        Elements urls = doc.select("img");
        List<String> imagesUrl = new ArrayList<>();

        for(org.jsoup.nodes.Element el: urls) {
            String url = el.attr("src");
            if(url!=null || url.isEmpty())
                imagesUrl.addAll(Collections.singleton(url));
        }

        return imagesUrl;
    }

    static public boolean isServerReachable(String url) {
//        try {
//            URL urlServer = new URL(serverUrl);
//            HttpURLConnection urlConn = (HttpURLConnection) urlServer.openConnection();
//            urlConn.setConnectTimeout(1000); //<- 1 Seconds Timeout
//            urlConn.connect();
//            if (urlConn.getResponseCode() == 200) {
//                return true;
//            } else {
//                return false;
//            }
//        } catch (MalformedURLException e1) {
//            return false;
//        } catch (IOException e) {
//            return false;
//        }

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(150);
            connection.setReadTimeout(150);
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (IOException exception) {
            return false;
        }
    }

    public static boolean isPingable(String host) {
        try (Socket socket = new Socket()) {
            URI uri = new URI(host);
            String protocol = uri.getScheme();

            if(protocol.equals("http"))
                socket.connect(new InetSocketAddress(host, 80), 150);
            else if(protocol.equals("https"))
                socket.connect(new InetSocketAddress(host, 443), 150);
            return true;
        } catch (IOException | URISyntaxException e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }

    public boolean isReachable(String url) {
        Response response = null;
        boolean result = false;

        try {
            // build httpurl and request for remote db
            HttpUrl httpUrl = buildHttpURL(url);
            final OkHttpClient httpClient = com.nocorp.scienceboard.utility.MyOkHttpClient.getClient();
            Request request = buildRequest(httpUrl);

            // performing request
            response = httpClient.newCall(request).execute();

            // check responses
            if (response.isSuccessful()) {
                result = true;
            }
            // if the response is unsuccesfull
            else result = false;
        }
        catch (ConnectException ce) {
            ce.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
        }

        return result;
    }

    public static HttpUrl buildHttpURL(String url) {
        HttpUrl httpUrl = new HttpUrl.Builder()
                .host(url)
                .build();

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





    private RetrofitAPI buildRetrofitService(String baseUrl) {
        final Retrofit retrofitClient = new Retrofit.Builder()
                .baseUrl("https://www.hdblog.it/")
                .client(MyOkHttpClient.getClient())
                .build();

        return retrofitClient.create(RetrofitAPI.class);
    }

}
