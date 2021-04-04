package com.nocorp.scienceboard.ui.webview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.nocorp.scienceboard.R;
import com.nocorp.scienceboard.databinding.FragmentWebviewBinding;

import static android.view.View.SCROLLBARS_INSIDE_OVERLAY;


public class WebviewFragment extends Fragment implements androidx.appcompat.widget.Toolbar.OnMenuItemClickListener {
    private final String TAG = this.getClass().getSimpleName();
    private WebView webView;
    private String url;
    private String sourceLogoUrl;
    private LinearProgressIndicator progressIndicator;
    private View view;
    private Snackbar snackbar;
    private FragmentWebviewBinding viewBinding;
    private Toolbar toolbar;
    private Toast toast;
    private ImageView imageViewSourceLogo;
    private WebSettings webSettings;
    private final int TEXT_SIZE_STEP = 20;
    private final int DEFAULT_TEXT_SIZE = 90;
    private int currentTextSize;
    private final int UPPER_TEXT_SIZE_LIMIT = 200;
    private final int LOWER_TEXT_SIZE_LIMIT = 0;



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
        webView = viewBinding.webViewWebviewFragment;
        progressIndicator = viewBinding.progressIndicatorWebviewFragment;
        toolbar = viewBinding.toolbarWebviewFragment;
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        currentTextSize = DEFAULT_TEXT_SIZE;
//        viewBinding.toolbarWebviewFragment.inflateMenu(R.menu.menu_webview);


        if (getArguments() != null) {
            // the url is always !=null and non-empty
            this.url = WebviewFragmentArgs.fromBundle(getArguments()).getUrl();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        applyBrowsingRecommendedSettings(webView);
        webView.loadUrl(url);
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
            webView.stopLoading();
            showBottomToast(getString(R.string.string_page_load_stopped));
            return true;
        }
        else if(item.getItemId() == R.id.option_webviewMenu_refresh) {
            if(snackbar!=null) snackbar.dismiss();
            webView.loadUrl(url);
            showBottomToast(getString(R.string.string_refreshing_page));
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
        else if(item.getItemId() == R.id.option_webviewMenu_share) {
            shareText(url);
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
            webSettings.setTextZoom(currentTextSize);// where 90 is 90%; default value is ... 100
        }
    }

    private void decreaseTextSize() {
        if(currentTextSize >= LOWER_TEXT_SIZE_LIMIT){
            currentTextSize = currentTextSize - TEXT_SIZE_STEP;
            webSettings.setTextZoom(currentTextSize);
        }
    }

    private void setDefaultTextSize() {
        currentTextSize = DEFAULT_TEXT_SIZE;
        webSettings.setTextZoom(currentTextSize);
    }


    /**
     * override back button behavior for webviews.
     * Back button will go back in case of webviews
     */
    private void setupBackButtonBehaviorForWebview(WebView webView) {
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

    private void applyBrowsingRecommendedSettings(WebView webView) {
        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webSettings.setSafeBrowsingEnabled(true);
        }
        webView.setScrollBarStyle(SCROLLBARS_INSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        defineWebclientBehavior(webView);
    }

    private void defineWebclientBehavior(WebView webView) {
        setupBackButtonBehaviorForWebview(webView);

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
            webView.loadUrl(url);
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
        MenuItem saveItem = viewBinding.toolbarWebviewFragment.getMenu().findItem(R.id.option_webviewMenu_stop);
        saveItem.setVisible(false);
    }

    private void showStopIcon() {
        MenuItem saveItem = viewBinding.toolbarWebviewFragment.getMenu().findItem(R.id.option_webviewMenu_stop);
        saveItem.setVisible(true);
    }



}// end WebviewFragment