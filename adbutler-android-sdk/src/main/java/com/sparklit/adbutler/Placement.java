package com.sparklit.adbutler;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Models the Placement with all its properties.
 */
public class Placement {
    @SerializedName("banner_id")
    private int bannerId;
    @SerializedName("redirect_url")
    private String redirectUrl;
    @SerializedName("image_url")
    private String imageUrl;
    @SerializedName("width")
    private int width;
    @SerializedName("height")
    private int height;
    @SerializedName("alt_text")
    private String altText;
    @SerializedName("target")
    private String target;
    @SerializedName("tracking_pixel")
    private String trackingPixel;
    @SerializedName("accupixel_url")
    private String accupixelUrl;
    @SerializedName("refresh_url")
    private String refreshUrl;
    @SerializedName("refresh_time")
    private String refreshTime;
    @SerializedName("body")
    private String body;
    @SerializedName("beacons")
    private List<Beacon> beacons;

    private boolean impressionRecorded = false;
    private boolean clickRecorded = false;

    /**
     * The unique ID of the banner returned.
     */
    public int getBannerId() {
        return bannerId;
    }
    /**
     * A pass-through click redirect URL.
     */
    public String getRedirectUrl() {
        return redirectUrl;
    }
    /**
     * The image banner URL.
     */
    public String getImageUrl() {
        return imageUrl;
    }
    /**
     * The width of this placement.
     */
    public int getWidth() {
        return width;
    }
    /**
     * The height of this placement.
     */
    public int getHeight() {
        return height;
    }
    /**
     * Alternate text for screen readers on the web.
     */
    public String getAltText() {
        return altText;
    }
    /**
     * An HTML target attribute.
     */
    public String getTarget() {
        return target;
    }
    /**
     * An optional user-specified tracking pixel URL.
     */
    public String getTrackingPixel() {
        return trackingPixel;
    }
    /**
     * Used to record an impression for this request.
     */
    public String getAccupixelUrl() {
        return accupixelUrl;
    }
    /**
     * Contains a zone URL to request a new ad.
     */
    public String getRefreshUrl() {
        return refreshUrl;
    }
    /**
     * The user-specified delay between refresh URL requests.
     */
    public String getRefreshTime() {
        return refreshTime;
    }
    /**
     * The HTML markup of an ad request.
     */
    public String getBody() {
        return body;
    }

    /**
     * Get the list of beacons for this placement
     * @return
     */
    public List<Beacon> getBeacons() {
        return beacons;
    }

    /**
     * Check whether a click was recorded.
     * @return boolean
     */
    public boolean getClickRecorded(){ return clickRecorded; }

    /**
     * Sends requests to record impressions for this placement.
     */
    public void requestImpressionBeacons() {
        if(!impressionRecorded){
            impressionRecorded = true;
            AdButler adButler = new AdButler();
            if (getAccupixelUrl() != null && getAccupixelUrl().length() > 0) {
                Log.d("Ads/AdButler", "Impression event [accupixel]: " + getAccupixelUrl());
                adButler.requestPixel(getAccupixelUrl());
            }
            if (getTrackingPixel() != null && getTrackingPixel().length() > 0) {
                Log.d("Ads/AdButler", "Impression event [tracking pixel]: " + getTrackingPixel());
                adButler.requestPixel(getTrackingPixel());
            }
            if (getBeacons() != null && getBeacons().size() > 0) {
                for (Beacon b : getBeacons()) {
                    if (b.getType().equals("impression")) {
                        Log.d("Ads/AdButler", "Impression event [beacon]: " + b.getUrl());
                        adButler.requestPixel(b.getUrl());
                    }
                }
            }
        }
    }

    /**
     * Sends requests to record impressions for this placement.
     * @deprecated
     */
    public void recordImpression() {
        requestImpressionBeacons();
    }

    /**
     * Sends request to record click for this Placement.
     */
    public void recordManualClick() {
        if (getRedirectUrl() != null && getRedirectUrl().length() > 0) {
            AdButler adButler = new AdButler();
            adButler.requestPixel(getRedirectUrl());
        }
    }

    /**
     * Sends request to record click for this Placement.
     * @deprecated
     */
    public void recordClick() {
        recordManualClick();
    }

    /**
     * Sends requests to record clicks for this placement.
     */
    public void requestClickBeacons() {
        if(!clickRecorded){
            clickRecorded = true;
            AdButler adButler = new AdButler();
            if (getBeacons() != null && getBeacons().size() > 0) {
                for (Beacon b : getBeacons()) {
                    if (b.getType().equals("click")) {
                        Log.d("Ads/AdButler", "Click event [beacon]: " + b.getUrl());
                        adButler.requestPixel(b.getUrl());
                    }
                }
            }else if(getRedirectUrl() != null && !getRedirectUrl().equals("")){
                adButler.requestPixel(getRedirectUrl());
            }
        }
    }
}

