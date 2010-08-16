package com.gamaray.arex.model;

public class OverlayHtml extends Overlay {

    private final Float width;
    private final Float height;
    private final String url;
    
    public OverlayHtml(String id, String anchor, Float x, Float y, String onPress, Boolean hidden, Float width,
            Float height, String url) {
        super(id, anchor, x, y, onPress, hidden);
        this.width = width;
        this.height = height;
        this.url = url;
    }

    public Float getWidth() {
        return width;
    }

    public Float getHeight() {
        return height;
    }

    public String getUrl() {
        return url;
    }
    
    
}
