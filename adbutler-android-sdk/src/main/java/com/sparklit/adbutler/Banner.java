package com.sparklit.adbutler;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
 * A Banner ad.  Can be an MRAID or standard Image ad.
 */
class Banner implements MRAIDListener, HTTPGetListener {
    private ViewGroup container; // use a single common parent to persist whatever is inside on screen rotations.  eg. close button, secondary webview.
    private WebView webView;
    private WebView webViewExpanded;
    private boolean absolutePositioned = false;
    private boolean windowIsFullscreen = false;
    private MRAIDHandler mraidHandler;
    public boolean isWebViewProvided = false;

    private boolean isMRAID = false;
    private Placement placement;
    private boolean suppressCurrentClick = false;

    private Context context;
    public void setContext(Context context){
        this.context = context;
    }

    private Fragment fragment;

    private String position = Positions.BOTTOM_CENTER;
    private View providedView;
    private Rect defaultRect;
    private Size defaultSize;
    private Size fallbackSize = new Size(320, 50);


    private FrameLayout.LayoutParams currentWebViewLayout;
    private FrameLayout.LayoutParams currentContainerLayout;

    public AdListener listener = null;
    private BannerView bannerView;

    public Banner(BannerView bv){
        bannerView = bv;
    }

//    /**
//     * Initintializes a banner by passing it a pre existing view.  This is a future feature and is currently unused.
//     *
//     * @param request
//     * @param view
//     * @param context
//     * @param listener
//     * @param fragment
//     */
//    public void initialize(AdRequest request, View view, Context context, AdListener listener, Fragment fragment){
//        providedView = view;
//        initCommon(request, context, listener, fragment);
//    }

    /**
     * Initialize the banner.  It will be displayed immediately.
     *
     * @param request AdRequest object containing all of the required mediation data.
     * @param position Position constant.  Determines where the banner will appear.   I.E. "bottom-center"
     * @param context Context from which the ad is being requested.
     * @param listener A delegate containing event functions for the ad to call.
     * @param fragment The fragment in which to place the ad content.  (Required to decouple expanding ads from other content in the parent view)
     */
    public void initialize(AdRequest request, String position, Context context, AdListener listener, Fragment fragment){
        this.position = position;
        absolutePositioned = true;
        initCommon(request, context, listener, fragment);
    }

    /**
     * Initialize the banner.  It will be displayed immediately.
     *
     * @param request AdRequest object containing all of the required mediation data.
     * @param container A containing frame layout to hold the banner (not compatible with MRAID)
     * @param context Context from which the ad is being requested.
     * @param listener A delegate containing event functions for the ad to call.
     */
    public void initialize(AdRequest request, FrameLayout container, Context context, AdListener listener){
        initWithProvidedView(request, context, listener, container);
    }

    /**
     * Destroys the banner view.
     */
    public void destroy(){
        container.removeAllViews();
        webView = null;
        webViewExpanded = null;
        if(!this.isWebViewProvided){
            // if banner is retrieved twice too quickly.. parent can be null
            if(container.getParent() != null) {
                ((ViewGroup) container.getParent()).removeView(container);
            }
        }
        if(mraidHandler != null){
            mraidHandler = null;
        }
        container = null;
    }

    private void initCommon(AdRequest request, Context context, AdListener listener, Fragment fragment){
        this.context = context;
        this.fragment = fragment;
        this.container = new FrameLayout(context);
        this.listener = listener;
        this.isWebViewProvided = false;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(0,0);
        params.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        params.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        this.container.setLayoutParams(params);
        int flags = ((Activity)context).getWindow().getAttributes().flags;
        windowIsFullscreen = (flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN;
        new PlacementRequest(request, context, listener, getResponseListener());
    }

    private void initWithProvidedView(AdRequest request, Context context, AdListener listener, FrameLayout container){
        this.context = context;
        this.container = container;
        this.listener = listener;
        this.isWebViewProvided = true;
        int flags = ((Activity)context).getWindow().getAttributes().flags;
        windowIsFullscreen = (flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN;
        new PlacementRequest(request, context, listener, getResponseListener());
    }

    protected View getWebView(){
        if(absolutePositioned){
            // when a fragment is reloaded, it must return a view. We don't want to return our content here, because then it would go
            // wherever the fragment is located on screen.  Instead we return an empty placeholder, then programmatically add our content to the root (Decor) view.
            View dummyView = new View(context);
            dummyView.setLayoutParams(new FrameLayout.LayoutParams(0,0));
            return dummyView; // return an empty view
        }else{
            return webView;
        }
    }

    protected MRAIDHandler getMRAIDHandler(){
        return mraidHandler;
    }

    private PlacementResponseListener getResponseListener(){
        final Banner banner = this;
        return new PlacementResponseListener() {
            @Override
            public void success(PlacementResponse response) {
                try{
                    placement = response.getPlacements().get(0);
                }catch(IndexOutOfBoundsException ex){
                    banner.listener.onAdFetchFailed(ErrorCode.NO_INVENTORY);
                    return;
                }

                if(placement == null){
                    banner.listener.onAdFetchFailed(ErrorCode.NO_INVENTORY);
                    return;
                }

                String body = placement.getBody();
                if(body == null){
                    banner.listener.onAdFetchFailed(ErrorCode.NO_INVENTORY);
                    return;
                }

                if(body.indexOf("mraid.js") > 0){
                    body = MRAIDUtilities.replaceMRAIDScript(body);
                    isMRAID = true;
                }
                body = MRAIDUtilities.validateHTMLStructure(body);
                listener.onAdFetchSucceeded();
                banner.initWebView(body);
            }

            @Override
            public void error(Throwable throwable) {
                listener.onAdFetchFailed(ErrorCode.NO_INVENTORY);
            }
        };
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(String body){
        webView = new WebView(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setWebContentsDebuggingEnabled(true);
        }
        mraidHandler = new MRAIDHandler(this, context, fragment);
        if(!placement.getImageUrl().equals("")){
            initWebViewImage(webView, placement.getImageUrl());
        }else{
            initWebViewCommon(webView, body);
        }


        // if we know the size, set it
        if(!this.isWebViewProvided){
            if(placement.getWidth() != 0 && placement.getHeight() != 0){
                defaultSize = new Size(placement.getWidth(), placement.getHeight());
                setSize(defaultSize);
            }else{
                currentWebViewLayout = new FrameLayout.LayoutParams(0,0);
                currentContainerLayout = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                reposition();
            }
        }
        container.addView(webView);
        if(absolutePositioned){
            addToRoot();
        }else{
            if(!this.isWebViewProvided){
                ((Activity)context).addContentView(webView, currentWebViewLayout);
            }
        }
    }

    private void initSecondaryWebView(String body){
        webViewExpanded = new WebView(context);
        initWebViewCommon(webViewExpanded, body);
        mraidHandler.activeWebView = webViewExpanded;
        container.addView(webViewExpanded);
        setFullScreen();
        mraidHandler.isExpanded = true;
        mraidHandler.initialize(webViewExpanded);
    }

    private void initWebViewCommon(WebView view, String body){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            view.setId(Utilities.generateViewId());
        } else {
            view.setId(View.generateViewId());
        }
        WebSettings settings = view.getSettings();

        view.setSaveEnabled(true);
        view.setSaveFromParentEnabled(true);
        settings.setJavaScriptEnabled(true);

        // disable scrollbars
        view.setVerticalScrollBarEnabled(false);
        view.setHorizontalScrollBarEnabled(false);
        view.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        // configure webview delegates
        initTouchListener(view);
        initWebClient(view);
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(mraidHandler.activeWebView != null){
                    webViewLayoutChanged(v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom);
                }
            }
        });
        view.loadDataWithBaseURL("http://servedbyadbutler.com", body, "text/html", "UTF-8", "");
    }

    private void initWebViewImage(WebView view, String imgURL){
        StringBuilder builder = new StringBuilder();
        int height = fallbackSize.height;
        int width = fallbackSize.width;

        if(placement.getHeight() >= 0 && placement.getWidth() >= 0){
            width = placement.getWidth();
            height = placement.getHeight();
        } else if(defaultSize != null){
            width = defaultSize.width;
            height = defaultSize.height;
        }

        builder.append("<html><head></head><body style=\"margin:0px\">");
        builder.append("<a href=\"" + placement.getRedirectUrl() + "\" target=\"_blank\">");
        builder.append("<img style=\"width:" + width + "; height:" + height + "\" src=\"" + imgURL + "\"/>");
        builder.append("</a></body></html>");
        view.loadData(builder.toString(), "text/html", "UTF-8");
        initTouchListener(view);
        bannerView.initializing = false;
    }

    private void webViewLayoutChanged(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom){
        Log.i("AdButler" , String.format("onLayoutChanged left=%d right=%d top=%d bottom=%d", left, right, top, bottom));
        int x = MRAIDUtilities.convertPixelsToDp(left, context);
        int y = MRAIDUtilities.convertPixelsToDp(top, context);
        int width = MRAIDUtilities.convertPixelsToDp(right, context) - x;
        int height = MRAIDUtilities.convertPixelsToDp(bottom, context) - y;


        int[] xy = new int[2];
        webView.getLocationOnScreen(xy);
        Rect screenRect = new Rect(MRAIDUtilities.convertPixelsToDp(xy[0], context), MRAIDUtilities.convertPixelsToDp(xy[1], context), width, height);
        if(defaultRect == null){
            defaultRect = screenRect;
            mraidHandler.setMRAIDDefaultPosition(defaultRect);
        }

        mraidHandler.setMRAIDCurrentPosition(screenRect);
        mraidHandler.setMRAIDSizeChanged();
    }

    protected void removeFromParent(){
        // if resized and removed from parent, set normal dimensions (rotated screen)
        if(mraidHandler != null && mraidHandler.state == States.RESIZED){
            setSize(new Size(defaultRect.width, defaultRect.height));
            mraidHandler.setMRAIDState(States.DEFAULT);
        }
        if(container.getParent() != null){
            ((ViewGroup)container.getParent()).removeView(container);
        }
    }

    protected void reposition(){
        if(mraidHandler.state.equals(States.EXPANDED)){
            setFullScreen();
        }else{
            webView.setLayoutParams(currentWebViewLayout);
            container.setLayoutParams(currentContainerLayout);
        }
    }

    protected void addToRoot(){
        container.setFitsSystemWindows(true);
        ViewGroup root = ((Activity)context).findViewById(android.R.id.content);
        removeFromParent();
        root.addView(container);
        reposition();
        container.bringToFront();
    }


    protected void setSize(Size rect){
        // set layout parameters
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, rect.width, context.getResources().getDisplayMetrics());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, rect.height, context.getResources().getDisplayMetrics());

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width,height);
        webView.setLayoutParams(params);

        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);

        // some weird bug happens on rotations when margin top is 0
        int marginLeft = 0, marginTop = 1, marginBottom = 0, marginRight = 0;

        int grav = Gravity.NO_GRAVITY;
        if(position.contains("top")){
            grav = grav | Gravity.TOP;
        }
        if(position.contains("bottom")){
            grav = grav | Gravity.BOTTOM;
        }
        if(position.contains("left")){
            grav = grav | Gravity.LEFT;
        }
        if(position.contains("right")){
            grav = grav | Gravity.RIGHT;
        }
        if(position == Positions.CENTER){
            grav = grav | Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
        }
        else if(position.contains("center")){
            if(!position.contains("top") && !position.contains("bottom")){
                grav = grav | Gravity.CENTER_VERTICAL;
            }
            if(!position.contains("left") && !position.contains("right")){
                grav = grav | Gravity.CENTER_HORIZONTAL;
            }
        }
        if(position.contains("status-bar")){
            marginTop += getStatusBarHeight();
        }
        containerParams.setMargins(marginLeft, marginTop, marginRight, marginBottom);
        containerParams.gravity = grav;
        currentWebViewLayout = params;
        currentContainerLayout = containerParams;
        container.setLayoutParams(containerParams);
    }



    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void setFullScreen(){
        if(!windowIsFullscreen){
            ((Activity)context).getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,FrameLayout.LayoutParams.FILL_PARENT);
        mraidHandler.activeWebView.setLayoutParams(params);

        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);
        containerParams.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
        container.setLayoutParams(containerParams);
    }



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

    private void initWebClient(WebView view){
        // Register onPageFinished event in the WebView to record an impression.
        view.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(isMRAID){
                    mraidHandler.initialize(view);
                }
                if(view == webView) {
                    if (defaultSize == null) {
                        setSize(fallbackSize);
                    } else {
                        setSize(defaultSize);
                    }
                    if(isMRAID){
                        mraidHandler.setMRAIDIsVisible(true);
                        mraidHandler.fireMRAIDEvent(Events.VIEWABLE_CHANGE, "true");
                    }
                    Log.d("Ads/AdButler", "WebView load complete, recording impression event.");
                    placement.requestImpressionBeacons();
                    bannerView.initializing = false;
                    listener.onAdFetchSucceeded();
                }
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
                if(url.contains("mraid.js")){
                    isMRAID = true;
                }
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

    public void close() {
        if(mraidHandler.state == States.EXPANDED || mraidHandler.state == States.RESIZED){
            if(webViewExpanded == null){
                setSize(new Size(defaultRect.width, defaultRect.height));
            }else{
                mraidHandler.activeWebView = webView;
                ((ViewGroup)webViewExpanded.getParent()).removeView(webViewExpanded);
                webViewExpanded = null;
                container.setLayoutParams(currentContainerLayout);
            }
            if(!windowIsFullscreen){
                ((Activity)context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            listener.onAdClosed();
        }
    }

    public void HTTPGetCallback(String str){
        final String body = str;
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String updatedBody = body;
                if(updatedBody.contains("mraid.js")) {
                    updatedBody = MRAIDUtilities.replaceMRAIDScript(updatedBody);
                }
                updatedBody = MRAIDUtilities.validateHTMLStructure(updatedBody);
                initSecondaryWebView(updatedBody);
                mraidHandler.addCloseButton(webViewExpanded, true, Positions.TOP_RIGHT);
            }
        });

    }

    public void expand(String url) {
        if(url != null){
            new HTTPGet(this).execute(url);
        }else{
            setFullScreen();
        }
        listener.onAdExpanded();
    }

    public void resize(ResizeProperties properties) {
        // we can always use margins for resizing.  Gravity isn't necessary because when rotating the screen
        // the resized view has to be closed, so the original gravity will be reapplied.
        int[] xypos = new int[2];
        webView.getLocationOnScreen(xypos);

        int offsetX = MRAIDUtilities.convertDpToPixel(properties.offsetX, context);
        int offsetY = MRAIDUtilities.convertDpToPixel(properties.offsetY, context);
        int width = MRAIDUtilities.convertDpToPixel(properties.width, context);
        int height = MRAIDUtilities.convertDpToPixel(properties.height, context);

        android.graphics.Rect rect = new android.graphics.Rect();

        ViewGroup root = ((Activity)context).findViewById(android.R.id.content);
        root.getDrawingRect(rect);

        Rect destinationRect = new Rect(xypos[0] + offsetX, xypos[1] + offsetY, width, height);
        if(!properties.allowOffscreen){
            if(destinationRect.x < 0){
                destinationRect.x = 0;
            }
            if(destinationRect.x + destinationRect.width > rect.width()){
                destinationRect.x = rect.width() - destinationRect.width;
            }
            if(destinationRect.y < 0){
                destinationRect.y = 0;
            }
            if(destinationRect.y + destinationRect.height > rect.height()){
                destinationRect.y = rect.height() - destinationRect.height;
            }
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        containerParams.setMargins(destinationRect.x, destinationRect.y, 0, 0);

        webView.setLayoutParams(params);
        container.setLayoutParams(containerParams);
        mraidHandler.setMRAIDCurrentPosition(destinationRect);
        listener.onAdResized();
    }

    public void onLeavingApplication(){
        listener.onAdLeavingApplication();
    }

    public void reportDOMSize(Size size) {
        if(defaultSize == null && placement.getWidth() <= 0 && placement.getHeight() <= 0){
            defaultSize = size;
        }
    }

    public void setOrientationProperties(OrientationProperties properties){
        if(mraidHandler.orientationProperties.forceOrientation != null) {
            if (mraidHandler.orientationProperties.forceOrientation.equals(Orientations.LANDSCAPE)) {
                ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            if (mraidHandler.orientationProperties.forceOrientation.equals(Orientations.PORTRAIT)) {
                ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            if (mraidHandler.orientationProperties.forceOrientation.equals(Orientations.NONE)) {
                ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        }
    }
}
