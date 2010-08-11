package com.gamaray.arex.model;

public class Feature3D extends Feature {
    
    private final String assetId;
    private final Float scale;
    private final Float xRot;
    private final Float yRot;
    private final Float zRot;
    

    public Feature3D(String id, Boolean showInRadar, String locationId, Float xLoc, Float yLoc, Float zLoc,
            String onPress, String showOnFocus, String assetId, Float scale, Float xRot, Float yRot, Float zRot) {
        super(id, showInRadar, locationId, xLoc, yLoc, zLoc, onPress, showOnFocus);
        this.assetId = assetId;
        this.scale = scale;
        this.xRot = xRot;
        this.yRot = yRot;
        this.zRot = zRot;
    }

    public Feature3D(String id, Boolean showInRadar, Location location, String onPress, String showOnFocus,
            String assetId, Float scale, Float xRot, Float yRot, Float zRot) {
        super(id, showInRadar, location, onPress, showOnFocus);
        this.assetId = assetId;
        this.scale = scale;
        this.xRot = xRot;
        this.yRot = yRot;
        this.zRot = zRot;
    }

    public Float getxRot() {
        return xRot;
    }

    public Float getyRot() {
        return yRot;
    }

    public Float getzRot() {
        return zRot;
    }

    
    public String getAssetId() {
        return assetId;
    }

    public Float getScale() {
        return scale;
    }


}
