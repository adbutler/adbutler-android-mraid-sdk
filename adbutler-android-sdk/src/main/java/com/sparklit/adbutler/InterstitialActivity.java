package com.sparklit.adbutler;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * When shown, an Interstitial will create it's own activity.
 */
public class InterstitialActivity extends AppCompatActivity {
    WebView webView;

    LinearLayout root;
    private int closeTimer = 0;
    private TextView closeButton;
    private int fontSize = 20;

    int closeFadeTime = 155; // milliseconds
    int closeFadeRate = 25; // milliseconds
    Timer countdownTimer;
    Timer fadeTimer;
    float fadeDirection = -1.0f;
    float fadeAmountPerTick;
    boolean closeClickable = false;
    MRAIDHandler mraidHandler;

    static AppCompatActivity instance;
    protected static AppCompatActivity getInstance(){
        return instance;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.activity_interstitial);

        instance = this;
        init();
    }

    public void init(){
        mraidHandler = InterstitialView.getInstance().getMRAIDHandler();
        if(mraidHandler != null && mraidHandler.orientationProperties.forceOrientation != null){
            if(mraidHandler.orientationProperties.forceOrientation.equals(Orientations.LANDSCAPE)){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            if(mraidHandler.orientationProperties.forceOrientation.equals(Orientations.PORTRAIT)){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            if(mraidHandler.orientationProperties.forceOrientation.equals(Orientations.NONE)){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        }

        root = findViewById(R.id.webViewContainer);
        root.removeAllViews();

        //TODO
        // we made the activity instance static so we can access its functions from the Interstitial class.
        // change this in the future so that the interstitial activity has all of the mraid handling inside it, rather than the interstitial class.
        // for now, remove the old instance of the interstitial activity, in case the app loses focus and needs to recreate this activity.
        webView = InterstitialView.getInstance().getWebView();
        if(webView.getParent() != null){
            ViewGroup parent = (ViewGroup)webView.getParent();
            parent.removeView(webView);
            ((Activity)parent.getContext()).finish();
        }

        root.addView(webView);
        InterstitialView.getInstance().shown = true;
        initializeCloseButton();
        recordImpression();
        if(mraidHandler != null){
            mraidHandler.setMRAIDIsVisible(true);
            mraidHandler.fireMRAIDEvent(Events.VIEWABLE_CHANGE, "true");
        }
    }


    void initializeCloseButton(){
        FrameLayout item = findViewById(R.id.btnBackground);
        item.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(closeClickable){
                    finish();
                }
            }
        });
        closeButton = new TextView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        closeButton.setLayoutParams(params);
        Typeface font = Typeface.create("Droid Sans Mono", Typeface.NORMAL);
        closeButton.setTextColor(Color.WHITE);
        closeButton.setTypeface(font);
        closeButton.setGravity(Gravity.TOP | Gravity.RIGHT);


        if(closeTimer > 0){
            setCloseButtonText(Integer.toString(closeTimer), 20f);
            countdownTimer = new Timer();
            TimerTask countdown = new TimerTask() {
                @Override
                public void run(){
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            updateTimer();
                        }
                    });
                }
            };
            countdownTimer.scheduleAtFixedRate(countdown, 0, 1000);
        }else{
            setCloseButtonText("X", 24f);
            closeClickable = true;
        }
        root.bringChildToFront(item);
        item.addView(closeButton);
    }

    void recordImpression(){
        InterstitialView interstitial = InterstitialView.getInstance();
        if (!interstitial.isImpressionRecorded) {
            interstitial.isImpressionRecorded = true;
            // Fetch successful, record an impression.
            Log.d("Ads/AdButler", "WebView load complete, recording impression event.");
            interstitial.placement.requestImpressionBeacons();
        }
    }

    void updateTimer(){
        closeTimer -= 1;
        if(closeTimer > 0){
            setCloseButtonText(Integer.toString(closeTimer), fontSize);
        }
        else{
            setCloseButtonActive();
        }
    }

    void setCloseButtonText(String text, float size){
        closeButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
        closeButton.setText(text);
    }

    void setCloseButtonActive(){
        fadeAmountPerTick = ((float)closeFadeRate / (float)closeFadeTime);
        countdownTimer.cancel();
        countdownTimer = null;
        closeClickable = true;
        fadeTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run(){
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        updateTimerFade();
                    }
                });
            }
        };
        fadeTimer.scheduleAtFixedRate(task, 0, (long)closeFadeRate);
    }

    void updateTimerFade(){

        if(fadeTimer != null) {
            float a = closeButton.getAlpha();
            a += fadeAmountPerTick * fadeDirection;
            closeButton.setAlpha(a);
            if (closeButton.getAlpha() < 0) {
                fadeDirection = 1.0f;
                setCloseButtonText("X", 24.0f);
            }
            if (closeButton.getAlpha() >= 1) {
                fadeTimer.cancel();
                fadeTimer = null;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!closeClickable) {
            return;
        } else {
            super.onBackPressed();
        }
    }
}
