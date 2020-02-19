package com.sparklit.adbutler;

class MRAIDConstants{
    public static final String MRAIDVersion = "2.0";
}

class MRAIDCalendarEvent {
    public String description;
    public String location;
    public String start;
    public String end;
    public String id;
    public MRAIDCalendarRecurrence recurrence;
    public String reminder;
    public String status;
    public String summary;
    public String transparency;
}

class MRAIDCalendarRecurrence {
    public String frequency;
    public int[] daysInWeek;
    public int[] daysInMonth;
    public int[] monthsInYear;
    public int[] daysInYear;
    public int[] weeksInYear;
    public String expires;
}

class Size  {
    public int width;
    public int height;
    public Size(int w, int h){
        this.width = w;
        this.height = h;
    }
}

class Rect {
    public int x;
    public int y;
    public int width;
    public int height;
    public Rect(int x, int y, int w, int h){
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }
}

class OrientationProperties {
    public boolean allowOrientationChange;
    public String forceOrientation;
}

class ResizeProperties {
    public int width;
    public int height;
    public int offsetX;
    public int offsetY;
    public String customClosePosition; // Positions constant
    public boolean allowOffscreen;
}

class ExpandProperties {
    public int width;
    public int height;
    public boolean useCustomClose;
    public boolean isModal;
}


class States {
    static final String LOADING = "loading";
    static final String DEFAULT = "default";
    static final String EXPANDED = "expanded";
    static final String RESIZED = "resized";
    static final String HIDDEN = "hidden";
}

class PlacementTypes {
    static final String INLINE = "inline";
    static final String INTERSTITIAL = "interstitial";
}

class Orientations {
    static final String PORTRAIT = "portrait";
    static final String LANDSCAPE = "landscape";
    static final String NONE = "none";
}

class Features {
    static final String SMS = "sms";
    static final String TEL = "tel";
    static final String STORE_PICTURE = "storePicture";
    static final String INLINE_VIDEO = "inlineVideo";
    static final String CALENDAR = "calendar";
}

class NativeEndpoints {
    static final String EXPAND = "expand";
    static final String OPEN = "open";
    static final String PLAY_VIDEO = "playVideo";
    static final String RESIZE = "resize";
    static final String STORE_PICTURE = "storePicture";
    static final String CREATE_CALENDAR_EVENT = "createCalendarEvent";
    static final String SET_ORIENTATION_PROPERTIES = "setOrientationProperties";
    static final String SET_RESIZE_PROPERTIES = "setResizeProperties";
    static final String REPORT_DOM_SIZE = "reportDOMSize";
    static final String REPORT_JS_LOG = "reportJSLog";
    static final String CLOSE = "close";
    static final String SET_EXPAND_PROPERTIES = "setExpandProperties";
}

class Events {
    static final String READY = "ready";
    static final String SIZE_CHANGE = "sizeChange";
    static final String STATE_CHANGE = "stateChange";
    static final String VIEWABLE_CHANGE = "viewableChange";
    static final String ERROR = "error";
}

class Permissions{
    static final int CALENDAR = 0;
    static final int SMS = 1;
    static final int CALL = 2;
    static final int PHOTO = 3;
}