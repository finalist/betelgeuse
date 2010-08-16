package com.gamaray.arex;

import com.gamaray.arex.context.ARXContext;
import com.gamaray.arex.gui.Bitmap;
import com.gamaray.arex.gui.DrawWindow;

public class OverlayImg extends Overlay {
    
    public void draw(DrawWindow dw) {
        if (asset.downloadStatus == ARXState.READY) {
            if (!asset.downloadResult.isError()) {
                Bitmap bmp = (Bitmap) asset.downloadResult.getObj();

                anchoredObj.prepare(bmp, getXmlAnchor());

                dw.drawObject(anchoredObj, getXmlX() - anchoredObj.getWidth() / 2, getXmlY() - anchoredObj.getHeight() / 2, 0, 1);
            }
        }
    }

    boolean press(float x, float y, ARXContext ctx, ARXState state) {
        boolean evtHandled = false;

        if (asset.downloadStatus == ARXState.READY && getXmlOnPress() != null && isVisible()) {
            if (isPressValid(x, y)) {
                evtHandled = state.handleEvent(ctx, getXmlId(), getXmlOnPress());
            }
        }

        return evtHandled;
    }
}
