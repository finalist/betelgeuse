package com.gamaray.arex.model;

public class FeatureImg extends Feature {

    private final String assetId;
    private final String anchor;

    public FeatureImg(String id, Boolean showInRadar, String locationId, Float xLoc, Float yLoc, Float zLoc,
            String onPress, String showOnFocus, String assetId, String anchor) {
        super(id, showInRadar, locationId, xLoc, yLoc, zLoc, onPress, showOnFocus);
        this.assetId = assetId;
        this.anchor = anchor;
    }

    public FeatureImg(String id, Boolean showInRadar, Location location, String onPress, String showOnFocus,
            String assetId, String anchor) {
        super(id, showInRadar, location, onPress, showOnFocus);
        this.assetId = assetId;
        this.anchor = anchor;
    }

    public String getAssetId() {
        return assetId;
    }

    public String getAnchor() {
        return anchor;
    }

}
