package com.nocorp.scienceboard.ui.webview;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.transition.MaterialContainerTransform;
import com.nocorp.scienceboard.MainActivity;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.databinding.ActivityMainBinding;
import com.nocorp.scienceboard.databinding.ActivityWebviewBinding;
import com.nocorp.scienceboard.model.Article;
import com.nocorp.scienceboard.system.MyOkHttpClient;
import com.nocorp.scienceboard.ui.bookmarks.BookmarksViewModel;

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
import okhttp3.ResponseBody;

import static android.webkit.WebSettings.FORCE_DARK_OFF;
import static android.webkit.WebSettings.FORCE_DARK_ON;

public class WebviewActivity extends AppCompatActivity  {
//    private final String TAG = this.getClass().getSimpleName();
//    private View view;
//    private Toolbar toolbar;
//    private ActivityWebviewBinding viewBinding;
//    private LinearProgressIndicator progressIndicator;
//    private Snackbar snackbar;
//    private Toast toast;
//
//
//    // viewmodels
//    private WebviewViewModel webviewViewModel;
//
//
//    // main webview
//    private String webpageUrl;
//    private String sourceName;
//    private WebView webViewMain;
//    private Article currentArticle;
//    private boolean articleAlreadyInBookmarks;
//
//    private final int TEXT_SIZE_STEP = 20;
//    private final int DEFAULT_TEXT_SIZE = 90;
//    private int currentTextSize;
//    private final int UPPER_TEXT_SIZE_LIMIT = 200;
//    private final int LOWER_TEXT_SIZE_LIMIT = 0;
//
//
//    // bottom sheet
//    private WebView webViewBottomSheet;
//    private ExtendedFloatingActionButton searchButton;
//    private View bottomSheet;
//    private View backgroundShadow;
//    // keywords
//    private ChipGroup chipGroup;
//    private HorizontalScrollView chipsContainer;
//    private List<String> keywords;
//    private List<Chip> chips;
//    private List<String> selectedKeywords;
//    private int chipId;
//
//
//    // read mode
//    private WebView webViewReadmode;
//    private boolean readModeEnabled = false;
//    private String readModeContent;
//
//    // animations
//    private int shortAnimationDuration;
//
//
//    // google search
//    private final String googleSearchQueryUrl = "https://www.google.com/search?q=";
////    private final String javascript_getTheSelectedWord = "(function(){return window.getSelection().toString()})()";
////    private String selectedWord;
//
//
//    // menuitems
//    private MenuItem readModeMenuItem;
//    private MenuItem stopMenuItem;
//    private MenuItem bookmarkMenuItem;
//    private MenuItem reloadPageMenuItem;
//    private MenuItem increaseTextSize;
//    private MenuItem decreaseTextSize;
//    private MenuItem defaultTextSize;
//    private MenuItem deleteCacheCookies;
//
//
//    //
//    private final String ARGUMENT_INTENT_KEY = "article webview argument";
//
//    //------------------------------------------------------------ ANDROID METHODS
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        initView();
//
//        //
//        Intent intent = getIntent();
//        currentArticle = (Article) intent.getSerializableExtra(ARGUMENT_INTENT_KEY);
//        if(currentArticle!=null) {
//            // the article is always non-null
//            this.webpageUrl = currentArticle.getWebpageUrl();
//            this.sourceName = currentArticle.getSourceRealName();
//            this.keywords = currentArticle.getKeywords();
//            this.articleAlreadyInBookmarks = currentArticle.isBookmarked();
//
//            if(sourceName!=null && !sourceName.isEmpty()) {
//                //TODO
////                toolbar.setTitleTextColor(getResources().getColor(R.color.white));
//                toolbar.setTitle(sourceName);
//            }
//
//            buildKeywordsChips(keywords, chipGroup);
//            checkIfAlreadyInBookmarks();
//            initContent(savedInstanceState);
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        viewBinding = null;
//        if(snackbar!=null && snackbar.isShown()) snackbar.dismiss();
//        webViewReadmode.stopLoading();
//        webViewBottomSheet.stopLoading();
//        webViewMain.stopLoading();
//    }
//
//    //------------------------------------------------------------ MY METHODS
//
//    private void initView() {
//        viewBinding = ActivityWebviewBinding.inflate(getLayoutInflater());
//        view = viewBinding.getRoot();
//        setContentView(view);
//
//        // my_child_toolbar is defined in the layout file
//        toolbar = viewBinding.toolbarWebviewFragment;
//        setSupportActionBar(toolbar);
////        // Get a support ActionBar corresponding to this toolbar
////        appBar = getSupportActionBar();
////        // Enable the Up button
////        if(appBar!=null) appBar.setDisplayHomeAsUpEnabled(true);
//    }
//
//    private void initContent(Bundle savedInstanceState) {
//        if(savedInstanceState!=null) {
//            readModeEnabled = savedInstanceState.getBoolean("readModeState");
//            readModeContent = savedInstanceState.getString("readModeContent");
//        }
//
//        applyCustomWebviewSettings_mainWebview(webViewMain);
//        applyBrowsingRecommendedSettings_bottomSheetWebview(webViewBottomSheet);
//        defineBottomSheetBehavior();
//
//        if(savedInstanceState!=null) {
//            webViewMain.restoreState(savedInstanceState.getBundle("webviewMainBundle"));
//            webViewBottomSheet.restoreState(savedInstanceState.getBundle("webviewBottomSheetBundle"));
//            webViewReadmode.restoreState(savedInstanceState.getBundle("webviewReadModeBundle"));
//            enableReadModeButton();
//            if(readModeEnabled) {
//                startReadModeActionWithoutReaload(readModeContent);
//            }
//        }
//        else {
//            webViewMain.loadUrl(webpageUrl);
//            webViewBottomSheet.loadUrl(getString(R.string.string_google_search_website));
//            preparePageForReadMode(webpageUrl);
//        }
//    }
//
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if(item.getItemId() == R.id.option_webviewMenu_stop) {
//            stopPageLoadingAction();
//            return true;
//        }
//        else if(item.getItemId() == R.id.option_webviewMenu_refresh) {
//            refreshPageAction();
//            return true;
//        }
//        else if(item.getItemId() == R.id.option_webviewMenu_bookmark) {
//            if(articleAlreadyInBookmarks) {
//                removeFromBookmarksAction(currentArticle);
//            }
//            else {
//                addToBookmarksAction(currentArticle);
//            }
//            return true;
//        }
//        else if(item.getItemId() == R.id.option_webviewMenu_share) {
//            shareTextAction(webpageUrl);
//            return true;
//        }
//        else if(item.getItemId() == R.id.option_webviewMenu_increaseTextSize) {
//            increaseTextSizeAction(webViewMain, currentTextSize);
//            return true;
//        }
//        else if(item.getItemId() == R.id.option_webviewMenu_decreaseTextSize) {
//            decreaseTextSizeAction(webViewMain, currentTextSize);
//            return true;
//        }
//        else if(item.getItemId() == R.id.option_webviewMenu_defaultTextSize) {
//            setDefaultTextSizeAction(webViewMain, currentTextSize);
//            return true;
//        }
//        else if(item.getItemId() == R.id.option_webviewMenu_delete) {
//            clearCacheCookiesAction();
//            return true;
//        }
//        else if(item.getItemId() == R.id.option_webviewMenu_readmode) {
//            if(readModeEnabled) {
//                stopReadMode();
//            }
//            else {
//                startReadModeAction(readModeContent);
//            }
//            return true;
//        }
//        return false;
//    }
//
//
//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState) {
//        Log.d(TAG, "onSaveInstanceState: called");
//        super.onSaveInstanceState(outState);
//        saveState(outState);
//    }
//
//    private void saveState(@NonNull Bundle outState) {
//        Bundle webviewMainBundle = new Bundle();
//        webViewMain.saveState(webviewMainBundle);
//        if(webviewMainBundle!=null)
//            outState.putBundle("webviewMainBundle", webviewMainBundle);
//
//        Bundle webviewBottomSheetBundle = new Bundle();
//        webViewBottomSheet.saveState(webviewBottomSheetBundle);
//        if(webviewBottomSheetBundle!=null)
//            outState.putBundle("webviewBottomSheetBundle", webviewBottomSheetBundle);
//
//        Bundle webviewReadModeBundle = new Bundle();
//        webViewReadmode.saveState(webviewReadModeBundle);
//        if(webviewReadModeBundle!=null)
//            outState.putBundle("webviewReadModeBundle", webviewReadModeBundle);
//
//        outState.putBoolean("readModeState", readModeEnabled);
//        outState.putString("readModeContent", readModeContent);
//    }
//
//
//
//
//    //---------------------------------------------------------------------- METHODS
//
//
//
//
//
//
//
//
//
//    //------------------------------------------------------------------------------------ METHODS
//
//    private void initView(@NonNull View view) {
//        toolbar = viewBinding.toolbarWebviewFragment;
//        webViewMain = viewBinding.webViewWebviewFragment;
//        progressIndicator = viewBinding.progressIndicatorWebviewFragment;
//        currentTextSize = DEFAULT_TEXT_SIZE;
//        chipGroup = viewBinding.includeWebviewFragment.chipGroupBottomSheetWebview;
//        chipsContainer = viewBinding.includeWebviewFragment.containerBottomSheetWebview;
//        chips = new ArrayList<>();
//        selectedKeywords = new ArrayList<>();
//
//        // menuitems
//        stopMenuItem = viewBinding.toolbarWebviewFragment.getMenu().findItem(R.id.option_webviewMenu_stop);
//        bookmarkMenuItem = viewBinding.toolbarWebviewFragment.getMenu().findItem(R.id.option_webviewMenu_bookmark);
//        readModeMenuItem = viewBinding.toolbarWebviewFragment.getMenu().findItem(R.id.option_webviewMenu_readmode);
//        reloadPageMenuItem = viewBinding.toolbarWebviewFragment.getMenu().findItem(R.id.option_webviewMenu_refresh);
//        increaseTextSize = viewBinding.toolbarWebviewFragment.getMenu().findItem(R.id.option_webviewMenu_increaseTextSize);
//        decreaseTextSize = viewBinding.toolbarWebviewFragment.getMenu().findItem(R.id.option_webviewMenu_decreaseTextSize);
//        defaultTextSize = viewBinding.toolbarWebviewFragment.getMenu().findItem(R.id.option_webviewMenu_defaultTextSize);
//        deleteCacheCookies = viewBinding.toolbarWebviewFragment.getMenu().findItem(R.id.option_webviewMenu_delete);
//
//
//        // viewmodels
//        webviewViewModel = new ViewModelProvider(this).get(WebviewViewModel.class);
//
//        // read mode
//        webViewReadmode = viewBinding.webViewWebviewFragmentReadMode;
//
//
//        // webview botomsheet
//        webViewBottomSheet = viewBinding.includeWebviewFragment.webviewBottomSheetWebview;
//        searchButton = viewBinding.includeWebviewFragment.buttonBottomSheetWebview;
//        bottomSheet = view.findViewById(R.id.include_webviewFragment);
//        backgroundShadow = viewBinding.includeWebviewFragment.viewBottomSheetWebviewShadow2;
//
//
//
//        // Retrieve and cache the system's default "short" animation time.
//        shortAnimationDuration = getResources().getInteger(
//                android.R.integer.config_shortAnimTime);
//
//    }
//
//
//
//
//
//    //--------------------------------------------------------- webviews
//
//    private void applyBasicWebviewSettings(WebView webView) {
//        WebSettings webSettings = webView.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//        webSettings.setSupportZoom(true);
//        webSettings.setDomStorageEnabled(true);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            webSettings.setSafeBrowsingEnabled(true);
//        }
//        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
//        webView.setScrollbarFadingEnabled(false);
//        webSettings.setLoadWithOverviewMode(true);
//        webSettings.setDatabaseEnabled(true);
//        // testing
////        webSettings.setAllowFileAccess(true);
//        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
////        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);// TODO: allow youtube to set a video fullscreen
//
//
////        webSettings.setBuiltInZoomControls(true);
////        webSettings.setDisplayZoomControls(false);
////        webView.setFocusable(true);
////        webView.setFocusableInTouchMode(true);
////        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null); // TODO: this might fix blank screen problem
//    }
//
//    private void applyCustomWebviewSettings_mainWebview(WebView webView) {
//        applyBasicWebviewSettings(webView);
//        webView.getSettings().setUseWideViewPort(true);
//        defineWebclientBehavior_mainWebview(webView);
//    }
//
//    private void applyCustomWebviewSettings_readModeWebview(WebView webView) {
//        applyBasicWebviewSettings(webView);
//        webView.setWebViewClient(new WebViewClient());
//    }
//
//    private void applyBrowsingRecommendedSettings_bottomSheetWebview(WebView webView) {
//        applyBasicWebviewSettings(webView);
//        webView.getSettings().setUseWideViewPort(true);
//        defineWebclientBehavior_bottomSheetWebview(webView);
//    }
//
//    private void defineWebclientBehavior_mainWebview(WebView webView) {
//        defineWebviewBackButtonBehavior(webView);
//        webView.setWebChromeClient(new WebChromeClient() {
//            //            @RequiresApi(api = Build.VERSION_CODES.N)
//            public void onProgressChanged(WebView view, int progress) {
//                try {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        progressIndicator.setProgress(progress, true); //Make the bar disappear after URL is loaded
//                    }
//                } catch (Exception e) {
//                    Log.e(TAG, "onProgressChanged: cause: ", e);
//                }
//            }
//        });
//        webView.setWebViewClient(new WebViewClient() {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                // TODO: doing this might cause blank pages!!!
////                view.loadUrl(url);
////                return true;
//
//                // TODO: doing this will prevent blank pages!!!
//                return super.shouldOverrideUrlLoading(view, url);
//            }
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
//                progressIndicator.setVisibility(View.GONE);
////                showLoadCompletedSnackbar();
//                hideMenuItemIcon(stopMenuItem);
//            }
//
//            @Override
//            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                super.onPageStarted(view, url, favicon);
//                progressIndicator.setVisibility(View.VISIBLE);
//                if(!readModeEnabled)
//                    showMenuIitemIcon(stopMenuItem);
//            }
//
//            @Override
//            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//                super.onReceivedError(view, errorCode, description, failingUrl);
//                String message = "An error occurred when displaying page." + "\ndescription: " + description + "\nerror: " + errorCode;
//                showErrorSnackbar(message, getApplicationContext());
//            }
//
//
////            @Override
////            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
////                super.onReceivedHttpError(view, request, errorResponse);
////                Log.d(TAG, "SCIENCE_BOARD - onReceivedHttpError: reason: " + errorResponse.getReasonPhrase());
////                Log.d(TAG, "SCIENCE_BOARD - onReceivedHttpError: statuscode: " + errorResponse.getStatusCode());
//////                showErrorSnackbar("HTTP error.", requireContext());
////            }
//        });
//
//
//
////        webView.evaluateJavascript(javascript_getTheSelectedWord,
////                selectedText -> {
////                    try {
////                        if(selectedText==null || selectedText.isEmpty()) return;
////                        if(selectedText.contains(" ")){
////                            selectedText= selectedText.substring(0, selectedText.indexOf(" "));
////                            selectedWord = selectedText;
////                        }
////
////                        if (selectedWord==null || selectedWord.isEmpty()) return;
////                        Log.v(TAG, "SCIENCE_BOARD - WEBVIEW SELECTION:" + selectedWord);
////                        searchButton.setText("search: " + selectedWord);
////                        searchButton.setOnClickListener(v -> searchSelecteWordOnGoogle(selectedWord));
////
////                    } catch (Exception e) {
////                        e.printStackTrace();
////                        Log.d(TAG, "defineWebclientBehavior_mainWebview: " + e.getMessage());
////                    }
////                });
//    }
//
//    private void defineWebclientBehavior_bottomSheetWebview(WebView webView) {
////        webView.setWebChromeClient(new WebChromeClient());
//        webView.setWebViewClient(new WebViewClient() {
//            //TODO
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);
//                return true;
//            }
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
//            }
//
//            @Override
//            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                super.onPageStarted(view, url, favicon);
//            }
//
//            @Override
//            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//                super.onReceivedError(view, errorCode, description, failingUrl);
//            }
//
//        });
//    }
//
//    /**
//     * override back button behavior for webviews.
//     * Back button will go back in case of webviews
//     */
//    private void defineWebviewBackButtonBehavior(WebView webView) {
//        webView.setOnKeyListener((v, keyCode, event) -> {
//            if(event.getAction() == KeyEvent.ACTION_DOWN) {
//                WebView currentWebview = (WebView) v;
//
//                switch(keyCode) {
//                    case KeyEvent.KEYCODE_BACK:
//                        if(currentWebview.canGoBack()) {
//                            currentWebview.goBack();
//                            return true;
//                        }
//                        break;
//                }
//            }
//            return false;
//        });
//    }
//
//
//
//
//
//    //--------------------------------------------------------- actions
//
//    private void enableReadModeLayout(){
//        increaseTextSize.setVisible(false);
//        decreaseTextSize.setVisible(false);
//        defaultTextSize.setVisible(false);
//        deleteCacheCookies.setVisible(false);
//    }
//
//    private void disableReadModeLayout(){
//        increaseTextSize.setVisible(true);
//        decreaseTextSize.setVisible(true);
//        defaultTextSize.setVisible(true);
//        deleteCacheCookies.setVisible(true);
//    }
//
//
//    private void shareTextAction(String message) {
//        try {
//            Intent shareIntent = new Intent(Intent.ACTION_SEND);
//            shareIntent.setType("text/plain");
//            shareIntent.putExtra(Intent.EXTRA_TEXT, message);
//            startActivity(Intent.createChooser(shareIntent, "Share"));
//        } catch(Exception e) {
//            e.printStackTrace();
//            Log.d(TAG, "onMenuItemClick: share failed");
//        }
//    }
//
//    private void increaseTextSizeAction(WebView webiew, int currentTextSize) {
//        if(currentTextSize <= UPPER_TEXT_SIZE_LIMIT) {
//            currentTextSize = currentTextSize + TEXT_SIZE_STEP;
//            webiew.getSettings().setTextZoom(currentTextSize);// where 90 is 90%; default value is ... 100
//        }
//    }
//
//    private void decreaseTextSizeAction(WebView webiew, int currentTextSize) {
//        if(currentTextSize >= LOWER_TEXT_SIZE_LIMIT){
//            currentTextSize = currentTextSize - TEXT_SIZE_STEP;
//            webiew.getSettings().setTextZoom(currentTextSize);
//        }
//    }
//
//    private void setDefaultTextSizeAction(WebView webiew, int currentTextSize) {
//        currentTextSize = DEFAULT_TEXT_SIZE;
//        webiew.getSettings().setTextZoom(currentTextSize);
//    }
//
//    private void startReadModeAction(String readModeContent) {
//        enableReadModeLayout();
////        webViewMain.loadDataWithBaseURL(null, extractedContentHtmlWithUtf8Encoding, "text/html", "UTF-8", null);
//        if(readModeContent ==null || readModeContent.isEmpty()) return;
//
//        readModeContent =
//                readModeContent.replace("<head>", "<head><style>body{background-color: Ivory;} img{max-width: 100%; width:auto; height: auto;}</style>");
//        // Parse your HTML file or String with Jsoup
//        org.jsoup.nodes.Document doc = Jsoup.parse(readModeContent);
//        // doc.select selects all tags in the the HTML document
////        doc.select("img").attr("width", "100%").attr();// find all images and set with to 100%
//
//        if(hasParagraphs(doc)) {
//            doc.select("figure").attr("style", "max-width: 100%; width: auto; height: auto");// find all figures and set with to 80%
//            doc.select("iframe").attr("style", "max-width: 100%; width: auto; height: auto"); // find all iframes and set with to 100%
//            // add more attributes or CSS to other HTML tags
//            readModeContent = doc.html();
//
//            Log.d(TAG, "onViewCreated: " + readModeContent);
//
//            readModeEnabled = true;
//            readModeMenuItem.setTitle("Exit Read mode");
//            webViewReadmode.setVisibility(View.VISIBLE);
//            webViewMain.setVisibility(View.GONE);
//            applyCustomWebviewSettings_readModeWebview(webViewReadmode);
//            webViewReadmode.loadData(readModeContent, "text/html", "UTF-8");
//            showRedSnackbar("Read mode is an experimental feature.", Snackbar.LENGTH_SHORT, "ok", (v)->snackbar.dismiss(), this);
//            hideMenuItemIcon(reloadPageMenuItem);
//            hideMenuItemIcon(stopMenuItem);
//        }
//        else {
//            showRedSnackbar("Content not found.", Snackbar.LENGTH_SHORT, "ok", (v)->snackbar.dismiss(), this);
//        }
//
//    }
//
//    private void startReadModeActionWithoutReaload(String readModeContent) {
////        webViewMain.loadDataWithBaseURL(null, extractedContentHtmlWithUtf8Encoding, "text/html", "UTF-8", null);
//        if(readModeContent ==null || readModeContent.isEmpty()) return;
//
//        readModeMenuItem.setTitle("Exit Read mode");
//        webViewReadmode.setVisibility(View.VISIBLE);
//        webViewMain.setVisibility(View.GONE);
//        applyCustomWebviewSettings_readModeWebview(webViewReadmode);
////        webViewReadmode.loadData(readModeContent, "text/html", "UTF-8");
////        showRedSnackbar("Read mode is an experimental feature.", Snackbar.LENGTH_SHORT, "ok", (v)->snackbar.dismiss(), requireContext());
//        hideMenuItemIcon(reloadPageMenuItem);
//        hideMenuItemIcon(stopMenuItem);
//    }
//
//    private void clearCacheCookiesAction() {
//        new MaterialAlertDialogBuilder(this, R.style.ScieceBoard_Dialog_MaterialAlertDialog)
//                .setTitle("Do you want clear cache/cookies?")
//                .setPositiveButton("yes", (dialog, listener) -> {
//                    //
//                    WebStorage.getInstance().deleteAllData();
//                    showBottomToast("cache/cookies deleted");
//                    dialog.dismiss();
//                })
//                .setNegativeButton("no", (dialog, listener)-> {
//                    //
//                    dialog.dismiss();
////                    showCenteredToast("operation aborted");
//
//                })
//                .show();
//
//    }
//
//    private void stopPageLoadingAction() {
//        if(snackbar!=null) snackbar.dismiss();
//        webViewMain.stopLoading();
//        showBottomToast(getString(R.string.string_page_load_stopped));
//    }
//
//    private void refreshPageAction() {
//        if(snackbar!=null) snackbar.dismiss();
//        webViewMain.loadUrl(webpageUrl);
//        showBottomToast(getString(R.string.string_refreshing_page));
//    }
//
//    private void addToBookmarksAction(Article article) {
//        webviewViewModel.getObservableAddToBookmarksResponse().observe(this, addedToBookmarks -> {
//            if(addedToBookmarks) {
//                showCenteredToast("saved in bookmarks");
//                changeMenuItemIcon(bookmarkMenuItem, R.drawable.ic_bookmark_added_yellow);
//                articleAlreadyInBookmarks = true;
//            }
//            else {
//                // TODO: fail message
//            }
//        });
//        webviewViewModel.addToBookmarks(article);
//    }
//
//    private void removeFromBookmarksAction(Article article) {
//        webviewViewModel.getObservableRemovedFromBookmarksResponse().observe(this, removedFromBookmarks -> {
//            if(removedFromBookmarks) {
//                showCenteredToast("removed from bookmarks");
//                changeMenuItemIcon(bookmarkMenuItem, R.drawable.ic_bookmark_outlined);
//                articleAlreadyInBookmarks = false;
//            }
//            else {
//                // TODO: fail message
//            }
//        });
//        webviewViewModel.removeFromBookmarks(article);
//    }
//
//
//
//
//
//
//    //---------------------------------------------------------
//
//    @RequiresApi(api = Build.VERSION_CODES.Q)
//    private void enableDarkMode(WebSettings webSettings) {
//        webSettings.setForceDark(FORCE_DARK_ON);
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.Q)
//    private void disableDarkMode(WebSettings webSettings) {
//        webSettings.setForceDark(FORCE_DARK_OFF);
//    }
//
//    private void defineBottomSheetBehavior() {
//        final BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
//        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//            @Override
//            public void onStateChanged(@NonNull View bottomSheet, int newState) {
//                if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED ||
//                        newState == BottomSheetBehavior.STATE_EXPANDED) {
//
//                    defineWebviewBackButtonBehavior(webViewBottomSheet);
//                    applyCrossfadeEnter(chipsContainer);
//                    searchButton.setText(R.string.similar_news);
//                    backgroundShadow.setVisibility(View.VISIBLE);
//                    searchButton.setOnClickListener(v -> {
//                        // ignore v
//
//                        if (behavior.getState() == BottomSheetBehavior.STATE_HALF_EXPANDED ||
//                                newState == BottomSheetBehavior.STATE_EXPANDED) {
//                            searchSimilarArticlesOnGoogle(keywords);
//                        }
//                    });
//                }
//                else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
//                    ignoreBackButton(webViewBottomSheet);
//                    applyCrossfadeExit(chipsContainer);
//                    backgroundShadow.setVisibility(View.GONE);
//                    searchButton.setText(R.string.string_search);
//                    searchButton.setOnClickListener(v -> {
//                        // ignore v
//
//                        if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
//                            behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
//                        }
//                    });
//                }
////                else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
////                    ignoreBackButton(webViewBottomSheet);
////                    showButton.setOnClickListener(v -> {
////                        if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
////                            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
////
////                        }
////                    });
////                }
////                else if (newState == BottomSheetBehavior.STATE_DRAGGING) {
////                    behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
////                }
//            }// end onStateChanged()
//
//            @Override
//            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                // ignore
//            }
//        });
//
//        searchButton.setOnClickListener(v -> {
//            // ignore v
//
//            if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
//                behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
//            }
//        });
//    }
//
//    private void applyCrossfadeEnter(View view) {
//        // Set the content view to 0% opacity but visible, so that it is visible
//        // (but fully transparent) during the animation.
//        view.setAlpha(0f);
//        view.setVisibility(View.VISIBLE);
//
//        // Animate the content view to 100% opacity, and clear any animation
//        // listener set on the view.
//        view.animate()
//                .alpha(1f)
//                .setDuration(shortAnimationDuration)
//                .setListener(null);
//
//    }
//
//    private void applyCrossfadeExit(View view) {
//        // Animate the loading view to 0% opacity. After the animation ends,
//        // set its visibility to GONE as an optimization step (it won't
//        // participate in layout passes, etc.)
//        view.animate()
//                .alpha(0f)
//                .setDuration(shortAnimationDuration)
//                .setListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        view.setVisibility(View.GONE);
//                    }
//                });
//
//
//    }
//
//    public void preparePageForReadMode(String url) {
//        OkHttpClient client = MyOkHttpClient.getClient();
//
//        okhttp3.Request request = new okhttp3.Request.Builder()
//                .url(url)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull okhttp3.Response response) {
//                try (ResponseBody responseBody = response.body()) {
//                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//                    if(responseBody!=null) {
//                        readModeContent = parseWebpage(responseBody, url);
//                        enableReadModeButton();
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                e.printStackTrace();
//            }
//        });
//    }
//
//
//    private String parseWebpage(ResponseBody responseBody, String url) throws IOException {
//        Readability4J readability4J = new Readability4JExtended(url, responseBody.string());
//        net.dankito.readability4j.Article article = readability4J.parse();
//        // returns extracted content in a <div> element
//        article.getContentWithUtf8Encoding();
//        return article.getContentWithUtf8Encoding();
//    }
//
//    private void addChip(String name, ChipGroup chipGroup) {
//        if(name==null || name.isEmpty()) return;
////        Chip newChip = new Chip(requireContext(), null, R.style.ScienceBoard_Button_Chip_Choice);
////        Chip newChip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_choice, chipGroup, false);
//        Chip newChip = new Chip(this);
////        newChip.setCheckable(true);
////        newChip.setCheckedIconVisible(false);
//        ChipDrawable chipDrawable = ChipDrawable.createFromAttributes(this,
//                null,
//                0,
//                R.style.ScienceBoard_Button_Chip_Choice);
//        newChip.setChipDrawable(chipDrawable);
//        newChip.setText(name);
//        try {
////            newChip.setTextAppearance(R.style.ScienceBoard_Button_Chip_Choice_TextAppearance);
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//                newChip.setTextAppearance(R.style.ScienceBoard_Button_Chip_Choice_TextAppearance);
//            else
//                newChip.setTextAppearance(this, R.style.ScienceBoard_Button_Chip_Choice_TextAppearance);
//        } catch (Exception e) {
//            Log.e(TAG, "addChip: cause: ", e.getCause());
//        }
//
//        int i = chipId++;
//        newChip.setId(i);
//        newChip.setTag(i);
//
//        newChip.setOnCheckedChangeListener((compoundButton, checked) -> {
//            if(checked) {
//                selectedKeywords.add(name);
//            }
//            else {
//                selectedKeywords.remove(name);
//            }
//            triggerKeywordsGoogleSearch(selectedKeywords);
//
//            Log.d(TAG, "addChip: " + selectedKeywords);
//        });
//
//        chips.add(newChip);
//        chipGroup.addView(newChip);
////        chipGroup.invalidate();
//    }
//
//
//    private void triggerKeywordsGoogleSearch(List<String> selectedKeywords) {
//        if(selectedKeywords==null) return;
//        if(selectedKeywords.isEmpty()) {
//            webViewBottomSheet.loadUrl(getString(R.string.string_google_search_website));
//            return;
//        }
//
//        StringBuilder builder = new StringBuilder();
//        for (String value : selectedKeywords) {
//            builder.append(value);
//            builder.append(" ");
//        }
//
//        String query = builder.toString();
//        String url = null;
//        try {
////            url = googleSearchQueryUrl + URLEncoder.encode(query, String.valueOf(StandardCharsets.UTF_8));
//            url = googleSearchQueryUrl + URLEncoder.encode(query, "utf-8");
//            webViewBottomSheet.loadUrl(url);
//        } catch (UnsupportedEncodingException e) {
//            Log.e(TAG, "NOPPYS_BOARD - triggerKeywordsGoogleSearch: cause", e.getCause());
//        } catch (Exception e) {
//            Log.e(TAG, "NOPPYS_BOARD - triggerKeywordsGoogleSearch: cause", e.getCause());
//        }
//    }
//
//    private void searchSimilarArticlesOnGoogle(List<String> selectedKeywords) {
//        String query = null;
//
//        if(selectedKeywords!=null && ! selectedKeywords.isEmpty()) {
//            StringBuilder stringBuilder = new StringBuilder();
//            for(String currentKeyword : keywords) {
//                stringBuilder.append(currentKeyword).append(" ");
//            }
//            query = stringBuilder.toString();
//        }
//        else {
//            query = currentArticle.getTitle();
//        }
//
//
//        String url = null;
//        try {
//            url = googleSearchQueryUrl + URLEncoder.encode(query, String.valueOf(StandardCharsets.UTF_8));
//            webViewBottomSheet.loadUrl(url);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void searchSelecteWordOnGoogle(String selectedWord) {
//        String query = null;
//
//        if(selectedWord!=null && ! selectedWord.isEmpty()) {
//            query = selectedWord;
//            String url = null;
//            try {
//                url = googleSearchQueryUrl + URLEncoder.encode(query, String.valueOf(StandardCharsets.UTF_8));
//                webViewBottomSheet.loadUrl(url);
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//
//    private void invalidateWebviewBackButtonBehavior(WebView webView) {
//        webView.setOnKeyListener(null);
//    }
//
//    private void ignoreBackButton(WebView webView) {
//        webView.setOnKeyListener(null);
//    }
//
//    private void enableReadModeButton() {
//        runOnUiThread(() -> {
//                    try {
//                        readModeMenuItem.setEnabled(true);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//        );
//    }
//
//    private void stopReadMode() {
//        readModeEnabled = false;
//        webViewMain.setVisibility(View.VISIBLE);
//        webViewReadmode.setVisibility(View.GONE);
//        readModeMenuItem.setTitle("Read mode");
//        showMenuIitemIcon(reloadPageMenuItem);
//        disableReadModeLayout();
//    }
//
//    private void buildKeywordsChips(List<String> keywords, ChipGroup chipGroup) {
//        if(keywords==null || keywords.isEmpty()) return;
//        for(String currentKeyword: keywords) {
//            addChip(currentKeyword, chipGroup);
//        }
//    }
//
//    private void checkIfAlreadyInBookmarks() {
//        if(articleAlreadyInBookmarks) {
//            changeMenuItemIcon(bookmarkMenuItem, R.drawable.ic_bookmark_added_yellow);
//        }
//        else {// if false, try again
//            webviewViewModel.getObservableBookmarkDuplicationResponse().observe(this, alreadyInBookmarks -> {
//                if(alreadyInBookmarks) {
//                    articleAlreadyInBookmarks = true;
//                    changeMenuItemIcon(bookmarkMenuItem, R.drawable.ic_bookmark_added_yellow);
//                }
//                else articleAlreadyInBookmarks = false;
//            });
//            webviewViewModel.checkIsInBookmarks(currentArticle);
//        }
//
//    }
//
//    private void showLoadCompletedSnackbar(Context context) {
//        String message = getString(R.string.string_page_load_completed);
//        snackbar = Snackbar.make(view, "",Snackbar.LENGTH_SHORT);
//        snackbar.setText(message);
//        snackbar.setText(message);
//        snackbar.setTextColor(context.getResources().getColor(R.color.white));
//        snackbar.setBackgroundTint(context.getResources().getColor(R.color.green));
//        snackbar.setAnchorView(R.id.bottom_nav_view);
//        snackbar.setAction("ok", v -> snackbar.dismiss());
//        snackbar.setActionTextColor(context.getResources().getColor(R.color.white));
//        snackbar.show();
//    }
//
//    private void showRedSnackbar(String message, int duration, String actionLabel, View.OnClickListener action, Context context) {
//        try {
//            snackbar = Snackbar.make(view, "", duration);
//            snackbar.setText(message);
//            snackbar.setTextColor(context.getResources().getColor(R.color.white));
//            snackbar.setBackgroundTint(context.getResources().getColor(R.color.red));
//            snackbar.setAction(actionLabel, action);
//            snackbar.setActionTextColor(context.getResources().getColor(R.color.white));
//            snackbar.show();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void showErrorSnackbar(String message, Context context) {
//        try {
//            snackbar = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE);
//            snackbar.setText(message);
//            snackbar.setTextColor(context.getResources().getColor(R.color.white));
//            snackbar.setBackgroundTint(context.getResources().getColor(R.color.red));
////        snackbar.setAnchorView(R.id.nav_view);
//            snackbar.setAction("retry", v -> {
//                snackbar.dismiss();
//                webViewMain.loadUrl(webpageUrl);
//            });
//            snackbar.setActionTextColor(context.getResources().getColor(R.color.white));
//            snackbar.show();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void showBottomToast(String message) {
//        if(toast!=null) toast.cancel();
//        toast = Toast.makeText(this,message, Toast.LENGTH_SHORT);
//        toast.show();
//    }
//
//    private void showCenteredToast(String message) {
//        if(toast!=null) toast.cancel();
//        toast = Toast.makeText(this,message, Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.CENTER, 0, 0);
//        toast.show();
//    }
//
//    private void hideMenuItemIcon(MenuItem menuItem) {
//        if(menuItem !=null)
//            menuItem.setVisible(false);
//    }
//
//    private void showMenuIitemIcon(MenuItem menuItem) {
//        if(menuItem !=null)
//            menuItem.setVisible(true);
//    }
//
//    private void changeMenuItemIcon(MenuItem menuItem, int iconId) {
//        if(menuItem !=null)
//            menuItem.setIcon(ResourcesCompat.getDrawable(getResources(), iconId, null));
//    }
//
//    private boolean hasParagraphs(org.jsoup.nodes.Document doc) {
//        boolean result = false;
//        if(doc==null) return result;
//
//
//        Elements pTag = doc.select("p");
//
//        if(pTag!=null && pTag.size()>0)
//            result = true;
//
//
//        return result;
//    }
//
////    private void setEnterTransition() {
////        // TRANSITION
////        MaterialContainerTransform transform = new MaterialContainerTransform();
//////        transform.setInterpolator(new FastOutSlowInInterpolator());
////        transform.setDrawingViewId(R.id.nav_host_fragment);
//////        transform.setContainerColor(Color.WHITE);
//////        transform.setFadeMode(MaterialContainerTransform.FADE_MODE_THROUGH);
////        transform.setDuration(700);
////        transform.setAllContainerColors(getResources().getColor(R.color.white));
////
////        setSharedElementEnterTransition(transform);
//////        setSharedElementReturnTransition(transform);
////    }

}// end WebviewActivity