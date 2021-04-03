package com.nocorp.scienceboard.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.utility.ad.admob.AdProvider;
import com.nocorp.scienceboard.viewpager.HomeViewPagerAdapter;


public class HomeFragment extends Fragment{
    private final String TAG = this.getClass().getSimpleName();
    private HomeViewPagerAdapter viewPagerAdapter;
    private ViewPager2 viewPager;
    private HomeViewModel homeViewModel;
    private View view;
    private AdProvider adProvider;
    private TabLayout tabLayout;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        adProvider = AdProvider.getInstance(); // is not guaranteed that
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

        NavController navController = Navigation.findNavController(view);
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).build();
        Toolbar toolbar = view.findViewById(R.id.toolbar_homeFragment);

        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);


        tabLayout = view.findViewById(R.id.tablayout_homeFragment);
        viewPager = view.findViewById(R.id.viewPager_homeFragment);
        FragmentManager fm = getChildFragmentManager();
        Lifecycle lifecycle = getViewLifecycleOwner().getLifecycle();
        viewPagerAdapter = new HomeViewPagerAdapter(fm, lifecycle);
        viewPager.setUserInputEnabled(false); // disables horiz. swipe to scroll tabs gestures
        viewPager.setAdapter(viewPagerAdapter);


        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("ALL");
                        break;
                    case 1:
                        tab.setText("Tech");
                        break;
                    case 2:
                        tab.setText("Physics");
                        break;
                    case 3:
                        tab.setText("Space");
                        break;
                    case 4:
                        tab.setText("Biology");
                        break;
                    case 5:
                        tab.setText("Medicine");
                        break;
                }
            }
        });
        tabLayoutMediator.attach();





    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }




    protected boolean isDestroyed() {
        return (this.isRemoving() || this.getActivity() == null || this.isDetached() || !this.isAdded() || this.getView() == null);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        adProvider.destroyAds();
    }

    @Override
    public void onResume() {
        super.onResume();
    }











    //--------------------------------------------------------------------- methods
//
//    private void testWebview() {
//        Runnable task = () -> {
//            try {
//                int index = 0;
//                String feedTag = "https://www.theverge.com/rss/index.xml";
//                String rdfTag = "https://www.nature.com/nmat.rss"; // unsecure (HTTP)
//                String rssTag = "https://home.cern/api/news/news/feed.rss";
//                String malformedRss = "https://www.theverge.com/";
//                String wired = "https://www.wired.com/feed/rss";
//                String nvidiaBlog = "https://feeds.feedburner.com/nvidiablog";
//
//                String inputUrl = "https://feeds.feedburner.com/nvidiablog";
//
//                String sanitizedUrl = HttpUtilities.sanitizeUrl(inputUrl);
//                SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(sanitizedUrl)));
//                String logo = getLogoUrl(feed);
//                List<SyndEntry> entries = feed.getEntries();
//                SyndEntry entry = entries.get(index);
//                String title = entry.getTitle();
//                String articleLink = entry.getLink();
//                String thumbnailUrl = getThumbnailUrl(entry);
//                List<SyndContent> contents = entry.getContents();
//
//                String htmlContent = null;
//                String contentType = null;
//                String contentMode = null;
//                String contentText = null;
//                List<String> imagesUrls = null;
//
//                if(contents.size()!=0) {
//                    SyndContent content = contents.get(0);
//                    htmlContent = content.getValue();
//                    contentType = content.getType();
//                    contentMode = content.getMode();
//                    contentText = extractReadableTextFromHtml(htmlContent);
//                    imagesUrls = extractImagesUrlFromHtml(htmlContent);
//
//                }
//                else {
//                    SyndContent description2 = entries.get(index).getDescription();
//                    SyndContent content = description2;
//                    htmlContent = content.getValue();
//                    contentType = content.getType();
//                    contentMode = content.getMode();
//                    contentText = extractReadableTextFromHtml(htmlContent);
//                    imagesUrls = extractImagesUrlFromHtml(htmlContent);
//                }
//
//                String frameVideo = "Video From YouTube<br><p><iframe width=\"100%\" height=\"200\" src=\"https://www.youtube.com/embed/47yJ2XCRLZs\" frameborder=\"0\" allowfullscreen></iframe>tralalal</p> blablabla <figure id=\"attachment_49306\" aria-describedby=\"caption-attachment-49306\" style=\"width: 90%\" class=\"wp-caption alignleft\">\n" +
//                        "                <a href=\"https://blogs.nvidia.com/wp-content/uploads/2021/03/Berzelius-end-image.jpg\">\n" +
//                        "                    <img loading=\"lazy\"\n" +
//                        "                        class=\"wp-image-49306 size-medium\"\n" +
//                        "                        src=\"https://blogs.nvidia.com/wp-content/uploads/2021/03/Berzelius-end-image-400x294.jpg\"\n" +
//                        "                        alt=\"BerzeLiUs supercomputer in Sweden e\" width=\"400\" height=\"294\"\n" +
//                        "                        srcset=\"\n" +
//                        "                        https://blogs.nvidia.com/wp-content/uploads/2021/03/Berzelius-end-image-400x294.jpg 400w, \n" +
//                        "                        https://blogs.nvidia.com/wp-content/uploads/2021/03/Berzelius-end-image-672x494.jpg 672w, \n" +
//                        "                        https://blogs.nvidia.com/wp-content/uploads/2021/03/Berzelius-end-image-768x565.jpg 768w, \n" +
//                        "                        https://blogs.nvidia.com/wp-content/uploads/2021/03/Berzelius-end-image-612x450.jpg 612w, \n" +
//                        "                        https://blogs.nvidia.com/wp-content/uploads/2021/03/Berzelius-end-image-292x215.jpg 292w, \n" +
//                        "                        https://blogs.nvidia.com/wp-content/uploads/2021/03/Berzelius-end-image-136x100.jpg 136w, \n" +
//                        "                        https://blogs.nvidia.com/wp-content/uploads/2021/03/Berzelius-end-image.jpg 1128w\"\n" +
//                        "                        sizes=\"(max-width: 90%) 100vw, 400px\" />\n" +
//                        "                </a>\n" +
//                        "                <figcaption id=\"caption-attachment-49306\" class=\"wp-caption-text\">\n" +
//                        "                    Above and at top: The BerzeLiUs system.\n" +
//                        "                    Pictures: Thor Balkhed, Linkoping University.\n" +
//                        "                </figcaption>\n" +
//                        "            </figure>" +
//                        "            <p>Berzelius (pronounced behr-zeh-LEE-us) invented chemistry’s shorthand (think H<sub>2</sub>0) and discovered a\n" +
//                        "                handful of elements including silicon. A 300-petaflops system now stands on the Linköping University (<a\n" +
//                        "                    href=\"https://liu.se/en\">LiU</a>) campus, less than 70 kilometers from his birthplace in south-central\n" +
//                        "                Sweden, like a living silicon tribute to innovations yet to come.</p>" +
//                        "            <p>“Many cities in Sweden have a square or street that bears Berzelius’s name, but the average person probably\n" +
//                        "                doesn’t know much about him,” said Niclas Andersson, technical director at the National Supercomputer Centre (<a\n" +
//                        "                    href=\"https://www.nsc.liu.se/\">NSC</a>) at Linköping University, which is home to the system based on the <a\n" +
//                        "                    href=\"https://www.nvidia.com/en-us/data-center/dgx-superpod/\">NVIDIA DGX SuperPOD</a>.</p>";
//
//                frameVideo = addEndingCode(frameVideo);
//
//                onFeedDownloaded(frameVideo);
//
//
//                List<String> elements = extractParagraphs(frameVideo);
//
////                System.out.println(description);
//
//
//            } catch (FeedException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }
//        };
//
//        ThreadManager threadManager = ThreadManager.getInstance();
//        threadManager.runTask(task);
//    }
//
//    private void onFeedDownloaded(String htmlContent) {
//        requireActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                WebSettings webSettings = webView.getSettings();
//                webSettings.setJavaScriptEnabled(true);
////                WebChromeClient webChromeClient = new WebChromeClient();
////                webView.setWebChromeClient(webChromeClient);
//                webView.loadData(htmlContent, "text/html", "UTF-8");
//            }
//        });
//    }
//
//    public String getThumbnailUrl(SyndEntry entry) {
//        if(entry==null) return null;
//        String thumbnailUrl = null;
//
//        // media namespace strategy
//        List<Element> elements = entry.getForeignMarkup();
//        for(Element element: elements) {
//            String namespace = element.getNamespacePrefix();
//            if(namespace.equals("media")) {
//                thumbnailUrl = element.getAttributeValue("url");
//                break;
//            }
//        }
//
//
//        return thumbnailUrl;
//    }
//
//    public String addEndingCode(String htmlCode) {
//        final String BREAK_TAG = "<br>";
//        final String breaks = BREAK_TAG + BREAK_TAG + BREAK_TAG + BREAK_TAG;
//
//        htmlCode = htmlCode + breaks;
//        return htmlCode;
//    }
//
//    private String extractReadableTextFromHtml(String htmlCode) {
//        Document doc = Jsoup.parse(htmlCode);
//        String text = doc.body().text();
//
//        return text;
//    }
//
//    private Elements extractElements(String htmlCode) {
//        Document doc = Jsoup.parse(htmlCode);
//        Elements elements = doc.body().getAllElements();
//
//        return elements;
//    }
//
//    private List<String> extractImagesUrlFromHtml(String htmlCode) {
//        if(htmlCode==null || htmlCode.isEmpty()) return null;
//
//        Document doc = Jsoup.parse(htmlCode);
//        Elements urls = doc.select("img");
//        List<String> imagesUrl = new ArrayList<>();
//
//        for(org.jsoup.nodes.Element el: urls) {
//            String url = el.attr("src");
//            if(url!=null || url.isEmpty())
//                imagesUrl.addAll(Collections.singleton(url));
//        }
//
//        return imagesUrl;
//    }
//
//    private List<String> extractParagraphs(String htmlCode) {
//        if(htmlCode==null || htmlCode.isEmpty()) return null;
//
////        htmlCode = Jsoup.clean(htmlCode, Whitelist.none().addTags("html", "head", "body", "meta", "title", "p", "a", "h", "figure", "figcaption", "sub", "strong", "img"));
//
//        Document doc = Jsoup.parse(htmlCode);
//        Elements elements = doc.getAllElements();
//        List<Node> nodes = doc.body().childNodes();
//
//        Elements paragraphs = new Elements();
//        Elements iframes = new Elements();
//        Elements images = new Elements();
//        Elements children = new Elements();
//
//        for (org.jsoup.nodes.Element element : elements) {
//            if(element.is("p")) {
//                paragraphs.add(element);
//                // extract iframes
//                if(element.childrenSize()!=0) {
//                    iframes.addAll(extraxtIframes(element.children()));
//                }
//            }
//        }
//
//        List<String> result = new ArrayList<>();
//
//
////        for(org.jsoup.nodes.Element el: elements) {
////            String url = el.attr("src");
////            if(url!=null || url.isEmpty())
////                result.addAll(Collections.singleton(url));
////        }
//
//        return result;
//    }
//
//    private Collection<? extends org.jsoup.nodes.Element> extraxtIframes(Elements elements) {
//        if(elements==null || elements.size()==0) return null;
//        Elements iframes = new Elements();
//
//        iframes = elements.select("iframe");
//
//        return iframes;
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

}