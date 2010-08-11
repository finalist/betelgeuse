package com.gamaray.arex.model;

public class FeatureTxt extends Feature {
    
    private final String text;
    private final String anchor;
    
    public FeatureTxt(String id, boolean showInRadar, Location location, String onPress, String showOnFocus,
            String text, String anchor) {
        super(id, showInRadar, location, onPress, showOnFocus);
        this.text = text;
        this.anchor = anchor;
    }

    public FeatureTxt(String id, Boolean showInRadar, String locationId, Float xLoc, Float yLoc, Float zLoc,
            String onPress, String showOnFocus, String text, String anchor) {
        super(id, showInRadar, locationId, xLoc, yLoc, zLoc, onPress, showOnFocus);
        this.text = text;
        this.anchor = anchor;
    }

    public String getText() {
        return text;
    }

    public String getAnchor() {
        return anchor;
    }

    
    
    

}
