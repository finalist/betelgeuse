package com.gamaray.arex;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.gamaray.arex.format3d.SimpleVFS;
import com.gamaray.arex.gui.Bitmap;
import com.gamaray.arex.gui.DrawWindow;
import com.gamaray.arex.gui.Drawable;
import com.gamaray.arex.gui.GUIUtil;
import com.gamaray.arex.gui.MenuItem;
import com.gamaray.arex.gui.Vector2D;
import com.gamaray.arex.render3d.Camera;
import com.gamaray.arex.render3d.Color;
import com.gamaray.arex.render3d.Matrix3D;
import com.gamaray.arex.render3d.Vector3D;

public class ARXView {
    ARXContext ctx;

    boolean init = false;
    int width, height;
    Camera cam;
    ARXState state = new ARXState();

    public static String VERSION = "1.0.10";
    String LOCAL_SERVER = "localhost:8080";
    String CLOUD_SERVER = "dimensions.gamaray.com";
    String SERVER = CLOUD_SERVER;
    // String REDIRECT_URL = "http://" + SERVER + "/Dimensions/Redirect";
    String HOME_URL = "http://www.gamaray.com/home.gddf";

    ArrayList icons = new ArrayList();

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
    MenuItem miHome = new MenuItem(9, "Home" + (SERVER.equals(LOCAL_SERVER) ? " (LOCAL)" : ""));

    public ARXView(ARXContext ctx) {
        this.ctx = ctx;
    }

    public void doLaunch() {
        state.launchNeeded = true;
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

            HashMap prefs = ctx.getPrefs();
            if (!prefs.containsKey("UID")) {
                prefs.put("UID", "UID" + Math.abs(ctx.getRandomLong()));

                ctx.setPrefs(prefs);
            }
            state.uid = (String) prefs.get("UID");

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
        }

        // Check if GPS signal received
        if (!ctx.gpsSignalReceived()) {
            ARXMessages.putMessage("GPS_WAIT", "Waiting for GPS signal", ARXMessages.worldIcon, 500);
        }

        // Check for compass accuracy
        if (ctx.isCompassAccuracyLow()) {
            ARXMessages.putMessage("COMPASS_ACCURACY", "Compass accuracy low", ARXMessages.compassIcon, 1000);
        }

        if (ctx.gpsEnabled()) {
            drawDimension(dw);
        } else {
            purgeEvents();
        }

        ARXMessages.expireMessages();
        int msgCount = ARXMessages.getMessageCount();
        float msgHeight = 0, msgWidth = 0;
        float msgPad = 10;
        int startMsg = 0, maxMsg = 1;

        if (state.messagesVisible && msgCount > maxMsg) {
            startMsg = msgCount - maxMsg;
        }

        for (int i = startMsg; i < msgCount; i++) {
            Message msg = (Message) ARXMessages.getMessage(i);
            if (!msg.init)
                msg.init(dw, width - msgPad * 2);

            if (state.messagesVisible) {
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
        ctx.getRotationMatrix(cam.transform);
        ctx.getCurrentLocation(state.curFix);

        // Determine bearing
        // TODO: fix math
        rotationInv.setTo(cam.transform);
        rotationInv.transpose();
        looking.setTo(1, 0, 0);
        looking.transform(rotationInv);
        state.curBearing = ARXUtil.getAngle(0, 0, looking.x, looking.z);
        state.curBearing = ((int) state.curBearing + 360) % 360;

        // Determine pitch
        // TODO: fix math
        rotationInv.transpose();
        looking.setTo(0, 1, 0);
        looking.transform(rotationInv);
        state.curPitch = -ARXUtil.getAngle(0, 0, looking.y, looking.z);

        state.screenWidth = width;
        state.screenHeight = height;

        if (ctx.getLaunchUrl().equals("") || ctx.getLaunchUrl().equals(state.launchUrl)) {
            state.launchNeeded = false;
        }

        // Load Layer
        if (state.nextLayerStatus == ARXState.NOT_STARTED ||
                (state.nextLayerStatus == ARXState.READY && state.launchNeeded)) {
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

            DownloadJobRequest request = new DownloadJobRequest();
            request.format = ARXDownload.GAMADIM;
            request.cacheable = false;
            request.params = state.getParams("init", "NULL");
            if (!ctx.getLaunchUrl().equals(""))
                request.url = ctx.getLaunchUrl();
            else
                request.url = HOME_URL;

            state.launchNeeded = false;
            state.launchUrl = ctx.getLaunchUrl();

            state.downloadJobId = ctx.getARXDownload().submitJob(request);

            state.nextLayerStatus = ARXState.JOB_SUBMITTED;
        } else if (state.nextLayerStatus == ARXState.JOB_SUBMITTED) {
            ARXMessages.putMessage("LOADING_" + state.downloadJobId, "Dimension loading", ARXMessages.refreshIcon, 500);

            if (ctx.getARXDownload().isJobComplete(state.downloadJobId)) {
                state.downloadResult = ctx.getARXDownload().getJobResult(state.downloadJobId);

                state.nextLayerStatus = ARXState.TRANSITION;
            }
        } else if (state.nextLayerStatus == ARXState.TRANSITION) {
            ctx.getARXDownload().pause();
            ctx.getARXRender().pause();

            if (ctx.getARXDownload().getState() == ARXDownload.PAUSED &&
                    ctx.getARXRender().getState() == ARXRender.PAUSED) {
                if (state.downloadResult.error) {
                    if (state.retryCount < 10) {
                        ARXMessages.removeMessage("LOADING_" + state.downloadJobId);
                        ARXMessages.removeMessage("ERROR_" + state.downloadJobId);

                        DownloadJobRequest request = new DownloadJobRequest();
                        request.format = ARXDownload.GAMADIM;
                        request.url = state.downloadResult.errorRequest.url;
                        request.cacheable = state.downloadResult.errorRequest.cacheable;
                        request.params = state.downloadResult.errorRequest.params;
                        state.downloadJobId = ctx.getARXDownload().submitJob(request);

                        state.nextLayerStatus = ARXState.JOB_SUBMITTED;
                        state.retryCount++;

                        ARXMessages.putMessage("ERROR_" + state.downloadJobId, "Error: " +
                                state.downloadResult.errorMsg, ARXMessages.warningIcon, Integer.MAX_VALUE);

                        ARXMessages.putMessage("LOADING_" + state.downloadJobId, "Dimension loading (retry #" +
                                state.retryCount + ")", ARXMessages.refreshIcon, Integer.MAX_VALUE);
                    } else {
                        state.nextLayerStatus = ARXState.READY;
                        state.layer = new Layer();

                        ARXMessages.removeMessage("LOADING_" + state.downloadJobId);

                        ARXMessages.putMessage("FATAL_ERROR_" + state.downloadJobId, "Unable to load dimension: " +
                                state.downloadResult.errorRequest.url, ARXMessages.errorIcon, Integer.MAX_VALUE);
                    }
                } else {
                    Layer oldLayer = state.layer;
                    Layer newLayer = (Layer) state.downloadResult.obj;

                    ARXMessages.removeMessage("ERROR_" + state.downloadJobId);

                    ARXMessages.removeMessage("LOADING_" + state.downloadJobId);

                    // ARXMessages.putMessage("LOADING_COMPLETE_" +
                    // state.downloadJobId,
                    // "Dimension '" + newLayer.xmlName + "' loaded",
                    // ARXMessages.refreshCompleteIcon, 3000);

                    // Clean up ARXRender and ARXDownload
                    ctx.getARXDownload().clearLists();
                    ctx.getARXRender().clearLists();

                    // Transfer valid renders
                    for (int i = 0; i < state.layer.placemarks.size(); i++) {
                        Placemark pm = (Placemark) state.layer.placemarks.get(i);

                        if (pm instanceof Placemark3D) {
                            Placemark3D src = (Placemark3D) pm;
                            Placemark3D dest = (Placemark3D) newLayer.placemarkMap.get(src.xmlId);

                            if (dest != null)
                                src.copyRenderState(dest);
                        }
                    }

                    // Transfer valid downloads
                    for (int i = 0; i < state.layer.assets.size(); i++) {
                        Asset das = (Asset) state.layer.assets.get(i);

                        Asset dest = (Asset) newLayer.assetMap.get(das.xmlId);
                        if (dest != null)
                            das.copyDownloadState(dest);
                    }

                    // Set layer creation time and location
                    newLayer.creationTime = System.currentTimeMillis();
                    newLayer.creationLoc.setTo(state.curFix);

                    // Show radar if available
                    if (newLayer.xmlRadarAvailable)
                        state.radarVisible = true;
                    if (newLayer.xmlHasRadarRange)
                        radarDialog.setRange(newLayer.xmlRadarRange);

                    state.layer = newLayer;
                    state.nextLayerStatus = ARXState.READY;
                    state.retryCount = 0;
                }

                ctx.getARXDownload().restart();
                ctx.getARXRender().restart();
            }
        }

        // Update placemarks
        for (int i = 0; i < state.layer.placemarks.size(); i++) {
            Placemark pm = (Placemark) state.layer.placemarks.get(i);

            pm.updateState(state.curFix, System.currentTimeMillis());
        }

        // Prepare placemarks for draw
        for (int i = 0; i < state.layer.placemarks.size(); i++) {
            Placemark pm = (Placemark) state.layer.placemarks.get(i);

            pm.prepareDraw(cam);
        }

        // Sort placemarks by centerMark.z
        Collections.sort(state.layer.placemarks, new PlacemarkZCompare());

        // Draw placemarks
        for (int i = 0; i < state.layer.placemarks.size(); i++) {
            Placemark pm = (Placemark) state.layer.placemarks.get(i);

            pm.draw(dw);
        }

        // Update asset download status
        for (int i = 0; i < state.layer.assets.size(); i++) {
            Asset das = (Asset) state.layer.assets.get(i);

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

        if (state.nextLayerStatus == ARXState.READY) {
            // Update renders
            for (int i = 0; i < state.layer.placemarks.size(); i++) {
                Placemark pm = (Placemark) state.layer.placemarks.get(i);

                if (pm instanceof Placemark3D) {
                    ((Placemark3D) pm).updateRenders(cam, ctx);
                }
            }

            // Handle show actions
            for (int i = 0; i < state.layer.overlays.size(); i++) {
                Overlay ovl = (Overlay) state.layer.overlays.get(i);
                ovl.show = false;
            }
            float minDist = Float.MAX_VALUE;
            Placemark closestPM = null;
            for (int i = 0; i < state.layer.placemarks.size(); i++) {
                Placemark pm = (Placemark) state.layer.placemarks.get(i);

                if (minDist > pm.distToCenter && pm.distToCenter < 50 && pm.xmlShowOnFocus != null) {
                    minDist = pm.distToCenter;
                    closestPM = pm;
                }

            }
            if (closestPM != null) {
                String ovlIds[] = closestPM.xmlShowOnFocus.split(",");

                for (int i = 0; i < ovlIds.length; i++) {
                    Overlay ovl = (Overlay) state.layer.overlayMap.get(ovlIds[i].trim());
                    if (ovl != null)
                        ovl.show = true;
                }
            }

            // Draw Overlays
            for (int i = 0; i < state.layer.overlays.size(); i++) {
                Overlay ovl = (Overlay) state.layer.overlays.get(i);

                if (ovl.isVisible())
                    ovl.draw(dw);
            }

            // Request asset downloads
            boolean allAssetsDownloaded = true;
            for (int i = 0; i < state.layer.assets.size(); i++) {
                Asset das = (Asset) state.layer.assets.get(i);

                das.isDownloadJobActive = false;
                if (das.downloadStatus == ARXState.NOT_STARTED) {
                    DownloadJobRequest request = new DownloadJobRequest();
                    request.format = das.xmlFormat;
                    request.url = das.xmlUrl;
                    request.cacheable = true;
                    request.params = null;
                    das.downloadJobId = ctx.getARXDownload().submitJob(request);

                    das.downloadStatus = ARXState.JOB_SUBMITTED;
                    allAssetsDownloaded = false;
                } else if (das.downloadStatus == ARXState.JOB_SUBMITTED) {
                    if (ctx.getARXDownload().isJobComplete(das.downloadJobId)) {
                        das.downloadResult = ctx.getARXDownload().getJobResult(das.downloadJobId);
                        das.downloadStatus = ARXState.READY;

                        if (!das.downloadResult.error) {
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
                            ARXMessages.putMessage("DOWNLOAD_ASSET", "Downloading '" + das.xmlId + "' (" +
                                    ARXUtil.formatDecimal(das.downloadJobPctComplete * 100, 2) + "%)", "[" + pctTxt +
                                    "%]", ARXMessages.downloadIcon, 500, true);
                        }
                    }

                    allAssetsDownloaded = false;
                }
            }

            // Set assetDownloadTime
            if (allAssetsDownloaded && !state.layer.allAssetsDownloaded) {
                state.layer.allAssetsDownloaded = true;
                state.layer.assetDownloadTime = System.currentTimeMillis();

                ARXMessages.putMessage("ASSETS_LOADING_COMPLETE", "Dimension '" + state.layer.xmlName + "' loaded", "",
                        ARXMessages.refreshCompleteIcon, 3000, true);
            }

            if (!allAssetsDownloaded) {
                ARXMessages.putMessage("ASSETS_LOADING", "Dimension '" + state.layer.xmlName + "' loading",
                        ARXMessages.refreshIcon, 500);
            }
        }

        // Draw Radar
        if (state.radarVisible && state.layer.xmlRadarAvailable) {
            String dirTxt = "";
            int bearing = (int) state.curBearing;
            int range = (int) (state.curBearing / (360f / 16f));
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
            dw.drawObject(radarObjects, radarX, radarY, -state.curBearing, 1);
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
        if (state.nextLayerStatus == ARXState.READY) {
            String evtName = null;
            if (state.layer.refreshOnDistance(state.curFix))
                evtName = "refreshOnDistance";
            if (state.layer.refreshOnTime(System.currentTimeMillis()))
                evtName = "refreshOnTime";

            if (evtName != null) {
                DownloadJobRequest request = new DownloadJobRequest();
                request.format = ARXDownload.GAMADIM;
                request.url = state.layer.xmlRefreshUrl;
                request.cacheable = false;
                request.params = state.getParams(evtName, "NULL");
                state.downloadJobId = ctx.getARXDownload().submitJob(request);

                state.nextLayerStatus = ARXState.JOB_SUBMITTED;
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

            String refreshDistanceTxt = ARXUtil.formatDistance((float) state.layer.calcRefreshDistance(state.curFix));
            String validRangeTxt = (state.layer.xmlHasRefreshDistance) ? ARXUtil
                    .formatDistance(state.layer.xmlValidWithinRange) : "NA";

            String refreshTimeTxt = ((System.currentTimeMillis() - state.layer.creationTime) / 1000) + "s";
            String validForTxt = (state.layer.xmlHasRefreshTime) ? (state.layer.xmlValidFor / 1000) + "s" : "NA";

            dw.drawText(infoCurX, infoCurY, "Dimension: " + state.layer.xmlName);
            infoCurY += infoLineHeight * 2;
            dw.drawText(infoCurX, infoCurY, "Latitude: " + state.curFix.lat);
            infoCurY += infoLineHeight;
            dw.drawText(infoCurX, infoCurY, "Longitude: " + state.curFix.lon);
            infoCurY += infoLineHeight;
            dw.drawText(infoCurX, infoCurY, "Altitude: " + ARXUtil.formatDistance((float) state.curFix.alt));
            infoCurY += infoLineHeight;
            dw.drawText(infoCurX, infoCurY, "Bearing: " + state.curBearing + ((char) 176));
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

            if (evt != null && evt.type == Event.CLICK && state.radarVisible && state.layer.xmlRadarAvailable) {
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
            state.radarVisible = false;
        } else if (evt.id == miShowRadar.id) {
            state.radarVisible = true;
        } else if (evt.id == miHideDistances.id) {
            state.layer.distancesVisible = false;
        } else if (evt.id == miShowDistances.id) {
            state.layer.distancesVisible = true;
        } else if (evt.id == miHideMessages.id) {
            state.messagesVisible = false;
        } else if (evt.id == miShowMessages.id) {
            state.messagesVisible = true;
        } else if (evt.id == miHideInfo.id) {
            dialogState = NO_DIALOG;
        } else if (evt.id == miShowInfo.id) {
            dialogState = INFO_DIALOG;
        } else if (evt.id == miRefresh.id) {
            if (state.nextLayerStatus == ARXState.READY) {
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

                DownloadJobRequest request = new DownloadJobRequest();
                request.format = ARXDownload.GAMADIM;
                request.cacheable = false;
                request.params = state.getParams("init", "NULL");
                if (!state.launchUrl.equals(""))
                    request.url = state.launchUrl;
                else
                    request.url = HOME_URL;

                state.launchNeeded = false;
                state.launchUrl = ctx.getLaunchUrl();

                state.downloadJobId = ctx.getARXDownload().submitJob(request);

                state.nextLayerStatus = ARXState.JOB_SUBMITTED;
            }
        } else if (evt.id == miHome.id) {
            if (state.nextLayerStatus == ARXState.READY) {
                ctx.getARXDownload().emptyCache();
                ARXMessages.clearAllMessages();

                /*
                 * DownloadJobRequest request = new DownloadJobRequest();
                 * request.format = ARXDownload.GAMADIM; request.url =
                 * REDIRECT_URL; request.cacheable = false; request.params =
                 * state.getParams("init","NULL");
                 * 
                 * state.launchUrl = ""; state.downloadJobId =
                 * ctx.getARXDownload().submitJob(request);
                 * 
                 * state.nextLayerStatus = ARXState.JOB_SUBMITTED;
                 */

                DownloadJobRequest request = new DownloadJobRequest();
                request.format = ARXDownload.GAMADIM;
                request.url = HOME_URL;
                request.cacheable = false;
                request.params = state.getParams("init", "NULL");

                state.launchUrl = "";
                state.downloadJobId = ctx.getARXDownload().submitJob(request);

                state.nextLayerStatus = ARXState.JOB_SUBMITTED;
            }
        }

        return evtHandled;
    }

    boolean handleClickEvent(ClickEvent evt) {
        boolean evtHandled = false;

        if (state.radarVisible && state.layer.xmlRadarAvailable) {
            if (evt.x > radarX && evt.x < radarX + radarObjects.getWidth() && evt.y > radarY &&
                    evt.y < radarY + radarObjects.getHeight()) {
                dialogState = RADAR_DIALOG;

                evtHandled = true;
            }
        }

        // Handle event
        if (state.nextLayerStatus == ARXState.READY) {
            // Loop through overlays
            for (int i = state.layer.overlays.size() - 1; i >= 0 && !evtHandled; i--) {
                Overlay ovl = (Overlay) state.layer.overlays.get(i);

                evtHandled = ovl.press(evt.x, evt.y, ctx, state);
            }

            // Loop through placemarks
            for (int i = state.layer.placemarks.size() - 1; i >= 0 && !evtHandled; i--) {
                Placemark pm = (Placemark) state.layer.placemarks.get(i);

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

    ArrayList events = new ArrayList();

    public void createMenuEvent(ArrayList menu) {
        if (!ctx.gpsEnabled())
            return;

        if (dialogState == INFO_DIALOG)
            menu.add(miHideInfo);
        else
            menu.add(miShowInfo);

        if (state.layer.xmlRadarAvailable) {
            // if (state.radarVisible)
            // menu.add(miHideRadar);
            // else
            // menu.add(miShowRadar);
        }

        if (state.layer.distancesVisible)
            menu.add(miHideDistances);
        else
            menu.add(miShowDistances);

        if (state.messagesVisible)
            menu.add(miHideMessages);
        else
            menu.add(miShowMessages);

        if (state.nextLayerStatus == ARXState.READY) {
            menu.add(miRefresh);
            menu.add(miHome);
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

        for (int i = 0; i < state.layer.placemarks.size(); i++) {
            Placemark pm = (Placemark) state.layer.placemarks.get(i);

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

class PlacemarkZCompare implements java.util.Comparator {
    public int compare(Object left, Object right) {
        Placemark leftPm = (Placemark) left;
        Placemark rightPm = (Placemark) right;

        return Float.compare(leftPm.centerMark.z, rightPm.centerMark.z);
    }
}
