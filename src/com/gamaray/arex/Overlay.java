package com.gamaray.arex;

import com.gamaray.arex.context.ARXContext;
import com.gamaray.arex.gui.DrawWindow;

public abstract class Overlay {
    
    
    private String xmlId;
    private String xmlAssetId;
    private String xmlOnPress;
    private float xmlX, xmlY;
    private String xmlAnchor;
    private boolean xmlHidden;
    private boolean show;

    AnchorUtil anchoredObj = new AnchorUtil();

    // Pointer to asset
    Asset asset;

    // Pointer to layer
    Layer layer;

    abstract public void draw(DrawWindow dw);

    abstract boolean press(float x, float y, ARXContext ctx, ARXState state);

    public boolean isVisible() {
        return (!xmlHidden || show);
    }

    boolean isPressValid(float x, float y) {
        float objX = xmlX + anchoredObj.x - anchoredObj.getWidth() / 2;
        float objY = xmlY + anchoredObj.y - anchoredObj.getHeight() / 2;
        float objW = anchoredObj.getWidth() / 2;
        float objH = anchoredObj.getHeight() / 2;

        if (x > objX && x < objX + objW && y > objY && y < objY + objH) {
            return true;
        } else {
            return false;
        }
    }

    public String getXmlId() {
        return xmlId;
    }

    public void setXmlId(String xmlId) {
        this.xmlId = xmlId;
    }

    public String getXmlOnPress() {
        return xmlOnPress;
    }

    public void setXmlOnPress(String xmlOnPress) {
        this.xmlOnPress = xmlOnPress;
    }

    public float getXmlX() {
        return xmlX;
    }

    public void setXmlX(float xmlX) {
        this.xmlX = xmlX;
    }

    public float getXmlY() {
        return xmlY;
    }

    public void setXmlY(float xmlY) {
        this.xmlY = xmlY;
    }

    public String getXmlAnchor() {
        return xmlAnchor;
    }

    public void setXmlAnchor(String xmlAnchor) {
        this.xmlAnchor = xmlAnchor;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public boolean isXmlHidden() {
        return xmlHidden;
    }

    public void setXmlHidden(boolean xmlHidden) {
        this.xmlHidden = xmlHidden;
    }

    public String getXmlAssetId() {
        return xmlAssetId;
    }

    public void setXmlAssetId(String xmlAssetId) {
        this.xmlAssetId = xmlAssetId;
    }
    
    
    
    
}