package com.gamaray.arex;

import com.gamaray.arex.context.ARXContext;
import com.gamaray.arex.gui.Bitmap;
import com.gamaray.arex.gui.DrawWindow;
import com.gamaray.arex.gui.TextBlock;

abstract public class Overlay {
    String xmlId;
    String xmlAssetId;
    String xmlOnPress;
    float xmlX, xmlY;
    String xmlAnchor;
    boolean xmlHidden;

    boolean show;

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
}

class OverlayImg extends Overlay {
    public void draw(DrawWindow dw) {
        if (asset.downloadStatus == ARXState.READY) {
            if (!asset.downloadResult.error) {
                Bitmap bmp = (Bitmap) asset.downloadResult.obj;

                anchoredObj.prepare(bmp, xmlAnchor);

                dw.drawObject(anchoredObj, xmlX - anchoredObj.getWidth() / 2, xmlY - anchoredObj.getHeight() / 2, 0, 1);
            }
        }
    }

    boolean press(float x, float y, ARXContext ctx, ARXState state) {
        boolean evtHandled = false;

        if (asset.downloadStatus == ARXState.READY && xmlOnPress != null && isVisible()) {
            if (isPressValid(x, y)) {
                evtHandled = state.handleEvent(ctx, xmlId, xmlOnPress);
            }
        }

        return evtHandled;
    }
}

class OverlayTxt extends Overlay {
    String xmlText;
    float xmlWidth;
    TextBlock textBlock;

    public void draw(DrawWindow dw) {
        if (textBlock == null) {
            textBlock = new TextBlock(xmlText, 12, xmlWidth, dw);
        }

        anchoredObj.prepare(textBlock, xmlAnchor);

        dw.drawObject(anchoredObj, xmlX - anchoredObj.getWidth() / 2, xmlY - anchoredObj.getHeight() / 2, 0, 1);
    }

    boolean press(float x, float y, ARXContext ctx, ARXState state) {
        boolean evtHandled = false;

        if (xmlOnPress != null && isVisible()) {
            if (isPressValid(x, y)) {
                evtHandled = state.handleEvent(ctx, xmlId, xmlOnPress);
            }
        }

        return evtHandled;
    }
}
