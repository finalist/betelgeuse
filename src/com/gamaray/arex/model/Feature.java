package com.gamaray.arex.model;

public abstract class Feature {

    private final String id;
    private final Boolean showInRadar;
    private final String locationId;
    private final Float xLoc;
    private final Float yLoc;
    private final Float zLoc;
    private final Location location;
    private final String onPress;
    private final String showOnFocus;
    
    public Feature(String id, Boolean showInRadar, String locationId, Float xLoc, Float yLoc, Float zLoc, String onPress,String showOnFocus) {
        super();
        this.id = id;
        this.showInRadar = showInRadar;
        this.locationId = locationId;
        this.xLoc = xLoc;
        this.yLoc = yLoc;
        this.zLoc = zLoc;
        this.onPress = onPress;
        this.location=null;
        this.showOnFocus=showOnFocus;
    }

    public Feature(String id, Boolean showInRadar, Location location, String onPress,String showOnFocus) {
        super();
        this.id = id;
        this.showInRadar = showInRadar;
        this.location = location;
        this.onPress = onPress;
        this.locationId = null;
        this.xLoc = null;
        this.yLoc = null;
        this.zLoc = null;
        this.showOnFocus = showOnFocus;
    }

    public String getId() {
        return id;
    }

    public Boolean getShowInRadar() {
        return showInRadar;
    }

    public String getLocationId() {
        return locationId;
    }

    public Float getxLoc() {
        return xLoc;
    }

    public Float getyLoc() {
        return yLoc;
    }

    public Float getzLoc() {
        return zLoc;
    }

    public Location getLocation() {
        return location;
    }

    public String getOnPress() {
        return onPress;
    }

    public String getShowOnFocus() {
        return showOnFocus;
    }

    
}
