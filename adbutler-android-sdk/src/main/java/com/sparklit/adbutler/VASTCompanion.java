package com.sparklit.adbutler;

import java.util.HashMap;
import java.util.Map;

class VASTCompanion {
    public String staticResource;
    public String htmlResource;
    public Map<String, String> trackingEvents;
    public String clickThrough;

    public VASTCompanion(){
        trackingEvents = new HashMap<String, String>();
    }
}
