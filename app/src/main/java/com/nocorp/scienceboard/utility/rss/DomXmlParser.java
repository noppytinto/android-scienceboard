package com.nocorp.scienceboard.utility.rss;

import android.content.Context;
import android.util.Log;

import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.utility.room.ChannelDao;
import com.nocorp.scienceboard.utility.room.EntryDao;
import com.nocorp.scienceboard.utility.room.ScienceBoardRoomDatabase;
import com.nocorp.scienceboard.utility.rss.model.Channel;
import com.nocorp.scienceboard.utility.rss.model.Entry;
import com.nocorp.scienceboard.utility.HttpUtilities;
import com.nocorp.scienceboard.utility.MyOkHttpClient;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DomXmlParser implements XmlParser{
    private final String TAG = this.getClass().getSimpleName();
    private final String TITLE_TAG = "title";
    private final String DESCRIPTION_TAG = "description";
    private final String CONTENT_TAG = "content";
    private final String CONTENT_ENCODED_TAG = "content:encoded";
    private final String LANGUAGE_TAG = "language";
    private final String LINK_TAG = "link";
    private final String ID_TAG = "id";
    private final String CHANNEL_TAG = "channel";
    private final String FEED_TAG = "feed";
    private final String ITEM_TAG = "item";
    private final String ENTRY_TAG = "entry";
    private final String PUBDATE_TAG = "pubDate";
    private final String PUBLISHED_TAG = "published";
    private final String DC_DATE_TAG = "dc:date";
    private final String LAST_BUILD_DATE_TAG = "lastBuildDate";
    private final String UPDATE_TAG = "update";
    private final String MEDIA_CONTENT_TAG = "media:content";
    private final String CONTENT_MEDIUM_TAG = "content:medium";
    private final String MEDIA_THUMBNAIL_TAG = "media:thumbnail";
    private final String IMAGE_TAG = "image";
    private final String IMG_TAG = "img";
    private final String THUMB_TAG = "thumb";
    private final String THUMBNAIL_TAG = "thumbnail";
    private final String TMB_TAG = "tmb";
    private final String ENCLOSURE_TAG = "enclosure";

    //
    private final String HREF_ATTRIBUTE = "href";
    private final String URL_ATTRIBUTE = "url";
    private final String IMAGE_TYPE = "image";
    private final String SRC_ATTRIBUTE = "src";
    private final String IMG_ATTRIBUTE = "img";

    private final int ENTRIES_LIMIT = 10;



    //-------------------------------------------------------------- CONSTRUCTORS




    //-------------------------------------------------------------- GETTERS/SETTERS

    @Override
    public Channel getChannel(String rssUrl, Context context) {
        Channel result = null;
        InputStream inputStream = null;
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
                        inputStream = responseBody.byteStream();

                        Document doc = buildDocument(inputStream);
                        List<String> knownChannelTags = new ArrayList<>();
                        knownChannelTags.add(CHANNEL_TAG);
                        knownChannelTags.add(FEED_TAG);

                        for(String candidateChannelTagName: knownChannelTags) {
                            NodeList candidatesChannels = doc.getElementsByTagName(candidateChannelTagName);
                            if(candidatesChannels!=null || candidatesChannels.getLength()>0) {
                                Node candidateChannel = candidatesChannels.item(0);
                                if(candidateChannel!=null && isElementNode(candidateChannel)){
                                    result = buildChannel(candidatesChannels.item(0), rssUrl, context);
                                    if(result!=null) {
                                        saveChannelInRoom(result, context);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
        }

        return result;
    }

    private void saveChannelInRoom(Channel channel, Context context) {
        ChannelDao channelDao = getChannelDao(context);
        Runnable task = () -> {
            channelDao.insert(channel);
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ChannelDao getChannelDao(Context context) {
        ScienceBoardRoomDatabase roomDatabase = ScienceBoardRoomDatabase.getInstance(context);
        ChannelDao dao = roomDatabase.getChannelDao();
        return dao;
    }

    private void saveEntryInRoom(Entry entry, Context context) {
        EntryDao entryDao = getEntryDao(context);
        Runnable task = () -> {
            entryDao.insert(entry);
        };

        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTask(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private EntryDao getEntryDao(Context context) {
        ScienceBoardRoomDatabase roomDatabase = ScienceBoardRoomDatabase.getInstance(context);
        EntryDao dao = roomDatabase.getEntryDao();
        return dao;
    }



    //--------------------------------------------------------------

    private boolean checkChannelLastUpdate(Node nodeChannel) {
        Date newDate = null;
        Date oldDate = null;





        return false;
    }

    private Channel buildChannel(Node channelNode, String url, Context context) throws ParseException {
        Channel channel = null;
        String name = null;
        String language = null;
        String websiteUrl = null;
        String rssUrl = null;
        Date lastUpdate = null;
        Date pubDate = null;
        List<Entry> entries = null;

        if(channelNode != null) {
            name = getChannelName(channelNode);
            language = getLanguage(channelNode);
            websiteUrl = getWebUrl(channelNode);
            rssUrl = url;
            String stringDate = getLastUpdate(channelNode);
            lastUpdate = convertStringToDate(stringDate);
//            String stringDate_2 = getPubDate(channelNode);
//            pubDate = convertStringToDate(stringDate_2);

            channel = new Channel();
            channel.setName(name);
            channel.setLanguage(language);
            if(websiteUrl==null || websiteUrl.isEmpty()){
                try {
                    websiteUrl = HttpUtilities.getBaseUrl(url);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            channel.setWebsiteUrl(websiteUrl);
            channel.setRssUrl(rssUrl);


            entries = getEntries(channelNode, ENTRIES_LIMIT, context);
            if(lastUpdate==null) {
                if(entries!=null && entries.size()>0)
                    lastUpdate = entries.get(0).getPubDate();
            }
            channel.setLastUpdate(lastUpdate);
//            channel.setPubDate(pubDate);
            entries = setChannel(entries, channel);
            channel.setEntries(entries);
        }


        return channel;
    }

    private List<Entry> setChannel(List<Entry> entries, Channel channel) {
        if(entries!=null && entries.size()>0){
            for(Entry entry: entries){
                entry.setChannel(channel);
            }
        }

        return entries;
    }

    private List<Entry> getEntries(Node channelNode, int limit, Context context) {
        List<Entry> results = null;

        try {
            results = new ArrayList<>();
            Document document = channelNode.getOwnerDocument();
            List<String> knownEntryTags = new ArrayList<>();
            knownEntryTags.add(ENTRY_TAG);
            knownEntryTags.add(ITEM_TAG);

            for(String candidateEntryTag: knownEntryTags) {
                NodeList entryNodes = document.getElementsByTagName(candidateEntryTag);
                if(entryNodes!=null) {
                    for(int i=0; i<entryNodes.getLength(); i++) {
                        Node entryNode = entryNodes.item(i);
                        if(entryNode!=null && isElementNode(entryNode)){
                            if(i>=limit) break;
                            Entry entry = buildEntry(entryNode);
                            if(entry!=null) {
                                results.add(entry);
                                saveEntryInRoom(entry, context);
                            }
                        }
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    private Entry buildEntry(Node entryNode) {
        Entry entry = null;
        String title = null;
        String description = null;
        String content = null;

        String webpageUrl = null;
        Date pubDate = null;
        String thumbnailUrl = null;

        if(entryNode != null) {
            title = getEntryTitle(entryNode);
            description = getDescription(entryNode);
            content = getContent(entryNode);
            String unsafeUrl = getWebUrl(entryNode);
            if( ! HttpUtilities.urlIsSafe(unsafeUrl))
                webpageUrl = unsafeUrl.replace("http://", "https://");
            else {
                webpageUrl = unsafeUrl;
            }
            String stringDate = getPubDate(entryNode);
            pubDate = convertStringToDate(stringDate);
            thumbnailUrl = getThumbnailUrl(entryNode, content, description);

            entry = new Entry();
            entry.setTitle(title);
            entry.setDescription(description);
            entry.setContent(content);
            entry.setWebpageUrl(webpageUrl);
            entry.setPubDate(pubDate);
            entry.setThumbnailUrl(thumbnailUrl);
        }

        return entry;
    }

    private String getThumbnailUrl(Node entryNode, String content, String description) {
        String result = null;
        if(entryNode==null) return result;
        NodeList childNodes = entryNode.getChildNodes();
        if(childNodes==null || childNodes.getLength()<=0) return result;

        // classic tag strategy
        result = getNodeValue(childNodes, THUMB_TAG, THUMBNAIL_TAG, IMAGE_TAG, IMG_TAG, CONTENT_MEDIUM_TAG);

        // attribute strategy
        if(result==null || result.isEmpty()) {
            result = getAttributeValue(childNodes, THUMB_TAG, THUMBNAIL_TAG, IMAGE_TAG, IMG_TAG, MEDIA_CONTENT_TAG, CONTENT_MEDIUM_TAG, ENCLOSURE_TAG, MEDIA_THUMBNAIL_TAG);
        }

        // crawling html strategy from content/description
        if(result==null || result.isEmpty()) {
            result = extractFirstImageFromHtml(content, description);
        }



        return result;
    }

    private String getDescription(Node entryNode) {
        String result = null;
        if(entryNode==null) return result;
        NodeList childNodes = entryNode.getChildNodes();
        if(childNodes==null || childNodes.getLength()<=0) return result;
        return getNodeValue(childNodes, DESCRIPTION_TAG);
    }

    private String getContent(Node entryNode) {
        String result = null;
        if(entryNode==null) return result;
        NodeList childNodes = entryNode.getChildNodes();
        if(childNodes==null || childNodes.getLength()<=0) return result;
        return getNodeValue(childNodes, CONTENT_TAG, CONTENT_ENCODED_TAG);
    }

    private String getEntryTitle(Node entryNode) {
        String result = null;
        if(entryNode==null) return result;
        NodeList childNodes = entryNode.getChildNodes();
        if(childNodes==null || childNodes.getLength()<=0) return result;
        return getNodeValue(childNodes, TITLE_TAG);
    }

    private Document buildDocument(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
        return docBuilder.parse(inputStream);
    }

    private boolean isElementNode(Node node) {
        return (node.getNodeType() == Node.ELEMENT_NODE);
    }

    private boolean isTextNode(Node node) {
        return (node.getNodeType() == Node.TEXT_NODE);
    }

    private boolean isCdataNode(Node node) {
        return (node.getNodeType() == Node.CDATA_SECTION_NODE);
    }

    private boolean isAttributeNode(Node node) {
        return (node.getNodeType() == Node.ATTRIBUTE_NODE);
    }

    private Date convertStringToDate(String stringDate) {
        if(stringDate==null || stringDate.isEmpty()) return null;

        stringDate = fixIncompatibleTimezones(stringDate); // EDT/EDS timezone are not supported by java

        List<SimpleDateFormat> knownPatterns = new ArrayList<>();
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm.ss'Z'"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"));
        knownPatterns.add(new SimpleDateFormat("EEE dd MMM yyyy HH:mm Z"));
        knownPatterns.add(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm Z"));
        knownPatterns.add(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z"));
        knownPatterns.add(new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss Z"));
        knownPatterns.add(new SimpleDateFormat("EEEE, dd MMM yyyy HH:mm:ss Z"));
        knownPatterns.add(new SimpleDateFormat("EEEE dd MMM yyyy HH:mm:ss Z"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd"));
        knownPatterns.add(new SimpleDateFormat("yyyy/MM/dd"));
        knownPatterns.add(new SimpleDateFormat("dd-MM-yyyy"));
        knownPatterns.add(new SimpleDateFormat("dd/MM/yyyy"));
        knownPatterns.add(new SimpleDateFormat("ddMMyyyy"));
        knownPatterns.add(new SimpleDateFormat("yyyyMMdd"));

        for (SimpleDateFormat currentPattern : knownPatterns) {
            try {
                // Take a try
                return new Date(currentPattern.parse(stringDate).getTime());

            } catch (ParseException pe) {
                // Loop on
            }
        }
        Log.d(TAG, "SCIENCE_BOARD - convertStringToDate: No known Date format found for: " + stringDate);
        return null;
    }

    /**
     * EDT/EDS timezone are not supported by java
     * @param stringDate
     * @return
     */
    private String fixIncompatibleTimezones(String stringDate) {
        String result = stringDate;
        if(stringDate!=null) {
            if(stringDate.contains("EDT"))
                result = stringDate.replace("EDT", "-0400");
            else if(stringDate.contains("EDS"))
                result = stringDate.replace("EDS", "-0500");
        }


        return result;
    }

    private String getChannelName(Node nodeChannel) {
        String result = null;
        if(nodeChannel==null) return result;
        NodeList childNodes = nodeChannel.getChildNodes();
        if(childNodes==null || childNodes.getLength()<=0) return result;
        return getNodeValue(childNodes, TITLE_TAG);
    }

    private String getLanguage(Node channelNode) {
        String result = null;
        if(channelNode==null) return result;
        NodeList childNodes = channelNode.getChildNodes();
        if(childNodes==null || childNodes.getLength()<=0) return result;
        return getNodeValue(childNodes, LANGUAGE_TAG);
    }

    private String getWebUrl(Node node) {
        String result = null;
        if(node==null) return result;
        NodeList childNodes = node.getChildNodes();
        if(childNodes==null || childNodes.getLength()<=0) return result;

//        // classic tag strategy
//        result = getNodeValue(childNodes, LINK_TAG, ID_TAG);
//
//        // attribute strategy
//        if(result==null || result.isEmpty()) {
//            result = getAttributeValue(childNodes, LINK_TAG);
//        }
//
        return getNodeValue(childNodes, LINK_TAG, ID_TAG);
    }

    private String getLastUpdate(Node channelNode) {
        String result = null;
        if(channelNode==null) return result;
        NodeList childNodes = channelNode.getChildNodes();
        if(childNodes==null || childNodes.getLength()<=0) return result;
        return getNodeValue(childNodes, LAST_BUILD_DATE_TAG, UPDATE_TAG);
    }

    private String getPubDate(Node node) {
        String result = null;
        if(node==null) return result;
        NodeList childNodes = node.getChildNodes();
        if(childNodes==null || childNodes.getLength()<=0) return result;
        return getNodeValue(childNodes, PUBDATE_TAG, PUBLISHED_TAG, DC_DATE_TAG);
    }

    private String getNodeValue(NodeList nodeList, String... nodeNames) {
        String result = null;
        if(nodeNames==null || nodeNames.length <= 0) return result;
        if(nodeList==null || nodeList.getLength()<=0) return result;

        for(int i=0; i<nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if(nodeContainsTheseTags(currentNode, nodeNames)){
                if(currentNode.hasChildNodes() && isTextNode(currentNode.getFirstChild())) {
                    result = currentNode.getFirstChild().getNodeValue();
                    return result;
                }
                else if(currentNode.hasChildNodes() && isCdataNode(currentNode.getFirstChild())) {
                    result = currentNode.getFirstChild().getTextContent();
                    return result;
                }
            }
        }

        return result;
    }

    private String getAttributeValue(NodeList nodeList, String... nodeName) {
        String result = null;
        if(nodeName==null || nodeName.length <= 0) return result;
        if(nodeList==null || nodeList.getLength()<=0) return result;

        for(int i=0; i<nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if(nodeContainsTheseTags(currentNode, nodeName)){
                if(currentNode.hasAttributes()) {
                    NamedNodeMap attributes = currentNode.getAttributes();
                    Node urlAttribute = attributes.getNamedItem("url");
                    Node typeAttribute = attributes.getNamedItem("type");

                    if(urlAttribute!=null) {
                        if(nodeName.equals(ENCLOSURE_TAG) &&
                                (typeAttribute!=null && typeAttribute.getNodeValue().contains(IMG_ATTRIBUTE))) {
                            result = attributes.getNamedItem("url").getNodeValue();
                        }
                        else {
                            result = attributes.getNamedItem("url").getNodeValue();
                        }
                    }

                    return result;
                }
                else if(currentNode.hasChildNodes() && isCdataNode(currentNode.getFirstChild())) {
                    result = currentNode.getFirstChild().getTextContent();
                    return result;
                }
            }
        }

        return result;
    }

    private boolean nodeContainsTheseTags(Node node, String... tags) {
        boolean result = false;
        if(tags==null || tags.length<=0) return result;
        if(node==null) return result;

        String nodeName = node.getNodeName();

        if(nodeName!=null && !nodeName.isEmpty()){
            for(String tag: tags) {
                if(nodeName.equals(tag)){
                    return true;
                }
            }
        }

        return result;
    }

    private boolean nodeContainsTheseAttributes(Node node, String... attributes) {
        boolean result = false;
        if(attributes==null || attributes.length<=0) return result;
        if(node==null) return result;

        String nodeName = node.getNodeName();

        if(nodeName!=null && !nodeName.isEmpty()){
            for(String tag: attributes) {
                if(nodeName.contains(tag)){
                    return true;
                }
            }
        }

        return result;
    }

    private List<String> extractImagesUrlFromHtml(String content, String description) {
        List<String> imagesUrl = new ArrayList<>();

        // checking images in description
        if(description != null) {
            org.jsoup.nodes.Document doc = Jsoup.parse(description);
            Elements urls = doc.select(IMG_ATTRIBUTE);

            for(org.jsoup.nodes.Element el: urls) {
                String url = el.attr(SRC_ATTRIBUTE);
                if(url!=null || url.isEmpty())
                    imagesUrl.addAll(Collections.singleton(url));
            }
        }

        // checking images in content
        if(content != null) {
            org.jsoup.nodes.Document doc = Jsoup.parse(content);
            Elements urls = doc.select(IMG_ATTRIBUTE);

            for(org.jsoup.nodes.Element el: urls) {
                String url = el.attr(SRC_ATTRIBUTE);
                if(url!=null || url.isEmpty())
                    imagesUrl.addAll(Collections.singleton(url));
            }
        }

        return imagesUrl;
    }

    private String extractFirstImageFromHtml(String content, String description) {
        String imageUrl = null;

        List<String> images = extractImagesUrlFromHtml(content, description);
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


}// end DomXmlParser
