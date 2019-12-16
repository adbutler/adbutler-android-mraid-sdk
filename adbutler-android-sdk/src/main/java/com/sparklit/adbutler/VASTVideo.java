package com.sparklit.adbutler;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * A VAST video ad object.
 */
public class VASTVideo {

    private Context context;
    private int zoneID;
    private int accountID;
    private int publisherID;
    private String poster;
    private List<Source> sources;
    private static VASTListener listenerInstance;
    private String orientation = "none";

    private static VASTCompanion endCard;

    // Video Preloading
    private RelativeLayout nonVideoLayout;
    private RelativeLayout videoLayout;
    private View loadingView;
    public static VideoEnabledWebView webViewInstance;
    public boolean ready = false;
    public boolean displayed = false;
    private static boolean closeButtonRequired = false;

    private void setListenerInstance(VASTListener listener){
        VASTVideo.listenerInstance = listener;
    }

    protected static VASTListener getListenerInstance(){
        return VASTVideo.listenerInstance;
    }

    protected static VideoEnabledWebView getWebView(){
        return VASTVideo.webViewInstance;
    }

    protected static VASTCompanion getEndCard(){
        return VASTVideo.endCard;
    }

    protected static boolean getCloseButtonRequired() { return VASTVideo.closeButtonRequired; }

    private class Source {
        protected String source;
        protected String type;

        public Source(String source, String type){
            this.source = source;
            this.type = type;
        }
    }

    public VASTVideo(Context context, int accountID, int zoneID, int publisherID, String orientation, VASTListener listener){
        this.context = context;
        this.zoneID = zoneID;
        this.accountID = accountID;
        this.publisherID = publisherID;
        this.orientation = orientation;
        setListenerInstance(listener);
    }

    /**
     * If there is meant to be a source video (not just an ad) you can add it with this method.
     * @param source The source url.
     * @param type The MIME type of the video, e.g. "video/mp4"
     */
    public void addSoure(String source, String type){
        sources.add(new Source(source, type));
    }

    public void setDefaultPoster(String posterUrl){
        poster = posterUrl;
    }


//    public void play(){
//        Intent intent = new Intent(context, VideoPlayer.class);
//        intent.putExtra("BODY", getVideoJSMarkup());
//        context.startActivity(intent);
//        switch(this.orientation){
//            case "none":
//                ((Activity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
//                break;
//            case "portrait":
//                ((Activity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                break;
//            case "landscape":
//                ((Activity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//                break;
//        }
//    }


    /**
     * Preload the VAST video ad.  onReady will be called on the VASTListener when the ad is ready to display
     * Once the ad is ready, you may call display() to show it.
     */
    public void preload(){
        ready = false;
        displayed = false;
        startTimer();
        if(webViewInstance != null){
            webViewInstance.destroy();
        }
        webViewInstance = new VideoEnabledWebView(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webViewInstance.setWebContentsDebuggingEnabled(true);
        }
        webViewInstance.setWebViewClient(new InsideWebViewClient());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.width = 0;
        params.height = 0;
        ((Activity)context).addContentView(webViewInstance, params);

        nonVideoLayout = new RelativeLayout(context);
        ((Activity)context).addContentView(nonVideoLayout, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

        videoLayout = new RelativeLayout(context);
        ((Activity)context).addContentView(videoLayout, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

        loadingView = ((Activity)context).getLayoutInflater().inflate(R.layout.view_loading_video, null);
        VideoEnabledWebChromeClient webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, loadingView, webViewInstance) // See all available constructors...
        {
            // Subscribe to standard events, such as onProgressChanged()...
            @Override
            public void onProgressChanged(WebView view, int progress)
            {

            }
        };
        webViewInstance.setWebChromeClient(webChromeClient);

        webViewInstance.loadDataWithBaseURL("http://servedbyadbutler.com", getVideoJSMarkup(), "text/html; charset=utf-8", "UTF-8", "");
    }

    public void startTimer(){

        TimerTask task = new TimerTask() {
            public void run() {
                ((Activity)context).runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        if(!ready && !displayed){
                            cleanUpViews();
                            listenerInstance.onError();
                        }
                    }
                });
            }
        };
        Timer timer = new Timer("Timeout");

        long delay = 5000L;
        timer.schedule(task, delay);
    }

    private void cleanUpViews(){
        nonVideoLayout.removeAllViews();
        ((ViewGroup)nonVideoLayout.getParent()).removeView(nonVideoLayout);
        nonVideoLayout = null;

        videoLayout.removeAllViews();
        ((ViewGroup)videoLayout.getParent()).removeView(videoLayout);
        videoLayout = null;

        loadingView = null;

        ((ViewGroup)webViewInstance.getParent()).removeView(webViewInstance);
    }

    /**
     * Show the VAST ad. (Call when ready == true)
     */
    public void display(){
        if(!this.ready){
            return;
        }

        ready = false; //TODO should we let it play twice?
        displayed = true;
        // dispose of temporary preloading views
        cleanUpViews();

        // VideoPlayer will get the webview instance, and add it to iself.
        Intent intent = new Intent(context, VideoPlayer.class);
        intent.putExtra("PRELOADED", true);
        intent.putExtra("CLOSEBUTTONREQUIRED", closeButtonRequired);
        context.startActivity(intent);
        switch(this.orientation){
            case "none":
                ((Activity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                break;
            case "portrait":
                ((Activity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case "landscape":
                ((Activity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }
    }

    private class InsideWebViewClient extends WebViewClient {
        @Override
        // Force links to be opened inside WebView and not in Default Browser
        // Thanks http://stackoverflow.com/a/33681975/1815624

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            System.out.println(url);
            if (url.contains("vast://vastresponse?xml=")) {
                String raw = url.replace("vast://vastresponse?xml=", "");
                try {
                    String xml = URLDecoder.decode(raw, "UTF-8");
                    parseVASTContent(xml);
                } catch (UnsupportedEncodingException ex) {
                    Log.e("\"Ads/AdButler\"", "Unsupported encoding on VAST XML");
                }
                return false;
            } else if (url.contains("vast://")) {
                handleEvent(url);
                return false;
            } else {
                System.out.println("URL REACHED =====  " + url);
                view.loadUrl(url);
            }
            return true;
        }
    }


    private void constructEndCard(Node node){
        endCard = new VASTCompanion();
        NodeList children = node.getChildNodes();
        for(int i = 0; i < children.getLength(); i++){
            Node n = children.item(i);
            switch(n.getNodeName()){
                case "StaticResource":
                    endCard.staticResource = n.getFirstChild().getNodeValue();
                    endCard.staticResource = endCard.staticResource.replace("http://", "https://");
                    break;
                case "HTMLResource":
                    endCard.htmlResource = n.getFirstChild().getNodeValue();
                    break;
                case "TrackingEvents":
                    NodeList events = n.getChildNodes();
                    for(int c = 0; c < events.getLength(); c++){
                        endCard.trackingEvents.put(events.item(c).getAttributes().getNamedItem("event").getNodeValue(), events.item(c).getFirstChild().getNodeValue());
                    }
                    break;
                case "CompanionClickThrough":
                    endCard.clickThrough = n.getFirstChild().getNodeValue();
                    break;
                default:break;
            }
        }
    }

    private Node findBestCompanion(NodeList list){
        android.graphics.Rect rect = new Rect();

        webViewInstance.getWindowVisibleDisplayFrame(rect);

        Node curBest = null;
        float aspectRatio = (float)MRAIDUtilities.convertPixelsToDp(rect.width(), context) / (float)MRAIDUtilities.convertPixelsToDp(rect.height(), context);
        float closestRatio = 0f;
        for(int i=0; i < list.getLength(); i++){
            NamedNodeMap map = list.item(i).getAttributes();
            int w = Integer.parseInt(map.getNamedItem("width").getNodeValue());
            int h = Integer.parseInt(map.getNamedItem("height").getNodeValue());
            float r = (float)w / (float)h;
            if(i == 0 || Math.abs(aspectRatio - r) < closestRatio){
                closestRatio = r;
                curBest = list.item(i);
            }
        }
        return curBest;
    }

    private void parseVASTContent(String str){
        final String body = str;
        Node bestFit = null;
        // deserialize
        try{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(str)));
            NodeList nodes = doc.getElementsByTagName("Companion");
            if(nodes.getLength() > 0){
                bestFit = findBestCompanion(nodes);
            }

            nodes = doc.getElementsByTagName("Linear");
            if(nodes.getLength() > 0){
                Node offset = nodes.item(0).getAttributes().getNamedItem("skipoffset");
                if(offset == null){
                    closeButtonRequired = true;
                }
            }
        }catch(Exception ex){
            System.out.println("AB - Problem finding end card companion.");
        }

        if(bestFit != null) {
            constructEndCard(bestFit);
//            for testing, immediately add end card
//            runOnUiThread(new Runnable(){
//                @Override
//                public void run(){
//                    displayEndCard();
//                }
//            });
        }
    }

    private String getVideoJSMarkup(){
        StringBuilder str = new StringBuilder();
        str.append("<html>");
        str.append("<head>");
        str.append("<meta name=\"viewport\" content=\"initial-scale=1.0\" />");
        str.append("<link href=\"http://vjs.zencdn.net/4.12/video-js.css\" rel=\"stylesheet\">");
        str.append("<script src=\"http://vjs.zencdn.net/4.12/video.js\"></script>");
        str.append("<link href=\"http://servedbyadbutler.com/videojs-vast-vpaid/bin/videojs.vast.vpaid.min.css\" rel=\"stylesheet\">");
        str.append("<script src=\"http://servedbyadbutler.com/videojs-vast-vpaid/bin/videojs_4.vast.vpaid.js?v=12\"></script>");
        str.append("</head>");
        str.append("<body style=\"margin:0px; background-color:black\">");
        str.append("<video id=\"av_video\" class=\"video-js vjs-default-skin\" playsinline=\"true\" autoplay muted ");
        str.append("controls preload=\"auto\" width=\"100%\" height=\"100%\" ");
        if(this.poster != null){
            str.append(String.format("poster=\"%s\" ", this.poster));
        }
        str.append("data-setup='{ ");
        str.append("\"plugins\": { ");
        str.append("\"vastClient\": { ");
        str.append(String.format("\"adTagUrl\": \"http://servedbyadbutler.com/vast.spark?setID=%d&ID=%d&pid=%d\", ", this.zoneID, this.accountID, this.publisherID));
        str.append("\"adCancelTimeout\": 5000, ");
        str.append("\"adsEnabled\": true ");
        str.append("} ");
        str.append("} ");
        str.append("}'> ");
        if(this.sources != null){
            for(Source s : this.sources) {
                str.append(String.format("<source src=\"%s\" type='%s'/>", s.source, s.type));
            }
        }else{
            str.append("<source src=\"http://servedbyadbutler.com/assets/blank.mp4\" type='video/mp4'/>");
        }
        str.append("<p class=\"vjs-no-js\">");
        str.append("To view this video please enable JavaScript, and consider upgrading to a web browser that");
        str.append("<a href=\"http://videojs.com/html5-video-support/\" target=\"_blank\">supports HTML5 video</a>");
        str.append("</p>");
        str.append("</video>");
        str.append("</body>");
        str.append("</html>");

        return str.toString();
    }

    private void handleEvent(String url) {
        String event = url.replaceFirst("vast://", "");
        switch (event) {
            case "mute":
                listenerInstance.onMute();
                break;
            case "unmute":
                listenerInstance.onUnmute();
                break;
            case "pause":
                listenerInstance.onPause();
                break;
            case "resume":
                listenerInstance.onResume();
                break;
            case "rewind":
                listenerInstance.onRewind();
                break;
            case "skip":
                listenerInstance.onSkip();
                break;
            case "playerExpand":
                listenerInstance.onPlayerExpand();
                break;
            case "playerCollapse":
                listenerInstance.onPlayerCollapse();
                break;
            case "notUsed":
                listenerInstance.onNotUsed();
                break;
            case "loaded":
                listenerInstance.onLoaded();
                break;
            case "start":
                listenerInstance.onStart();
                break;
            case "firstQuartile":
                listenerInstance.onFirstQuartile();
                break;
            case "midpoint":
                listenerInstance.onMidpoint();
                break;
            case "thirdQuartile":
                listenerInstance.onThirdQuartile();
                break;
            case "complete":
                listenerInstance.onComplete();
                break;
            case "closeLinear":
                listenerInstance.onCloseLinear();
                break;
            case "close":
                listenerInstance.onClose();
                break;
            case "ready":
                this.ready = true;
                listenerInstance.onReady();
                break;
        }
    }
}
