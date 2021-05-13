package com.nocorp.scienceboard.utility.ad.appodeal;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.Native;
import com.appodeal.ads.NativeAd;
import com.appodeal.ads.NativeCallbacks;
import com.explorestack.consent.Consent;
import com.explorestack.consent.ConsentForm;
import com.explorestack.consent.ConsentFormListener;
import com.explorestack.consent.ConsentInfoUpdateListener;
import com.explorestack.consent.ConsentManager;
import com.explorestack.consent.exception.ConsentManagerException;
import com.nocorp.scienceboard.BuildConfig;
import com.nocorp.scienceboard.databinding.ActivityWebviewBinding;
import com.nocorp.scienceboard.ui.viewholder.ListItem;
import com.nocorp.scienceboard.utility.ad.admob.AdProvider;
import com.nocorp.scienceboard.utility.ad.admob.model.ListAd;
import com.yandex.metrica.impl.ob.If;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppodealAdProvider {
    private final String TAG = this.getClass().getSimpleName();
    private static AppodealAdProvider singletonInstance;
    private List<NativeAd> nativeAdsList;
    private List<NativeAd> cachedAds;
    private ConsentForm consentForm;
//    private Consent consent;
    private long NUM_ADS_TO_LOAD;



    private AppodealAdProvider() {
        nativeAdsList = new ArrayList<>();
        cachedAds = new ArrayList<>();
        Appodeal.disableLocationPermissionCheck();
//        Appodeal.disableWriteExternalStoragePermissionCheck();
//        Appodeal.setAutoCache(Appodeal.NATIVE, false);
        Appodeal.setLogLevel(com.appodeal.ads.utils.Log.LogLevel.debug);
//        Appodeal.setTesting(true);
    }

    public static AppodealAdProvider getInstance() {
        if(singletonInstance==null) {
            singletonInstance = new AppodealAdProvider();
            Log.d(AppodealAdProvider.class.getSimpleName(), "NOPPYS_BOARD - getInstance: ad provider instantiated");
        }

        return singletonInstance;
    }






    public void requestAppodealConsent(Activity activity, int numAsToLoad) {
        this.NUM_ADS_TO_LOAD = numAsToLoad;
        ConsentManager.getInstance(activity).requestConsentInfoUpdate(
                BuildConfig.BABAD_A,
                new ConsentInfoUpdateListener() {
                    @Override
                    public void onConsentInfoUpdated(Consent consent) {
                        Log.d(TAG, "onConsentInfoUpdated: called");
                        showConsentLogs(consent);
                        boolean consentFormRequired = checkShouldShowConsentForm(activity);
                        if(consentFormRequired) {
                            if(consent.getIabConsentString() == null || consent.getIabConsentString().isEmpty()) {
                                showConsentForm(activity);
                            }
                            else {
                                initAppodeal(activity);
                            }
                        }
                        else {
                            initAppodeal(activity);
                        }
                    }

                    @Override
                    public void onFailedToUpdateConsentInfo(ConsentManagerException exception) {
                        int errorCode = exception.getCode();
                        Log.d(TAG, "onFailedToUpdateConsentInfo: cause: " + exception.getReason() + " (code: " + errorCode + ")");
                    }
                });
    }


    private void showConsentLogs(Consent consent) {
        // Check regulation
        Log.d(TAG, "showConsentLogs: zone: " + consent.getZone());

        // Check consent status
        Log.d(TAG, "showConsentLogs: status: " + consent.getStatus());

        // Get consent string
        Log.d(TAG, "showConsentLogs: IabConsentString: " + consent.getIabConsentString());
    }


    /**
     * only for EEU/UK residents or CCPA
     */
    private void showConsentForm(Activity activity) {
        Log.d(TAG, "showConsentForm: called");
        // Create new Consent form listener
        ConsentFormListener consentFormListener = new ConsentFormListener() {
            @Override
            public void onConsentFormLoaded() {
                // Consent form was loaded. Now you can display consent form as activity or as dialog
                Log.d(TAG, "onConsentFormLoaded: called");

                // Show consent dialog as Dialog
                consentForm.showAsDialog();
            }

            @Override
            public void onConsentFormError(ConsentManagerException error) {
                // Consent form loading or showing failed. More info can be found in 'error' object
                Log.d(TAG, "onConsentFormError: cause: " + error.getReason() + "( "+ error.getCode() + " )");
            }

            @Override
            public void onConsentFormOpened() {
                // Consent form was shown
                Log.d(TAG, "onConsentFormOpened: called");
            }

            @Override
            public void onConsentFormClosed(Consent consent) {
                // Consent form was closed
                Log.d(TAG, "onConsentFormClosed: called");
                initAppodeal(activity);
            }


        };

        // Create new Consent form instance
        consentForm = new ConsentForm.Builder(activity)
                .withListener(consentFormListener)
                .build();

        //
        consentForm.load();

        //
//        // Indicates that consent window ready to present
//        boolean loaded = consentForm.isLoaded();
    }

    private void initAppodeal(Activity activity) {
        Appodeal.updateConsent(true);
        Appodeal.setRequiredNativeMediaAssetType(Native.MediaAssetType.ALL);
        Appodeal.setNativeAdType(Native.NativeAdType.NoVideo);
        Consent consent = ConsentManager.getInstance(activity).getConsent();
        Appodeal.initialize(activity, BuildConfig.BABAD_A, Appodeal.NATIVE, consent);
        Log.d(TAG, "initAppodeal: called");

        //
        loadNativeAds(activity);
    }

    private void loadNativeAds(Activity activity) {
        Log.d(TAG, "loadNativeAds: called, num ads to load: " + NUM_ADS_TO_LOAD);
        Appodeal.cache(activity, Appodeal.NATIVE, (int) NUM_ADS_TO_LOAD);


//        int i=0;
//        while(true) {
//            if(Appodeal.isLoaded(Appodeal.NATIVE)) {
//                int numAdsLoaded = Appodeal.getAvailableNativeAdsCount();
//                Log.d(TAG, "loadNativeAds: ads loaded: " + numAdsLoaded);
//                nativeAdsList = Appodeal.getNativeAds(5);
//                break;
//            }
//            else {
//                Log.d(TAG, "loadNativeAds: not loaded yet " + ++i);
//            }
//        }

        Appodeal.setNativeCallbacks(new NativeCallbacks() {
            @Override
            public void onNativeLoaded() {
                // Called when native ads are loaded
                int numAdsLoaded = Appodeal.getAvailableNativeAdsCount();
                Log.d(TAG, "onNativeLoaded: ads loaded: " + numAdsLoaded);

//                nativeAdsList = Appodeal.getNativeAds(1);


            }
            @Override
            public void onNativeFailedToLoad() {
                // Called when native ads are failed to load
                Log.d(TAG, "onNativeFailedToLoad: called");
            }
            @Override
            public void onNativeShown(NativeAd nativeAd) {
                // Called when native ad is shown
                Log.d(TAG, "onNativeShown: called");
            }
            @Override
            public void onNativeShowFailed(NativeAd nativeAd) {
                // Called when native ad show failed
                Log.d(TAG, "onNativeShowFailed: cause: " + nativeAd.getTitle());
            }
            @Override
            public void onNativeClicked(NativeAd nativeAd) {
                // Called when native ads is clicked
                Log.d(TAG, "onNativeClicked: called for: " + nativeAd.getTitle());
            }
            @Override
            public void onNativeExpired() {
                // Called when native ads is expired
                Log.d(TAG, "onNativeExpired: called");
            }
        });
    }




    private boolean checkShouldShowConsentForm(Context context) {
        boolean result = false;

        // Get manager
        ConsentManager consentManager = ConsentManager.getInstance(context);

        // Get current ShouldShow status
        Consent.ShouldShow consentShouldShow = consentManager.shouldShowConsentDialog();

        if (consentShouldShow == Consent.ShouldShow.TRUE){
            // show dialog
            Log.d(TAG, "checkShouldShowConsentForm: YES, your are in EEU/UK");
            result = true;
        }
        else {
            Log.d(TAG, "checkShouldShowConsentForm: NO, you're not in EEU/UK");
        }

        return result;
    }



    public List<ListItem> populateListWithAds(List<ListItem> listToPopulate, int eachNitems) {
        List<ListItem> oldList = new ArrayList<>(listToPopulate);

        //
        fetchCachedAds();

        //
        if(nativeAdsList == null || nativeAdsList.isEmpty()) {
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

    private void fetchCachedAds() {
        if(cachedAds==null || cachedAds.isEmpty()) {
//            if(Appodeal.isLoaded(Appodeal.NATIVE)) {
//
//            }

            List<NativeAd> temp = Appodeal.getNativeAds((int) NUM_ADS_TO_LOAD);
            if(temp!=null) Log.d(TAG, "fetchCachedAds: temp: " + temp.size());
            if(temp!=null && !temp.isEmpty()) {
                Log.d(TAG, "fetchCachedAds: temp: " + temp.size());
                cachedAds.addAll(temp);
            }
        }
        else {
            nativeAdsList = cachedAds;
            Log.d(TAG, "fetchCachedAds: nativeAdsList size: " + nativeAdsList.size());
        }

        //        if(cachedAds!=null && !cachedAds.isEmpty()) {
//            Log.d(TAG, "populateListWithAds: native ads cached");
//            nativeAdsList = cachedAds;
//        }
//        else {
//            Log.d(TAG, "populateListWithAds: native ads cache now is null");
//        }
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
            Log.d(TAG, "populateListWithAds: adToLoad: " + adToLoad.getTitle() + " ( " + adToLoad.toString() + " )");
        } catch (Exception e) {
            Log.e(TAG, "pickRandomAd: cannot pick ad, cause: " + e.getCause());
        }

        return adToLoad;
    }






}// end AppodealAdProvider
