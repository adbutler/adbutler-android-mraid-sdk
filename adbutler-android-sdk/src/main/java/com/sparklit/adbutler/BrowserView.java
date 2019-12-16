package com.sparklit.adbutler;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

class BrowserView extends AppCompatActivity {
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser_view);
        String url = getIntent().getStringExtra("URL");
        load(url);
    }

    protected void load(String url){
        FrameLayout container = findViewById(R.id.web_view);
        webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d("Ads/AdButler", "onPageStarted: " + url);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            //
            // DEBUGGING
            //


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
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        webView.setLayoutParams(params);
        container.addView(webView);
        webView.loadUrl(url);
    }

    private void animateButton(View view){
        Animation anim = new AlphaAnimation(0.2f, 1.0f);
        anim.setDuration(330);
        view.startAnimation(anim);
    }

    public void back(View view){
        animateButton(view);
        if(webView.canGoBack()){
            webView.goBack();
        }
    }

    public void forward(View view){
        animateButton(view);

        if(webView.canGoForward()){
            webView.goForward();
        }
    }

    public void reload(View view){
        animateButton(view);
        webView.reload();
    }

    public void close(View view){
        animateButton(view);
        finish();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()){
                webView.goBack();
                return true;
            }else{
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
