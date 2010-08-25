package com.gamaray.arex;

import com.gamaray.arex.context.ARXContext;
import com.gamaray.arex.geo.GeoPoint;
import com.gamaray.arex.loader.DownloadJobRequest;
import com.gamaray.arex.loader.DownloadJobResult;

public class ARXState {
    public final static int NOT_STARTED = 0; // No result ready, job not submitted
    public final static int JOB_SUBMITTED = 1; // Job submitted
    public final static int TRANSITION = 2; // Job complete, ready to switch to new
                                      // result
    public final static int READY = 3; // Transition complete, new result ready to use
    public final static int ERROR = 4; // Transition complete, new result ready to use

    private int retryCount = 0;

    // Download properties
    private int nextLayerStatus = ARXState.NOT_STARTED;
    private String downloadJobId;
    private DownloadJobResult downloadResult;

    private GeoPoint curFix = new GeoPoint();
    private float curBearing, curPitch;
    private float screenWidth, screenHeight;
    private String uid;

    private boolean launchNeeded = false;
    private String launchUrl = "";

    private boolean radarVisible = true, messagesVisible = true;

    private Layer layer = new Layer();

    boolean handleEvent(ARXContext ctx, String xmlId, String xmlOnPress) {
        boolean evtHandled = false;

        if (xmlOnPress.startsWith("refresh")) {
            DownloadJobRequest request = new DownloadJobRequest(ARXDownload.GAMADIM,layer.xmlRefreshUrl,getParams("refreshOnPress", xmlId),false);
            downloadJobId = ctx.getARXDownload().submitJob(request);
            nextLayerStatus = ARXState.JOB_SUBMITTED;
            evtHandled = true;
        } else if (xmlOnPress.startsWith("webpage")) {
            try {
                // TODO: find a way to pass lat,lon,alt,etc. to web page
                String webpage = ARXUtil.parseAction(xmlOnPress);
                ctx.showWebPage(webpage);
            } catch (Exception ex) {
                ARXMessages.putMessage("ERROR_WEBPAGE", "Error showing webpage: " + ex, ARXMessages.errorIcon, 3000);
            }

            evtHandled = true;
        } else if (xmlOnPress.startsWith("dimension")) {
            DownloadJobRequest request = new DownloadJobRequest(ARXDownload.GAMADIM, ARXUtil.parseAction(xmlOnPress), getParams("init", "NULL"), false);
            downloadJobId = ctx.getARXDownload().submitJob(request);

            launchUrl = request.getUrl();

            nextLayerStatus = ARXState.JOB_SUBMITTED;
            evtHandled = true;
        }

        return evtHandled;
    }

    public String getParams(String event, String eventSrc) {
        return "event=" + event + "&eventSrc=" + eventSrc + "&lat=" + curFix.lat + "&lon=" + curFix.lon + "&alt=" +
                curFix.alt + "&bearing=" + (int) curBearing + "&pitch=" + (int) curPitch + "&width=" +
                (int) screenWidth + "&height=" + (int) screenHeight + "&uid=" + uid + "&time=" +
                System.currentTimeMillis();
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getNextLayerStatus() {
        return nextLayerStatus;
    }

    public void setNextLayerStatus(int nextLayerStatus) {
        this.nextLayerStatus = nextLayerStatus;
    }

    public String getDownloadJobId() {
        return downloadJobId;
    }

    public void setDownloadJobId(String downloadJobId) {
        this.downloadJobId = downloadJobId;
    }

    public DownloadJobResult getDownloadResult() {
        return downloadResult;
    }

    public void setDownloadResult(DownloadJobResult downloadResult) {
        this.downloadResult = downloadResult;
    }

    public GeoPoint getCurFix() {
        return curFix;
    }

    public void setCurFix(GeoPoint curFix) {
        this.curFix = curFix;
    }

    public float getCurBearing() {
        return curBearing;
    }

    public void setCurBearing(float curBearing) {
        this.curBearing = curBearing;
    }

    public float getCurPitch() {
        return curPitch;
    }

    public void setCurPitch(float curPitch) {
        this.curPitch = curPitch;
    }

    public float getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(float screenWidth) {
        this.screenWidth = screenWidth;
    }

    public float getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(float screenHeight) {
        this.screenHeight = screenHeight;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isLaunchNeeded() {
        return launchNeeded;
    }

    public void setLaunchNeeded(boolean launchNeeded) {
        this.launchNeeded = launchNeeded;
    }

    public String getLaunchUrl() {
        return launchUrl;
    }

    public void setLaunchUrl(String launchUrl) {
        this.launchUrl = launchUrl;
    }

    public boolean isRadarVisible() {
        return radarVisible;
    }

    public void setRadarVisible(boolean radarVisible) {
        this.radarVisible = radarVisible;
    }

    public boolean isMessagesVisible() {
        return messagesVisible;
    }

    public void setMessagesVisible(boolean messagesVisible) {
        this.messagesVisible = messagesVisible;
    }

    public Layer getLayer() {
        return layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }
    
    
    
}
