package com.nocorp.scienceboard.ui.webview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.databinding.FragmentWebviewBinding;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.system.MyOkHttpClient;

import net.dankito.readability4j.Readability4J;
import net.dankito.readability4j.extended.Readability4JExtended;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;

import static android.view.View.SCROLLBARS_INSIDE_OVERLAY;

import okhttp3.ResponseBody;

public class WebviewFragment extends Fragment implements androidx.appcompat.widget.Toolbar.OnMenuItemClickListener {
    private final String TAG = this.getClass().getSimpleName();
    private WebView webViewMain;
    private String webpageUrl;
    private String sourceName;
    private LinearProgressIndicator progressIndicator;
    private View view;
    private Snackbar snackbar;
    private FragmentWebviewBinding viewBinding;
    private Toolbar toolbar;
    private Toast toast;
    private WebSettings webSettingsMain;
    private final int TEXT_SIZE_STEP = 20;
    private final int DEFAULT_TEXT_SIZE = 90;
    private int currentTextSize;
    private final int UPPER_TEXT_SIZE_LIMIT = 200;
    private final int LOWER_TEXT_SIZE_LIMIT = 0;
    private MenuItem stopMenuItem;
    private Article currentArticle;
    private MenuItem bookmarkMenuItem;
    private WebviewViewModel webviewViewModel;
    private boolean articleAlreadyInBookmarks;


    private WebSettings webSettingsBottomSheet;
    private WebView webViewBottomSheet;
    private ExtendedFloatingActionButton showButton;
    private View bottomSheet;
    private ChipGroup chipGroup;
    private HorizontalScrollView horizontalScrollView;
    private List<String> keywords;
    private List<Chip> chips;
    private List<String> selectedKeywords;
    private int chipId;

    private MenuItem readModeMenuItem;
    private boolean readModeEnabled = false;
    private String extractedContentHtmlWithUtf8Encoding;
    private WebView webViewReadmode;



    //------------------------------------------------------------------------------------ ANDROID METHODS

//    public static WebviewFragment newInstance(String param1, String param2) {
//        WebviewFragment fragment = new WebviewFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentWebviewBinding.inflate(inflater, container, false);
        view = viewBinding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initiView(view);


        //
        if (getArguments() != null) {
            // the article is always non-null
            this.currentArticle = WebviewFragmentArgs.fromBundle(getArguments()).getArticleArgument();
            this.webpageUrl = currentArticle.getWebpageUrl();
            this.sourceName = currentArticle.getSourceRealName();
            this.keywords = currentArticle.getKeywords();

            if(sourceName!=null && !sourceName.isEmpty()) {
                //TODO
//                toolbar.setTitleTextColor(getResources().getColor(R.color.white));
                toolbar.setTitle(sourceName);
            }

            buildKeywordsChips(keywords, chipGroup);

            checkIfAlreadyInBookmarks();
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // main webview
        applyBrowsingRecommendedSettingsMain(webViewMain);
        webViewMain.loadUrl(webpageUrl);

        // bottom sheet webview
        applyBrowsingRecommendedSettingsBottomSheet(webViewBottomSheet);
        webViewBottomSheet.loadUrl(getString(R.string.string_google_search_website));
        defineBottomSheetBehavior();

        //
        preparePageForReadMode(webpageUrl);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewBinding = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(snackbar!=null) snackbar.dismiss();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == R.id.option_webviewMenu_stop) {
            stopPageLoadingAction();
            return true;
        }
        else if(item.getItemId() == R.id.option_webviewMenu_refresh) {
            refreshPageAction();
            return true;
        }
        else if(item.getItemId() == R.id.option_webviewMenu_bookmark) {
            if(articleAlreadyInBookmarks) {
                removeFromBookmarksAction(currentArticle.getId());
            }
            else {
                addToBookmarksAction(currentArticle);
            }
            return true;
        }
        else if(item.getItemId() == R.id.option_webviewMenu_share) {
            shareTextAction(webpageUrl);
            return true;
        }
        else if(item.getItemId() == R.id.option_webviewMenu_increaseTextSize) {
            increaseTextSizeAction();
            return true;
        }
        else if(item.getItemId() == R.id.option_webviewMenu_decreaseTextSize) {
            decreaseTextSizeAction();
            return true;
        }
        else if(item.getItemId() == R.id.option_webviewMenu_defaultTextSize) {
            setDefaultTextSizeAction();
            return true;
        }
        else if(item.getItemId() == R.id.option_webviewMenu_delete) {
            clearCacheCookiesAction();
            return true;
        }
        else if(item.getItemId() == R.id.option_webviewMenu_readmode) {
            if(readModeEnabled) {
                stopReadMode();
            }
            else {
                startReadMode();
            }
            return true;
        }
        return false;
    }






    //---------------------------------------------------------------------- METHODS

    public void preparePageForReadMode(String url) {
        OkHttpClient client = MyOkHttpClient.getClient();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull okhttp3.Response response) {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    if(responseBody!=null) {
                        parseWebpage(responseBody, url);
                        enableReadModeButton();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void parseWebpage(ResponseBody responseBody, String url) throws IOException {
        Readability4J readability4J = new Readability4JExtended(url, responseBody.string());
        net.dankito.readability4j.Article article = readability4J.parse();
        // returns extracted content in a <div> element
        extractedContentHtmlWithUtf8Encoding = article.getContentWithUtf8Encoding();

    }

    private void startReadMode() {
//        webViewMain.loadDataWithBaseURL(null, extractedContentHtmlWithUtf8Encoding, "text/html", "UTF-8", null);
        if(extractedContentHtmlWithUtf8Encoding==null || extractedContentHtmlWithUtf8Encoding.isEmpty()) return;

        extractedContentHtmlWithUtf8Encoding = extractedContentHtmlWithUtf8Encoding.replace("<head>", "<head><style>img{max-width: 100%; width:auto; height: auto;}</style>");
        // Parse your HTML file or String with Jsoup
        org.jsoup.nodes.Document doc = Jsoup.parse(extractedContentHtmlWithUtf8Encoding);
        // doc.select selects all tags in the the HTML document
//        doc.select("img").attr("width", "100%").attr();// find all images and set with to 100%

        if(hasParagraphs(doc)) {
            doc.select("figure").attr("style", "max-width: 100%; width: auto; height: auto");// find all figures and set with to 80%
            doc.select("iframe").attr("style", "max-width: 100%; width: auto; height: auto"); // find all iframes and set with to 100%
            // add more attributes or CSS to other HTML tags
            extractedContentHtmlWithUtf8Encoding = doc.html();

            Log.d(TAG, "onViewCreated: " + extractedContentHtmlWithUtf8Encoding);

            readModeEnabled = true;
            readModeMenuItem.setTitle("Exit Read mode");
            webViewReadmode = viewBinding.webViewWebviewFragmentReadMode;
            webViewReadmode.setVisibility(View.VISIBLE);
            webViewMain.setVisibility(View.GONE);
            applyBestSettingsForWebviewReadMode(webViewReadmode);
            webViewReadmode.loadData(extractedContentHtmlWithUtf8Encoding, "text/html", "UTF-8");
            showRedSnackbar("Read mode is an experimental feature.");
        }
        else {
            showRedSnackbar("Content not found.");
        }

    }

    private boolean hasParagraphs(org.jsoup.nodes.Document doc) {
        boolean result = false;
        if(doc==null) return result;


        Elements pTag = doc.select("p");

        if(pTag!=null && pTag.size()>0)
            result = true;


        return result;
    }

    private void applyBestSettingsForWebviewReadMode(WebView webView) {
        WebSettings webSettingsReadMode = webView.getSettings();
        webSettingsReadMode.setJavaScriptEnabled(true);
        webSettingsReadMode.setLoadWithOverviewMode(true);
        webSettingsReadMode.setUseWideViewPort(false);
        webSettingsReadMode.setSupportZoom(true);
        webSettingsReadMode.setBuiltInZoomControls(true);
        webSettingsReadMode.setDisplayZoomControls(false);
        webSettingsReadMode.setDomStorageEnabled(true);
        webSettingsReadMode.setDatabaseEnabled(true);
//        webSettingsReadMode.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webSettingsReadMode.setSafeBrowsingEnabled(true);
        }
        webView.setScrollBarStyle(SCROLLBARS_INSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
//        defineWebclientBehaviorBottomSheet(webView);
//        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
    }

    private void enableReadModeButton() {
        requireActivity().runOnUiThread(() -> {
                    try {
                        readModeMenuItem.setEnabled(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    private void stopReadMode() {
        readModeEnabled = false;
        webViewMain.setVisibility(View.VISIBLE);
        webViewReadmode.setVisibility(View.GONE);
        readModeMenuItem.setTitle("Read mode");
    }

    private void buildKeywordsChips(List<String> keywords, ChipGroup chipGroup) {
        if(keywords==null || keywords.isEmpty()) return;
        for(String currentKeyword: keywords) {
            addChip(currentKeyword, chipGroup);
        }
    }

    private void defineBottomSheetBehavior() {
        final BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setDraggable(false);
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                    setupBackButtonBehaviorForWebviewMain(webViewBottomSheet);
                    horizontalScrollView.setVisibility(View.VISIBLE);
                    showButton.setText(R.string.string_close);
                    showButton.setOnClickListener(v -> {
                        if (behavior.getState() == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                    });
                }
                else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    ignoreBackButton(webViewBottomSheet);
                    horizontalScrollView.setVisibility(View.GONE);
                    showButton.setText(R.string.string_search);
                    showButton.setOnClickListener(v -> {
                        if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                            behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);

                        }
                    });
                }
//                else if (newState == BottomSheetBehavior.STATE_DRAGGING) {
//                    behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
//                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // ignore
            }
        });

        showButton.setOnClickListener(v -> {
            if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            }
        });
    }



    private void initiView(@NonNull View view) {
        toolbar = viewBinding.toolbarWebviewFragment;
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        webViewMain = viewBinding.webViewWebviewFragment;
        progressIndicator = viewBinding.progressIndicatorWebviewFragment;
        currentTextSize = DEFAULT_TEXT_SIZE;
        stopMenuItem = viewBinding.toolbarWebviewFragment.getMenu().findItem(R.id.option_webviewMenu_stop);
        bookmarkMenuItem = viewBinding.toolbarWebviewFragment.getMenu().findItem(R.id.option_webviewMenu_bookmark);
        webviewViewModel = new ViewModelProvider(this).get(WebviewViewModel.class);
        chipGroup = viewBinding.includeWebviewFragment.chipGroupBottomSheetWebview;
        horizontalScrollView = viewBinding.includeWebviewFragment.containerBottomSheetWebview;
        chips = new ArrayList<>();
        selectedKeywords = new ArrayList<>();
        readModeMenuItem = viewBinding.toolbarWebviewFragment.getMenu().findItem(R.id.option_webviewMenu_readmode);


        // webview botomsheet
        webViewBottomSheet = viewBinding.includeWebviewFragment.webviewBottomSheetWebview;
        showButton = viewBinding.includeWebviewFragment.buttonBottomSheetWebview;
        bottomSheet = view.findViewById(R.id.include_webviewFragment);
//        bottomSheet.setOnFocusChangeListener((v, hasFocus) -> {
//            if(hasFocus) {
//                setupBackButtonBehaviorForWebviewMain(webViewBottomSheet);
//            }
//            else {
//                ignoreBackButton(webViewBottomSheet);
//            }
//        });




    }

    private void addChip(String name, ChipGroup chipGroup) {
        if(name==null || name.isEmpty()) return;
//        Chip newChip = new Chip(requireContext(), null, R.style.ScienceBoard_Button_Chip_Choice);
//        Chip newChip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_choice, chipGroup, false);
        Chip newChip = new Chip(requireContext());
//        newChip.setCheckable(true);
//        newChip.setCheckedIconVisible(false);
        ChipDrawable chipDrawable = ChipDrawable.createFromAttributes(requireContext(),
                null,
                0,
                R.style.ScienceBoard_Button_Chip_Choice);
        newChip.setChipDrawable(chipDrawable);
        newChip.setText(name);
        newChip.setTextAppearance(R.style.Cinemates_Button_Chip_Choice_TextAppearance);

        int i = chipId++;
        newChip.setId(i);
        newChip.setTag(i);

        newChip.setOnCheckedChangeListener((compoundButton, checked) -> {
            if(checked) {
                selectedKeywords.add(name);
            }
            else {
                selectedKeywords.remove(name);
            }
            triggerGoogleSearch(selectedKeywords);

            Log.d(TAG, "addChip: " + selectedKeywords);
        });

        chips.add(newChip);
        chipGroup.addView(newChip);
//        chipGroup.invalidate();
    }

    private void triggerGoogleSearch(List<String> selectedKeywords) {
        if(selectedKeywords==null) return;
        if(selectedKeywords.isEmpty()) {
            webViewBottomSheet.loadUrl(getString(R.string.string_google_search_website));
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (String value : selectedKeywords) {
            builder.append(value).append(" ");
        }

        String query = builder.toString();
        String url = null;
        try {
            url = "https://www.google.com/search?q=" + URLEncoder.encode(query, String.valueOf(StandardCharsets.UTF_8));
            webViewBottomSheet.loadUrl(url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearCacheCookiesAction() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Do you want clear cache/cookies?")
                .setPositiveButton("yes", (dialog, listener) -> {
                    //
                    WebStorage.getInstance().deleteAllData();
                    showBottomToast("cache/cookies deleted");
                    dialog.dismiss();
                })
                .setNegativeButton("no", (dialog, listener)-> {
                    //
                    dialog.dismiss();
//                    showCenteredToast("operation aborted");

                })
                .show();

    }

    private void stopPageLoadingAction() {
        if(snackbar!=null) snackbar.dismiss();
        webViewMain.stopLoading();
        showBottomToast(getString(R.string.string_page_load_stopped));
    }

    private void refreshPageAction() {
        if(snackbar!=null) snackbar.dismiss();
        webViewMain.loadUrl(webpageUrl);
        showBottomToast(getString(R.string.string_refreshing_page));
    }

    private void addToBookmarksAction(Article article) {
        webviewViewModel.getObservableAddToBookmarksResponse().observe(getViewLifecycleOwner(), addedToBookmarks -> {
            if(addedToBookmarks) {
                showCenteredToast("saved in bookmarks");
                changeBookmarkIcon();
                articleAlreadyInBookmarks = true;
            }
            else {
                // TODO: fail message
            }
        });
        webviewViewModel.addToBookmarks(article);
    }

    private void removeFromBookmarksAction(String articleId) {
        webviewViewModel.getObservableRemovedFromBookmarksResponse().observe(getViewLifecycleOwner(), removedFromBookmarks -> {
            if(removedFromBookmarks) {
                showCenteredToast("removed from bookmarks");
                restoreBookmarkIcon();
                articleAlreadyInBookmarks = false;
            }
            else {
                // TODO: fail message
            }
        });
        webviewViewModel.removeFromBookmarks(articleId);
    }





    //------------------------------------------------------------------------------------ METHODS

    private void applyBrowsingRecommendedSettingsBottomSheet(WebView webView) {
        webSettingsBottomSheet = webView.getSettings();
        webSettingsBottomSheet.setJavaScriptEnabled(true);
        webSettingsBottomSheet.setLoadWithOverviewMode(true);
        webSettingsBottomSheet.setUseWideViewPort(true);
        webSettingsBottomSheet.setSupportZoom(true);
        webSettingsBottomSheet.setBuiltInZoomControls(true);
        webSettingsBottomSheet.setDisplayZoomControls(false);
        webSettingsBottomSheet.setDomStorageEnabled(true);
        webSettingsBottomSheet.setDatabaseEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webSettingsBottomSheet.setSafeBrowsingEnabled(true);
        }
        webView.setScrollBarStyle(SCROLLBARS_INSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        defineWebclientBehaviorBottomSheet(webView);
    }

    private void defineWebclientBehaviorBottomSheet(WebView webView) {
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            //TODO
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

        });
    }

    private void checkIfAlreadyInBookmarks() {
        webviewViewModel.getObservableBookmarkDuplicationResponse().observe(getViewLifecycleOwner(), alreadyInBookmarks -> {
            if(alreadyInBookmarks) {
                articleAlreadyInBookmarks = true;
                changeBookmarkIcon();
            }
            else articleAlreadyInBookmarks = false;
        });
        webviewViewModel.checkIsInBookmarks(currentArticle);
    }

    private void shareTextAction(String message) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(shareIntent, "Share"));
        } catch(Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onMenuItemClick: share failed");
        }
    }

    private void increaseTextSizeAction() {
        if(currentTextSize <= UPPER_TEXT_SIZE_LIMIT) {
            currentTextSize = currentTextSize + TEXT_SIZE_STEP;
            webSettingsMain.setTextZoom(currentTextSize);// where 90 is 90%; default value is ... 100
        }
    }

    private void decreaseTextSizeAction() {
        if(currentTextSize >= LOWER_TEXT_SIZE_LIMIT){
            currentTextSize = currentTextSize - TEXT_SIZE_STEP;
            webSettingsMain.setTextZoom(currentTextSize);
        }
    }

    private void setDefaultTextSizeAction() {
        currentTextSize = DEFAULT_TEXT_SIZE;
        webSettingsMain.setTextZoom(currentTextSize);
    }

    /**
     * override back button behavior for webviews.
     * Back button will go back in case of webviews
     */
    private void setupBackButtonBehaviorForWebviewMain(WebView webView) {
        webView.setOnKeyListener((v, keyCode, event) -> {
            if(event.getAction() == KeyEvent.ACTION_DOWN) {
                WebView webView1 = (WebView) v;

                switch(keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        if(webView1.canGoBack()) {
                            webView1.goBack();
                            return true;
                        }
                        break;
                }
            }
            return false;
        });
    }



    private void ignoreBackButton(WebView webView) {
        webView.setOnKeyListener(null);
    }

    private void applyBrowsingRecommendedSettingsMain(WebView webView) {
        webSettingsMain = webView.getSettings();
        webSettingsMain.setJavaScriptEnabled(true);
//        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);// TODO: allow youtube to set a video fullscreen
        webSettingsMain.setLoadWithOverviewMode(true);
        webSettingsMain.setUseWideViewPort(true);
        webSettingsMain.setSupportZoom(true);
        webSettingsMain.setBuiltInZoomControls(true);
        webSettingsMain.setDisplayZoomControls(false);
        webSettingsMain.setDomStorageEnabled(true);
        webSettingsMain.setDatabaseEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webSettingsMain.setSafeBrowsingEnabled(true);
        }
        webView.setScrollBarStyle(SCROLLBARS_INSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
//        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null); // TODO: this might fix blank screen porblem
        defineWebclientBehaviorMain(webView);
    }

    private void defineWebclientBehaviorMain(WebView webView) {
        setupBackButtonBehaviorForWebviewMain(webView);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressIndicator.setVisibility(View.GONE);
//                showLoadCompletedSnackbar();
                hideStopIcon();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressIndicator.setVisibility(View.VISIBLE);
                showStopIcon();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                String message = "An error occurred when displaying page." + "\ndescription: " + description + "\nerror: " + errorCode;
                showErrorSnackbar(message);
            }

        });
    }

    private void showLoadCompletedSnackbar() {
        String message = getString(R.string.string_page_load_completed);
        snackbar = Snackbar.make(view, "",Snackbar.LENGTH_SHORT);
        snackbar.setText(message);
        snackbar.setText(message);
        snackbar.setTextColor(getResources().getColor(R.color.white));
        snackbar.setBackgroundTint(getResources().getColor(R.color.green));
        snackbar.setAnchorView(R.id.nav_view);
        snackbar.setAction("ok", v -> snackbar.dismiss());
        snackbar.setActionTextColor(getResources().getColor(R.color.white));
        snackbar.show();
    }

    private void showRedSnackbar(String message) {
        snackbar = Snackbar.make(view, "",Snackbar.LENGTH_LONG);
        snackbar.setText(message);
        snackbar.setTextColor(getResources().getColor(R.color.white));
        snackbar.setBackgroundTint(getResources().getColor(R.color.red));
        snackbar.setAction("ok", v -> {
            snackbar.dismiss();
        });
        snackbar.setActionTextColor(getResources().getColor(R.color.white));
        snackbar.show();
    }


    private void showErrorSnackbar(String message) {
        snackbar = Snackbar.make(view, "",Snackbar.LENGTH_INDEFINITE);
        snackbar.setText(message);
        snackbar.setTextColor(getResources().getColor(R.color.white));
        snackbar.setBackgroundTint(getResources().getColor(R.color.red));
//        snackbar.setAnchorView(R.id.nav_view);
        snackbar.setAction("retry", v -> {
            snackbar.dismiss();
            webViewMain.loadUrl(webpageUrl);
        });
        snackbar.setActionTextColor(getResources().getColor(R.color.white));
        snackbar.show();
    }

    private void showBottomToast(String message) {
        if(toast!=null) toast.cancel();
        toast = Toast.makeText(requireContext(),message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void showCenteredToast(String message) {
        if(toast!=null) toast.cancel();
        toast = Toast.makeText(requireContext(),message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void hideStopIcon() {
        if(stopMenuItem !=null)
            stopMenuItem.setVisible(false);
    }

    private void showStopIcon() {
        if(stopMenuItem !=null)
            stopMenuItem.setVisible(true);
    }

    private void changeBookmarkIcon() {
        if(bookmarkMenuItem !=null)
            bookmarkMenuItem.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_bookmark_added_orange, null));
    }

    private void restoreBookmarkIcon() {
        if(bookmarkMenuItem !=null)
            bookmarkMenuItem.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_bookmark_outlined, null));
    }


}// end WebviewFragment