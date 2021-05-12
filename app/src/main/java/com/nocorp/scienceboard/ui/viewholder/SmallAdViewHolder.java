package com.nocorp.scienceboard.ui.viewholder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.appodeal.ads.NativeAd;
import com.appodeal.ads.NativeAdView;
import com.appodeal.ads.NativeIconView;
import com.nocorp.scienceboard.R;

public class SmallAdViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = this.getClass().getSimpleName();
    public ConstraintLayout parent;
    public View view;

    public SmallAdViewHolder(@NonNull View itemView) {
        super(itemView);
        view = itemView;
        parent = itemView.findViewById(R.id.constraintLayout_layoutNativeAdArticlesListLevel_parent);
    }


    public void displayNativeAd(NativeAd givenNativeAd) {
        if(givenNativeAd==null) return;

        //
        NativeAdView nativeAdView = (NativeAdView) view.findViewById(R.id.native_layout);
        nativeAdView.unregisterViewForInteraction();

        //
        TextView titleView = (TextView) nativeAdView.findViewById(R.id.textView_layoutNativeAdArticlesListLevel_headline);
        titleView.setText(givenNativeAd.getTitle());
        nativeAdView.setTitleView(titleView);

        //
        NativeIconView nativeIconView = (NativeIconView) nativeAdView.findViewById(R.id.imageView_layoutNativeAdArticlesListLevel_adImage);
        nativeAdView.setNativeIconView(nativeIconView);

        //
        View providerView = givenNativeAd.getProviderView(itemView.getContext());
        if (providerView != null) {
            if (providerView.getParent() != null && providerView.getParent() instanceof ViewGroup) {
                ((ViewGroup) providerView.getParent()).removeView(providerView);
            }
            FrameLayout providerViewContainer = (FrameLayout) nativeAdView.findViewById(R.id.view_layoutNativeAdArticlesListLevel_providerView);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            providerViewContainer.addView(providerView, layoutParams);
        }
        nativeAdView.setProviderView(providerView);

        //
        Button ctaButton = (Button) nativeAdView.findViewById(R.id.button_layoutNativeAdArticlesListLevel_action);
        ctaButton.setText(givenNativeAd.getCallToAction());
        nativeAdView.setCallToActionView(ctaButton);

        //
        nativeAdView.registerView(givenNativeAd);
    }





//    public void displayNativeAd(NativeAd givenNativeAd) {
//        if(givenNativeAd!=null) {
//            // Inflate a layout and add it to the parent ViewGroup.
//            LayoutInflater inflater = (LayoutInflater) parent.getContext()
//                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            NativeAdView adView = (NativeAdView) inflater
//                    .inflate(R.layout.layout_small_ad_articles_list_level, null);
//
//            TextView headline = adView.findViewById(R.id.textView_layoutNativeAdArticlesListLevel_headline);
//            headline.setText(givenNativeAd.getHeadline());
//            adView.setHeadlineView(headline);
//
//            Button actionButton = adView.findViewById(R.id.buttonlayoutNativeAdArticlesListLevel_action);
//            actionButton.setText(givenNativeAd.getCallToAction());
//            adView.setCallToActionView(actionButton);
//
//            // OLD
////            ImageView adImage = adView.findViewById(R.id.imageView_layoutNativeAdArticlesListLevel_adImage);
////            List<NativeAd.Image> images = nativeAd.getImages();
////            Collections.shuffle(images);
////            adImage.setImageDrawable(nativeAd.getImages().get(0).getDrawable());
////            adView.setImageView(adImage);
//
//            // NEW, with mediation
//            MediaView mediaView = adView.findViewById(R.id.imageView_layoutNativeAdArticlesListLevel_adImage);
//            mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
//            MediaContent mediaContent = givenNativeAd.getMediaContent();
//            if(mediaContent != null) {
//                boolean hasVideoContent = mediaContent.hasVideoContent();
//                if(hasVideoContent) {
//                    Log.d(TAG, "displayNativeAd: has video content");
//                }
//                else {
//                    Log.d(TAG, "displayNativeAd: could have image content");
//                    Drawable mainImage = mediaContent.getMainImage();
//                    if(mainImage==null) {
//                        Log.d(TAG, "displayNativeAd: image content is null");
////                        Bundle extras = givenNativeAd.getExtras();
////                        if (extras.containsKey(FacebookAdapter.KEY_SOCIAL_CONTEXT_ASSET)) {
////                            String socialContext = extras.get(FacebookAdapter.KEY_SOCIAL_CONTEXT_ASSET);
////                        }
//
//                    }
//                    else {
//                        Log.d(TAG, "displayNativeAd: it does have image content");
//                    }
//                }
//                mediaView.setMediaContent(mediaContent);
//            }
//
//
//            adView.setMediaView(mediaView);
//
//
//            // Call the NativeAdView's setNativeAd method to register the
//            // NativeAdObject.
//            adView.setNativeAd(givenNativeAd);
//
//            // Ensure that the parent view doesn't already contain an ad view.
//            parent.removeAllViews();
//
//            // Place the AdView into the parent.
//            parent.addView(adView);
//        }
//    }


}
