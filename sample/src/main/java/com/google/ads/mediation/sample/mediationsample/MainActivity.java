/*
 * Copyright (C) 2019 Sparklit Networks Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ads.mediation.sample.mediationsample;

import android.app.FragmentManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sparklit.adbutler.AdRequest;
import com.sparklit.adbutler.BannerView;
import com.sparklit.adbutler.Positions;
import com.sparklit.adbutler.InterstitialView;
import com.sparklit.adbutler.AdListener;
import com.sparklit.adbutler.ErrorCode;
import com.sparklit.adbutler.AdButler;
import com.sparklit.adbutler.VASTListener;
import com.sparklit.adbutler.VASTVideo;

import java.util.Date;
import java.util.Random;

import android.view.View;

/**
 * A simple {@link android.app.Activity} that displays adds using the sample adapter and sample
 * custom event.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private BannerView bannerView;
    private InterstitialView interstitial;
    VASTVideo vast;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize AdButler (It's okay to call this multiple times, but make sure it's called at least once)
        AdButler.initialize(this);
    }

    protected @Override void onDestroy(){
        super.onDestroy();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    public void onGetInterstitialClick(View view){
        interstitial = new InterstitialView();
        AdRequest request = new AdRequest(50088, 354196);
        request.setCoppa(0);
        request.setAge(30);
        request.setGender(getUserGender());
        request.setLocation(getUserLocation());
        request.setBirthday(new Date());
        interstitial.initialize(request, this, new AdListener() {
            @Override
            public void onAdFetchSucceeded() {
                super.onAdFetchSucceeded();
            }

            @Override
            public void onInterstitialReady(){
                super.onInterstitialReady();
                if(interstitial.isReady){
                    interstitial.show();
                }
            }

            @Override
            public void onAdFetchFailed(ErrorCode code) {
                super.onAdFetchFailed(code);
            }

            @Override
            public void onInterstitialDisplayed() {
                super.onInterstitialDisplayed();
            }

            @Override
            public void onAdExpanded(){
                super.onAdExpanded();
            }

            @Override
            public void onAdResized(){
                super.onAdResized();
            }

            @Override
            public void onAdLeavingApplication(){
                super.onAdLeavingApplication();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
            }
        });
    }


    public void onGetBannerClick(View view){
        FragmentManager fm = getFragmentManager();
        bannerView = (BannerView)fm.findFragmentById(R.id.adbutler_fragment);
        AdRequest request = new AdRequest(50088, 354134);
        request.setCoppa(0);
        request.setAge(30);
        request.setGender(getUserGender());
        request.setLocation(getUserLocation());
        request.setBirthday(new Date());
        bannerView.initialize(request, Positions.BOTTOM_CENTER, this, new AdListener() {
            @Override
            public void onAdFetchSucceeded() {
                super.onAdFetchSucceeded();
            }

            @Override
            public void onAdFetchFailed(ErrorCode code) {
                super.onAdFetchFailed(code);
            }

            @Override
            public void onInterstitialDisplayed() {
                super.onInterstitialDisplayed();
            }

            @Override
            public void onAdExpanded(){
                super.onAdExpanded();
            }

            @Override
            public void onAdResized(){
                super.onAdResized();
            }

            @Override
            public void onAdLeavingApplication(){
                super.onAdLeavingApplication();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
            }
        });
    }

    public void onGetVASTClick(View view){
        VASTListener listener = new VASTListener() {
            @Override
            public void onMute() {
                System.out.println("mute");
            }

            @Override
            public void onUnmute() {
                System.out.println("unmute");
            }

            @Override
            public void onPause() {
                System.out.println("pause");
            }

            @Override
            public void onResume() {
                System.out.println("resume");
            }

            @Override
            public void onRewind() {
                System.out.println("rewind");
            }

            @Override
            public void onSkip() {
                System.out.println("skip");
            }

            @Override
            public void onPlayerExpand() {
                System.out.println("playerExpand");
            }

            @Override
            public void onPlayerCollapse() {
                System.out.println("playerCollapse");
            }

            @Override
            public void onNotUsed() {
                System.out.println("notUsed");
            }

            @Override
            public void onLoaded() {
                System.out.println("loaded");
            }

            @Override
            public void onStart() {
                System.out.println("start");
            }

            @Override
            public void onFirstQuartile() {
                System.out.println("firstQuartile");
            }

            @Override
            public void onMidpoint() {
                System.out.println("midpoint");
            }

            @Override
            public void onThirdQuartile() {
                System.out.println("thirdQuartile");
            }

            @Override
            public void onComplete() {
                System.out.println("complete");
            }

            @Override
            public void onCloseLinear() {
                System.out.println("closeLinear");
            }

            @Override
            public void onReady() {
                System.out.println("ready");
                vast.display();
            }

            @Override
            public void onError() {
                System.out.println("error");
            }
        };
        vast = new VASTVideo(this, 50088, 7205, 67540, "none", listener);
        vast.preload();
    }

    public void displayVast(){

    }

    // dummy gender
    public int getUserGender() {
        Random rand = new Random();
        int i = rand.nextInt(3);
        int[] genders = {AdRequest.GENDER_UNKNOWN, AdRequest.GENDER_MALE, AdRequest.GENDER_FEMALE};
        return genders[i];
    }

    public Location getUserLocation() {
        Location loc = new Location("Dummy");
        loc.setLatitude(37.4220);
        loc.setLongitude(122.0841);
        return loc;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        bannerView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
