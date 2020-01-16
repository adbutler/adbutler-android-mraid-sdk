package com.example.sdktester;

import android.app.Activity;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.sparklit.adbutler.*;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Button btnGetBanner;
    private Button btnGetInterstitial;
    private Button btnGetVASTVideo;
    private Button btnDisplayInterstitial;
    private Button btnDestroy;

    private Button btnTopLeft;
    private Button btnTopCenter;
    private Button btnTopRight;
    private Button btnCenterLeft;
    private Button btnCenter;
    private Button btnCenterRight;
    private Button btnBottomLeft;
    private Button btnBottomCenter;
    private Button btnBottomRight;
    HashMap<Button, String> positions;

    private Button selectedPosition;

    private StringBuilder logBuilder = new StringBuilder();
    private EditText txtAccountID;
    private EditText txtZoneID;
    private EditText txtPublisherID;
    private TextView txtLog;

    private int accountID;
    private int zoneID;
    private int publisherID;
    private String position = Positions.BOTTOM_CENTER;

    private SDKConsumer sdk;
    private InterstitialView interstitial;

    private Spinner spinnerOrientations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI(findViewById(R.id.containerView));

        btnGetBanner = findViewById(R.id.btnGetBanner);
        btnGetInterstitial = findViewById(R.id.btnGetInterstitial);
        btnGetVASTVideo = findViewById(R.id.btnGetVASTVideo);
        btnDisplayInterstitial = findViewById(R.id.btnDisplayInterstitial);
        btnDestroy = findViewById(R.id.btnDestroy);

        btnTopLeft = findViewById(R.id.btnTopLeft);
        btnTopCenter = findViewById(R.id.btnTopCenter);
        btnTopRight = findViewById(R.id.btnTopRight);
        btnCenterLeft = findViewById(R.id.btnCenterLeft);
        btnCenter = findViewById(R.id.btnCenter);
        btnCenterRight = findViewById(R.id.btnCenterRight);
        btnBottomLeft = findViewById(R.id.btnBottomLeft);
        btnBottomCenter = findViewById(R.id.btnBottomCenter);
        btnBottomRight =  findViewById(R.id.btnBottomRight);

        positions = new HashMap<Button, String>() {
            {
                put(btnTopLeft, Positions.TOP_LEFT);
                put(btnTopCenter, Positions.TOP_CENTER);
                put(btnTopRight, Positions.TOP_RIGHT);
                put(btnCenterLeft, Positions.LEFT_CENTER);
                put(btnCenter, Positions.CENTER);
                put(btnCenterRight, Positions.RIGHT_CENTER);
                put(btnBottomLeft, Positions.BOTTOM_LEFT);
                put(btnBottomCenter, Positions.BOTTOM_CENTER);
                put(btnBottomRight, Positions.BOTTOM_RIGHT);
            }
        };

        btnDisplayInterstitial.setVisibility(View.INVISIBLE);
        btnDestroy.setVisibility(View.INVISIBLE);

        txtAccountID =  findViewById(R.id.txtAccountID);
        txtZoneID =  findViewById(R.id.txtZoneID);
        txtPublisherID =  findViewById(R.id.txtPublisherID);
        txtLog = findViewById(R.id.txtLog);

        selectedPosition = btnBottomCenter;

        spinnerOrientations = (Spinner)findViewById(R.id.spinOrientation);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.orientations, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrientations.setAdapter(adapter);

        AdButler.initialize(this);
        sdk = new SDKConsumer(this);
    }

    public void onGetBannerClick(View v){
        if(!validateInputs(false)) return;
        AdListener listener = new AdListener() {
            @Override
            public void onAdFetchSucceeded() {
                log("onAdFetchSucceded");
                btnDestroy.setVisibility(View.VISIBLE);
                super.onAdFetchSucceeded();
            }

            @Override
            public void onAdFetchFailed(ErrorCode code) {
                log("onAdFetchFailed");
                super.onAdFetchFailed(code);
            }

            @Override
            public void onAdExpanded(){
                log("onAdExpanded");
                super.onAdExpanded();
            }

            @Override
            public void onAdResized(){
                log("onAdResized");
                super.onAdResized();
            }

            @Override
            public void onAdLeavingApplication(){
                log("onAdLeavingApplication");
                super.onAdLeavingApplication();
            }

            @Override
            public void onAdClosed() {
                log("onAdClosed");
                super.onAdClosed();
            }

            @Override
            public void onAdClicked() {
                log("onAdClicked");
                super.onAdClicked();
            }
        };
        if(position != null){
            sdk.getBanner(accountID, zoneID, position, listener);
        }else{
            sdk.getBanner(accountID, zoneID, (FrameLayout)findViewById(R.id.banner_container), listener);
        }

    }

    public void onGetInterstitialClick(View v){
        if(!validateInputs(false)) return;
        sdk.getInterstitial(accountID, zoneID, new AdListener() {
            @Override
            public void onAdFetchSucceeded() {
                log("onAdFetchSucceeded");
                super.onAdFetchSucceeded();
            }

            @Override
            public void onInterstitialReady(){
                log("onInterstitialReady");
                btnDisplayInterstitial.setVisibility(View.VISIBLE);
                super.onInterstitialReady();
            }

            @Override
            public void onAdFetchFailed(ErrorCode code) {
                log("onAdFetchFailed");
                super.onAdFetchFailed(code);
            }

            @Override
            public void onInterstitialDisplayed() {
                log("onInterstitialDisplayed");
                super.onInterstitialDisplayed();
            }

            @Override
            public void onAdExpanded(){
                log("onAdExpanded");
                super.onAdExpanded();
            }

            @Override
            public void onAdResized(){
                log("onAdResized");
                super.onAdResized();
            }

            @Override
            public void onAdLeavingApplication(){
                log("onAdLeavingApplication");
                super.onAdLeavingApplication();
            }

            @Override
            public void onAdClosed() {
                log("onAdClosed");
                super.onAdClosed();
            }

            @Override
            public void onAdClicked() {
                log("onAdClicked");
                super.onAdClicked();
            }
        });
    }

    public void onGetVASTVideoClick(View v){
        if(!validateInputs(true)) return;
        log("Retrieving VAST video.  Will autoplay once ready.");
        sdk.getVASTVideo(accountID, zoneID, publisherID, spinnerOrientations.getSelectedItem().toString(), new VASTListener() {
            @Override
            public void onMute() {
                log("VAST :: onMute");
                super.onMute();
            }

            @Override
            public void onUnmute() {
                log("VAST :: onUnmute");
                super.onUnmute();
            }

            @Override
            public void onPause() {
                log("VAST :: onPause");
                super.onPause();
            }

            @Override
            public void onResume() {
                log("VAST :: onResume");
                super.onResume();
            }

            @Override
            public void onRewind() {
                log("VAST :: onRewind");
                super.onRewind();
            }

            @Override
            public void onSkip() {
                log("VAST :: onSkip");
                super.onSkip();
            }

            @Override
            public void onPlayerExpand() {
                log("VAST :: onPlayerExpand");
                super.onPlayerExpand();
            }

            @Override
            public void onPlayerCollapse() {
                log("VAST :: onPlayerCollapse");
                super.onPlayerCollapse();
            }

            @Override
            public void onNotUsed() {
                log("VAST :: onNotUsed");
                super.onNotUsed();
            }

            @Override
            public void onLoaded() {
                log("VAST :: onLoaded");
                super.onLoaded();
            }

            @Override
            public void onStart() {
                log("VAST :: onStart");
                super.onStart();
            }

            @Override
            public void onFirstQuartile() {
                log("VAST :: onFirstQuartile");
                super.onFirstQuartile();
            }

            @Override
            public void onMidpoint() {
                log("VAST :: onMidpoint");
                super.onMidpoint();
            }

            @Override
            public void onThirdQuartile() {
                log("VAST :: onThirdQuartile");
                super.onThirdQuartile();
            }

            @Override
            public void onComplete() {
                log("VAST :: onComplete");
                super.onComplete();
            }

            @Override
            public void onCloseLinear() {
                log("VAST :: onCloseLinear");
                super.onCloseLinear();
            }

            @Override
            public void onClose(){
                log("VAST :: onClose");
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }

            @Override
            public void onReady(){
                log("VAST :: onReady");
                sdk.displayVASTVideo();
            }

            @Override
            public void onError(){
                log("VAST :: onError");
            }
        });
    }

    public void onDisplayInterstitialClick(View v){
        if(sdk.interstitial != null && sdk.interstitial.isReady){
            sdk.interstitial.show();
            btnDisplayInterstitial.setVisibility(View.INVISIBLE);
        }
    }

    public void onDestroyClick(View v){
        sdk.destroyBanner();
        btnDestroy.setVisibility(View.INVISIBLE);
    }

    private Boolean validateInputs(Boolean includePublisher){
        String text = txtAccountID.getText().toString();
        if(!text.isEmpty()){
            try{
                int val = Integer.parseInt(text);
                accountID = val;
            }catch(Exception ex){
                log(ex.getMessage());
                return false;
            }
        }else{
            log("No Account ID provided.");
            return false;
        }

        text = txtZoneID.getText().toString();
        if(!text.isEmpty()){
            try{
                int val = Integer.parseInt(text);
                zoneID = val;
            }catch(Exception ex){
                log(ex.getMessage());
                return false;
            }
        }else{
            log("No Zone ID provided.");
            return false;
        }

        if(includePublisher){
            text = txtPublisherID.getText().toString();
            if(!text.isEmpty()){
                try{
                    int val = Integer.parseInt(text);
                    publisherID = val;
                }catch(Exception ex){
                    log(ex.getMessage());
                    return false;
                }
            }else{
                log("No Publisher ID provided.");
                return false;
            }
        }
        return true;
    }

    public void onPositionClick(View v){
        selectedPosition.setBackgroundResource(R.color.colorPrimaryDark);
        selectedPosition = (Button)v;
        if(positions.get(selectedPosition) == position){
            position = null;
        }else{
            selectedPosition.setBackgroundResource(R.color.colorPrimary);
            position = positions.get(selectedPosition);
        }
    }
    private void log(String str){
        logBuilder.insert(0, "\n> " + str);
        if(logBuilder.length() > 32767){
            logBuilder.setLength(32764); // int16
            logBuilder.append("...");
        }
        txtLog.setText(logBuilder.toString());
    }



    // thanks https://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext
    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(MainActivity.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(activity.getCurrentFocus() != null){
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}
