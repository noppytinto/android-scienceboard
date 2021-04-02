package com.nocorp.scienceboard.utility;

import android.util.Log;

import com.nocorp.scienceboard.model.Channel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DomXmlParser implements XmlParser{
    private final String TAG = this.getClass().getSimpleName();
    private final String TITLE_TAG = "title";
    private final String LANGUAGE_TAG = "language";
    private final String LINK_TAG = "link";
    private final String CHANNEL_TAG = "channel";
    private final String FEED_TAG = "feed";

    private final String ITEM_TAG = "item";
    private final String ENTRY_TAG = "entry";
    private final String PUBDATE_TAG = "pubDate";
    private final String LAST_BUILD_DATE_TAG = "lastBuildDate";
    private final String UPDATE_TAG = "update";

    public Channel getChannelInfo(String inputStream) {
        Channel result = null;
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
            InputStream targetStream = new ByteArrayInputStream(inputStream.getBytes());
            Document doc = docBuilder.parse(targetStream);

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
            Log.d(TAG, "parseChannel: ");
        }

        return result;
    }

    private boolean isElementNode(Node node) {
        return (node.getNodeType() == Node.ELEMENT_NODE);
    }

    private boolean isTextNode(Node node) {
        return (node.getNodeType() == Node.TEXT_NODE);
    }

    private boolean isAttributeNode(Node node) {
        return (node.getNodeType() == Node.ATTRIBUTE_NODE);
    }


    private Channel buildChannel(Node nodeChannel) throws ParseException {
        Channel channel = null;
        String name = null;
        String language = null;
        String websiteUrl = null;
        String rssUrl = null;
        Date lastUpdate = null;
        Date pubDate = null;

        if(nodeChannel != null) {
            name = getChannelName(nodeChannel);
            language = getLanguage(nodeChannel);
            websiteUrl = getWebsiteUrl(nodeChannel);
            String stringDate = getLastUpdate(nodeChannel);
            lastUpdate = convertStringToDate(stringDate);
            String stringDate_2 = getPubDate(nodeChannel);
            pubDate = convertStringToDate(stringDate_2);

            channel = new Channel();
            channel.setName(name);
            channel.setLanguage(language);
            channel.setWebsiteUrl(websiteUrl);
            channel.setRssUrl(rssUrl);
            channel.setLastUpdate(lastUpdate);
            channel.setPubDate(pubDate);

        }


        return channel;
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

    private String getLanguage(Node nodeChannel) {
        String result = null;
        if(nodeChannel==null) return result;
        NodeList childNodes = nodeChannel.getChildNodes();
        if(childNodes==null || childNodes.getLength()<=0) return result;
        return getNodeValue(childNodes, LANGUAGE_TAG);
    }

    private String getWebsiteUrl(Node nodeChannel) {
        String result = null;
        if(nodeChannel==null) return result;
        NodeList childNodes = nodeChannel.getChildNodes();
        if(childNodes==null || childNodes.getLength()<=0) return result;
        return getNodeValue(childNodes, LINK_TAG);
    }

    private String getLastUpdate(Node nodeChannel) {
        String result = null;
        if(nodeChannel==null) return result;
        NodeList childNodes = nodeChannel.getChildNodes();
        if(childNodes==null || childNodes.getLength()<=0) return result;
        return getNodeValue(childNodes, LAST_BUILD_DATE_TAG, UPDATE_TAG);
    }

    private String getPubDate(Node nodeChannel) {
        String result = null;
        if(nodeChannel==null) return result;
        NodeList childNodes = nodeChannel.getChildNodes();
        if(childNodes==null || childNodes.getLength()<=0) return result;
        return getNodeValue(childNodes, PUBDATE_TAG);
    }



    private String getNodeValue(NodeList nodeList, String... nodeName) {
        String result = null;
        if(nodeName==null || nodeName.length <= 0) return result;
        if(nodeList==null || nodeList.getLength()<=0) return result;

        for(int i=0; i<nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if(nodeContainsTheseTag(currentNode, nodeName)){
                if(currentNode.hasChildNodes() && isTextNode(currentNode.getFirstChild())) {
                    result = currentNode.getFirstChild().getNodeValue();
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
            if(nodeContainsTheseTag(currentNode, nodeName)){
                if(currentNode.hasChildNodes() && isAttributeNode(currentNode.getFirstChild())) {
                    result = currentNode.getFirstChild().getNodeValue();
                    return result;
                }
            }
        }

        return result;
    }

    private boolean nodeContainsTheseTag(Node node, String... tags) {
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
