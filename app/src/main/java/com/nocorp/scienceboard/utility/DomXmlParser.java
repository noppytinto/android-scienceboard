package com.nocorp.scienceboard.utility;

import android.util.Log;

import com.nocorp.scienceboard.model.xml.Channel;
import com.nocorp.scienceboard.model.xml.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private final String CHANNEL_TAG = "channel";
    private final String FEED_TAG = "feed";
    private final String ITEM_TAG = "item";
    private final String ENTRY_TAG = "entry";
    private final String PUBDATE_TAG = "pubDate";
    private final String LAST_BUILD_DATE_TAG = "lastBuildDate";
    private final String UPDATE_TAG = "update";
    private final String MEDIA_CONTENT_TAG = "media:content";
    private final String CONTENT_MEDIUM_TAG = "content:medium";
    private final String IMAGE_TAG = "image";
    private final String IMG_TAG = "img";
    private final String THUMB_TAG = "thumb";
    private final String THUMBNAIL_TAG = "thumbnail";
    private final String TMB_TAG = "tmb";

    //
    private final String HREF_ATTRIBUTE = "href";
    private final String URL_ATTRIBUTE = "url";
    private final String IMAGE_TYPE = "image";

    private final int ENTRY_LIMIT = 10;








    public Channel getChannel(String rssUrl) {
        Channel result = null;
        InputStream inputStream = null;
        Response response = null;

        try {
            String sanitizedUrl = HttpUtilities.sanitizeUrl(rssUrl);
            HttpUrl httpUrl = buildHttpURL(sanitizedUrl);
            final OkHttpClient httpClient = MyOkHttpClient.getClient();
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
                                    result = buildChannel(candidatesChannels.item(0), rssUrl);
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



    private boolean checkChannelLastUpdate(Node nodeChannel) {
        Date newDate = null;
        Date oldDate = null;





        return false;
    }



    private Channel buildChannel(Node channelNode, String url) throws ParseException {
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
            entries = getEntries(channelNode, ENTRY_LIMIT);

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
            if(lastUpdate==null) {
                if(entries!=null && entries.size()>0)
                    lastUpdate = entries.get(0).getPubDate();
            }
            channel.setLastUpdate(lastUpdate);
//            channel.setPubDate(pubDate);
            channel.setEntries(entries);
        }


        return channel;
    }

    private List<Entry> getEntries(Node channelNode, int limit) {
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
                            if(entry!=null) results.add(entry);
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
            webpageUrl = getWebUrl(entryNode);
            String stringDate = getPubDate(entryNode);
            pubDate = convertStringToDate(stringDate);
            thumbnailUrl = getThumbnailUrl(entryNode);

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

    private String getThumbnailUrl(Node entryNode) {
        String result = null;
        if(entryNode==null) return result;
        NodeList childNodes = entryNode.getChildNodes();
        if(childNodes==null || childNodes.getLength()<=0) return result;

        // classic tag strategy
        result = getNodeValue(childNodes, THUMB_TAG, THUMBNAIL_TAG, IMAGE_TAG, IMG_TAG);

        // crawling html strategy
        if(result==null || result.isEmpty()) {

        }

        // attribute strategy
        if(result==null || result.isEmpty()) {

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

        stringDate = fixEDTtimezone(stringDate); // EDT timezone is not supported by java

        List<SimpleDateFormat> knownPatterns = new ArrayList<>();
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm.ss'Z'"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"));
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
        Log.d(TAG, "convertStringToDate: No known Date format found for: " + stringDate);
        return null;
    }

    private String fixEDTtimezone(String stringDate) {
        String result = stringDate;
        if(stringDate!=null && stringDate.contains("EDT")) {
            result = stringDate.replace("EDT", "-0400");
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
        return getNodeValue(childNodes, LINK_TAG);
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
        return getNodeValue(childNodes, PUBDATE_TAG);
    }



    private String getNodeValue(NodeList nodeList, String... nodeName) {
        String result = null;
        if(nodeName==null || nodeName.length <= 0) return result;
        if(nodeList==null || nodeList.getLength()<=0) return result;

        for(int i=0; i<nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if(nodeContainsTheseTags(currentNode, nodeName)){
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
                if(currentNode.hasChildNodes() && isAttributeNode(currentNode.getFirstChild())) {
                    result = currentNode.getFirstChild().getNodeValue();
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

    private String inputStreamToString(InputStream inputStream) throws IOException {
        String result = org.apache.commons.io.IOUtils.toString(inputStream, StandardCharsets.UTF_8);

        return result;
    }


}// end DomXmlParser
