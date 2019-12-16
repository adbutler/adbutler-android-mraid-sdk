package com.sparklit.adbutler;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import android.graphics.Rect;
import android.location.Location;

import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.util.Log;

import android.view.MotionEvent;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import android.webkit.WebView;
import android.webkit.WebViewClient;


import java.util.Calendar;


/**
 * An interstitial ad view for the AdButler SDK.
 *
 * Standalone version for use with wrapper sdks such as AdMob
 *
 * Does not support MRAID, just simple html interstitials.
 */
@Deprecated
public class AdButlerInterstitialView extends WebView {

    public Placement placement;

    private AdSize mAdSize;
    private AdListener mListener;
    //private WebView mWebView;
    public Boolean isReady = false;

    // AdButler data
    private Integer accountID;
    private Integer zoneID;

    protected boolean isImpressionRecorded = false;
    private boolean isClickRecorded = false;
    private boolean suppressCurrentClick = false;

    private int closeTimer = 5;

    private static AdButlerInterstitialView Instance;

    public static AdButlerInterstitialView getInstance() {
        return Instance;
    }
    public static void setInstance(AdButlerInterstitialView view){
        Instance = view;
    }

    public AdSize getSize(){
        return mAdSize;
    }

    /**
     * Create a new {@link AdButlerInterstitialView}.
     *
     * @param context An Android {@link Context}.
     */
    public AdButlerInterstitialView(Context context) {
        super(context);
    }

    /**
     * @param accountID The account ID.
     */
    public void setAccount(Integer accountID) {
        this.accountID = accountID;
    }

    /**
     * @param zoneID The zone ID to serve.
     */
    public void setZone(Integer zoneID) {
        this.zoneID = zoneID;
    }
    /**
     * @param closeTimer The zone ID to serve.
     */
    public void setCloseTimer(Integer closeTimer) {
        this.closeTimer = closeTimer;
    }

    public int getCloseTimer() {
        return closeTimer;
    }

    /**
     * Sets the size of the banner.
     *
     * @param size The banner size.
     */
    public void setSize(AdSize size) {
        this.mAdSize = size;
    }

    /**
     * Sets a {@link AdListener} to listen for ad events.
     *
     * @param listener The ad listener.
     */
    public void setAdListener(AdListener listener) {
        this.mListener = listener;
    }

    /**
     * Get the actual ad markup.
     *
     * @param body The ad body.
     * @return String
     */
    public String getAdMarkup(String body) {
        return "<!DOCTYPE HTML><html><head><link rel=\"icon\" href=\"data:;base64,iVBORw0KGgo=\"><style>html,body{padding:0;margin:0;background-color:white;}iframe{border:0;overflow:none;}a{outline:0;-webkit-tap-highlight-color:transparent;}body>*{margin:0 auto;width:" + this.placement.getWidth() + "px;display:block;}</style></head><body>"
                + body + "</body></html>";
    }


    public void show(){
        Intent intent = new Intent(getContext(), InterstitialActivity.class);
        getContext().startActivity(intent);

    }

    /**
     * Fetch an ad from AdButler.
     *
     * @param request The ad request with targeting information.
     */
    public void fetchAd(AdRequest request) {


        Log.d("Ads/AdButler", "AdButler AdMob SDK v" + BuildConfig.VERSION_NAME + " - Beginning Ad Fetch");

        // Permit Chrome Debugging if >KITKAT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        if (mListener == null) {
            Log.e("Ads/AdButler", "Exception: Ad Listener has been destroyed before we could get started, do not proceed.");
            return;
        }

        if (this.accountID == 0 || this.zoneID == 0 || mAdSize == null) {
            try {
                mListener.onAdFetchFailed(ErrorCode.BAD_REQUEST);
            } catch (Exception e) {
                Log.e("Ads/AdButler", "Exception: Ad Listener has been destroyed before we could report a Bad Request, do not proceed.");
            }
            return;
        }

        // Gather required info now that we know we need it.
        final AdButlerInterstitialView adView = this;

        AdButler AdButlerSDK = AdButler.getInstance();
//        AdButlerSDK.setApiHostname("adbutler-fermion.com");
//        AdButlerSDK.setApiAppVersion("adserve-p");

        AppInfo appInfo = new AppInfo();
        appInfo.initialize(getContext());

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.initialize();

        NetworkInfo networkInfo = new NetworkInfo();
        networkInfo.initialize(getContext());

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
        PlacementRequestConfig.Builder requestBuilder = new PlacementRequestConfig.Builder(this.accountID, this.zoneID);

        // Proper User Agent
        requestBuilder.setUserAgent(this.getSettings().getUserAgentString());

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

        // Age & year of birth
        if(AdButler.isPersonalAdsAllowed()){
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

        // Compliance
        requestBuilder.setCoppa(request.getCoppa());

        // Custom Extras
        if (null != request.getCustomExtras()) {
            requestBuilder.setCustomExtras(request.getCustomExtras());
        }

        // Finalize request config build.
        final PlacementRequestConfig config = requestBuilder.build();


        Log.d("Ads/AdButler", "Requesting ad from AdButler...");

        AdButlerSDK.requestPlacement(config, getResponseListener());
    }

    private PlacementResponseListener getResponseListener(){

        final AdButlerInterstitialView adView = this;
        return new PlacementResponseListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void success(PlacementResponse response) {
                if(adView.mListener == null){
                    return; // something took too long with configuring adbutler, and admob has destroyed the custom event
                }
                Placement placement = null;
                final String placementRedirectURL;
                String markup = "", body = "", encodedBody = "", wrapper = "";
                boolean containsHtmlStart, containsHtmlEnd;

                if(null == response) {
                    Log.d("Ads/AdButler", "Error trying to deserialize ad response.");
                } else {

                    for (Placement placementEl : response.getPlacements()) {
                        Log.d("Ads/AdButler", "BannerID: " + placementEl.getBannerId());
                    }

                    if (response.getPlacements().size() > 0) {
                        placement = response.getPlacements().get(0);
                    }

                }

                if (null == placement) {
                    Log.d("Ads/AdButler", "No ads to show, deferring to AdMob.");
                    try {
                        adView.mListener.onAdFetchFailed(ErrorCode.NO_INVENTORY);
                    } catch (Exception e) {
                        Log.e("Ads/AdButler", "Exception: adView was destroyed before we could report onAdFetchFailed.");
                    }

                } else {
                    //mWebView = new WebView(getContext());
                    getSettings().setJavaScriptEnabled(true);
                    adView.placement = placement;

                    body = adView.placement.getBody();
                    containsHtmlStart = body.contains("<html") || body.contains("<HTML");
                    containsHtmlEnd = body.contains("</html") || body.contains("</HTML");
                    if (containsHtmlStart && containsHtmlEnd) {
                        // If the response is a complete HTML document, inject into an iframe.
                        try {
                            encodedBody = EscapeJavaScriptString(body);
                            encodedBody = encodedBody.replaceAll("</script>", "</scr\"+\"ipt>");
                            encodedBody = encodedBody.replaceAll("<script>", "<scr\"+\"ipt>");

                            wrapper += "<iframe id=\"adbutler-admob-frame\" frameborder=0 scrolling=no noresize=noresize marginheight=0 marginwidth=0 height=" + adView.placement.getWidth() + " width=" + adView.placement.getHeight() + "></iframe>";
                            wrapper += "<script>document.getElementById('adbutler-admob-frame').srcdoc = \"" + encodedBody + "\";</script>";
                            markup = getAdMarkup(wrapper);

                        } catch (Exception e) {
                            Log.d("Ads/AdButler", "Failed to encode body String.");
                        }

                    } else {
                        // If the response is an partial HTML document, wrap in a plain div (in case of legacy style tags) and inject it directly.
                        markup = getAdMarkup("<div>" + body + "</div>");
                    }


                    adView.loadDataWithBaseURL("", markup, "text/html", "UTF-8", "");
                    setWebViewClient(new WebViewClient(){
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            Log.d("Ads/AdButler", "onPageFinished: " + url);
                            super.onPageFinished(view, url);
                            adView.isReady = true;
                            adView.mListener.onAdFetchSucceeded();
                            setInstance(adView);
                        }


                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            if (url != null && !adView.suppressCurrentClick && (url.startsWith("http://") || url.startsWith("https://"))) {
                                Log.d("Ads/AdButler", "Received click interaction, loading intent in default browser. (" + url + ")");
                                view.getContext().startActivity(
                                        new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

                                // Trigger click beacon requests.
                                if (!adView.isClickRecorded) {
                                    adView.isClickRecorded = true;
                                    adView.placement.requestClickBeacons();

                                    // Register successful ad click.
                                    try {
                                        adView.mListener.onAdClicked();
                                    } catch (Exception e) {
                                        // do nothing
                                        Log.d("Ads/AdButler", "Listener destroyed, ignoring click for this ad.");
                                    }
                                }

                            } else {
                                Log.d("Ads/AdButler", "Received click interaction, suppressed due to likely false tap event. (" + url + ")");
                            }
                            return true;
                        }

                        //
                        // DEBUGGING
                        //

                        @Override
                        public void onPageStarted(WebView view, String url, Bitmap favicon) {
                            Log.d("Ads/AdButler", "onPageStarted: " + url);
                            super.onPageStarted(view, url, favicon);
                        }

                        @Override
                        public void onLoadResource(WebView view, String url) {
                            Log.d("Ads/AdButler", "Loading URL: " + url);
                            super.onLoadResource(view, url);
                        }

                        @Override
                        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                            Log.d("Ads/AdButler", "onReceivedError: " + failingUrl);
                            Log.d("Ads/AdButler", "onReceivedError Error: " + errorCode + ", " + description);
                            super.onReceivedError(view, errorCode, description, failingUrl);
                        }

                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                            Log.d("Ads/AdButler", "onReceivedError: " + request.getUrl());
                            Log.d("Ads/AdButler", "onReceivedError Error: " + error.getErrorCode() + ", " + error.getDescription());
                            super.onReceivedError(view, request, error);
                        }

                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                            Log.d("Ads/AdButler", "onReceivedHttpError: " + request.getUrl());
                            Log.d("Ads/AdButler", "onReceivedHttpError Status: " + errorResponse.getStatusCode());
                            super.onReceivedHttpError(view, request, errorResponse);
                        }

                        @Override
                        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                            Log.d("Ads/AdButler", "onReceivedSslError: " + error.getUrl());
                            Log.d("Ads/AdButler", "onReceivedSslError Status: " + error.getPrimaryError());
                            super.onReceivedSslError(view, handler, error);
                        }
                    });

                    try {
                        // Register the selected placement.
                        adView.placement = placement;

                        // Set up the OnTouchListener
                        adView.setOnTouchListener(new OnTouchListener() {
                            private static final int MAX_CLICK_DURATION = 50;
                            private long clickStartTime;

                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {
                                boolean preventTouch = false;

                                switch (motionEvent.getAction()) {
                                    case MotionEvent.ACTION_DOWN:
                                        clickStartTime = Calendar.getInstance().getTimeInMillis();
                                        break;

                                    case MotionEvent.ACTION_UP:
                                        Rect rect = new Rect();
                                        getHitRect(rect);
                                        if (rect.contains((int) motionEvent.getX(), (int) motionEvent.getY())) {
                                            long clickDuration = Calendar.getInstance().getTimeInMillis() - clickStartTime;
                                            adView.suppressCurrentClick = clickDuration < MAX_CLICK_DURATION;
                                        } else {
                                            Log.d("Ads/AdButler", "Touch release did not occur over the ad view.");
                                        }
                                        break;

                                    case MotionEvent.ACTION_MOVE:
                                        preventTouch = true;
                                        break;
                                }

                                return preventTouch;
                            }
                        });

                    } catch (Exception e) {
                        Log.e("Ads/AdButler", "Exception: adView was destroyed before we were able to complete this response.");
                    }
                }
            }

            @Override
            public void error(Throwable throwable) {
                Log.d("Ads/AdButler", "Zone request error occurred.");
                try {
                    adView.mListener.onAdFetchFailed(ErrorCode.NETWORK_ERROR);
                } catch (Exception e) {
                    Log.e("Ads/AdButler", "Exception: adView was destroyed before we could report onAdFetchFailed.");
                }
            }
        };
    }

    private String EscapeJavaScriptString(String param) {
        char[] chars = param.toCharArray();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(chars[i]);
                    break;
            }
        }

        return sb.toString();
    }

    /**
     * Destroy the banner.
     */
    public void destroy() {
        mListener = null;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        // Before final draw, set the calculated layout to correctly indicate the dimensions of the ad.
//        if (null != this.calculatedLayout) {
//            this.setLayoutParams(this.calculatedLayout);
//        }
    }
}
