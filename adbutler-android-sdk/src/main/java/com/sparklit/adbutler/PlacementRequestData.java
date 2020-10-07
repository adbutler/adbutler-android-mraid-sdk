package com.sparklit.adbutler;

import com.google.gson.annotations.SerializedName;

public class PlacementRequestData {
    @SerializedName("zoneID")
    private int zoneID;

    @SerializedName("accountID")
    private int accountID;

    @SerializedName("frequencyCappingData")
    private String frequencyCappingData;


    public int getZoneID() {
        return zoneID;
    }

    public void setZoneID(int zoneID) {
        this.zoneID = zoneID;
    }

    public int getAccountID() {
        return accountID;
    }

    public void setAccountID(int accountID) {
        this.accountID = accountID;
    }

    public String getfrequencyCappingData() {
        return frequencyCappingData;
    }

    public void setfrequencyCappingData(String frequencyCappingData) {
        this.frequencyCappingData = frequencyCappingData;
    }
}
