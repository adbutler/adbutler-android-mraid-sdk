package com.sparklit.adbutler;
import com.google.gson.annotations.SerializedName;

public class FrequencyCappingData {

    @SerializedName("placement_id")
    private String placementID;
    @SerializedName("views")
    private String views;
    @SerializedName("start")
    private String start;
    @SerializedName("expiry")
    private String expiry;

    public String getPlacementID() {
        return placementID;
    }

    public void setPlacementID(String placementID) {
        this.placementID = placementID;
    }

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }
}
