package com.gamaray.arex.model;

public abstract class Overlay {

    private final String id;
    private final String anchor;
    private final Float x;
    private final Float y;
    private final String onPress;
    private final Boolean hidden;
    
    public Overlay(String id, String anchor, Float x, Float y, String onPress, Boolean hidden) {
        super();
        this.id = id;
        this.anchor = anchor;
        this.x = x;
        this.y = y;
        this.onPress = onPress;
        this.hidden = hidden;
    }
    
    public Boolean getHidden() {
        return hidden;
    }
    public String getAnchor() {
        return anchor;
    }
    public Float getX() {
        return x;
    }
    public Float getY() {
        return y;
    }

    public String getId() {
        return id;
    }
    
    public String getOnPress() {
        return onPress;
    }
    
    
}
