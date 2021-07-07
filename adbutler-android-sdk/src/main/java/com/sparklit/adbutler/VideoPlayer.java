package com.sparklit.adbutler;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Rect;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public class VideoPlayer extends AppCompatActivity implements HTTPGetListener {
    VideoEnabledWebView webView;
    VideoEnabledWebChromeClient webChromeClient;
    VASTListener listener;
    VASTCompanion endCard;
    AppCompatActivity me = this;


    private TextView closeButton;
    private boolean closeButtonVisible = false;

    long tapTime;
    long releaseTime;
    long tapDelay = 200; // 200 milliseconds or less counts as a click

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player);
        String url = getIntent().getStringExtra("URL");
        String body = getIntent().getStringExtra("BODY");
        boolean preloaded = getIntent().getBooleanExtra("PRELOADED", false);
        boolean closeButtonRequired = getIntent().getBooleanExtra("CLOSEBUTTONREQUIRED", true);
        listener = VASTVideo.getListenerInstance();

        if(preloaded){
            webView = findViewById(R.id.webView);
            ViewParent parent = webView.getParent();
            ((ViewGroup)parent).removeView(webView);
            webView = VASTVideo.getWebView();
            webView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            ((ViewGroup)parent).addView(webView);
            //((ViewGroup) this.findViewById(android.R.id.content)).addView(webView);
            //addContentView(webView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        }else {
            // Save the web view
            webView = findViewById(R.id.webView);
        }
        // Initialize the VideoEnabledWebChromeClient and set event handlers
        View nonVideoLayout = findViewById(R.id.nonVideoLayout); // Your own view, read class comments
        ViewGroup videoLayout = findViewById(R.id.videoLayout); // Your own view, read class comments
        //noinspection all
        View loadingView = getLayoutInflater().inflate(R.layout.view_loading_video, null); // Your own view, read class comments
        webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, loadingView, webView) // See all available constructors...
        {
            // Subscribe to standard events, such as onProgressChanged()...
            @Override
            public void onProgressChanged(WebView view, int progress)
            {

            }
        };
        webChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback()
        {
            @Override
            public void toggledFullscreen(boolean fullscreen)
            {
                // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                if (fullscreen)
                {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14)
                    {
                        //noinspection all
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                    }
                }
                else
                {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14)
                    {
                        //noinspection all
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }

            }
        });
        webView.setWebChromeClient(webChromeClient);
        // Call private class InsideWebViewClient
        webView.setWebViewClient(new InsideWebViewClient());

        if(!preloaded){
            if(url != null){
                webView.loadUrl(url);
            }
            else if(body != null){
                webView.loadDataWithBaseURL("https://" + AdButler.getInstance().getApiHostname(), body, "text/html; charset=utf-8", "UTF-8", "");
            }
        }else{
            if(closeButtonRequired){
                initializeCloseButton();
            }
            webView.loadUrl("javascript:document.getElementById('av_video').player.play();");
        }
    }

    private class InsideWebViewClient extends WebViewClient {
        @Override
        // Force links to be opened inside WebView and not in Default Browser
        // Thanks http://stackoverflow.com/a/33681975/1815624
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(url.contains("vast://")){
                handleEvent(url);
                return false;
            }
            else if(!url.contains("callback-p.spark")){
                Intent intent = new Intent(me, BrowserView.class);
                intent.putExtra("URL", url);
                me.startActivity(intent);
            }else{
                System.out.println("URL REACHED =====  " + url);
                view.loadUrl(url);
            }
            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if(BuildConfig.DEBUG) {
                handler.proceed();
                return;
            }
            super.onReceivedSslError(view, handler, error);
        }
    }

    private void handleEvent(String url){
        String event = url.replaceFirst("vast://", "");
        switch(event){
            case "mute":
                listener.onMute();
                break;
            case "unmute":
                listener.onUnmute();
                break;
            case "pause":
                listener.onPause();
                break;
            case "resume":
                listener.onResume();
                break;
            case "rewind":
                listener.onRewind();
                break;
            case "skip":
                listener.onSkip();
                endCard = VASTVideo.getEndCard();
                if(endCard != null && (endCard.staticResource != null || endCard.htmlResource != null)){
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            displayEndCard();
                        }
                    });
                } else {
                    listener.onClose();
                    finish();
                    overridePendingTransition(0,0);
                }
                break;
            case "playerExpand":
                listener.onPlayerExpand();
                break;
            case "playerCollapse":
                listener.onPlayerCollapse();
                break;
            case "notUsed":
                listener.onNotUsed();
                break;
            case "loaded":
                listener.onLoaded();
                break;
            case "start":
                listener.onStart();
                break;
            case "firstQuartile":
                listener.onFirstQuartile();
                break;
            case "midpoint":
                listener.onMidpoint();
                break;
            case "thirdQuartile":
                listener.onThirdQuartile();
                break;
//            case "otherAdInteraction":
//                listener.onOtherAdInteraction();
//                break;
            case "complete":
                listener.onComplete();
                endCard = VASTVideo.getEndCard();
                if(endCard != null && (endCard.staticResource != null || endCard.htmlResource != null)){
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            displayEndCard();
                        }
                    });
                } else{
                    listener.onClose();
                    finish();
                    overridePendingTransition(0,0);
                }

                break;
            case "closeLinear":
                listener.onCloseLinear();
                break;
            case "close":
                listener.onClose();
                finish();
                break;
            case "ready":
                listener.onReady();
        }
    }

    @Override
    public void onBackPressed()
    {
        if(endCard == null){
            // Notify the VideoEnabledWebChromeClient, and handle it ourselves if it doesn't handle it
            if (!webChromeClient.onBackPressed())
            {
                if(closeButtonVisible){

                }
                else if (webView.canGoBack())
                {
                    webView.goBack();
                }
                else
                {
                    // Standard back button implementation (for example this could close the app)
                    super.onBackPressed();
                }
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private void displayEndCard(){
        String markup = getEndCardMarkup();
        webView.loadDataWithBaseURL("https://" + AdButler.getInstance().getApiHostname(), markup,"text/html; charset=utf-8", "UTF-8", "");
        Iterator i = endCard.trackingEvents.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry keyVal = (Map.Entry)i.next();
            if(keyVal.getKey().equals("creativeView")){
                try{
                    new HTTPGet(this).execute((String)keyVal.getValue());
                } catch(Exception ex){
                    Log.e("Ads:Phuwnare", "Error reporting companion view event.");
                }
            }
            i.remove();
        }
        if(endCard.staticResource != null){
            webView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            tapTime = new Date().getTime();
                            break;
                        case MotionEvent.ACTION_UP:
                            releaseTime = new Date().getTime();
                            if(releaseTime - tapTime < tapDelay){
                                Intent intent = new Intent(me, BrowserView.class);
                                intent.putExtra("URL", endCard.clickThrough);
                                me.startActivity(intent);
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            break;
                        default:
                            break;

                    }
                    return false;
                }
            });
        }
        initializeCloseButton();
    }

    public void HTTPGetCallback(String str){
        // no need to do anything
    }

    //TODO Refactor close buttons to a common place
    void initializeCloseButton(){
        FrameLayout item = findViewById(R.id.btnBackground);
        item.setVisibility(View.VISIBLE);
        item.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //if(closeClickable){
                if(listener != null){
                    listener.onClose();
                }
                finish();
                overridePendingTransition(0,0);
                //}
            }
        });
        closeButton = new TextView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        closeButton.setLayoutParams(params);
        Typeface font = Typeface.create("Droid Sans Mono", Typeface.NORMAL);
        closeButton.setTextColor(Color.WHITE);
        closeButton.setTypeface(font);
        closeButton.setGravity(Gravity.TOP | Gravity.RIGHT);


        setCloseButtonText("X", 24.0f);
        final ViewGroup root = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
        root.bringChildToFront(item);
        item.addView(closeButton);
        closeButtonVisible = true;
    }

    void setCloseButtonText(String text, float size){
        closeButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
        closeButton.setText(text);
    }

    private String getEndCardMarkup(){
        Rect windowRect = new Rect();
        webView.getWindowVisibleDisplayFrame(windowRect);
        if(endCard.staticResource != null){
            int h = MRAIDUtilities.convertPixelsToDp(windowRect.height(), this);
            int w = MRAIDUtilities.convertPixelsToDp(windowRect.width(), this);

            StringBuilder str = new StringBuilder();
            str.append("<!DOCTYPE html>");
            str.append("<html>");
            str.append("<head>");
            str.append("<style>");
            str.append("body {");
            str.append("background: url('" + endCard.staticResource + "') no-repeat fixed;");
            str.append("background-size: contain;");
            str.append("background-position: center;");
            str.append("}");
            str.append("</style>");
            str.append("</script>");
            str.append("</head>");
            str.append("<body style=\"background-color:black; margin:0; padding:0; font-size:0px; width:" + w + "px; height:" + h + "px;\">");
            str.append("</div>");
            str.append("<body>");
            str.append("</html>");
            return str.toString();
        }
        if(endCard.htmlResource != null){
            if(endCard.htmlResource.contains("</html>")){
                return endCard.htmlResource;
            }
            StringBuilder str = new StringBuilder();
            str.append("<!DOCTYPE html>");
            str.append("<html>");
            str.append("<head>");
            str.append("</head>");
            str.append("<body>");
            str.append(endCard.htmlResource);
            str.append("</body>");
            str.append("</html>");
            return str.toString();
        }
        return "";
    }

}

