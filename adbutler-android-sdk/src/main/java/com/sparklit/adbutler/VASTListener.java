package com.sparklit.adbutler;

public abstract class VASTListener {
    // player operation
    public void onMute(){}
    public void onUnmute(){}
    public void onPause(){}
    public void onResume(){}
    public void onRewind(){}
    public void onSkip(){}
    public void onPlayerExpand(){}
    public void onPlayerCollapse(){}
    public void onNotUsed(){}

    // linear
    public void onLoaded(){}
    public void onStart(){}
    public void onFirstQuartile(){}
    public void onMidpoint(){}
    public void onThirdQuartile(){}
    public void onComplete(){}
    //    public void onOtherAdInteraction(){} future addition
    public void onCloseLinear(){}

    // non linear
    // future additions
    //    public void onCreativeView(){}
    //    public void onAcceptInvitation(){}
    //    public void onAdExpand(){}
    //    public void onAdCollapse(){}
    //    public void onMinimize(){}
    public void onClose(){}
    //    public void onOverlayViewDuration(){}

    public void onBrowserOpening(){}
    public void onBrowserClosing(){}
    public void onReady(){}
    public void onError(){}
}
