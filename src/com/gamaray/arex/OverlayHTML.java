package com.gamaray.arex;

import com.gamaray.arex.context.ARXContext;
import com.gamaray.arex.gui.DrawWindow;

public class OverlayHTML extends Overlay {

    private String url;

    @Override
    public void draw(DrawWindow dw) {
        dw.drawUrlWindow(url, getXmlX(), getXmlY());
    }

    @Override
    boolean press(float x, float y, ARXContext ctx, ARXState state) {
        boolean evtHandled = false;

        if (getXmlOnPress() != null && isVisible()) {
            if (isPressValid(x, y)) {
                evtHandled = state.handleEvent(ctx, getXmlId(), getXmlOnPress());
            }
        }

        return evtHandled;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
