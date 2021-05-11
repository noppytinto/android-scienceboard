package com.nocorp.scienceboard.ui.about;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.nocorp.scienceboard.databinding.FragmentAboutWebviewBinding;


public class AboutWebviewFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    private FragmentAboutWebviewBinding viewBinding;
    private View view;
    private WebView webview;
    private final String TERMS_URL = "https://noppy.altervista.org/terms.html";
    private final String PRIVACY_URL = "https://noppy.altervista.org";


    // TODO: Rename and change types and number of parameters
    public static AboutWebviewFragment newInstance() {
        return new AboutWebviewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentAboutWebviewBinding.inflate(inflater, container, false);
        view = viewBinding.getRoot();
        Log.d(TAG, "onCreateView: called");
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webview = viewBinding.webviewAboutFragment;

        if (getArguments() != null) {
            String infoType = AboutWebviewFragmentArgs.fromBundle(getArguments()).getAboutInfoType();
            applyBasicWebviewSettings(webview);

            if(infoType.equals("privacy")) {
                webview.loadUrl(PRIVACY_URL);
            }
            else if(infoType.equals("terms")) {
                webview.loadUrl(TERMS_URL);
            }
        }


    }

    private void applyBasicWebviewSettings(WebView webView) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setSupportZoom(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webSettings.setSafeBrowsingEnabled(true);
        }
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        webView.setScrollbarFadingEnabled(false);
        webSettings.setLoadWithOverviewMode(true);
//        webSettings.setUseWideViewPort(true);

    }
}// end AboutWebviewFragment