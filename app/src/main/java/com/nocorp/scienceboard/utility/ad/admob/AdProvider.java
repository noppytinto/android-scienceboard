package com.nocorp.scienceboard.utility.ad.admob;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.nocorp.scienceboard.BuildConfig;
import com.nocorp.scienceboard.utility.ad.admob.model.ListAd;
import com.nocorp.scienceboard.ui.viewholder.ListItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AdProvider{
    private final String TAG = this.getClass().getSimpleName();
    private static AdProvider singletonInstance;
    private static boolean adMobInitialized;
//    private String abc = "ca-app-pub-3940256099942544/2247696110"; // TODO: this is a test id, change on production

    private List<NativeAd> nativeAdsList;
    private OnNativeAdsloadedListener listener;
    private static boolean viewDestroyed;
    private AdLoader adLoader;

    private int numAdsLoaded;
    //    private final int NUM_ADS_TO_LOAD = 5;
    private long NUM_ADS_TO_LOAD;

    //
    private OnAdmobInitilizedListener admobInitilizedListener;


    public interface OnNativeAdsloadedListener {
        public void onNativeAdsloaded();
    }


    private AdProvider() {
        nativeAdsList = new ArrayList<>();
        NUM_ADS_TO_LOAD = 1;
    }

    private AdProvider(OnAdmobInitilizedListener admobInitilizedListener) {
        this();
        this.admobInitilizedListener = admobInitilizedListener;
    }


    public void initAdMob(Context context, OnAdmobInitilizedListener admobInitilizedListener) {
        if(adMobInitialized) return;

        this.admobInitilizedListener = admobInitilizedListener;
        MobileAds.initialize(context, initializationStatus -> {
            // old
//            Log.d(AdProvider.class.getSimpleName(), "NOPPYS_BOARD - onInitializationComplete: admob initilized");
//            adMobInitialized = true;

            // new, with mediation
            Log.d(AdProvider.class.getSimpleName(), "NOPPYS_BOARD - onInitializationComplete: admob initilized");
            Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
            for (String adapterClass : statusMap.keySet()) {
                AdapterStatus status = statusMap.get(adapterClass);
                Log.d("MyApp", String.format("Adapter name: %s, Description: %s, Latency: %d", adapterClass, status.getDescription(), status.getLatency()));
            }

            //
            buildAdLoader(context);

            //
            adMobInitialized = true;

            //
            admobInitilizedListener.onAdmobInitialized();
        });
    }



    public static AdProvider getInstance() {
        if(singletonInstance==null) {
            singletonInstance = new AdProvider();
            Log.d(AdProvider.class.getSimpleName(), "NOPPYS_BOARD - getInstance: ad provider instantiated, now call initAdMob()");
        }

        return singletonInstance;
    }

    public static AdProvider getInstance(OnAdmobInitilizedListener admobInitilizedListener) {
        if(singletonInstance==null) {
            singletonInstance = new AdProvider(admobInitilizedListener);
            Log.d(AdProvider.class.getSimpleName(), "NOPPYS_BOARD - getInstance: ad provider instantiated, now call initAdMob()");
        }

        return singletonInstance;
    }



    public void destroyAds() {
        viewDestroyed = true;
        destroyCurrentAds();
    }

    private void destroyCurrentAds() {
        for(NativeAd ad: nativeAdsList) {
            ad.destroy();
        }
        nativeAdsList = new ArrayList<>();
    }


    public void loadSomeAds(long adsToLoad, AdRequest adRequest) {
        if( ! adMobInitialized) {
            Log.e(TAG, "NOPPYS_BOARD - loadSomeAds: admob not initilized");
//            return;
        }

        NUM_ADS_TO_LOAD = adsToLoad;
        nativeAdsList = new ArrayList<>();


//            buildAdLoader(context);

        // old
//        adLoader.loadAds(new AdRequest.Builder().build(), adsToLoad);

        // new, with mediation
        Log.d(TAG, "NOPPYS_BOARD - loadSomeAds: ads to request: " + adsToLoad);
        adLoader.loadAd(adRequest);
    }


    public void loadSomeAds(long adsToLoad) {
        if( ! adMobInitialized) {
            Log.e(TAG, "NOPPYS_BOARD - loadSomeAds: admob not initilized");
//            return;
        }

        NUM_ADS_TO_LOAD = adsToLoad;
        nativeAdsList = new ArrayList<>();


//            buildAdLoader(context);

        // old
//        adLoader.loadAds(new AdRequest.Builder().build(), adsToLoad);

        // new, with mediation
        Log.d(TAG, "NOPPYS_BOARD - loadSomeAds: ads to request: " + adsToLoad);
        adLoader.loadAd(new AdRequest.Builder().build());
    }


    public void reloadAds(AdRequest adRequest) {
        if( ! adMobInitialized) {
            Log.e(TAG, "NOPPYS_BOARD - loadSomeAds: admob not initilized");
            return;
        }
        nativeAdsList = new ArrayList<>();
        numAdsLoaded = 0;
        adLoader.loadAd(adRequest);
    }

    private void buildAdLoader(Context context) {
        adLoader = new AdLoader.Builder(context, BuildConfig.BABAD) // TODO: this is a test id, change on production
                .forNativeAd(nativeAd -> {
                    // Show the ad.
                    if (viewDestroyed) {
                        nativeAd.destroy();
                        Log.d(TAG, "NOPPYS_BOARD - loadSomeAds: ad destroyed");
                        return;
                    }

                    if (adLoader.isLoading()) {
                        // The AdLoader is still loading ads.
                        // Expect more adLoaded or onAdFailedToLoad callbacks.
                        Log.d(TAG, "NOPPYS_BOARD - loadSomeAds: ad loading");

                    } else {
                        // The AdLoader has finished loading ads.
                        nativeAdsList.add(nativeAd);

                        // load multiple ads
                        numAdsLoaded++;
                        Log.d(TAG, "NOPPYS_BOARD - loadSomeAds: ads loaded: " + numAdsLoaded);
                        if(numAdsLoaded < NUM_ADS_TO_LOAD) {
                            adLoader.loadAd(new AdRequest.Builder().build());
                        }
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        numAdsLoaded++;
                        // Handle the failure by logging, altering the UI, and so on.
                        Log.e(TAG, "NOPPYS_BOARD - onAdFailedToLoad: ad failed to load, cause: " + adError.getMessage());
//                        Toast.makeText(context, "onAdFailedToLoad: " + adError.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onAdClicked() {
                        // Log the click event or other custom behavior.
                        Log.d(TAG, "NOPPYS_BOARD - onAdClicked: ad clicked");
                    }

                })
                .withNativeAdOptions(new NativeAdOptions.Builder().build())
                .build();
    }


    public List<ListItem> populateListWithAds(List<ListItem> listToPopulate, int eachNitems) {
        List<ListItem> oldList = new ArrayList<>(listToPopulate);

        if( ! adMobInitialized) {
            Log.e(TAG, "NOPPYS_BOARD - populateListWithAds: admob not initilized");
            return oldList;
        }
        if(nativeAdsList ==null || nativeAdsList.isEmpty()) {
            Log.e(TAG, "NOPPYS_BOARD - populateListWithAds: nativeAds list is empty");
            return oldList;
        }

        List<ListItem> newList = new ArrayList<>();

        int j=0;
        int baseStep = eachNitems;
        int increment = baseStep + 1;

        for(int i=0; i<listToPopulate.size(); i++) {
            ListItem currentListItem = oldList.get(i);
            if(i==baseStep) {
//                if(j >= nativeAdsList.size()) j=0; // for picking native ads in sequential order

                ListAd listAd = new ListAd();
//                listAd.setAd(nativeAdsList.get(j));
                NativeAd adToLoad = pickRandomAd(nativeAdsList);
                if(adToLoad!=null) {
                    listAd.setAd(adToLoad);
                    // add ad
                    newList.add(listAd);
                }
                // and add old article
                newList.add(currentListItem);

                baseStep = baseStep + increment;
            }
            else {
                newList.add(currentListItem);
            }
        }

        return newList;
    }

    private NativeAd pickRandomAd(List<NativeAd> nativeAdsList) {
        if(nativeAdsList ==null || nativeAdsList.isEmpty()) {
            Log.e(TAG, "NOPPYS_BOARD - populateListWithAds: nativeAds list is empty");
            return null;
        }
        NativeAd adToLoad = null;

        try {
            Collections.shuffle(this.nativeAdsList);
            adToLoad = this.nativeAdsList.get(0);
            Log.d(TAG, "populateListWithAds: adToLoad: " + adToLoad.getHeadline() + " ( " + adToLoad.toString() + " )");
        } catch (Exception e) {
            Log.e(TAG, "pickRandomAd: cannot pick ad, cause: " + e.getCause());
        }

        return adToLoad;
    }


}// ned AdProvider
