package com.gamaray.arex.model;

public class OverlayTxt extends Overlay {

    private final String text;
    private final Float width;
    
    public OverlayTxt(String id, String anchor, Float x, Float y, String onPress, Boolean hidden, String text,
            Float width) {
        super(id, anchor, x, y, onPress, hidden);
        this.text = text;
        this.width = width;
    }
    
    public String getText() {
        return text;
    }
    public Float getWidth() {
        return width;
    }

    
    
}
