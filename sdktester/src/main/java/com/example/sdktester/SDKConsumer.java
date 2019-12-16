package com.example.sdktester;

import android.app.Activity;
import android.app.FragmentManager;

import com.sparklit.adbutler.AdListener;
import com.sparklit.adbutler.AdRequest;
import com.sparklit.adbutler.BannerView;
import com.sparklit.adbutler.InterstitialView;
import com.sparklit.adbutler.VASTListener;
import com.sparklit.adbutler.VASTVideo;


public class SDKConsumer {
    Activity context;
    BannerView banner;
    VASTVideo vastVideo;
    protected InterstitialView interstitial;

    public SDKConsumer(Activity context){
        this.context = context;
    }

    public void destroyBanner(){
        banner.destroy();
    }

    public void getBanner(int accountID, int zoneID, String position, AdListener listener){
        FragmentManager fm = context.getFragmentManager();
        banner = (BannerView)fm.findFragmentById(R.id.adbutler_fragment);
        AdRequest request = new AdRequest(accountID, zoneID);
        banner.initialize(request, position, context, listener);
    }

    public void getInterstitial(int accountID, int zoneID, AdListener listener){
        interstitial = new InterstitialView();
        AdRequest request = new AdRequest(accountID, zoneID);
        interstitial.initialize(request, context, listener);
    }

    public void getVASTVideo(int accountID, int zoneID, int publisherID, String orientation, VASTListener listener){
        vastVideo = new VASTVideo(context, accountID, zoneID, publisherID, orientation, listener);
        vastVideo.preload();
    }

    public void displayVASTVideo(){
        vastVideo.display();
    }
}
