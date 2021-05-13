package com.nocorp.scienceboard.utility.ad.admob;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;

import java.net.MalformedURLException;
import java.net.URL;

public class AdmobConsentProvider {
    private final String TAG = this.getClass().getSimpleName();
    private static AdmobConsentProvider singletonInstance;
    private OnAdmobConsentUpdatedListener admobConsentUpdatedListener;
    private ConsentForm form;


    public interface OnAdmobConsentUpdatedListener {
        public void onAdmobConsentUpdatedCompleted(AdRequest adRequest);
    }

    public AdmobConsentProvider() {

    }


    public static AdmobConsentProvider getInstance() {
        if(singletonInstance==null) {
            singletonInstance = new AdmobConsentProvider();
        }

        return singletonInstance;
    }

    public void checkUserConsent(Context context, OnAdmobConsentUpdatedListener admobConsentUpdatedListener) {
        ConsentInformation consentInformation = ConsentInformation.getInstance(context);
//        List<AdProvider> adProviders = consentInformation.getAdProviders();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            adProviders.forEach(adProvider -> Log.d(TAG, "onConsentInfoUpdated: " + adProvider.getId() + ", " + adProvider.getName()));
//        }
        String[] publisherIds = {"pub-5571913923339240" /*, "844199266194036_846361955977767", "372882920506261" ,"745426219483497"*/};

        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                Log.d(TAG, "onConsentInfoUpdated: consent status: " + consentStatus.name());

                if(ConsentInformation.getInstance(context).isRequestLocationInEeaOrUnknown()) {
                    Log.d(TAG, "onConsentInfoUpdated: user located in EU or unknown");
                    if(consentStatus.equals(ConsentStatus.PERSONALIZED) || consentStatus.equals(ConsentStatus.NON_PERSONALIZED)) {
                        updateConsentStatus(context, consentStatus, admobConsentUpdatedListener);
                    }
                    else { // UNKNOWN
                        // TODO: show form
                        showConsentForm(context, admobConsentUpdatedListener);

                    }
                }
                else {
                    Log.d(TAG, "onConsentInfoUpdated: user not located in EU");
                    // TODO: make requeste to Google Mobile Ads SDK
                    updateConsentStatus(context, consentStatus, admobConsentUpdatedListener);
                }
            }

            @Override
            public void onFailedToUpdateConsentInfo(String reason) {
                Log.d(TAG, "onFailedToUpdateConsentInfo: reason: " + reason);

            }
        });
    }


    private void showConsentForm(Context context, OnAdmobConsentUpdatedListener admobConsentUpdatedListener) {
        URL privacyUrl = null;
        try {
            privacyUrl = new URL("https://noppy.altervista.org/");
        } catch (MalformedURLException e) {
            // Handle error.
            Log.d(TAG, "showConsentForm: MalformedURLException: reason: " + e.getCause());
        }
        form = new ConsentForm.Builder(context, privacyUrl)
                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        // Consent form loaded successfully.
                        Log.d(TAG, "onConsentFormLoaded: called");

                        //
                        AdmobConsentProvider.this.form.show();
                    }

                    @Override
                    public void onConsentFormOpened() {
                        // Consent form was displayed.
                        Log.d(TAG, "onConsentFormOpened: called");
                    }

                    @Override
                    public void onConsentFormClosed(
                            ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        // Consent form was closed.
                        Log.d(TAG, "onConsentFormClosed: called");

                        //
                        updateConsentStatus(context, consentStatus, admobConsentUpdatedListener);

                    }

                    @Override
                    public void onConsentFormError(String errorDescription) {
                        // Consent form error.
                        Log.d(TAG, "onConsentFormError: reason: " + errorDescription);
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
//                .withAdFreeOption()
                .build();

        //
        form.load();


    }// end showConsentForm

    private void updateConsentStatus(Context context, ConsentStatus consentStatus, OnAdmobConsentUpdatedListener admobConsentUpdatedListener) {
        //                        ConsentInformation.getInstance(context).setTagForUnderAgeOfConsent(true);

        AdRequest adRequest;

        if(consentStatus.equals(ConsentStatus.PERSONALIZED)) {
            Log.d(TAG, "updateConsentStatus: consent status: PERSONALIZED");
//            ConsentInformation.getInstance(context).setConsentStatus(ConsentStatus.PERSONALIZED);
            adRequest = new AdRequest.Builder()
                    .build();

        }
        else if(consentStatus.equals(ConsentStatus.NON_PERSONALIZED)) {
            Log.d(TAG, "updateConsentStatus: consent status: NON-PERSONALIZED");

            Bundle extras = new Bundle();
            extras.putString("npa", "1");
            adRequest = new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                    .build();
        }
        else {
            Log.d(TAG, "updateConsentStatus: consent status: UNKNOWN");

            Bundle extras = new Bundle();
            extras.putString("npa", "1");
            adRequest = new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                    .build();
        }


        admobConsentUpdatedListener.onAdmobConsentUpdatedCompleted(adRequest);
    }


}// end AdmobConsentProvider
