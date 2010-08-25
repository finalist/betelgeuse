package com.gamaray.arex;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.gamaray.arex.context.ARXContext;
import com.gamaray.arex.format3d.SimpleVFS;
import com.gamaray.arex.gui.Bitmap;
import com.gamaray.arex.gui.DrawWindow;
import com.gamaray.arex.gui.Drawable;
import com.gamaray.arex.gui.GUIUtil;
import com.gamaray.arex.gui.MenuItem;
import com.gamaray.arex.gui.Vector2D;
import com.gamaray.arex.loader.DownloadJobRequest;
import com.gamaray.arex.render3d.Camera;
import com.gamaray.arex.render3d.Color;
import com.gamaray.arex.render3d.Matrix3D;
import com.gamaray.arex.render3d.Vector3D;

public class ARXView {
    ARXContext ctx;

    boolean init = false;
    int width, height;
    Camera cam;
    private ARXState state = new ARXState();

    public static String VERSION = "1.0.10";
    // String LOCAL_SERVER = "localhost:8080";
    // String CLOUD_SERVER = "dimensions.gamaray.com";
    // String SERVER = CLOUD_SERVER;
    // String REDIRECT_URL = "http://" + SERVER + "/Dimensions/Redirect";
    private static String HOME_URL = "gamaray://betelgeusedesigner.appspot.com/gamaray/43001.gddf";

    // ArrayList icons = new ArrayList();
    private List<Event> events = new ArrayList<Event>();

    public int NORTH = 0, EAST = 90, SOUTH = 180, WEST = 270;
    public int NO_DIALOG = 0, RADAR_DIALOG = 1, INFO_DIALOG = 2;
    int dialogState = NO_DIALOG;
    RadarDialog radarDialog;

    RadarObjects radarObjects = new RadarObjects();
    Matrix3D rotationInv = new Matrix3D();
    Vector3D looking = new Vector3D();
    Vector2D leftRadarLine = new Vector2D();
    Vector2D rightRadarLine = new Vector2D();
    float radarX = 10, radarY = 20;

    MenuItem miShowInfo = new MenuItem(0, "Show Info");
    MenuItem miHideInfo = new MenuItem(1, "Hide Info");
    MenuItem miShowRadar = new MenuItem(2, "Show Radar");
    MenuItem miHideRadar = new MenuItem(3, "Hide Radar");
    MenuItem miShowDistances = new MenuItem(4, "Show Distances");
    MenuItem miHideDistances = new MenuItem(5, "Hide Distances");
    MenuItem miShowMessages = new MenuItem(6, "Show Messages");
    MenuItem miHideMessages = new MenuItem(7, "Hide Messages");
    MenuItem miRefresh = new MenuItem(8, "Reload");
    MenuItem miShowMap = new MenuItem(9, "Show Map");
    MenuItem miHideMap = new MenuItem(10, "Hide Map");
    
    public ARXView(ARXContext ctx) {
        this.ctx = ctx;
    }

    public void doLaunch() {
        state.setLaunchNeeded(true);
    }

    public boolean isInit() {
        return init;
    }

    public void init(int widthInit, int heightInit) {
        try {
            radarDialog = new RadarDialog(ctx);

            width = widthInit;
            height = heightInit;

            cam = new Camera(width, height, true);
            cam.setViewAngle(Camera.DEFAULT_VIEW_ANGLE);

            ARXMessages.loadIcons(ctx);

            Map<String, Object> prefs = ctx.getPrefs();
            if (!prefs.containsKey("UID")) {
                prefs.put("UID", "UID" + Math.abs(ctx.getRandomLong()));

                ctx.setPrefs(prefs);
            }
            state.setUid((String) prefs.get("UID"));

            leftRadarLine.setTo(0, -RadarObjects.RADIUS);
            leftRadarLine.rotate(Camera.DEFAULT_VIEW_ANGLE / 2);
            leftRadarLine.add(radarX + RadarObjects.RADIUS, radarY + RadarObjects.RADIUS);
            rightRadarLine.setTo(0, -RadarObjects.RADIUS);
            rightRadarLine.rotate(-Camera.DEFAULT_VIEW_ANGLE / 2);
            rightRadarLine.add(radarX + RadarObjects.RADIUS, radarY + RadarObjects.RADIUS);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        init = true;
    }

    public void draw(DrawWindow dw) {
        // Check if GPS enabled
        if (!ctx.gpsEnabled()) {
            ARXMessages.putMessage("GPS_ENABLED", "GPS not Enabled (see Settings->Security & location->Enable GPS)",
                    ARXMessages.errorIcon, 500);
        } else if (!ctx.gpsSignalReceived()) {
            // Check if GPS signal received
            ARXMessages.putMessage("GPS_WAIT", "Waiting for GPS signal", ARXMessages.worldIcon, 500);
        }

        // Check for compass accuracy
        if (ctx.isCompassAccuracyLow()) {
            ARXMessages.putMessage("COMPASS_ACCURACY", "Compass accuracy low", ARXMessages.compassIcon, 1000);
        }

//        if (ctx.gpsEnabled()) {
            drawDimension(dw);
//        } else {
//            purgeEvents();
//        }

        ARXMessages.expireMessages();
        int msgCount = ARXMessages.getMessageCount();
        float msgHeight = 0, msgWidth = 0;
        float msgPad = 10;
        int startMsg = 0, maxMsg = 1;

        if (state.isMessagesVisible() && msgCount > maxMsg) {
            startMsg = msgCount - maxMsg;
        }

        for (int i = startMsg; i < msgCount; i++) {
            Message msg = (Message) ARXMessages.getMessage(i);
            if (!msg.init)
                msg.init(dw, width - msgPad * 2);

            if (state.isMessagesVisible()) {
                msg.showFullMessage(true);
                msgHeight += msg.getHeight() + 1;
                dw.drawObject(msg, msgPad, height - msgHeight - msgPad, 0, 1);
            } else {
                if (msg.isImportant()) {
                    msgHeight = msg.getHeight();
                    msg.showFullMessage(false);
                    dw.drawObject(msg, msgPad + msgWidth, height - msgHeight - msgPad, 0, 1);
                    msgWidth += msg.getWidth() + 1;
                }
            }
        }
    }

    public void drawDimension(DrawWindow dw) {
        cam.transform.setTo(ctx.getRotationMatrix());
        state.setCurFix(ctx.getCurrentLocation());

        // Determine bearing
        // TODO: fix math
        rotationInv.setTo(cam.transform);
        rotationInv.transpose();
        looking.setTo(1, 0, 0);
        looking.transform(rotationInv);
        state.setCurBearing(ARXUtil.getAngle(0, 0, looking.x, looking.z));
        state.setCurBearing(((int) state.getCurBearing() + 360) % 360);

        // Determine pitch
        // TODO: fix math
        rotationInv.transpose();
        looking.setTo(0, 1, 0);
        looking.transform(rotationInv);
        state.setCurPitch( -ARXUtil.getAngle(0, 0, looking.y, looking.z));

        state.setScreenWidth(width);
        state.setScreenHeight(height);
        

        if (ctx.getLaunchUrl().equals("") || ctx.getLaunchUrl().equals(state.getLaunchUrl())) {
            state.setLaunchNeeded(false);
        }

        // Load Layer
        if (state.getNextLayerStatus() == ARXState.NOT_STARTED ||
                (state.getNextLayerStatus() == ARXState.READY && state.isLaunchNeeded())) {
            /*
             * DownloadJobRequest request = new DownloadJobRequest();
             * request.format = ARXDownload.GAMADIM; request.url = REDIRECT_URL;
             * request.cacheable = false;
             * 
             * state.launchNeeded = false; state.launchUrl = ctx.getLaunchUrl();
             * if (!state.launchUrl.equals("")) request.params =
             * state.getParams("init","NULL") + "&url=" + state.launchUrl; else
             * request.params = state.getParams("init","NULL");
             * 
             * state.downloadJobId = ctx.getARXDownload().submitJob(request);
             * 
             * state.nextLayerStatus = ARXState.JOB_SUBMITTED;
             */

            String requestUrl;
            if (!ctx.getLaunchUrl().equals("")) {
                requestUrl = ctx.getLaunchUrl();
            } else {
                requestUrl = HOME_URL;
            }
            DownloadJobRequest request = new DownloadJobRequest(ARXDownload.GAMADIM, requestUrl, state.getParams(
                    "init", "NULL"), false);

            state.setLaunchNeeded(false);
            state.setLaunchUrl(ctx.getLaunchUrl());

            state.setDownloadJobId(ctx.getARXDownload().submitJob(request));

            state.setNextLayerStatus(ARXState.JOB_SUBMITTED);
        } 
        if (state.getNextLayerStatus() == ARXState.JOB_SUBMITTED) {
            ARXMessages.putMessage("LOADING_" + state.getDownloadJobId(), "Dimension loading", ARXMessages.refreshIcon, 500);

            if (ctx.getARXDownload().isJobComplete(state.getDownloadJobId())) {
                state.setDownloadResult(ctx.getARXDownload().getJobResult(state.getDownloadJobId()));

                state.setNextLayerStatus(ARXState.TRANSITION);
            }
        } 
        if (state.getNextLayerStatus() == ARXState.TRANSITION) {
            ctx.getARXDownload().pause();
            ctx.getARXRender().pause();

            if (ctx.getARXDownload().getState() == ARXDownload.PAUSED &&
                    ctx.getARXRender().getState() == ARXRender.PAUSED) {
                if (state.getDownloadResult().isError()) {
                    if (state.getRetryCount() < 10) {
                        ARXMessages.removeMessage("LOADING_" + state.getDownloadJobId());
                        ARXMessages.removeMessage("ERROR_" + state.getDownloadJobId());

                        DownloadJobRequest request = new DownloadJobRequest(ARXDownload.GAMADIM, state.getDownloadResult()
                                .getErrorRequest().getUrl(), state.getDownloadResult().getErrorRequest().getParams(),
                                state.getDownloadResult().getErrorRequest().isCacheable());
                        state.setDownloadJobId(ctx.getARXDownload().submitJob(request));

                        state.setNextLayerStatus(ARXState.JOB_SUBMITTED);
                        state.setRetryCount(state.getRetryCount()+1);

                        ARXMessages.putMessage("ERROR_" + state.getDownloadJobId(),
                                "Error: " + state.getDownloadResult().getErrorMsg(), ARXMessages.warningIcon,
                                Integer.MAX_VALUE);

                        ARXMessages.putMessage("LOADING_" + state.getDownloadJobId(), "Dimension loading (retry #" +
                                state.getRetryCount() + ")", ARXMessages.refreshIcon, Integer.MAX_VALUE);
                    } else {
                        state.setNextLayerStatus(ARXState.READY);
                        state.setLayer(new Layer());

                        ARXMessages.removeMessage("LOADING_" + state.getDownloadJobId());

                        ARXMessages.putMessage("FATAL_ERROR_" + state.getDownloadJobId(), "Unable to load dimension: " +
                                state.getDownloadResult().getErrorRequest().getUrl(), ARXMessages.errorIcon,
                                Integer.MAX_VALUE);
                    }
                } else {
                    // Layer oldLayer = state.layer;
                    Layer newLayer = (Layer) state.getDownloadResult().getObj();

                    ARXMessages.removeMessage("ERROR_" + state.getDownloadJobId());

                    ARXMessages.removeMessage("LOADING_" + state.getDownloadJobId());

                    // ARXMessages.putMessage("LOADING_COMPLETE_" +
                    // state.downloadJobId,
                    // "Dimension '" + newLayer.xmlName + "' loaded",
                    // ARXMessages.refreshCompleteIcon, 3000);

                    // Clean up ARXRender and ARXDownload
                    ctx.getARXDownload().clearLists();
                    ctx.getARXRender().clearLists();

                    // Transfer valid renders

                    for (Placemark pm : state.getLayer().getZOrderedPlacemarks()) {
                        if (pm instanceof Placemark3D) {
                            Placemark3D src = (Placemark3D) pm;
                            Placemark3D dest = (Placemark3D) newLayer.getPlacemark(src.xmlId);
                            if (dest != null) {
                                src.copyRenderState(dest);
                            }
                        }

                    }

                    // Transfer valid downloads
                    for (Asset das : state.getLayer().getAssets()) {
                        Asset dest = (Asset) newLayer.getAsset(das.xmlId);
                        if (dest != null) {
                            das.copyDownloadState(dest);
                        }
                    }

                    // Set layer creation time and location
                    newLayer.creationTime = System.currentTimeMillis();
                    newLayer.creationLoc.setTo(state.getCurFix());

                    // Show radar if available
                    if (newLayer.xmlRadarAvailable)
                        state.setRadarVisible(true);
                    if (newLayer.xmlHasRadarRange)
                        radarDialog.setRange(newLayer.xmlRadarRange);

                    state.setLayer(newLayer);
                    state.setNextLayerStatus(ARXState.READY);
                    state.setRetryCount(0);
                }

                ctx.getARXDownload().restart();
                ctx.getARXRender().restart();
            }
        }

        for (Placemark pm : state.getLayer().getZOrderedPlacemarks()) {
            // Update placemarks
            pm.updateState(state.getCurFix(), System.currentTimeMillis());

            // Prepare placemarks for draw
            pm.prepareDraw(cam);
        }


        // Draw placemarks
        for (Placemark pm : state.getLayer().getZOrderedPlacemarks()) {
            pm.draw(dw);
        }

        // Update asset download status
        for (Asset das : state.getLayer().getAssets()) {

            das.isDownloadJobActive = false;
            if (das.downloadStatus == ARXState.JOB_SUBMITTED) {
                String activeJobId = ctx.getARXDownload().getActiveJobId();

                if (das.downloadJobId.equals(activeJobId)) {
                    das.isDownloadJobActive = true;
                    das.downloadJobPctComplete = ctx.getARXDownload().getActiveJobPctComplete();

                    String pctTxt = ARXUtil.formatDecimal(das.downloadJobPctComplete * 100, 2);
                    ARXMessages.putMessage("DOWNLOAD_ASSET", "Downloading '" + das.xmlId + "' (" + pctTxt + "%)", "[" +
                            pctTxt + "%]", ARXMessages.downloadIcon, 500, true);
                }
            }
        }

        if (state.getNextLayerStatus() == ARXState.READY) {
            // Update renders
            for (Placemark pm : state.getLayer().getZOrderedPlacemarks()) {

                if (pm instanceof Placemark3D) {
                    ((Placemark3D) pm).updateRenders(cam, ctx);
                }
            }

            // Handle show actions
            for (Overlay ovl : state.getLayer().getOverlays()) {
                ovl.setShow(false);
            }

            float minDist = Float.MAX_VALUE;
            Placemark closestPM = null;
            for (Placemark pm : state.getLayer().getZOrderedPlacemarks()) {

                if (minDist > pm.distToCenter && pm.distToCenter < 50 && pm.xmlShowOnFocus != null) {
                    minDist = pm.distToCenter;
                    closestPM = pm;
                }

            }
            if (closestPM != null) {
                String ovlIds[] = closestPM.xmlShowOnFocus.split(",");

                for (int i = 0; i < ovlIds.length; i++) {
                    Overlay ovl = (Overlay) state.getLayer().overlayMap.get(ovlIds[i].trim());
                    if (ovl != null)
                        ovl.setShow(true);
                }
            }

            // Draw Overlays
            for (Overlay ovl : state.getLayer().getOverlays()) {

                if (ovl.isVisible())
                    ovl.draw(dw);
            }

            // Request asset downloads
            boolean allAssetsDownloaded = true;
            for (Asset das : state.getLayer().getAssets()) {

                das.isDownloadJobActive = false;
                if (das.downloadStatus == ARXState.NOT_STARTED) {
                    DownloadJobRequest request = new DownloadJobRequest(das.xmlFormat, das.xmlUrl, null, true);
                    das.downloadJobId = ctx.getARXDownload().submitJob(request);

                    das.downloadStatus = ARXState.JOB_SUBMITTED;
                    allAssetsDownloaded = false;
                } else if (das.downloadStatus == ARXState.JOB_SUBMITTED) {
                    if (ctx.getARXDownload().isJobComplete(das.downloadJobId)) {
                        das.downloadResult = ctx.getARXDownload().getJobResult(das.downloadJobId);
                        das.downloadStatus = ARXState.READY;

                        if (!das.downloadResult.isError()) {
                            ARXMessages.putMessage("DOWNLOAD_ASSET", "Download of '" + das.xmlId + "' complete", "",
                                    ARXMessages.downloadCompleteIcon, 500, true);
                        } else {
                            ARXMessages.putMessage("DOWNLOAD_" + das.downloadJobId, "Error downloading '" + das.xmlId +
                                    "'", "", ARXMessages.downloadErrorIcon, 10000, true);
                        }
                    } else {
                        String activeJobId = ctx.getARXDownload().getActiveJobId();

                        if (das.downloadJobId.equals(activeJobId)) {
                            das.isDownloadJobActive = true;
                            das.downloadJobPctComplete = ctx.getARXDownload().getActiveJobPctComplete();

                            String pctTxt = ARXUtil.formatDecimal(das.downloadJobPctComplete * 100, 2);
                            ARXMessages.putMessage(
                                    "DOWNLOAD_ASSET",
                                    "Downloading '" + das.xmlId + "' (" +
                                            ARXUtil.formatDecimal(das.downloadJobPctComplete * 100, 2) + "%)", "[" +
                                            pctTxt + "%]", ARXMessages.downloadIcon, 500, true);
                        }
                    }

                    allAssetsDownloaded = false;
                }
            }

            // Set assetDownloadTime
            if (allAssetsDownloaded && !state.getLayer().allAssetsDownloaded) {
                state.getLayer().allAssetsDownloaded = true;
                state.getLayer().assetDownloadTime = System.currentTimeMillis();

                ARXMessages.putMessage("ASSETS_LOADING_COMPLETE", "Dimension '" + state.getLayer().xmlName + "' loaded", "",
                        ARXMessages.refreshCompleteIcon, 3000, true);
            }

            if (!allAssetsDownloaded) {
                ARXMessages.putMessage("ASSETS_LOADING", "Dimension '" + state.getLayer().xmlName + "' loading",
                        ARXMessages.refreshIcon, 500);
            }
        }

        // Draw Radar
        if (state.isRadarVisible() && state.getLayer().xmlRadarAvailable) {
            String dirTxt = "";
            int bearing = (int) state.getCurBearing();
            int range = (int) (state.getCurBearing() / (360f / 16f));
            if (range == 15 || range == 0)
                dirTxt = "N";
            else if (range == 1 || range == 2)
                dirTxt = "NE";
            else if (range == 3 || range == 4)
                dirTxt = "E";
            else if (range == 5 || range == 6)
                dirTxt = "SE";
            else if (range == 7 || range == 8)
                dirTxt = "S";
            else if (range == 9 || range == 10)
                dirTxt = "SW";
            else if (range == 11 || range == 12)
                dirTxt = "W";
            else if (range == 13 || range == 14)
                dirTxt = "NW";

            radarObjects.state = state;
            radarObjects.range = radarDialog.getRange();
            dw.drawObject(radarObjects, radarX, radarY, -state.getCurBearing(), 1);
            dw.setFill(false);
            dw.setColor(Color.argb(150, 0, 220, 0));
            dw.drawLine(leftRadarLine.x, leftRadarLine.y, radarX + RadarObjects.RADIUS, radarY + RadarObjects.RADIUS);
            dw.drawLine(rightRadarLine.x, rightRadarLine.y, radarX + RadarObjects.RADIUS, radarY + RadarObjects.RADIUS);
            dw.setColor(Color.rgb(255, 255, 255));
            dw.setFontSize(12);
            drawRadarTextLine(dw, ARXUtil.formatDistance(radarDialog.getRange()), radarX + RadarObjects.RADIUS, radarY +
                    RadarObjects.RADIUS * 2 + 5);
            drawRadarTextLine(dw, "" + bearing + ((char) 176) + " " + dirTxt, radarX + RadarObjects.RADIUS, radarY - 5);
        }

        // Check for time and distance refresh
        if (state.getNextLayerStatus() == ARXState.READY) {
            String evtName = null;
            if (state.getLayer().refreshOnDistance(state.getCurFix()))
                evtName = "refreshOnDistance";
            if (state.getLayer().refreshOnTime(System.currentTimeMillis()))
                evtName = "refreshOnTime";

            if (evtName != null) {
                DownloadJobRequest request = new DownloadJobRequest(ARXDownload.GAMADIM, state.getLayer().xmlRefreshUrl,
                        state.getParams(evtName, "NULL"), false);
                state.setDownloadJobId(ctx.getARXDownload().submitJob(request));

                state.setNextLayerStatus(ARXState.JOB_SUBMITTED);
            }
        }

        // Get next event
        Event evt = null;
        synchronized (events) {
            if (events.size() > 0) {
                evt = (Event) events.get(0);
                events.remove(0);
            }
        }

        if (evt != null && evt.type == Event.MENU) {
            handleMenuEvent((MenuEvent) evt);
            evt = null;
        }

        if (dialogState == INFO_DIALOG) {
            dw.setFontSize(12);
            float infoPad = 50, infoInnerPad = 10;
            float infoLineHeight = dw.getTextAscent() + dw.getTextDescent();
            float infoCurY = infoPad + infoInnerPad + dw.getTextAscent(), infoCurX = infoPad + infoInnerPad;
            float infoX = infoPad, infoY = infoPad, infoW = width - infoPad * 2, infoH = infoLineHeight * 10 +
                    infoInnerPad * 2;

            dw.setColor(Color.rgb(0, 0, 0));
            dw.setFill(true);
            dw.drawRectangle(infoX, infoY, infoW, infoH);
            dw.setColor(Color.rgb(255, 255, 255));
            dw.setFill(false);
            dw.drawRectangle(infoX, infoY, infoW, infoH);

            String refreshDistanceTxt = ARXUtil.formatDistance((float) state.getLayer().calcRefreshDistance(state.getCurFix()));
            String validRangeTxt = (state.getLayer().xmlHasRefreshDistance) ? ARXUtil
                    .formatDistance(state.getLayer().xmlValidWithinRange) : "NA";

            String refreshTimeTxt = ((System.currentTimeMillis() - state.getLayer().creationTime) / 1000) + "s";
            String validForTxt = (state.getLayer().xmlHasRefreshTime) ? (state.getLayer().xmlValidFor / 1000) + "s" : "NA";

            dw.drawText(infoCurX, infoCurY, "Dimension: " + state.getLayer().xmlName);
            infoCurY += infoLineHeight * 2;
            dw.drawText(infoCurX, infoCurY, "Latitude: " + state.getCurFix().lat);
            infoCurY += infoLineHeight;
            dw.drawText(infoCurX, infoCurY, "Longitude: " + state.getCurFix().lon);
            infoCurY += infoLineHeight;
            dw.drawText(infoCurX, infoCurY, "Altitude: " + ARXUtil.formatDistance((float) state.getCurFix().alt));
            infoCurY += infoLineHeight;
            dw.drawText(infoCurX, infoCurY, "Bearing: " + state.getCurBearing() + ((char) 176));
            infoCurY += infoLineHeight;
            dw.drawText(infoCurX, infoCurY, "Declination: " + ctx.getDeclination() + ((char) 176));
            infoCurY += infoLineHeight;
            dw.drawText(infoCurX, infoCurY, "Refresh Distance: " + refreshDistanceTxt + "/" + validRangeTxt);
            infoCurY += infoLineHeight;
            dw.drawText(infoCurX, infoCurY, "Refresh Time: " + refreshTimeTxt + "/" + validForTxt);
            infoCurY += infoLineHeight;
            dw.drawText(infoCurX, infoCurY, "Version: " + VERSION);
            infoCurY += infoLineHeight;

            if (evt != null && evt.type == Event.KEY)
                dialogState = NO_DIALOG;
        } else if (dialogState == RADAR_DIALOG) {
            float radarDialogX = width / 2 - radarDialog.getWidth() / 2;
            float radarDialogY = height / 2 - radarDialog.getHeight() / 2;
            dw.drawObject(radarDialog, radarDialogX, radarDialogY, 0, 1);

            if (evt != null && evt.type == Event.CLICK && state.isRadarVisible() && state.getLayer().xmlRadarAvailable) {
                ClickEvent radarEvt = new ClickEvent(((ClickEvent) evt).x - radarDialogX, ((ClickEvent) evt).y -
                        radarDialogY);

                radarDialog.doAccept = false;
                radarDialog.handleEvent(radarEvt);
                if (radarDialog.doAccept)
                    dialogState = NO_DIALOG;
            }
        } else {
            if (evt != null && evt.type == Event.CLICK) {
                handleClickEvent((ClickEvent) evt);
            }
        }
    }

    boolean handleMenuEvent(MenuEvent evt) {
        boolean evtHandled = true;

        if (evt.id == miHideRadar.id) {
            state.setRadarVisible(false);
        } else if (evt.id == miShowRadar.id) {
            state.setRadarVisible(true);
        } else if (evt.id == miHideDistances.id) {
            state.getLayer().distancesVisible = false;
        } else if (evt.id == miShowDistances.id) {
            state.getLayer().distancesVisible = true;
        } else if (evt.id == miHideMessages.id) {
            state.setMessagesVisible(false);
        } else if (evt.id == miShowMessages.id) {
            state.setMessagesVisible(true);
        } else if (evt.id == miHideInfo.id) {
            dialogState = NO_DIALOG;
        } else if (evt.id == miShowInfo.id) {
            dialogState = INFO_DIALOG;
        } else if (evt.id == miRefresh.id) {
            if (state.getNextLayerStatus() == ARXState.READY) {
                ctx.getARXDownload().emptyCache();
                ARXMessages.clearAllMessages();

                /*
                 * DownloadJobRequest request = new DownloadJobRequest();
                 * request.format = ARXDownload.GAMADIM; request.url =
                 * REDIRECT_URL; request.cacheable = false;
                 * 
                 * if (!state.launchUrl.equals("")) request.params =
                 * state.getParams("init","NULL") + "&url=" + state.launchUrl;
                 * else request.params = state.getParams("init","NULL");
                 * 
                 * state.downloadJobId =
                 * ctx.getARXDownload().submitJob(request);
                 * 
                 * state.nextLayerStatus = ARXState.JOB_SUBMITTED;
                 */

                String requestUrl;
                if (!state.getLaunchUrl().equals("")) {
                    requestUrl = state.getLaunchUrl();
                } else {
                    requestUrl = HOME_URL;
                }

                DownloadJobRequest request = new DownloadJobRequest(ARXDownload.GAMADIM, requestUrl, state.getParams(
                        "init", "NULL"), false);

                state.setLaunchNeeded(false);
                state.setLaunchUrl(ctx.getLaunchUrl());

                state.setDownloadJobId(ctx.getARXDownload().submitJob(request));

                state.setNextLayerStatus(ARXState.JOB_SUBMITTED);
            }
//        } else if (evt.id == miHome.id) {
//            if (state.getNextLayerStatus() == ARXState.READY) {
//                ctx.getARXDownload().emptyCache();
//                ARXMessages.clearAllMessages();
//
//                /*
//                 * DownloadJobRequest request = new DownloadJobRequest();
//                 * request.format = ARXDownload.GAMADIM; request.url =
//                 * REDIRECT_URL; request.cacheable = false; request.params =
//                 * state.getParams("init","NULL");
//                 * 
//                 * state.launchUrl = ""; state.downloadJobId =
//                 * ctx.getARXDownload().submitJob(request);
//                 * 
//                 * state.nextLayerStatus = ARXState.JOB_SUBMITTED;
//                 */
//
//                DownloadJobRequest request = new DownloadJobRequest(ARXDownload.GAMADIM, HOME_URL, state.getParams(
//                        "init", "NULL"), false);
//
//                state.setLaunchUrl("");
//                state.setDownloadJobId(ctx.getARXDownload().submitJob(request));
//
//                state.setNextLayerStatus(ARXState.JOB_SUBMITTED);
//            }
        }

        return evtHandled;
    }

    boolean handleClickEvent(ClickEvent evt) {
        boolean evtHandled = false;

        if (state.isRadarVisible() && state.getLayer().xmlRadarAvailable) {
            if (evt.x > radarX && evt.x < radarX + radarObjects.getWidth() && evt.y > radarY &&
                    evt.y < radarY + radarObjects.getHeight()) {
                dialogState = RADAR_DIALOG;

                evtHandled = true;
            }
        }

        // Handle event
        if (state.getNextLayerStatus() == ARXState.READY) {
            // Loop through overlays
            Iterator<Overlay> overlayIt = state.getLayer().getOverlays().iterator();
            while (!evtHandled && overlayIt.hasNext()) {
                Overlay ovl = overlayIt.next();
                evtHandled = ovl.press(evt.x, evt.y, ctx, state);
            }

            // TODO:these should also be ordered according to distance.
            Iterator<Placemark> placemarkIt = state.getLayer().getZOrderedPlacemarks().iterator();
            while (!evtHandled && placemarkIt.hasNext()) {
                Placemark pm = placemarkIt.next();
                evtHandled = pm.press(evt.x, evt.y, ctx, state);
            }

        }

        return evtHandled;
    }

    void drawRadarTextLine(DrawWindow dw, String txt, float x, float y) {
        float padw = 4, padh = 2;
        float w = dw.getTextWidth(txt) + padw * 2;
        float h = dw.getTextAscent() + dw.getTextDescent() + padh * 2;
        dw.setColor(Color.rgb(0, 0, 0));
        dw.setFill(true);
        dw.drawRectangle(x - w / 2, y - h / 2, w, h);
        dw.setColor(Color.rgb(255, 255, 255));
        dw.setFill(false);
        dw.drawRectangle(x - w / 2, y - h / 2, w, h);
        dw.drawText(padw + x - w / 2, padh + dw.getTextAscent() + y - h / 2, txt);
    }

    public void destroy() {

    }

    public void createMenuEvent(List<MenuItem> menu) {
        if (!ctx.gpsEnabled())
            return;

        if (dialogState == INFO_DIALOG)
            menu.add(miHideInfo);
        else
            menu.add(miShowInfo);

        if (state.getLayer().xmlRadarAvailable) {
            // if (state.radarVisible)
            // menu.add(miHideRadar);
            // else
            // menu.add(miShowRadar);
        }

        if (state.getLayer().distancesVisible)
            menu.add(miHideDistances);
        else
            menu.add(miShowDistances);

        if (state.isMessagesVisible())
            menu.add(miHideMessages);
        else
            menu.add(miShowMessages);

        if (state.getNextLayerStatus() == ARXState.READY) {
            menu.add(miRefresh);
//            menu.add(miHome);
        }
    }

    public boolean isInfoBoxOpen() {
        return dialogState == INFO_DIALOG;
    }

    public void menuEvent(int id) {
        synchronized (events) {
            events.add(new MenuEvent(id));
        }
    }

    public void clickEvent(float x, float y) {
        synchronized (events) {
            events.add(new ClickEvent(x, y));
        }
    }

    public void keyEvent(int keyCode) {
        synchronized (events) {
            events.add(new KeyEvent(keyCode));
        }
    }

    public void purgeEvents() {
        synchronized (events) {
            events.clear();
        }
    }
}

class Event {
    public static int CLICK = 0, MENU = 1, KEY = 2;
    public int type;
}

class ClickEvent extends Event {
    public float x, y;

    public ClickEvent(float x, float y) {
        this.type = CLICK;
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "(" + x + "," + y + ")";
    }
}

class MenuEvent extends Event {
    public int id;

    public MenuEvent(int id) {
        this.type = MENU;
        this.id = id;
    }

    public String toString() {
        return "(" + id + ")";
    }
}

class KeyEvent extends Event {
    public int keyCode;

    public KeyEvent(int keyCode) {
        this.type = KEY;
        this.keyCode = keyCode;
    }

    public String toString() {
        return "(" + keyCode + ")";
    }
}

class RadarDialog implements Drawable {
    boolean doAccept = false;

    // float maxRange = 5000;
    // float range;

    float rangeShift = 2.0f;
    float sliderVal = 3f - rangeShift;
    float sliderRange = 3f;

    Bitmap acceptIcon;
    float width = 400, height = 50, pad = 10;
    float iconHeight, iconWidth, iconX, iconY;
    float sliderBarWidth, sliderBarHeight, sliderBarX, sliderBarY, sliderWidth, sliderX, sliderY;

    public RadarDialog(ARXContext ctx) {
        acceptIcon = GUIUtil.loadIcon("accept.png", ctx);
        iconHeight = acceptIcon.getHeight();
        iconWidth = acceptIcon.getWidth();
        sliderBarWidth = width - pad * 3 - iconWidth;
        sliderBarHeight = height - pad * 2;
        sliderBarX = pad;
        sliderBarY = pad;
        sliderWidth = 10;
        iconX = width - (pad + iconWidth);
        iconY = height / 2 - iconHeight / 2;
    }

    public float getRange() {
        return (float) Math.pow(10, sliderVal + rangeShift);
    }

    public void setRange(float newRange) {
        sliderVal = (float) Math.log10(newRange) - rangeShift;
    }

    public void draw(DrawWindow dw) {
        sliderX = pad + (sliderBarWidth - sliderWidth) * (sliderVal / sliderRange);
        sliderY = pad;

        dw.setFontSize(22);
        dw.setColor(Color.rgb(0, 0, 0));
        dw.setFill(true);
        dw.drawRectangle(0, 0, width, height);
        dw.setColor(Color.rgb(255, 255, 255));
        dw.setFill(false);
        dw.drawRectangle(0, 0, width, height);

        dw.setColor(Color.rgb(100, 100, 100));
        dw.setFill(true);
        dw.drawRectangle(sliderBarX, sliderBarY, sliderBarWidth, sliderBarHeight);
        dw.setColor(Color.rgb(255, 255, 255));
        dw.setFill(false);
        dw.drawRectangle(sliderBarX, sliderBarY, sliderBarWidth, sliderBarHeight);

        dw.setColor(Color.rgb(200, 200, 200));
        dw.setFill(true);
        dw.drawRectangle(sliderX, sliderY, sliderWidth, sliderBarHeight);
        dw.setColor(Color.rgb(255, 255, 255));
        dw.setFill(false);
        dw.drawRectangle(sliderX, sliderY, sliderWidth, sliderBarHeight);

        dw.drawBitmap(acceptIcon, iconX, iconY, 0, 1);
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public boolean handleEvent(ClickEvent event) {
        float ex = event.x;
        float ey = event.y;

        if (ex > iconX && ex < iconX + iconWidth && ey > iconY && ey < iconY + iconWidth) {
            // accept range
            doAccept = true;
        } else if (ex > sliderBarX - 2 && ex < sliderBarX + sliderBarWidth && ey > sliderBarY &&
                ey < sliderBarY + sliderBarHeight) {
            // move slider

            sliderVal = ((ex - sliderBarX) / (float) (sliderBarWidth - sliderWidth)) * sliderRange;

            if (sliderVal < 0)
                sliderVal = 0;
            if (sliderVal > sliderRange)
                sliderVal = sliderRange;
        }

        return true;
    }
}

class RadarObjects implements Drawable {
    ARXState state;
    float range;
    public static float RADIUS = 40;

    public void draw(DrawWindow dw) {
        dw.setFill(true);
        dw.setColor(Color.argb(100, 0, 200, 0));
        dw.drawCircle(RADIUS, RADIUS, RADIUS);

        float scale = range / RADIUS;

        //TODO:these should also be ordered.
        
        for (Placemark pm : state.getLayer().getZOrderedPlacemarks()) {
            float x = pm.obj.location.x / scale;
            float y = pm.obj.location.z / scale;

            if (x * x + y * y < RADIUS * RADIUS && pm.xmlShowInRadar) {
                dw.setFill(true);
                dw.setColor(Color.rgb(0, 255, 0));
                dw.drawRectangle(x + RADIUS - 1, y + RADIUS - 1, 2, 2);
            }
        }
        
    }

    public float getWidth() {
        return RADIUS * 2;
    }

    public float getHeight() {
        return RADIUS * 2;
    }
}

class ResFileSystem implements SimpleVFS {
    ARXContext ctx;

    ResFileSystem(ARXContext ctx) {
        this.ctx = ctx;
    }

    public InputStream getInputStream(String file) throws Exception {
        return ctx.getResourceInputStream(file);
    }

    public void returnInputStream(InputStream is) throws Exception {
        ctx.returnResourceInputStream(is);
    }

    public OutputStream getOutputStream(String file) throws Exception {
        return null;
    }

    public void returnOutputStream(OutputStream os) throws Exception {

    }
}
