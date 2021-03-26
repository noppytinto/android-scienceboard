package com.nocorp.scienceboard.ui.home;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.utility.HttpUtilities;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndImage;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import org.jdom2.Element;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private WebView webView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webView = view.findViewById(R.id.webView);

        Runnable task = () -> {
            try {
                int index = 1;
                String feedTag = "https://www.theverge.com/rss/index.xml";
                String rdfTag = "https://www.nature.com/nmat.rss"; // unsecure (HTTP)
                String rssTag = "https://home.cern/api/news/news/feed.rss";
                String malformedRss = "https://www.theverge.com/";
                String wired = "https://www.wired.com/feed/rss";

                String inputUrl = "https://feeds.feedburner.com/nvidiablog";

                String sanitizedUrl = HttpUtilities.sanitizeUrl(inputUrl);

                SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(sanitizedUrl)));
                String logo = getLogoUrl(feed);
                List<SyndEntry> entries = feed.getEntries();
                SyndEntry entry = entries.get(index);
                String title = entry.getTitle();
                String articleLink = entry.getLink();
                List<Element> thumbnailUrl = entry.getForeignMarkup();
                List<SyndContent> description = entry.getContents();

                String htmlContent = null;
                String contentType = null;
                String contentMode = null;
                String contentText = null;
                List<String> imagesUrls = null;

                if(description.size()!=0) {
                    SyndContent content = description.get(0);
                    htmlContent = content.getValue();
                    contentType = content.getType();
                    contentMode = content.getMode();
                    contentText = extractReadableTextFromHtml(htmlContent);
                    imagesUrls = extractImagesUrlFromHtml(htmlContent);

                }
                else {
                    SyndContent description2 = entries.get(index).getDescription();
                    SyndContent content = description2;
                    htmlContent = content.getValue();
                    contentType = content.getType();
                    contentMode = content.getMode();
                    contentText = extractReadableTextFromHtml(htmlContent);
                    imagesUrls = extractImagesUrlFromHtml(htmlContent);
                }

                onFeedDownloaded(htmlContent);


//                Elements elements = extractElements(htmlContent);

//                System.out.println(description);


            } catch (FeedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        };

        ThreadManager threadManager = ThreadManager.getInstance();
        threadManager.runTaskInPool(task);
    }
    private void onFeedDownloaded(String htmlContent) {

        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WebSettings webSettings = webView.getSettings();
                webSettings.setJavaScriptEnabled(false);
                webView.loadData(htmlContent,
                        "text/html", "UTF-8");
            }
        });
    }

    private String extractReadableTextFromHtml(String htmlCode) {
        Document doc = Jsoup.parse(htmlCode);
        String text = doc.body().text();

        return text;
    }

    private Elements extractElements(String htmlCode) {
        Document doc = Jsoup.parse(htmlCode);
        Elements elements = doc.body().getAllElements();

        return elements;
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
}