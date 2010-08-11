package com.gamaray.arex.model;

public class OverlayImg extends Overlay {

    private final String assetId;

    public OverlayImg(String id, String anchor, Float x, Float y, String onPress, Boolean hidden, String assetId) {
        super(id, anchor, x, y, onPress, hidden);
        this.assetId = assetId;
    }



    public String getAssetId() {
        return assetId;
    }

    
    
}
