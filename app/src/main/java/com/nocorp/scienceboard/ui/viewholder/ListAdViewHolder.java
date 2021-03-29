package com.nocorp.scienceboard.ui.viewholder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.nocorp.scienceboard.R;

import java.util.Collections;
import java.util.List;

public class ListAdViewHolder extends RecyclerView.ViewHolder {
    public ConstraintLayout parent;

    public ListAdViewHolder(@NonNull View itemView) {
        super(itemView);
        parent = itemView.findViewById(R.id.constraintLayout_layoutNativeAdArticlesListLevel_parent);
    }

    public void displayNativeAd(NativeAd nativeAd) {
        if(nativeAd!=null) {
            // Inflate a layout and add it to the parent ViewGroup.
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            NativeAdView adView = (NativeAdView) inflater
                    .inflate(R.layout.layout_native_ad_articles_list_level, null);

            TextView headline = adView.findViewById(R.id.textView_layoutNativeAdArticlesListLevel_headline);
            headline.setText(nativeAd.getHeadline());
            adView.setHeadlineView(headline);

            Button actionButton = adView.findViewById(R.id.buttonlayoutNativeAdArticlesListLevel_action);
            actionButton.setText(nativeAd.getCallToAction());
            adView.setCallToActionView(actionButton);

            ImageView adImage = adView.findViewById(R.id.imageView_layoutNativeAdArticlesListLevel_adImage);
            List<NativeAd.Image> images = nativeAd.getImages();
            Collections.shuffle(images);
            adImage.setImageDrawable(nativeAd.getImages().get(0).getDrawable());
            adView.setImageView(adImage);

            // Call the NativeAdView's setNativeAd method to register the
            // NativeAdObject.
            adView.setNativeAd(nativeAd);

            // Ensure that the parent view doesn't already contain an ad view.
            parent.removeAllViews();

            // Place the AdView into the parent.
            parent.addView(adView);
        }
    }


}
