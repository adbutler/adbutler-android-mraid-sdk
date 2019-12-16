package com.sparklit.adbutler;

public interface MRAIDListener {
    // stuff that interstitial and banner ads should handle differently
    void open(String url);
    void close();
    void expand(String url);
    void resize(ResizeProperties properties);
    void reportDOMSize(Size size);
    void setOrientationProperties(OrientationProperties properties);
    void onLeavingApplication();
}
