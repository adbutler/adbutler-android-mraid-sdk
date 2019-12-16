package com.sparklit.adbutler;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import java.util.Calendar;

/**
 * An Interstitial object, which can be displayed once.  Subsequent ads need to have new requests made.
 */
public class InterstitialView implements MRAIDListener {
    public boolean isReady = false;
    protected String htmlBody = "";
    protected boolean shown = false;
    private boolean isMRAID = false;

    protected AdListener listener = null;
    protected Placement placement;
    protected boolean isImpressionRecorded = false;


    private Context context;
    MRAIDHandler mraidHandler;

    protected MRAIDHandler getMRAIDHandler(){
        return mraidHandler;
    }
    WebView webView;

    protected WebView getWebView(){
        return webView;
    }
    private boolean suppressCurrentClick = false;
    private static InterstitialView instance;
    protected static InterstitialView getInstance(){
        return instance;
    }

    public InterstitialView(){
        instance = this;
    }


    /**
     * Retrieves the interstitial placement.
     * @param request AdRequest object containing all required mediation data.
     * @param context The context from which the request is being made.
     * @param listener A delegate containing event functions for the ad to call.
     */
    public void initialize(AdRequest request, Context context, AdListener listener){
        if(!shown){
            this.context = context;
            this.listener = listener;
            new PlacementRequest(request, context, listener, getResponseListener());
        }
    }

    private PlacementResponseListener getResponseListener(){
        final InterstitialView interstitial = this;
        return new PlacementResponseListener() {
            @Override
            public void success(PlacementResponse response) {
                try {
                    interstitial.placement = response.getPlacements().get(0);
                }catch(IndexOutOfBoundsException ex){
                    interstitial.listener.onAdFetchFailed(ErrorCode.NO_INVENTORY);
                    return;
                }
                if(placement == null){
                    interstitial.listener.onAdFetchFailed(ErrorCode.NO_INVENTORY);
                    return;
                }

                String body = placement.getBody();
                if(body == null){
                    interstitial.listener.onAdFetchFailed(ErrorCode.NO_INVENTORY);
                    return;
                }

                if(body.equals("") && !placement.getImageUrl().equals("")){
                    listener.onAdFetchSucceeded();
                    initImageWebView(interstitial.placement);
                }
                else if(body.indexOf("mraid.js") > 0){
                    body = MRAIDUtilities.replaceMRAIDScript(body);
                    isMRAID = true;
                    interstitial.htmlBody = MRAIDUtilities.validateHTMLStructure(body);
                    listener.onAdFetchSucceeded();
                    initWebView(htmlBody);
                }

            }

            @Override
            public void error(Throwable throwable) {
                listener.onAdFetchFailed(ErrorCode.NO_INVENTORY);
            }
        };
    }

    @SuppressLint("")
    private void initImageWebView(Placement placement){

        webView = new WebView(context);
        webView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.setId(Utilities.generateViewId());
        } else {
            webView.setId(View.generateViewId());
        }
        WebSettings settings = webView.getSettings();

        webView.setSaveEnabled(true);
        webView.setSaveFromParentEnabled(true);
        settings.setJavaScriptEnabled(false);

        // disable scrollbars
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);


        initTouchListener(webView);

        initWebClient(webView);

        webView.loadDataWithBaseURL("http://servedbyadbutler.com", getImageMarkup(placement), "text/html", "UTF-8", "");
    }

    private String getImageMarkup(Placement placement){
        StringBuilder str = new StringBuilder();
        str.append("<!DOCTYPE html>");
        str.append("<html>");
        str.append("<head>");
        str.append("</head>");
        str.append("<body style=\"padding:0; margin:0;\">");
        str.append(String.format("<a href=\"%s\">", placement.getRedirectUrl()));
        str.append(String.format("<img src=\"%s\" style=\"width:100%%; height:100%%;\" />", placement.getImageUrl()));
        str.append("</a>");
        str.append("</body>");
        str.append("</html>");
        return str.toString();
    }

    @SuppressLint("")
    private void initWebView(String body){

        webView = new WebView(context);
        webView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        mraidHandler = new MRAIDHandler(this, context, null);
        mraidHandler.isInterstitial = true;
        mraidHandler.activeWebView = webView;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.setId(Utilities.generateViewId());
        } else {
            webView.setId(View.generateViewId());
        }
        WebSettings settings = webView.getSettings();

        webView.setSaveEnabled(true);
        webView.setSaveFromParentEnabled(true);
        settings.setJavaScriptEnabled(true);

        // disable scrollbars
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        // configure webview delegates
        if(!isMRAID && placement.getBody().length() <= 0) {
            initTouchListener(webView);
        }
        initWebClient(webView);
        webView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(mraidHandler.activeWebView != null){
                    //webViewLayoutChanged(v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom);
                    mraidHandler.setMRAIDCurrentPosition(new Rect(0,0,MRAIDUtilities.convertPixelsToDp(right, context), MRAIDUtilities.convertPixelsToDp(bottom, context)));
                }
            }
        });
        webView.loadDataWithBaseURL("http://servedbyadbutler.com", body, "text/html", "UTF-8", "");
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initTouchListener(WebView view){
        view.setOnTouchListener(new View.OnTouchListener() {
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
                        android.graphics.Rect rect = new android.graphics.Rect();
                        view.getHitRect(rect);
                        if (rect.contains((int) motionEvent.getX(), (int) motionEvent.getY())) {
                            long clickDuration = Calendar.getInstance().getTimeInMillis() - clickStartTime;
                            suppressCurrentClick = clickDuration < MAX_CLICK_DURATION;
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
    }

    private void setReady(){
        isReady = true;
        listener.onInterstitialReady();
    }

    private void initWebClient(WebView view){
        // Register onPageFinished event in the WebView to record an impression.
        view.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Rect fsRect = MRAIDUtilities.getFullScreenRectDP((Activity)context);
                if(mraidHandler != null){
                    mraidHandler.initialize(view);
                    mraidHandler.setMRAIDCurrentPosition(new Rect(0,0,fsRect.width, fsRect.height));
                }

                // allow for some javascript that may run in the ad content
                // e.g. setting orientation properties, and then checking those same properties
                // if the interstitial is told it's viewable too quickly, there won't be time
                // to have set the proper currentPosition
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setReady();
                    }
                }, 150);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.contains("servedbyadbutler.com")){
                    return false;
                }
                else if(url.contains("mraid://")){
                    if(mraidHandler != null){ // just in case the mraidHandler is destroyed somehow
                        mraidHandler.handleEndpoint(url);
                    }
                    return true;
                } else {
                    if (!isMRAID && !suppressCurrentClick && (url.startsWith("http://") || url.startsWith("https://"))) {
                        Log.d("Ads/AdButler", "Received click interaction, loading intent in default browser. (" + url + ")");
                        view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

                        // Trigger click beacon requests.
                        placement.requestClickBeacons();
                        if(!placement.getClickRecorded()){
                            // Register successful ad click.
                            try {
                                listener.onAdClicked();
                            } catch (Exception e) {
                                // do nothing
                                Log.d("Ads/AdButler", "Listener destroyed, ignoring click for this ad.");
                            }
                        }
                        return true;
                    } else {
                        Log.d("Ads/AdButler", "Received click interaction, suppressed due to likely false tap event. (" + url + ")");
                    }
                }
                return false;
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
    }

    /*
       ======== MRAID Listener =========
     */

    /**
     * Called by MRAID ads.
     * @param url
     */
    public void open(String url){
        placement.requestClickBeacons();
        if(!placement.getClickRecorded()){
            // Register successful ad click.
            try {
                listener.onAdClicked();
            } catch (Exception e) {
                // do nothing
                Log.d("Ads/AdButler", "Listener destroyed, ignoring click for this ad.");
            }
        }
    }

    /**
     * Called by MRAID ads.
     */
    public void close() {
        InterstitialActivity.getInstance().finish();
        listener.onAdClosed();
    }

    /**
     * Called by MRAID ads.
     */
    public void expand(String url) {
        // TODO can't be expanded
    }

    /**
     * Called by MRAID ads.
     */
    public void resize(ResizeProperties properties) {
        //TODO can't be resized
    }

    /**
     * Called by MRAID ads.
     */
    public void onLeavingApplication(){
        listener.onAdLeavingApplication();
    }

    /**
     * Called by MRAID ads.
     */
    public void reportDOMSize(Size size) {
        // do nothing
    }

    /**
     * Called by MRAID ads.
     */
    public void setOrientationProperties(OrientationProperties properties) {
        if(shown){
            if(mraidHandler.orientationProperties.forceOrientation != null){
                if(mraidHandler.orientationProperties.forceOrientation.equals(Orientations.LANDSCAPE)){
                    InterstitialActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                if(mraidHandler.orientationProperties.forceOrientation.equals(Orientations.PORTRAIT)){
                    InterstitialActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                if(mraidHandler.orientationProperties.forceOrientation.equals(Orientations.NONE)){
                    InterstitialActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
            }
        }else{
            // if not shown but orientation is set, we need to force the "current position"
            // to think that it's already rotated.  otherwise when the activity starts
            // the rotation will begin, and not be finished before the ad can check it's orientation
            // and do whatever margin calculation it may do
            // bit of a hack..
            if(mraidHandler.orientationProperties.forceOrientation != null){
                Rect fsRect = MRAIDUtilities.getFullScreenRectDP((Activity)context);
                if(mraidHandler.orientationProperties.forceOrientation.equals(Orientations.LANDSCAPE)){
                    mraidHandler.setMRAIDCurrentPosition(new Rect(0, 0, Math.max(fsRect.width, fsRect.height), Math.min(fsRect.width, fsRect.height)));
                }
                if(mraidHandler.orientationProperties.forceOrientation.equals(Orientations.PORTRAIT)){
                    mraidHandler.setMRAIDCurrentPosition(new Rect(0, 0, Math.min(fsRect.width, fsRect.height), Math.max(fsRect.width, fsRect.height)));
                }
                if(mraidHandler.orientationProperties.forceOrientation.equals(Orientations.NONE)){
                    mraidHandler.setMRAIDCurrentPosition(fsRect);
                }
            }
        }
    }

    /**
     * Call this to display the interstitial.
     */
    @SuppressWarnings("unused")
    public void show(){
        if(isReady){
            Intent intent = new Intent(context, InterstitialActivity.class);
            context.startActivity(intent);
            listener.onInterstitialDisplayed();
        }
    }
}
