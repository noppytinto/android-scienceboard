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
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.databinding.FragmentWebviewBinding;
import com.nocorp.scienceboard.model.Article;

import static android.view.View.SCROLLBARS_INSIDE_OVERLAY;


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
        // Inflate the layout for this fragment
        viewBinding = FragmentWebviewBinding.inflate(inflater, container, false);
        view = viewBinding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar = viewBinding.toolbarWebviewFragment;
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        webViewMain = viewBinding.webViewWebviewFragment;
        progressIndicator = viewBinding.progressIndicatorWebviewFragment;
        currentTextSize = DEFAULT_TEXT_SIZE;
        stopMenuItem = viewBinding.toolbarWebviewFragment.getMenu().findItem(R.id.option_webviewMenu_stop);
        bookmarkMenuItem = viewBinding.toolbarWebviewFragment.getMenu().findItem(R.id.option_webviewMenu_bookmark);
        webviewViewModel = new ViewModelProvider(this).get(WebviewViewModel.class);

        // webview botomsheet
        webViewBottomSheet = viewBinding.includeWebviewFragment.webviewBottomSheetWebview;
        showButton = viewBinding.includeWebviewFragment.buttonWebviewFragmentBottomSheet;
        bottomSheet = view.findViewById(R.id.include_webviewFragment);
//        bottomSheet.setOnFocusChangeListener((v, hasFocus) -> {
//            if(hasFocus) {
//                setupBackButtonBehaviorForWebviewMain(webViewBottomSheet);
//            }
//            else {
//                ignoreBackButton(webViewBottomSheet);
//            }
//        });


        //
        if (getArguments() != null) {
            // the article is always non-null
            this.currentArticle = WebviewFragmentArgs.fromBundle(getArguments()).getArticleArgument();
            this.webpageUrl = currentArticle.getWebpageUrl();
            this.sourceName = currentArticle.getSourceName();

            if(sourceName!=null && !sourceName.isEmpty()) {
                //TODO
//                toolbar.setTitleTextColor(getResources().getColor(R.color.white));
                toolbar.setTitle(sourceName);
            }

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
        final BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setDraggable(false);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                    setupBackButtonBehaviorForWebviewMain(webViewBottomSheet);
                    showButton.setText(R.string.string_close);
                    showButton.setOnClickListener(v -> {
                        if (behavior.getState() == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                    });
                }
                else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    ignoreBackButton(webViewBottomSheet);
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
            if(snackbar!=null) snackbar.dismiss();
            webViewMain.stopLoading();
            showBottomToast(getString(R.string.string_page_load_stopped));
            return true;
        }
        else if(item.getItemId() == R.id.option_webviewMenu_refresh) {
            if(snackbar!=null) snackbar.dismiss();
            webViewMain.loadUrl(webpageUrl);
            showBottomToast(getString(R.string.string_refreshing_page));
            return true;
        }
        else if(item.getItemId() == R.id.option_webviewMenu_bookmark) {
            if( ! articleAlreadyInBookmarks) {
                webviewViewModel.getObservableAddToBookmarksResponse().observe(getViewLifecycleOwner(), addedToBookmarks -> {
                    if(addedToBookmarks) {
                        showCenteredToast("saved in bookmarks");
                        changeBookmarkIcon();
                    }
                    else {
                        // TODO: fail message
                    }
                });
                webviewViewModel.saveInBookmarks(currentArticle);
            }
            return true;
        }
        else if(item.getItemId() == R.id.option_webviewMenu_share) {
            shareText(webpageUrl);
            return true;
        }
        else if(item.getItemId() == R.id.option_webviewMenu_increaseTextSize) {
            increaseTextSize();
            return true;
        }
        else if(item.getItemId() == R.id.option_webviewMenu_decreaseTextSize) {
            decreaseTextSize();
            return true;
        }
        else if(item.getItemId() == R.id.option_webviewMenu_defaultTextSize) {
            setDefaultTextSize();
            return true;
        }
        else if(item.getItemId() == R.id.option_webviewMenu_delete) {
            WebStorage.getInstance().deleteAllData();
            showBottomToast(getString(R.string.string_browsing_data_deleted));
            return true;
        }
        return false;
    }












    //------------------------------------------------------------------------------------ METHODS

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

    private void shareText(String message) {
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

    private void increaseTextSize() {
        if(currentTextSize <= UPPER_TEXT_SIZE_LIMIT) {
            currentTextSize = currentTextSize + TEXT_SIZE_STEP;
            webSettingsMain.setTextZoom(currentTextSize);// where 90 is 90%; default value is ... 100
        }
    }

    private void decreaseTextSize() {
        if(currentTextSize >= LOWER_TEXT_SIZE_LIMIT){
            currentTextSize = currentTextSize - TEXT_SIZE_STEP;
            webSettingsMain.setTextZoom(currentTextSize);
        }
    }

    private void setDefaultTextSize() {
        currentTextSize = DEFAULT_TEXT_SIZE;
        webSettingsMain.setTextZoom(currentTextSize);
    }

    /**
     * override back button behavior for webviews.
     * Back button will go back in case of webviews
     */
    private void setupBackButtonBehaviorForWebviewMain(WebView webView) {
        webView.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN) {
                    WebView webView = (WebView) v;

                    switch(keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            if(webView.canGoBack()) {
                                webView.goBack();
                                return true;
                            }
                            break;
                    }
                }
                return false;
            }
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

    private void showErrorSnackbar(String message) {
        snackbar = Snackbar.make(view, "",Snackbar.LENGTH_INDEFINITE);
        snackbar.setText(message);
        snackbar.setTextColor(getResources().getColor(R.color.white));
        snackbar.setBackgroundTint(getResources().getColor(R.color.red));
        snackbar.setAnchorView(R.id.nav_view);
        snackbar.setAction("retry", v -> {
            snackbar.dismiss();
            webViewMain.loadUrl(webpageUrl);
        });
        snackbar.setActionTextColor(getResources().getColor(R.color.white));
        snackbar.show();
    }

    private void showBottomToast(String message) {
        if(toast!=null) toast.cancel();
        toast = Toast.makeText(requireContext(),message, Toast.LENGTH_LONG);
        toast.show();
    }

    private void showCenteredToast(String message) {
        if(toast!=null) toast.cancel();
        toast = Toast.makeText(requireContext(),message, Toast.LENGTH_LONG);
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
            bookmarkMenuItem.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_bookmark_added_white, null));
    }

}// end WebviewFragment