package com.sparklit.adbutler;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.URLDecoder;
import java.util.ArrayList;

class MRAIDHandler {

    public String state = States.LOADING;
    public boolean isInterstitial = false;
    public boolean isExpanded = false;
    public WebView activeWebView;

    private MRAIDListener mraidListener;
    private Context context;
    private Fragment fragment;

    // TODO nullable properties
    public OrientationProperties orientationProperties = new OrientationProperties();
    private ResizeProperties resizeProperties = new ResizeProperties();
    private ExpandProperties expandProperties = new ExpandProperties();

    private ArrayList<String> supportedFeatures;

    FrameLayout closeButton;

    // keep reference to objects for use on permission callbacks
    MRAIDCalendarEvent calendarEvent;
    String photoURL;
    String phoneNumber;

    public void setContext(Context context){
        this.context = context;
    }


    public MRAIDHandler(MRAIDListener listener, Context context, Fragment fragment){
        this.mraidListener = listener;
        this.context = context;
        this.fragment = fragment;
    }

    public void initialize(WebView view){
        activeWebView = view;
        getSupportedFeatures();
        setMRAIDSupports(Features.CALENDAR);
        setMRAIDSupports(Features.TEL);
        setMRAIDSupports(Features.STORE_PICTURE);
        setMRAIDSupports(Features.INLINE_VIDEO);
        setMRAIDSupports(Features.SMS);
        setMRAIDScreenSize();
        setMRAIDMaxSize();
        setMRAIDVersion(MRAIDConstants.MRAIDVersion);
        if(isExpanded){
            setMRAIDState(States.EXPANDED);
        }else{
            setMRAIDState(States.DEFAULT);
        }
        fireMRAIDEvent(Events.READY, null);
    }

    public void handleEndpoint(String fullUrl){
        fullUrl = fullUrl.replace("mraid://", "");
        String toFind = "?args=";
        String url = null;
        String args = null;
        int index = fullUrl.indexOf(toFind);
        if(index > -1){
            url = fullUrl.substring(0, index);
            args = fullUrl.substring(index + toFind.length(), fullUrl.length());
            args = URLDecoder.decode(args);
        }else{
            url = fullUrl;
        }
        switch(url){
            case NativeEndpoints.REPORT_JS_LOG:
                android.util.Log.d("MRAID_CONSOLE::", args);
                break;
            case NativeEndpoints.SET_RESIZE_PROPERTIES:
                setResizeProperties(args);
                break;
            case NativeEndpoints.RESIZE:
                resize();
                break;
            case NativeEndpoints.SET_EXPAND_PROPERTIES:
                setExpandProperties(args);
                break;
            case NativeEndpoints.EXPAND:
                expand(args);
                break;
            case NativeEndpoints.SET_ORIENTATION_PROPERTIES:
                setOrientationProperties(args);
                break;
            case NativeEndpoints.OPEN:
                open(args);
                break;
            case NativeEndpoints.CLOSE:
                close();
                break;
            case NativeEndpoints.CREATE_CALENDAR_EVENT:
                createCalendarEvent(args);
                break;
            case NativeEndpoints.PLAY_VIDEO:
                playVideo(args);
                break;
            case NativeEndpoints.REPORT_DOM_SIZE:
                Gson gson = new GsonBuilder().create();
                Size reportedSize = gson.fromJson(args, Size.class);
                if(reportedSize  != null && reportedSize.width > 0 && reportedSize.height > 0){
                    mraidListener.reportDOMSize(reportedSize);
                }
                break;
            case NativeEndpoints.STORE_PICTURE:
                createPhoto(args);
                break;
        }
    }

    private void getSupportedFeatures(){
        // if device should disallow features, do it here
        supportedFeatures = new ArrayList<>();
        if (((TelephonyManager)activeWebView.getContext().getSystemService(Context.TELEPHONY_SERVICE)).getPhoneType() != TelephonyManager.PHONE_TYPE_NONE) {
            supportedFeatures.add(Features.SMS);
            supportedFeatures.add(Features.TEL);
        }
        supportedFeatures.add(Features.CALENDAR);
        supportedFeatures.add(Features.INLINE_VIDEO);
        supportedFeatures.add(Features.STORE_PICTURE);
    }


    public void addCloseButton(WebView view, boolean showButton, String position){
        closeButton = new FrameLayout(context);
        int width = MRAIDUtilities.convertDpToPixel(50, context);
        int height = MRAIDUtilities.convertDpToPixel(50, context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        int gravity = Gravity.NO_GRAVITY;
        if(position.contains("top")){
            gravity = gravity | Gravity.TOP;
        }
        if(position.contains("right")){
            gravity = gravity | Gravity.RIGHT;
        }
        if(position.contains("left")){
            gravity = gravity | Gravity.LEFT;
        }
        if(position.contains("bottom")){
            gravity = gravity | Gravity.BOTTOM;
        }
        if(position.equals("center")){
            gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
        }else if(position.contains("center")){
            gravity = gravity | Gravity.CENTER;
        }
        params.gravity = gravity;
        ViewGroup parent = (ViewGroup)view.getParent();
        parent.addView(closeButton, params);
        closeButton.bringToFront();
        if((expandProperties == null || !expandProperties.useCustomClose) && showButton){
            closeButton.setBackgroundColor(0xaa000000);
            TextView tv = new TextView(context);
            FrameLayout.LayoutParams tparams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            tparams.gravity = Gravity.CENTER;
            Typeface font = Typeface.create("Droid Sans Mono", Typeface.NORMAL);
            tv.setTextColor(Color.WHITE);
            tv.setTypeface(font);
            tv.setText("X");
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24f);
            closeButton.addView(tv);
            tv.setLayoutParams(tparams);
            tv.bringToFront();
        }
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
    }

    /*
        ======== CALLED BY MRAID =========
     */

    private void expand(String url){
        Log.d("Ads/AdButler", "MRAID :: expand");
        if(this.state == States.DEFAULT){
            mraidListener.expand(url);
        }
        if(url == null){
            addCloseButton(activeWebView, true, Positions.TOP_RIGHT);
        }
        setMRAIDState(States.EXPANDED);
    }

    private void resize(){
        Log.d("Ads/AdButler", "MRAID :: resize");
        if(resizeProperties == null){
            fireMRAIDEvent(Events.ERROR, "resize cannot be called before setResizeProperties");
            return;
        }
        if(state == States.DEFAULT || state == States.RESIZED) {
            String pos = resizeProperties.customClosePosition != null ? resizeProperties.customClosePosition : Positions.TOP_RIGHT;
            if (resizeProperties.allowOffscreen) {
                // make sure the close button would be on screen
                boolean valid = true;
                int[] xypos = new int[2];
                activeWebView.getLocationOnScreen(xypos);
                int btnSize = MRAIDUtilities.convertDpToPixel(50, context);
                Rect resizedRect = new Rect(xypos[0], xypos[1], MRAIDUtilities.convertDpToPixel(resizeProperties.width, context), MRAIDUtilities.convertDpToPixel(resizeProperties.height, context));
                Rect fullscreen = MRAIDUtilities.getFullScreenRect((Activity) context);
                int offsetX = MRAIDUtilities.convertDpToPixel(resizeProperties.offsetX, context);
                int offsetY = MRAIDUtilities.convertDpToPixel(resizeProperties.offsetY, context);


                if (pos.contains("right")) {
                    int btnRight = resizedRect.x + resizedRect.width + offsetX;
                    boolean offscreenLeft = btnRight < btnSize;
                    boolean offscreenRight = btnRight > fullscreen.width;
                    if (offscreenLeft || offscreenRight) {
                        valid = false;
                    }
                }
                if (pos.contains("left")) {
                    int btnLeft = resizedRect.x + offsetX;
                    boolean offscreenLeft = btnLeft < 0;
                    boolean offscreenRight = btnLeft > (fullscreen.width - btnSize);
                    if (offscreenLeft || offscreenRight) {
                        valid = false;
                    }
                }
                if (pos.contains("bottom")) {
                    int btnBottom = resizedRect.y + resizedRect.height + offsetY;
                    boolean offscreenTop = btnBottom < btnSize;
                    boolean offscreenBottom = btnBottom > fullscreen.height;
                    if (offscreenTop || offscreenBottom) {
                        valid = false;
                    }
                }
                if (pos.contains("top")) {
                    int btnTop = resizedRect.y + offsetY;
                    boolean offscreenTop = btnTop < 0;
                    boolean offscreenBottom = btnTop > (fullscreen.height - btnSize);
                    if (offscreenTop || offscreenBottom) {
                        valid = false;
                    }
                }
                if (!valid) {
                    fireMRAIDEvent(Events.ERROR, "Current resize properties would result in the close region being off screen.  Ignoring resize.");
                    return;
                }
            }
            mraidListener.resize(resizeProperties);
            addCloseButton(activeWebView, false, pos);
            setMRAIDState(States.RESIZED);
        }
    }

    private void open(String url){
        Log.d("Ads/AdButler", "MRAID :: open");
        if(url.startsWith("tel://")){
            String number = url.substring("tel://".length(), url.length());
            attemptPhoneCall(number);
        }else if (url.startsWith("sms://")){
            String number = url.substring("sms://".length(), url.length());
            createSMS(number);
        }else{
            Intent intent = new Intent(context, BrowserView.class);
            intent.putExtra("URL", url);
            mraidListener.onLeavingApplication();
            context.startActivity(intent);
        }
        mraidListener.open(url);
    }

    private void close(){
        Log.d("Ads/AdButler", "MRAID :: close");
        isExpanded = false;
        if(closeButton != null){
            closeButton.removeAllViews();
            ((ViewGroup)closeButton.getParent()).removeView(closeButton);
            closeButton = null;
        }
        mraidListener.close();
        ((Activity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        if(!isInterstitial){
            setMRAIDState(States.DEFAULT);
        }else{
            setMRAIDState(States.HIDDEN);
            setMRAIDIsVisible(false);
        }
    }

    private void playVideo(String url){
        Log.d("Ads/AdButler", "MRAID :: playVideo");
        Intent intent = new Intent(context, VideoPlayer.class);
        intent.putExtra("URL", url);
        context.startActivity(intent);
    }

    private void attemptPhoneCall(String number){
        phoneNumber = number;
        if(ContextCompat.checkSelfPermission(fragment.getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            // not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(fragment.getActivity(), Manifest.permission.CALL_PHONE)) {
                // TODO async explanation dialog followed by permission request
            } else {
                // No explanation needed; request the permission
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    fragment.requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, Permissions.CALL);
                }else{
                    ActivityCompat.requestPermissions(fragment.getActivity(), new String[]{Manifest.permission.CALL_PHONE}, Permissions.CALL);
                }
            }
        }else{
            makePhoneCall();
        }
    }

    // called after permission acquired
    public void makePhoneCall(){
        mraidListener.onLeavingApplication();
        MRAIDUtilities.makePhoneCall(phoneNumber, context);
        phoneNumber = null;
    }

    private void createSMS(String number){
        phoneNumber = number;
        if(ContextCompat.checkSelfPermission(fragment.getActivity(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            // not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(fragment.getActivity(), Manifest.permission.SEND_SMS)) {
                // TODO async explanation dialog followed by permission request
            } else {
                // No explanation needed; request the permission
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    fragment.requestPermissions(new String[]{Manifest.permission.SEND_SMS}, Permissions.SMS);
                }else{
                    ActivityCompat.requestPermissions(fragment.getActivity(), new String[]{Manifest.permission.SEND_SMS}, Permissions.SMS);
                }
            }
        }else{
            sendSMS();
            phoneNumber = null;
        }
    }

    public void sendSMS(){
        mraidListener.onLeavingApplication();
        MRAIDUtilities.sendSMS(phoneNumber, context);
    }

    private void createPhoto(String args){
        Log.d("Ads/AdButler", "MRAID :: storePhoto");
        photoURL = args;
        if(ContextCompat.checkSelfPermission(fragment.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            // not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(fragment.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // TODO async explanation dialog followed by permission request
            } else {
                // No explanation needed; request the permission
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    fragment.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Permissions.PHOTO);
                }else {
                    ActivityCompat.requestPermissions(fragment.getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Permissions.PHOTO);
                }
            }
        }else{
            savePhoto();
        }
    }

    // called after permission acquired
    public void savePhoto(){
        new PhotoDownloader((Activity)context).savePhoto(photoURL);
        photoURL = null;
    }

    private void createCalendarEvent(String args){
        Log.d("Ads/AdButler", "MRAID :: createCalendarEvent");
        Gson gson = new GsonBuilder().create();
        calendarEvent = gson.fromJson(args, MRAIDCalendarEvent.class);
        if(calendarEvent != null){
            if(ContextCompat.checkSelfPermission(fragment.getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED){
                // not granted
                if (ActivityCompat.shouldShowRequestPermissionRationale(fragment.getActivity(), Manifest.permission.WRITE_CALENDAR)) {
                    // TODO async explanation dialog followed by permission request
                } else {
                    // No explanation needed; request the permission
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        fragment.requestPermissions(new String[]{Manifest.permission.WRITE_CALENDAR}, Permissions.CALENDAR);
                    }else {
                        ActivityCompat.requestPermissions(fragment.getActivity(), new String[]{Manifest.permission.WRITE_CALENDAR}, Permissions.CALENDAR);
                    }
                }
            }else{
                writeCalendarEvent();
            }
        }
    }

    // called after permission acquired
    public void writeCalendarEvent(){
        MRAIDUtilities.writeCalendarEvent(calendarEvent, (Activity)context);
        calendarEvent = null;
    }

    private void setExpandProperties(String args){
        Log.d("Ads/AdButler", "MRAID :: setExpandProperties");
        Gson gson = new GsonBuilder().create();
        ExpandProperties newProperties = gson.fromJson(args, ExpandProperties.class);
        if(newProperties != null){
            // only replace properties which are present.  (deserialization defaults nullable types to null when not provided in the string)
            if(args.contains("useCustomClose")){
                expandProperties.useCustomClose = newProperties.useCustomClose;
            }
            if(args.contains("width")){
                expandProperties.width = newProperties.width;
            }
            if(args.contains("isModal")){
                expandProperties.isModal = newProperties.isModal;
            }
            if(args.contains("height")){
                expandProperties.height = newProperties.height;
            }
        }
    }

    private void setResizeProperties(String args){
        Log.d("Ads/AdButler", "MRAID :: setResizeProperties");
        Gson gson = new GsonBuilder().create();
        ResizeProperties newProperties = gson.fromJson(args, ResizeProperties.class);
        if(newProperties != null){
            // only replace properties which are present.  (deserialization defaults nullable types to null when not provided in the string)
            if(args.contains("allowOffscreen")){
                resizeProperties.allowOffscreen = newProperties.allowOffscreen;
            }
            if(args.contains("customClosePosition")){
                resizeProperties.customClosePosition = newProperties.customClosePosition;
            }
            if(args.contains("height")){
                resizeProperties.height = newProperties.height;
            }
            if(args.contains("width")){
                resizeProperties.width = newProperties.width;
            }
            if(args.contains("offsetX")){
                resizeProperties.offsetX = newProperties.offsetX;
            }
            if(args.contains("offsetY")){
                resizeProperties.offsetY = newProperties.offsetY;
            }
        }
    }

    private void setOrientationProperties(String args){
        Log.d("Ads/AdButler", "MRAID :: setOrientationProperties");
        Gson gson = new GsonBuilder().create();
        OrientationProperties newProperties = gson.fromJson(args, OrientationProperties.class);
        if(newProperties != null){
            if(args.contains("allowOrientationChange")){
                orientationProperties.allowOrientationChange = newProperties.allowOrientationChange;
            }
            if(args.contains("forceOrientation")){
                orientationProperties.forceOrientation = newProperties.forceOrientation;
            }
            mraidListener.setOrientationProperties(newProperties);
        }
    }

    /*
        ========= MRAID JS Methods =========
     */
    public void setMRAIDState(String state){
        this.state = state;
        String js = "window.mraid.setState(\"" + state + "\");";
        executeJavascript(js);
    }

    public void fireMRAIDEvent(String event, String args){
        String script = "window.mraid.fireEvent('" + event + "', " + (args != null ? args : "") + ");";
        executeJavascript(script);
    }

    public void setMRAIDSupports(String feature){
        boolean supports = supportedFeatures.indexOf(feature) > -1;
        String script = String.format("window.mraid.setSupports(\"%s\", %b);", feature, supports);
        executeJavascript(script);
    }

    public void setMRAIDSizeChanged(){
        int width = MRAIDUtilities.convertPixelsToDp(activeWebView.getWidth(), activeWebView.getContext());
        int height = MRAIDUtilities.convertPixelsToDp(activeWebView.getHeight(), activeWebView.getContext());
        fireMRAIDEvent(Events.SIZE_CHANGE, String.format("{ width:\"%d\", height:\"%d\"}", width, height));
    }

    public void setMRAIDVersion(String version){
        String script = "window.mraid.setVersion(\"" + version + "\");";
        executeJavascript(script);
    }

    private void setMRAIDMaxSize(){
        //TODO if we will support non-fullscreen apps, determine the allowable space. not recommended by IAB
        DisplayMetrics displayMetrics = activeWebView.getContext().getResources().getDisplayMetrics();
        int dpHeight = Math.round(displayMetrics.heightPixels / displayMetrics.density);
        int dpWidth = Math.round(displayMetrics.widthPixels / displayMetrics.density);
        String script = String.format("window.mraid.setMaxSize({width:%d, height:%d});", dpWidth, dpHeight);
        executeJavascript(script);
    }

    public void setMRAIDScreenSize(){
        DisplayMetrics displayMetrics = activeWebView.getContext().getResources().getDisplayMetrics();
        int dpHeight = Math.round(displayMetrics.heightPixels / displayMetrics.density);
        int dpWidth = Math.round(displayMetrics.widthPixels / displayMetrics.density);

        String script = String.format("window.mraid.setScreenSize({width:%d, height:%d});", dpWidth, dpHeight);
        executeJavascript(script);
    }

    public void setMRAIDCurrentPosition(Rect pos){
        String js = String.format("window.mraid.setCurrentPosition({x:%s, y:%s, width:%s, height:%s});", pos.x, pos.y, pos.width, pos.height);
        executeJavascript(js);
    }

    public void setMRAIDDefaultPosition(Rect pos){
        String js = String.format("window.mraid.setDefaultPosition({x:%s, y:%s, width:%s, height:%s});", pos.x, pos.y, pos.width, pos.height);
        executeJavascript(js);
    }

    public void setMRAIDIsVisible(boolean visible){
        String js = "window.mraid.setIsViewable(" + visible + ");";
        executeJavascript(js);
    }

    public void executeJavascript(String script){
        activeWebView.loadUrl("javascript:" + script);
    }
}
