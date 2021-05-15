package com.nocorp.scienceboard.utility.ad.admob;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;
import com.nocorp.scienceboard.MainActivity;

import java.net.MalformedURLException;
import java.net.URL;

public class AdmobConsentManager {
    private final String TAG = this.getClass().getSimpleName();
    private static AdmobConsentManager singletonInstance;
    private OnAdmobConsentUpdatedListener admobConsentUpdatedListener;
    private ConsentForm form;

    // new (funding choices)
    private com.google.android.ump.ConsentInformation consentInformation;
    private com.google.android.ump.ConsentForm consentForm;

    //
    public interface OnAdmobConsentUpdatedListener {
        public void onAdmobConsentUpdatedCompleted(AdRequest adRequest);
    }







    //---------------------------------------------------------------------- CONSTRUCTORS

    public AdmobConsentManager() {

    }





    //---------------------------------------------------------------------- METHODS

    public static AdmobConsentManager getInstance() {
        if(singletonInstance==null) {
            singletonInstance = new AdmobConsentManager();
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
                        AdmobConsentManager.this.form.show();
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





    //----------------------------------------------------------- NEW (funding choices)

    private void getAdmobUserConsent_new(Context context, Activity activity) {
        // Set tag for underage of consent. false means users are not underage.
        ConsentRequestParameters params = new ConsentRequestParameters
                .Builder()
                .setTagForUnderAgeOfConsent(false)
                .build();

        consentInformation = UserMessagingPlatform.getConsentInformation(context);

        consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                new com.google.android.ump.ConsentInformation.OnConsentInfoUpdateSuccessListener() {
                    @Override
                    public void onConsentInfoUpdateSuccess() {
                        // The consent information state was updated.
                        // You are now ready to check if a form is available.
                        if (consentInformation.isConsentFormAvailable()) {
                            Log.d(TAG, "onConsentInfoUpdateSuccess: called, consent form available");

                            loadForm_new(context, activity);
                        }
                        else {
                            Log.d(TAG, "onConsentInfoUpdateSuccess: called, consent form NOT available");
                        }

                        Log.d(TAG, "onConsentInfoUpdateSuccess: called, consent type: " + consentInformation.getConsentType());
                        Log.d(TAG, "onConsentInfoUpdateSuccess: called, consent status: " + consentInformation.getConsentStatus());
                    }
                },
                new com.google.android.ump.ConsentInformation.OnConsentInfoUpdateFailureListener() {
                    @Override
                    public void onConsentInfoUpdateFailure(FormError formError) {
                        // Handle the error.
                        Log.d(TAG, "onConsentInfoUpdateFailure: " + formError.getMessage());
                    }
                });
    }

    public void loadForm_new(Context context, Activity activity) {
        Log.d(TAG, "loadForm: called");
        UserMessagingPlatform.loadConsentForm(
                context,
                new UserMessagingPlatform.OnConsentFormLoadSuccessListener() {
                    @Override
                    public void onConsentFormLoadSuccess(com.google.android.ump.ConsentForm consentForm) {
                        AdmobConsentManager.this.consentForm = consentForm;
                        if(consentInformation.getConsentStatus() == com.google.android.ump.ConsentInformation.ConsentStatus.REQUIRED) {
                            Log.d(TAG, "onConsentFormLoadSuccess: REQUIRED");
                            consentForm.show(
                                    activity,
                                    new com.google.android.ump.ConsentForm.OnConsentFormDismissedListener() {
                                        @Override
                                        public void onConsentFormDismissed(@Nullable FormError formError) {
                                            // Handle dismissal by reloading form.
                                            Log.d(TAG, "onConsentFormDismissed: called");
                                            loadForm_new(context, activity);
                                        }
                                    });
                        }

                        else if(consentInformation.getConsentStatus() == com.google.android.ump.ConsentInformation.ConsentStatus.NOT_REQUIRED) {
                            Log.d(TAG, "onConsentFormLoadSuccess: NOT_REQUIRED");
                        }

                        else if(consentInformation.getConsentStatus() == com.google.android.ump.ConsentInformation.ConsentStatus.UNKNOWN) {
                            Log.d(TAG, "onConsentFormLoadSuccess: UNKNOWN");
                        }
                        else if(consentInformation.getConsentStatus() == com.google.android.ump.ConsentInformation.ConsentStatus.OBTAINED) {
                            Log.d(TAG, "onConsentFormLoadSuccess: OBTAINED");

                            updateConsentStatus_new(consentInformation.getConsentStatus());

                        }
                    }
                },
                new UserMessagingPlatform.OnConsentFormLoadFailureListener() {
                    @Override
                    public void onConsentFormLoadFailure(FormError formError) {
                        // Handle the error
                        Log.d(TAG, "onConsentFormLoadFailure: " + formError.getMessage());
                    }
                }
        );
    }

    private void updateConsentStatus_new(int consentStatus) {
        //                        ConsentInformation.getInstance(context).setTagForUnderAgeOfConsent(true);

        AdRequest adRequest;

        if(consentStatus == com.google.android.ump.ConsentInformation.ConsentType.PERSONALIZED) {
            Log.d(TAG, "updateConsentStatus: consent status: PERSONALIZED");
            adRequest = new AdRequest.Builder()
                    .build();

        }
        else if(consentStatus == com.google.android.ump.ConsentInformation.ConsentType.NON_PERSONALIZED) {
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

//        initAdProvider(this, adRequest);


    }



}// end AdmobConsentProvider
