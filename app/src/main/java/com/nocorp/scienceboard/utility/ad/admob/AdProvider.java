package com.nocorp.scienceboard.utility.ad.admob;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.nocorp.scienceboard.utility.ad.admob.model.ListAd;
import com.nocorp.scienceboard.ui.viewholder.ListItem;

import java.util.ArrayList;
import java.util.List;

public class AdProvider {
    private final String TAG = this.getClass().getSimpleName();
    private static AdProvider singletonInstance;
    private static boolean adMobInitialized;
    private final String AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"; // TODO: this is a test id, change on production

    private List<NativeAd> nativeAds;
    private OnNativeAdsloadedListener listener;
    private static boolean viewDestroyed;
    private AdLoader adLoader;

    public interface OnNativeAdsloadedListener {
        public void onNativeAdsloaded();
    }


    private AdProvider() {
        nativeAds = new ArrayList<>();
    }


    public void initAdMob(Context context) {
        if(adMobInitialized) return;

        MobileAds.initialize(context, initializationStatus -> {
            Log.d(AdProvider.class.getSimpleName(), "SCIENCE_BOARD - onInitializationComplete: admob initilized");
            adMobInitialized = true;
        });
    }

    public static AdProvider getInstance() {
        if(singletonInstance==null) {
            singletonInstance = new AdProvider();
            Log.d(AdProvider.class.getSimpleName(), "SCIENCE_BOARD - getInstance: ad provider instantiated, now call initAdMob()");

        }

       return singletonInstance;
    }

    /**
     * populate a List<ListItem> list, with ads
     *
     * @param listToPopulate
     * @param eachNitems      add an ad each n items
     * @return
     */
    public List<ListItem> populateListWithAds(List<ListItem> listToPopulate, int eachNitems) {
        List<ListItem> oldList = new ArrayList<>(listToPopulate);

        if( ! adMobInitialized) {
            Log.e(TAG, "SCIENCE_BOARD - populateListWithAds: admob not initilized");
            return oldList;
        }
        if(nativeAds==null || nativeAds.size()==0) {
            Log.e(TAG, "SCIENCE_BOARD - populateListWithAds: nativeAds list is empty");
            return oldList;
        }

        List<ListItem> newList = new ArrayList<>();

        int j=0;
        int baseStep = eachNitems;
        int increment = baseStep + 1;

        for(int i=0; i<listToPopulate.size(); i++) {
            ListItem listItem = oldList.get(i);
            if(i==baseStep) {
                if(j>=nativeAds.size()) j=0;

                ListAd listAd = new ListAd();
                listAd.setAd(nativeAds.get(j));
                newList.add(listAd);

                baseStep = baseStep + increment;
            }
            else {
                newList.add(listItem);
            }
        }

        return newList;
    }


    public void destroyAds() {
        viewDestroyed = true;
        destroyCurrentAds();
    }

    private void destroyCurrentAds() {
        for(NativeAd ad: nativeAds) {
            ad.destroy();
        }
        nativeAds = new ArrayList<>();
    }


    public void loadSomeAds(int adsToLoad, Context context) {
        if( ! adMobInitialized) {
            Log.e(TAG, "SCIENCEBOARD - loadSomeAds: admob not initilized");
//            return;
        }

        nativeAds = new ArrayList<>();

        adLoader = new AdLoader.Builder(context, AD_UNIT_ID) // TODO: this is a test id, change on production
                .forNativeAd(ad -> {
                    // Show the ad.
                    if (viewDestroyed) {
                        ad.destroy();
                        Log.d(TAG, "SCIENCE_BOARD - onActivityCreated: ad destroyed");
                        return;
                    }

                    if (adLoader.isLoading()) {
                        // The AdLoader is still loading ads.
                        // Expect more adLoaded or onAdFailedToLoad callbacks.
                        Log.d(TAG, "SCIENCE_BOARD - onActivityCreated: ad loading");

                    } else {
                        Log.d(TAG, "SCIENCE_BOARD - onActivityCreated: ad loaded");
                        // The AdLoader has finished loading ads.
                        nativeAds.add(ad);
//                        displayNativeAd(nativeAd);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        // Handle the failure by logging, altering the UI, and so on.
                        Log.e(TAG, "SCIENCE_BOARD - onAdFailedToLoad: ad failed to load");
                    }

                    @Override
                    public void onAdClicked() {
                        // Log the click event or other custom behavior.
                        Log.d(TAG, "SCIENCE_BOARD - onAdClicked: ad clicked");
                    }

                })
                .withNativeAdOptions(new NativeAdOptions.Builder().build())
                .build();
        adLoader.loadAds(new AdRequest.Builder().build(), adsToLoad);
    }






}
