package com.nocorp.scienceboard.ui.webview;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nocorp.scienceboard.R;

import java.lang.reflect.Field;

import static android.view.View.SCROLLBARS_INSIDE_OVERLAY;

public class GoogleSearchFragment extends BottomSheetDialogFragment implements View.OnTouchListener {
    private final String TAG = this.getClass().getSimpleName();
    private WebSettings webSettingsBottomSheet;
    private WebView webViewBottomSheet;
    private View view;
    private FloatingActionButton closeButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_bottom_sheet_webview, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webViewBottomSheet = view.findViewById(R.id.webview_bottomSheetWebview);
        applyBrowsingRecommendedSettingsBottomSheet(webViewBottomSheet);
        webViewBottomSheet.loadUrl("https://www.google.com");
        closeButton = view.findViewById(R.id.floatingActionButton_bottomSheetWebview);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }


    @Override
    public void setupDialog(Dialog dialog, int style) {
        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
        bottomSheetDialog.setContentView(R.layout.layout_bottom_sheet_webview);

        try {
            Field behaviorField = bottomSheetDialog.getClass().getDeclaredField("behavior");
            behaviorField.setAccessible(true);
            final BottomSheetBehavior behavior = (BottomSheetBehavior) behaviorField.get(bottomSheetDialog);
            behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING){
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                }
            });
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

























    private void applyBrowsingRecommendedSettingsBottomSheet(WebView webView) {
        webSettingsBottomSheet = webView.getSettings();
        webSettingsBottomSheet.setJavaScriptEnabled(true);
//        webSettingsBottomSheet.setJavaScriptCanOpenWindowsAutomatically(true);// TODO: allow youtube to set a video fullscreen
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



}// end GoogleSearchFragment
