package com.sparklit.adbutler;

/**
 * An AdButler ad listener to listen for ad events.
 */
public abstract class AdListener {
    /**
     * Called when an ad is successfully fetched.
     */
    public void onAdFetchSucceeded() {
        // Default is to do nothing.
    }

    /**
     * Called when an ad fetch fails.
     * @param code The reason the fetch failed.
     */
    public void onAdFetchFailed(ErrorCode code) {
        // Default is to do nothing.
    }

    /**
     * Called when an ad goes full screen.
     */
    public void onInterstitialDisplayed() {
        // Default is to do nothing.
    }

    /**
     *  Called when the interstitial web view is finished loading.
     */
    public void onInterstitialReady(){
        // Default is to do nothing.
    }

    /**
     *  MRAID Expand called.
     */
    public void onAdExpanded(){
        // Default is to do nothing.
    }

    /**
     *  Called before an ad causes another application to open. E.G. Web Browser.
     */
    public void onAdLeavingApplication(){
        // Default is to do nothing.
    }

    /**
     *  MRAID Resize called.
     */
    public void onAdResized(){
        // Default is to do nothing.
    }

    /**
     * Called when an ad is closed. (MRAID ads or interstitials)
     */
    public void onAdClosed() {
        // Default is to do nothing.
    }

    /**
     * Called when the ad is clicked.
     */
    public void onAdClicked() {
        // Default is to do nothing.
    }
}
