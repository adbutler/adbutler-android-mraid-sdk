package com.sparklit.adbutler;

import com.google.gson.annotations.SerializedName;

/**
 * Models a beacon from a Placement.
 */
public class Beacon {
    @SerializedName("type")
    private String type;
    @SerializedName("url")
    private String url;

    /**
     * Get the type of Beacon.
     * @return String
     */
    public String getType() {
        return type;
    }

    /**
     * Get the url of the Beacon.
     * @return String
     */
    public String getUrl() {
        return url;
    }
}
