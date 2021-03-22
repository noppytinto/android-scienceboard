package com.nocorp.scienceboard;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nocorp.scienceboard.system.ThreadManager;
import com.nocorp.scienceboard.utility.HttpUtilities;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndImage;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();


        Runnable task = () -> {
            try {
                String feedTag = "https://www.theverge.com/rss/index.xml";
                String rdfTag = "http://feeds.nature.com/nature/rss/current?format=xml"; // unsecure (HTTP)
                String rssTag = "https://home.cern/api/news/news/feed.rss";
                String malformedRss = "https://www.theverge.com/";

                String inputUrl = rdfTag;

                String sanitizedUrl = HttpUtilities.sanitizeUrl(inputUrl);

                SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(sanitizedUrl)));
                System.out.println(feed.getTitle());
                String logo = getLogoUrl(feed);
                System.out.println(logo);
                List<SyndEntry> entries = feed.getEntries();
                System.out.println(entries);

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

    private String getLogoUrl(SyndFeed feed) {
        String logo = null;
        SyndImage syndImage = feed.getIcon();
        if (syndImage!=null) {
            logo = syndImage.getUrl();
        }
        else {
            syndImage = feed.getImage();
            if (syndImage!=null) {
                logo = syndImage.getUrl();
            }
        }
        return logo;
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

}