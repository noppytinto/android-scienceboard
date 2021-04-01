package com.nocorp.scienceboard.webview;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.nocorp.scienceboard.R;

import static android.view.View.SCROLLBARS_INSIDE_OVERLAY;


public class WebviewFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    private WebView webView;
    private String url;
    private LinearProgressIndicator progressIndicator;
    private View view;
    private Snackbar snackbar;



    //-------------------------------------------------------------------------


//    public static WebviewFragment newInstance(String param1, String param2) {
//        WebviewFragment fragment = new WebviewFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_webview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webView = view.findViewById(R.id.webView_webviewFragment);
        progressIndicator = view.findViewById(R.id.progressIndicator_webviewFragment);

        this.view = view;
        if (getArguments() != null) {
            // the url is always !=null and non-empty
            this.url = WebviewFragmentArgs.fromBundle(getArguments()).getUrl();
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        applyBrowsingRecommendedSettings(webView);
        webView.loadUrl("url");
    }



    //-------------------------------------------------------------------------

    private void applyBrowsingRecommendedSettings(WebView webView) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
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
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressIndicator.setVisibility(View.VISIBLE);
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
        String message = "Load completed.";
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

    @Override
    public void onDetach() {
        super.onDetach();
        if(snackbar!=null) snackbar.dismiss();
//        webView.clearCache(false);
//        WebStorage.getInstance().deleteAllData();
    }


}// end WebviewFragment