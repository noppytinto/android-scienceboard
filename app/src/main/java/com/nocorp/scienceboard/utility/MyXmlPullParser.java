package com.nocorp.scienceboard.utility;

import android.util.Xml;

import com.nocorp.scienceboard.model.Article;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MyXmlPullParser {
    // We don't use namespaces
    private static final String ns = null;
    private final String IMAGE_TAG = "image";
    private final XmlPullParser parser;
    private final String RDF_TAG = "image";


    public MyXmlPullParser(XmlPullParser parser) {
        this.parser =  Xml.newPullParser();

    }


    public List<Article> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return startReadingFeedWithRssTag(parser);
        } finally {
            in.close();
        }
    }

    private String startParsing(InputStream input) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        org.w3c.dom.Document doc = dBuilder.parse(input);
        org.w3c.dom.Element rootElement = doc.getDocumentElement();
        rootElement.normalize();

        NodeList nList = doc.getElementsByTagName("entry");

        for (int i=0; i<nList.getLength(); i++) {
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                org.w3c.dom.Element childElement = (org.w3c.dom.Element) node;
                String test = getValue("name", childElement);
            }
        }



        return null;

    }


    private static String getValue(String tag, org.w3c.dom.Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }


    private List<Article> startReadingFeedWithRssTag(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Article> entries = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "rss");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("channel")) {
                entries.addAll(readChannel(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    private List<Article> startReadingFeedWithfeedTag(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Article> entries = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "rss");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("channel")) {
                entries.addAll(readChannel(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    private List<Article> startReadingFeedWithRdfTag(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Article> entries = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "rss");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("channel")) {
                entries.addAll(readChannel(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }



    private List<Article> readChannel(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Article> entries = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "channel");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("item")) {
                entries.add(readItem(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
    private Article readItem(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "item");
        String title = null;
        String contentText = null;
        String thumbnailUrl = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("title")) {
                title = readTitle(parser);
            }
            else if(name.equals("content:encoded")) {
                contentText = readContent(parser);
            }
            else if(name.equals("thumb")) {
                thumbnailUrl = readThumbnailUrl(parser);
            }
            else {
                skip(parser);
            }
        }
        return new Article(title, contentText, thumbnailUrl);
    }

    private String getSourceLogo(XmlPullParser parser) throws IOException, XmlPullParserException {
//        parser.require(XmlPullParser.START_TAG, ns, "item");
//        while (parser.next() != XmlPullParser.END_TAG) {
//            if (parser.getEventType() != XmlPullParser.START_TAG) {
//                continue;
//            }
//            String name = parser.getName();
//            if (name.equals("title")) {
//                title = readTitle(parser);
//            }
//            else if(name.equals("content:encoded")) {
//                contentText = readContent(parser);
//            }
//            else if(name.equals("thumb")) {
//                thumbnailUrl = readThumbnailUrl(parser);
//            }
//            else {
//                skip(parser);
//            }
//        }
        return null;
    }

    // Processes title tags in the feed.
    private String readContent(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "content:encoded");
        String unsafeHtmlRawData = readText(parser);
        String safeHtmlRawData = Jsoup.clean(unsafeHtmlRawData, Whitelist.basicWithImages()); // sanitize from xss attacks
        parser.require(XmlPullParser.END_TAG, ns, "content:encoded");

        String text = extractReadableTextFromHtml(safeHtmlRawData);
        List<String> imageUrl = extractImagesUrlFromHtml(safeHtmlRawData);

        return text;
    }

    private String extractReadableTextFromHtml(String htmlString) {
        Document doc = Jsoup.parse(htmlString);
        String text = doc.body().text();

        return text;
    }

    private List<String> extractImagesUrlFromHtml(String htmlString) {
        Document doc = Jsoup.parse(htmlString);
        Elements urls = doc.select("img");

        List<String> imageUrls = new ArrayList<>();

        for(Element el: urls) {
            String url = el.attr("src");
            if(url!=null || url.isEmpty())
                imageUrls.addAll(Collections.singleton(url));
        }

        return new ArrayList<>();
    }

    // Processes thumbnail tags in the feed.
    private String readThumbnailUrl(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "thumb");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "thumb");
        return title;
    }

    private String readImage(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "image");
        String imageUrl = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("url")) {
                imageUrl = readUrl(parser);
            }
            else {
                skip(parser);
            }
        }
        return imageUrl;
    }

    // Processes url tags in the feed.
    private String readUrl(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "url");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "url");
        return title;
    }

    // Processes title tags in the feed.
    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "title");
        return title;
    }

    // For the tags text and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }


}// end XmlParser
