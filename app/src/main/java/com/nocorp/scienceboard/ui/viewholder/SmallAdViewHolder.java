package com.nocorp.scienceboard.ui.viewholder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ads.mediation.facebook.FacebookAdapter;
import com.google.ads.mediation.facebook.FacebookMediationAdapter;
import com.google.android.gms.ads.MediaContent;
import com.google.android.gms.ads.ResponseInfo;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.nocorp.scienceboard.R;

import java.util.Collections;
import java.util.List;

public class SmallAdViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = this.getClass().getSimpleName();
    public ConstraintLayout parent;

    public SmallAdViewHolder(@NonNull View itemView) {
        super(itemView);
        parent = itemView.findViewById(R.id.constraintLayout_layoutNativeAdArticlesListLevel_parent);
    }

    public void displayNativeAd(NativeAd givenNativeAd) {
        if(givenNativeAd!=null) {
            // Inflate a layout and add it to the parent ViewGroup.
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            NativeAdView adView = (NativeAdView) inflater
                    .inflate(R.layout.layout_small_ad_articles_list_level, null);

            TextView headline = adView.findViewById(R.id.textView_layoutNativeAdArticlesListLevel_headline);
            headline.setText(givenNativeAd.getHeadline());
            adView.setHeadlineView(headline);

            Button actionButton = adView.findViewById(R.id.buttonlayoutNativeAdArticlesListLevel_action);
            actionButton.setText(givenNativeAd.getCallToAction());
            adView.setCallToActionView(actionButton);

            // OLD
//            ImageView adImage = adView.findViewById(R.id.imageView_layoutNativeAdArticlesListLevel_adImage);
//            List<NativeAd.Image> images = nativeAd.getImages();
//            Collections.shuffle(images);
//            adImage.setImageDrawable(nativeAd.getImages().get(0).getDrawable());
//            adView.setImageView(adImage);

            // NEW, with mediation
            MediaView mediaView = adView.findViewById(R.id.imageView_layoutNativeAdArticlesListLevel_adImage);
            mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
            MediaContent mediaContent = givenNativeAd.getMediaContent();
            if(mediaContent != null) {
                boolean hasVideoContent = mediaContent.hasVideoContent();
                if(hasVideoContent) {
                    Log.d(TAG, "displayNativeAd: has video content");
                }
                else {
                    Log.d(TAG, "displayNativeAd: could have image content");
                    Drawable mainImage = mediaContent.getMainImage();
                    if(mainImage==null) {
                        Log.d(TAG, "displayNativeAd: image content is null");
//                        Bundle extras = givenNativeAd.getExtras();
//                        if (extras.containsKey(FacebookAdapter.KEY_SOCIAL_CONTEXT_ASSET)) {
//                            String socialContext = extras.get(FacebookAdapter.KEY_SOCIAL_CONTEXT_ASSET);
//                        }

                    }
                    else {
                        Log.d(TAG, "displayNativeAd: it does have image content");
                    }
                }
                mediaView.setMediaContent(mediaContent);
            }


            adView.setMediaView(mediaView);


            // Call the NativeAdView's setNativeAd method to register the
            // NativeAdObject.
            adView.setNativeAd(givenNativeAd);

            // Ensure that the parent view doesn't already contain an ad view.
            parent.removeAllViews();

            // Place the AdView into the parent.
            parent.addView(adView);
        }
    }


}
