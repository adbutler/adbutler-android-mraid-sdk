package com.sparklit.adbutler;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds the response status and list of placements.
 */
public class PlacementResponse {
    @SerializedName("status")
    private String status;
    @SerializedName("placements")
    private Map<String, Placement> placements;

    /**
     * @return list of placements received from the API.
     */
    public List<Placement> getPlacements() {
        List<Placement> placements = new ArrayList<>();
        if(null != this.placements){
            for (Map.Entry<String, Placement> placementEntry: this.placements.entrySet()) {
                placements.add(placementEntry.getValue());
            }
        }
        return placements;
    }

    void setPlacements(List<Placement> placements) {
        this.placements = new HashMap<>();
        for (int i = 0; i < placements.size(); i++) {
            String name = "placement_" + (i + 1);
            this.placements.put(name, placements.get(i));
        }
    }

    /**
     * @return the status of the response.
     */
    public String getStatus() {
        return status;
    }

    void setStatus(String status) {
        this.status = status;
    }

}
