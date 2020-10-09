package com.sparklit.adbutler;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;
import java.util.Calendar;

/**
 * If you want to get the placement directly, and create your own way of displaying them, you can do so using this object.
 */
public class PlacementRequest {

    protected PlacementRequestConfig config;
    /**
     * Creates a PlacementRequest
     * @param request AdRequest object containing all required mediation data.
     * @param context The context from which the ad request will be made.
     * @param listener A delegate containing event functions for the ad to call.
     * @param placementListener A delegate overriding the success or failure methods of the request.
     */
    public PlacementRequest(AdRequest request, Context context, AdListener listener, PlacementResponseListener placementListener){
        Log.d("Ads/AdButler", "AdButler AdMob SDK v" + BuildConfig.VERSION_NAME + " - Beginning Ad Fetch");

        // Permit Chrome Debugging if >KITKAT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        if (listener == null) {
            Log.e("Ads/AdButler", "Exception: Ad Listener has been destroyed before we could get started, do not proceed.");
            return;
        }

        if (request.getAccountID() == 0 || request.getZoneID() == 0) {
            try {
                listener.onAdFetchFailed(ErrorCode.BAD_REQUEST);
            } catch (Exception e) {
                Log.e("Ads/AdButler", "Exception: Ad Listener has been destroyed before we could report a Bad Request, do not proceed.");
            }
            return;
        }

        // Gather required info now that we know we need it.

        AdButler sdk = AdButler.getInstance();

        AppInfo appInfo = new AppInfo();
        appInfo.initialize(context);

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.initialize();

        NetworkInfo networkInfo = new NetworkInfo();
        networkInfo.initialize(context);

        //
        // PASS TO ADBUTLER
        //

        Calendar nowInstance = Calendar.getInstance();
        int currentYear = nowInstance.get(Calendar.YEAR);

        Location loc;
        Calendar birthdayCalendar = null;
        int age = 0;
        int yearOfBirth = 0;

        if (null != request.getBirthday()) {
            birthdayCalendar = Calendar.getInstance();
            birthdayCalendar.setTime(request.getBirthday());
        }

        //
        loc = request.getLocation();

        if(AdButler.isPersonalAdsAllowed()){
            if (request.getAge() > 0) {
                age = request.getAge();
            }
            if (request.getYearOfBirth() > 0) {
                yearOfBirth = request.getYearOfBirth();
            }

            // try to fill age in through secondary methods
            if (age <= 0) {
                if (yearOfBirth > 0) {
                    age = currentYear - yearOfBirth;
                } else if (null != birthdayCalendar) {
                    age = currentYear - birthdayCalendar.get(Calendar.YEAR);
                }
            }
            // try to fill in year of birth through secondary methods
            if (yearOfBirth <= 0) {
                if (age > 0) {
                    yearOfBirth = currentYear - age;
                } else if (null != birthdayCalendar) {
                    yearOfBirth = birthdayCalendar.get(Calendar.YEAR);
                }
            }
        }


        // Begin request build.
        PlacementRequestConfig.Builder requestBuilder;
        if(request.getWidth() > 0 && request.getHeight() > 0){
            requestBuilder = new PlacementRequestConfig.Builder(request.getAccountID(), request.getZoneID(), request.getWidth(), request.getHeight());
        }else{
            requestBuilder = new PlacementRequestConfig.Builder(request.getAccountID(), request.getZoneID());
        }


        // Proper User Agent
        requestBuilder.setUserAgent(new WebView(context).getSettings().getUserAgentString());

        // Advertising ID & DNT
        if (null != AdButler.AdvertisingInfo.advertisingId) {
            if(AdButler.isPersonalAdsAllowed()) {
                requestBuilder
                        .setAdvertisingId(AdButler.AdvertisingInfo.advertisingId)
                        .setDoNotTrack(AdButler.AdvertisingInfo.limitAdTrackingEnabled ? 1 : 0);
            }
        }

        // Location
        if (loc != null) {
            requestBuilder.setLatitude(loc.getLatitude());
            requestBuilder.setLongitude(loc.getLongitude());
        }

        if(AdButler.isPersonalAdsAllowed()) {
            // Age & year of birth
            if (age > 0) {
                requestBuilder.setAge(age);
            }
            switch (request.getGender()) {
                case AdRequest.GENDER_MALE:
                    requestBuilder.setGender("male");
                    break;
                case AdRequest.GENDER_FEMALE:
                    requestBuilder.setGender("female");
                    break;
                default:
                case AdRequest.GENDER_UNKNOWN:
                    requestBuilder.setGender("unknown");
                    break;
            }
            if (yearOfBirth > 0) {
                requestBuilder.setYearOfBirth(yearOfBirth);
            }
        }

        // App
        requestBuilder.setAppName(appInfo.appName);
        requestBuilder.setAppPackageName(appInfo.packageName);
        requestBuilder.setAppVersion(appInfo.appVersion);

        // Device
        requestBuilder.setLanguage(deviceInfo.language);
        requestBuilder.setOsName(deviceInfo.osName);
        requestBuilder.setOsVersion(deviceInfo.osVersion);
        requestBuilder.setDeviceType(deviceInfo.isTablet ? "tablet" : "phone");
        requestBuilder.setDeviceModel(deviceInfo.model);
        requestBuilder.setDeviceManufacturer(deviceInfo.manufacturer);
        requestBuilder.setScreenWidth(deviceInfo.screenWidth);
        requestBuilder.setScreenHeight(deviceInfo.screenHeight);
        requestBuilder.setScreenPixelDensity(deviceInfo.density);
        requestBuilder.setScreenDotsPerInch(deviceInfo.dpi);

        // Network
        requestBuilder.setNetworkClass(networkInfo.networkClass);
        requestBuilder.setCarrierCountryIso(networkInfo.carrierCountryIso);
        requestBuilder.setCarrier(networkInfo.carrierName);
        requestBuilder.setCarrierCode(networkInfo.carrierCode);

        if(request.getKeywords() != null){
            requestBuilder.setKeywords(request.getKeywords());
        }

        // Compliance
        requestBuilder.setCoppa(request.getCoppa());

        // Custom Extras
        if (null != request.getCustomExtras()) {
            requestBuilder.setCustomExtras(request.getCustomExtras());
        }

        // Finalize request config build.
        final PlacementRequestConfig config = requestBuilder.build();

        Log.d("Ads/AdButler", "Requesting ad from AdButler...");
        this.config = config;
        sdk.requestPlacement(config, placementListener);
    }
}
