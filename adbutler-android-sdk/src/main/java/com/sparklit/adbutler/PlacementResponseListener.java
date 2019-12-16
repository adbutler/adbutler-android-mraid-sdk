package com.sparklit.adbutler;

/**
 * Listener for the PlacementResponse to a placement request
 */
public interface PlacementResponseListener {
    public void success(PlacementResponse response);
    public void error(Throwable throwable);
}
