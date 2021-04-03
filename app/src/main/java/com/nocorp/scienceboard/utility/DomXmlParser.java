package com.nocorp.scienceboard.utility;

import com.nocorp.scienceboard.model.xml.Channel;
import com.nocorp.scienceboard.model.xml.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class DomXmlParser implements XmlParser{
    private final String TAG = this.getClass().getSimpleName();
    private final String TITLE_TAG = "title";
    private final String DESCRIPTION_TAG = "description";
    private final String CONTENT_TAG = "content";
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
    private final String IMAGE_TAG = "image";
    private final String IMG_TAG = "img";
    private final String THUMB_TAG = "thumb";
    private final String THUMBNAIL_TAG = "thumbnail";
    private final int ENTRY_LIMIT = 20;

    public Channel getChannelInfo(String inputStream) {
        Channel result = null;
        try {
            Document doc = buildDocument(inputStream);

            List<String> knownChannelTags = new ArrayList<>();
            knownChannelTags.add(CHANNEL_TAG);
            knownChannelTags.add(FEED_TAG);

            for(String candidateChannelTagName: knownChannelTags) {
                NodeList candidatesChannels = doc.getElementsByTagName(candidateChannelTagName);
                if(candidatesChannels!=null || candidatesChannels.getLength()>0) {
                    Node candidateChannel = candidatesChannels.item(0);
                    if(candidateChannel!=null && isElementNode(candidateChannel)){
                        result = buildChannel(candidatesChannels.item(0));
                        break;
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private Channel buildChannel(Node channelNode) throws ParseException {
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
            String stringDate = getLastUpdate(channelNode);
            lastUpdate = convertStringToDate(stringDate);
            String stringDate_2 = getPubDate(channelNode);
            pubDate = convertStringToDate(stringDate_2);
            entries = getEntries(channelNode, ENTRY_LIMIT);

            channel = new Channel();
            channel.setName(name);
            channel.setLanguage(language);
            channel.setWebsiteUrl(websiteUrl);
            channel.setRssUrl(rssUrl);
            channel.setLastUpdate(lastUpdate);
            channel.setPubDate(pubDate);
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
        return getNodeValue(childNodes, DESCRIPTION_TAG);
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
        return getNodeValue(childNodes, CONTENT_TAG);
    }




    private String getEntryTitle(Node entryNode) {
        String result = null;
        if(entryNode==null) return result;
        NodeList childNodes = entryNode.getChildNodes();
        if(childNodes==null || childNodes.getLength()<=0) return result;
        return getNodeValue(childNodes, TITLE_TAG);
    }








    private Document buildDocument(String inputStream) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
        InputStream targetStream = new ByteArrayInputStream(inputStream.getBytes());
        return docBuilder.parse(targetStream);
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
        System.err.println("No known Date format found: " + stringDate);
        return null;
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
                if(nodeName.contains(tag)){
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


}// end DomXmlParser
