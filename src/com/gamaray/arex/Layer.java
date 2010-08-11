package com.gamaray.arex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gamaray.arex.geo.GeoPoint;
import com.gamaray.arex.geo.GeoUtil;
import com.gamaray.arex.model.Dimension;
import com.gamaray.arex.model.Feature;
import com.gamaray.arex.model.Feature3D;
import com.gamaray.arex.model.FeatureImg;
import com.gamaray.arex.model.FeatureTxt;
import com.gamaray.arex.model.Location;
import com.gamaray.arex.xml.Element;

public class Layer {
    
    
    private String xmlVersion;
    String xmlName;
    String xmlDescription;
    String xmlRefreshUrl;
    String xmlSoundUrl;
    boolean xmlHasRefreshTime;
    long xmlTimeOrigin;
    int xmlValidFor;
    boolean xmlWaitForAssets;
    boolean xmlHasRefreshDistance;
    GeoPoint xmlOriginLoc = new GeoPoint();
    float xmlValidWithinRange;
    boolean xmlRelativeAltitude;
    boolean xmlRadarAvailable;
    boolean xmlHasRadarRange;
    float xmlRadarRange;

    long creationTime;
    GeoPoint creationLoc = new GeoPoint();
    boolean allAssetsDownloaded;
    long assetDownloadTime;
    boolean distancesVisible;

    private Map<String, Asset> assetMap = new HashMap<String, Asset>();

    private List<Placemark> zOrderedPlacemarks;
    private Map<String, Placemark> placemarkMap = new HashMap<String, Placemark>();

    Map<String, Overlay> overlayMap = new HashMap<String, Overlay>();

    private List<GeoPoint> refPoints = new ArrayList<GeoPoint>();
    // ArrayList animations = new ArrayList();

    private Map<String, GeoPoint> refPointMap = new HashMap<String, GeoPoint>();
    private Map<String, String> idMap = new HashMap<String, String>();

    private static final Comparator<Placemark> Z_ORDER_COMPARATOR= new Comparator<Placemark>() {
        
        @Override
        public int compare(Placemark leftPm, Placemark rightPm) {

            return Float.compare(leftPm.centerMark.z, rightPm.centerMark.z);
        }
    };

    
    private int idCounter = 0;

    public Layer() {
        xmlName = "NO_NAME";
        xmlDescription = "...";

        xmlHasRefreshTime = false;
        xmlHasRefreshDistance = false;
        xmlRadarAvailable = true;
        allAssetsDownloaded = false;
        distancesVisible = false;
    }

    public Layer(Dimension dimension) throws Exception {
        super();
        load_v1_0(dimension);
    }

    public boolean refreshOnDistance(GeoPoint curFix) {
        if (!xmlHasRefreshDistance)
            return false;
        double dist = GeoUtil.calcDistance(curFix.lat, curFix.lon, creationLoc.lat, creationLoc.lon);

        return (dist > xmlValidWithinRange);
    }

    public double calcRefreshDistance(GeoPoint curFix) {
        return GeoUtil.calcDistance(curFix.lat, curFix.lon, creationLoc.lat, creationLoc.lon);
    }

    public boolean refreshOnTime(long curTime) {
        if (!xmlHasRefreshTime)
            return false;

        if (!xmlWaitForAssets) {
            return (creationTime + xmlValidFor < curTime);
        } else {
            return (assetDownloadTime + xmlValidFor < curTime && allAssetsDownloaded);
        }
    }

    public void load(Element root) throws Exception {
        Element layerElem = root.getChildElement("dimension");

        xmlVersion = layerElem.getAttribValue("version");
        if (xmlVersion == null)
            throw new Exception("Dimension does not specify a version");

        if (xmlVersion.equals("1.0")) {
            load_v1_0(layerElem);
        } else {
            throw new Exception("Dimension version '" + xmlVersion + "' not supported");
        }
    }

    private void load_v1_0(Dimension dimension) throws Exception {
        xmlName = dimension.getName();
        xmlRefreshUrl = dimension.getRefreshUrl();

        // Radar range
        xmlHasRadarRange = false;
        if (dimension.getRadarRange() != null) {
            xmlRadarRange = dimension.getRadarRange();
            xmlHasRadarRange = true;
        }

        // Sound
        xmlSoundUrl = dimension.getPlaySoundUrl();

        xmlRelativeAltitude = (Boolean) defaultIfNull(dimension.getRelativeAltitude(), true);

        xmlRadarAvailable = (Boolean) defaultIfNull(dimension.getRadarAvailable(), true);

        // Refresh Seconds
        xmlHasRefreshTime = dimension.getRefreshTime() != null;
        xmlValidFor = (Integer) defaultIfNull(dimension.getRefreshTime(), 3000);

        xmlWaitForAssets = (Boolean) defaultIfNull(dimension.getWaitForAssets(), true);

        xmlValidWithinRange = (Integer) defaultIfNull(dimension.getRefreshDistance(), 1000);

        // Locations
        for (Location location : dimension.getLocations()) {
            GeoPoint refpt = new GeoPoint();

            String locId = generateIdIfNecessary(location.getId());
            refpt.lat = location.getLat();
            refpt.lon = location.getLon();
            refpt.alt = location.getAlt();

            refPoints.add(refpt);
            refPointMap.put(locId, refpt);
            putId(locId);

        }

        for (com.gamaray.arex.model.Asset asset : dimension.getAssets()) {
            Asset das = new Asset();

            das.xmlId = generateIdIfNecessary(asset.getId());
            das.xmlUrl = asset.getUrl();

            if (das.xmlUrl == null) {
                throw new Exception("No URL in asset '" + das.xmlId + "'");
            }

            if (asset.getFormat().equals("PNG")) {
                das.xmlFormat = ARXDownload.PNG;
            } else if (asset.getFormat().equals("JPG")) {
                das.xmlFormat = ARXDownload.PNG;
            } else if (asset.getFormat().equals("GAMA3D")) {
                das.xmlFormat = ARXDownload.GAMA3D;
            } else {
                throw new Exception("Format '" + asset.getFormat() + "' not supported in asset '" + das.xmlId + "'.");
            }

            assetMap.put(das.xmlId, das);
            putId(das.xmlId);

        }

        for (Feature feature : dimension.getFeatures()) {
            Placemark pm = null;

            if (feature instanceof Feature3D) {
                pm = new Placemark3D();
            } else if (feature instanceof FeatureImg) {
                pm = new PlacemarkImg();
            } else if (feature instanceof FeatureTxt) {
                pm = new PlacemarkTxt();
            }

            if (pm != null) {
                pm.xmlId = generateIdIfNecessary(feature.getId());
                pm.xmlLocationId = feature.getLocationId();
                if (pm.xmlLocationId == null) {

                    if (feature.getLocation() == null) {
                        throw new Exception("No location in feature '" + pm.xmlId + "'");
                    }

                    pm.xmlGeoLoc.lat = feature.getLocation().getLat();
                    pm.xmlGeoLoc.lon = feature.getLocation().getLon();
                    pm.xmlGeoLoc.alt = (Integer) defaultIfNull(feature.getLocation().getAlt(), 0);

                } else {
                    checkId(pm.xmlId, pm.xmlLocationId);
                    pm.xmlGeoLoc.setTo((GeoPoint) refPointMap.get(pm.xmlLocationId));
                }
                pm.xmlOnPress = feature.getOnPress();
                pm.xmlLocX = (Float) defaultIfNull(feature.getxLoc(), 0.0f);
                pm.xmlLocY = (Float) defaultIfNull(feature.getyLoc(), 0.0f);
                pm.xmlLocZ = (Float) defaultIfNull(feature.getzLoc(), 0.0f);

                pm.xmlShowOnFocus = feature.getShowOnFocus();
                pm.xmlShowInRadar = (Boolean) defaultIfNull(feature.getShowOnFocus(), true);

                if (pm instanceof Placemark3D) {
                    Placemark3D pm3d = (Placemark3D) pm;
                    Feature3D feature3d = (Feature3D) feature;
                    pm3d.xmlRotX = (Float) defaultIfNull(feature3d.getxRot(), 0.0);
                    pm3d.xmlRotY = (Float) defaultIfNull(feature3d.getyRot(), 0.0);
                    pm3d.xmlRotZ = (Float) defaultIfNull(feature3d.getzRot(), 0.0);
                    pm3d.xmlScale = (Float) defaultIfNull(feature3d.getScale(), 1.0);

                    pm3d.xmlAssetId = feature3d.getAssetId();
                    checkId(pm.xmlId, pm3d.xmlAssetId);
                    pm3d.asset = (Asset) assetMap.get(pm3d.xmlAssetId);

                    if (pm3d.asset.xmlFormat != ARXDownload.GAMA3D) {
                        throw new Exception("Invalid format for asset in feature '" + pm.xmlId + "'");
                    }

                } else if (pm instanceof PlacemarkImg) {
                    PlacemarkImg pmImg = (PlacemarkImg) pm;
                    FeatureImg featureImg = (FeatureImg) feature;
                    pmImg.xmlAnchor = (String) defaultIfNull(featureImg.getAnchor(), "BC");

                    pmImg.xmlAssetId = featureImg.getAssetId();
                    checkId(pm.xmlId, pmImg.xmlAssetId);
                    pmImg.asset = (Asset) assetMap.get(pmImg.xmlAssetId);

                    if (pmImg.asset.xmlFormat != ARXDownload.PNG && pmImg.asset.xmlFormat != ARXDownload.JPG) {
                        throw new Exception("Invalid format for asset in feature '" + pm.xmlId + "'");
                    }
                } else if (pm instanceof PlacemarkTxt) {
                    PlacemarkTxt pmTxt = (PlacemarkTxt) pm;
                    FeatureTxt featureTxt = (FeatureTxt) feature;
                    pmTxt.xmlText = (String) defaultIfNull(featureTxt.getText(), ".....");
                    pmTxt.xmlAnchor = (String) defaultIfNull(featureTxt.getAnchor(), "BC");
                }

                pm.layer = this;

                placemarkMap.put(pm.xmlId, pm);
                putId(pm.xmlId);
            }

        }

        for (com.gamaray.arex.model.Overlay overlay : dimension.getOverlays()) {
            Overlay ovl = null;

            if (overlay instanceof com.gamaray.arex.model.OverlayImg) {
                ovl = new OverlayImg();
            } else if (overlay instanceof com.gamaray.arex.model.OverlayTxt) {
                ovl = new OverlayTxt();
            }

            if (ovl != null) {
                ovl.xmlId = generateIdIfNecessary(overlay.getId());
                ovl.xmlOnPress = overlay.getOnPress();
                ovl.xmlX = (Float) defaultIfNull(overlay.getX(), 0.0);
                ovl.xmlY = (Float) defaultIfNull(overlay.getY(), 0.0);
                ovl.xmlAnchor = (String) defaultIfNull(overlay.getAnchor(), "TL");
                ovl.xmlHidden = (Boolean) defaultIfNull(overlay.getHidden(), false);

                if (ovl instanceof OverlayImg) {
                    OverlayImg ovlImg = (OverlayImg) ovl;
                    com.gamaray.arex.model.OverlayImg overlayImg = (com.gamaray.arex.model.OverlayImg) overlay;

                    ovlImg.xmlAssetId = overlayImg.getAssetId();
                    checkId(ovl.xmlId, ovlImg.xmlAssetId);
                    ovlImg.asset = (Asset) assetMap.get(ovlImg.xmlAssetId);

                    if (ovlImg.asset.xmlFormat != ARXDownload.PNG && ovlImg.asset.xmlFormat != ARXDownload.JPG) {
                        throw new Exception("Invalid format for asset in overlay '" + ovl.xmlId + "'");
                    }
                } else if (ovl instanceof OverlayTxt) {
                    OverlayTxt ovlTxt = (OverlayTxt) ovl;
                    com.gamaray.arex.model.OverlayTxt overlayTxt = (com.gamaray.arex.model.OverlayTxt) overlay;

                    ovlTxt.xmlText = (String) defaultIfNull(overlayTxt.getText(), "...");
                    ovlTxt.xmlWidth = (Float) defaultIfNull(overlayTxt.getWidth(), 200.0);
                }

                ovl.layer = this;

                overlayMap.put(ovl.xmlId, ovl);
                putId(ovl.xmlId);
            }
        }

    }

    private String generateIdIfNecessary(String id) {
        if (id != null) {
            return id;
        } else {
            return "AUTO_ID_" + (idCounter++);
        }
    }

    private Object defaultIfNull(Object value, Object defaultValue) {
        if (value != null) {
            return value;
        } else {
            return defaultValue;
        }
    }

    public void load_v1_0(Element layerElem) throws Exception {
        xmlName = layerElem.getChildElementValue("name", "NO_NAME");
        xmlRefreshUrl = layerElem.getChildElementValue("refreshUrl");

        // Radar range
        xmlHasRadarRange = false;
        if (layerElem.getChildElementValue("radarRange") != null) {
            xmlRadarRange = Float.parseFloat(layerElem.getChildElementValue("radarRange", "1000"));
            xmlHasRadarRange = true;
        }

        // Sound
        Element soundElem = layerElem.getChildElement("playSound");
        if (soundElem != null)
            xmlSoundUrl = soundElem.getChildElementValue("url");

        xmlRelativeAltitude = Boolean.parseBoolean(layerElem.getChildElementValue("relativeAltitude", "false"));

        xmlRadarAvailable = Boolean.parseBoolean(layerElem.getChildElementValue("radarAvailable", "true"));

        // Refresh Seconds
        xmlHasRefreshTime = false;
        Element refreshTimeElem = layerElem.getChildElement("refreshTime");
        if (refreshTimeElem != null) {
            xmlValidFor = Integer.parseInt(refreshTimeElem.getChildElementValue("validFor", "30000"));
            xmlWaitForAssets = Boolean.parseBoolean(refreshTimeElem.getChildElementValue("waitForAssets", "true"));
            xmlHasRefreshTime = true;
        }

        // Refresh Meters
        xmlHasRefreshDistance = false;
        Element refreshDistanceElem = layerElem.getChildElement("refreshDistance");
        if (refreshDistanceElem != null) {
            xmlValidWithinRange = Float
                    .parseFloat(refreshDistanceElem.getChildElementValue("validWithinRange", "1000"));
            xmlHasRefreshDistance = true;
        }

        // Locations
        Element refptsElem = layerElem.getChildElement("locations");
        if (refptsElem != null) {
            for (int i = 0; i < refptsElem.getChildElements().size(); i++) {
                Element refptElem = (Element) refptsElem.getChildElements().get(i);

                if (refptElem.getName().equals("location")) {
                    GeoPoint refpt = new GeoPoint();

                    String locId = refptElem.getAttribValue("id", "AUTO_ID_" + (idCounter++));
                    refpt.lat = Double.parseDouble(refptElem.getChildElementValue("lat", "0"));
                    refpt.lon = Double.parseDouble(refptElem.getChildElementValue("lon", "0"));
                    refpt.alt = Double.parseDouble(refptElem.getChildElementValue("alt", "0"));

                    refPoints.add(refpt);
                    refPointMap.put(locId, refpt);
                    putId(locId);
                }
            }
        }

        // Assets
        Element assetsElem = layerElem.getChildElement("assets");
        if (assetsElem != null) {
            for (int i = 0; i < assetsElem.getChildElements().size(); i++) {
                Element assetElem = (Element) assetsElem.getChildElements().get(i);

                if (assetElem.getName().equals("asset")) {
                    Asset das = new Asset();

                    das.xmlId = assetElem.getAttribValue("id", "AUTO_ID_" + (idCounter++));
                    das.xmlUrl = assetElem.getChildElementValue("url");

                    if (das.xmlUrl == null)
                        throw new Exception("No URL in asset '" + das.xmlId + "'");

                    String formatStr = assetElem.getChildElementValue("format");
                    if (formatStr.equals("PNG"))
                        das.xmlFormat = ARXDownload.PNG;
                    else if (formatStr.equals("JPG"))
                        das.xmlFormat = ARXDownload.PNG;
                    else if (formatStr.equals("GAMA3D"))
                        das.xmlFormat = ARXDownload.GAMA3D;
                    else
                        throw new Exception("Format '" + formatStr + "' not supported in asset '" + das.xmlId + "'.");

                    assetMap.put(das.xmlId, das);
                    putId(das.xmlId);
                }
            }
        }

        // Placemarks
        Element placemarksElem = layerElem.getChildElement("features");
        if (placemarksElem != null) {
            for (int i = 0; i < placemarksElem.getChildElements().size(); i++) {
                Element pmElem = (Element) placemarksElem.getChildElements().get(i);
                Placemark pm = null;

                if (pmElem.getName().equals("feature3d")) {
                    pm = new Placemark3D();
                } else if (pmElem.getName().equals("featureImg")) {
                    pm = new PlacemarkImg();
                } else if (pmElem.getName().equals("featureTxt")) {
                    pm = new PlacemarkTxt();
                }

                if (pm != null) {
                    pm.xmlId = pmElem.getAttribValue("id", "AUTO_ID_" + (idCounter++));
                    pm.xmlLocationId = pmElem.getChildElementValue("locationId");
                    if (pm.xmlLocationId == null) {
                        Element locElem = pmElem.getChildElement("location");

                        if (locElem == null)
                            throw new Exception("No location in feature '" + pm.xmlId + "'");

                        pm.xmlGeoLoc.lat = Double.parseDouble(locElem.getChildElementValue("lat", "0"));
                        pm.xmlGeoLoc.lon = Double.parseDouble(locElem.getChildElementValue("lon", "0"));
                        pm.xmlGeoLoc.alt = Double.parseDouble(locElem.getChildElementValue("alt", "0"));
                    } else {
                        checkId(pm.xmlId, pm.xmlLocationId);
                        pm.xmlGeoLoc.setTo((GeoPoint) refPointMap.get(pm.xmlLocationId));
                    }
                    pm.xmlOnPress = pmElem.getChildElementValue("onPress");
                    pm.xmlLocX = Float.parseFloat(pmElem.getChildElementValue("xLoc", "0"));
                    pm.xmlLocY = Float.parseFloat(pmElem.getChildElementValue("yLoc", "0"));
                    pm.xmlLocZ = Float.parseFloat(pmElem.getChildElementValue("zLoc", "0"));
                    pm.xmlShowOnFocus = pmElem.getChildElementValue("showOnFocus");

                    if (pm instanceof Placemark3D) {
                        Placemark3D pm3d = (Placemark3D) pm;
                        pm3d.xmlRotX = Float.parseFloat(pmElem.getChildElementValue("xRot", "0"));
                        pm3d.xmlRotY = Float.parseFloat(pmElem.getChildElementValue("yRot", "0"));
                        pm3d.xmlRotZ = Float.parseFloat(pmElem.getChildElementValue("zRot", "0"));
                        pm3d.xmlScale = Float.parseFloat(pmElem.getChildElementValue("scale", "1"));
                        pm3d.xmlShowInRadar = Boolean.parseBoolean(pmElem.getChildElementValue("showInRadar", "true"));

                        pm3d.xmlAssetId = pmElem.getChildElementValue("assetId");
                        checkId(pm.xmlId, pm3d.xmlAssetId);
                        pm3d.asset = (Asset) assetMap.get(pm3d.xmlAssetId);

                        if (pm3d.asset.xmlFormat != ARXDownload.GAMA3D) {
                            throw new Exception("Invalid format for asset in feature '" + pm.xmlId + "'");
                        }
                    } else if (pm instanceof PlacemarkImg) {
                        PlacemarkImg pmImg = (PlacemarkImg) pm;
                        pmImg.xmlAnchor = pmElem.getChildElementValue("anchor", "BC");
                        pmImg.xmlShowInRadar = Boolean.parseBoolean(pmElem.getChildElementValue("showInRadar", "true"));

                        pmImg.xmlAssetId = pmElem.getChildElementValue("assetId");
                        checkId(pm.xmlId, pmImg.xmlAssetId);
                        pmImg.asset = (Asset) assetMap.get(pmImg.xmlAssetId);

                        if (pmImg.asset.xmlFormat != ARXDownload.PNG && pmImg.asset.xmlFormat != ARXDownload.JPG) {
                            throw new Exception("Invalid format for asset in feature '" + pm.xmlId + "'");
                        }
                    } else if (pm instanceof PlacemarkTxt) {
                        PlacemarkTxt pmTxt = (PlacemarkTxt) pm;
                        pmTxt.xmlText = pmElem.getChildElementValue("text", "...");
                        pmTxt.xmlAnchor = pmElem.getChildElementValue("anchor", "BC");
                        pmTxt.xmlShowInRadar = Boolean
                                .parseBoolean(pmElem.getChildElementValue("showInRadar", "false"));
                    }

                    pm.layer = this;

                    placemarkMap.put(pm.xmlId, pm);
                    putId(pm.xmlId);
                }
            }
        }

        // Overlays
        Element overlaysElem = layerElem.getChildElement("overlays");
        if (overlaysElem != null) {
            for (int i = 0; i < overlaysElem.getChildElements().size(); i++) {
                Element ovlElem = (Element) overlaysElem.getChildElements().get(i);
                Overlay ovl = null;

                if (ovlElem.getName().equals("overlayImg")) {
                    ovl = new OverlayImg();
                } else if (ovlElem.getName().equals("overlayTxt")) {
                    ovl = new OverlayTxt();
                }

                if (ovl != null) {
                    ovl.xmlId = ovlElem.getAttribValue("id", "AUTO_ID_" + (idCounter++));
                    ovl.xmlOnPress = ovlElem.getChildElementValue("onPress");
                    ovl.xmlX = Float.parseFloat(ovlElem.getChildElementValue("x", "0"));
                    ovl.xmlY = Float.parseFloat(ovlElem.getChildElementValue("y", "0"));
                    ovl.xmlAnchor = ovlElem.getChildElementValue("anchor", "TL");
                    ovl.xmlHidden = Boolean.parseBoolean(ovlElem.getChildElementValue("hidden", "false"));

                    if (ovl instanceof OverlayImg) {
                        OverlayImg ovlImg = (OverlayImg) ovl;

                        ovlImg.xmlAssetId = ovlElem.getChildElementValue("assetId");
                        checkId(ovl.xmlId, ovlImg.xmlAssetId);
                        ovlImg.asset = (Asset) assetMap.get(ovlImg.xmlAssetId);

                        if (ovlImg.asset.xmlFormat != ARXDownload.PNG && ovlImg.asset.xmlFormat != ARXDownload.JPG) {
                            throw new Exception("Invalid format for asset in overlay '" + ovl.xmlId + "'");
                        }
                    } else if (ovl instanceof OverlayTxt) {
                        OverlayTxt ovlTxt = (OverlayTxt) ovl;

                        ovlTxt.xmlText = ovlElem.getChildElementValue("text", "...");
                        ovlTxt.xmlWidth = Float.parseFloat(ovlElem.getChildElementValue("width", "200"));
                    }

                    ovl.layer = this;

                    overlayMap.put(ovl.xmlId, ovl);
                    putId(ovl.xmlId);
                }
            }
        }
    }

    private void checkId(String requestorId, String id) throws Exception {
        if (!idMap.containsKey(id))
            throw new Exception("Id '" + id + "' not found, requested by '" + requestorId + "'");
    }

    private void putId(String id) throws Exception {
        if (idMap.containsKey(id))
            throw new Exception("Duplicate id '" + id + "'");
        idMap.put(id, "VALUE");
    }

    public List<Placemark> getZOrderedPlacemarks() {
        if (zOrderedPlacemarks==null){
            //TODO:Maybe syncronize
            zOrderedPlacemarks=new ArrayList<Placemark>(placemarkMap.values());
            Collections.sort(zOrderedPlacemarks, Z_ORDER_COMPARATOR);
        }
        return zOrderedPlacemarks;
    }

    public Placemark getPlacemark(String id) {
        return placemarkMap.get(id);

    }

    public Collection<Asset> getAssets() {
        return assetMap.values();
    }

    public Asset getAsset(String id) {
        return assetMap.get(id);
    }

    public Collection<Overlay> getOverlays() {
        return overlayMap.values();
    }

    public Overlay getOverlay(String id) {
        return overlayMap.get(id);
    }

}