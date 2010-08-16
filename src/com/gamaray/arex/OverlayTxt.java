package com.gamaray.arex;

import com.gamaray.arex.context.ARXContext;
import com.gamaray.arex.gui.DrawWindow;
import com.gamaray.arex.gui.TextBlock;

public class OverlayTxt extends Overlay {

    private String xmlText;
    private float xmlWidth;
    private TextBlock textBlock;

    public void draw(DrawWindow dw) {
        if (textBlock == null) {
            textBlock = new TextBlock(xmlText, 12, xmlWidth, dw);
        }

        anchoredObj.prepare(textBlock, getXmlAnchor());

        dw.drawObject(anchoredObj, getXmlX() - anchoredObj.getWidth() / 2, getXmlY() - anchoredObj.getHeight() / 2, 0, 1);
    }

    boolean press(float x, float y, ARXContext ctx, ARXState state) {
        boolean evtHandled = false;

        if (getXmlOnPress() != null && isVisible()) {
            if (isPressValid(x, y)) {
                evtHandled = state.handleEvent(ctx, getXmlId(), getXmlOnPress());
            }
        }

        return evtHandled;
    }

    public String getXmlText() {
        return xmlText;
    }

    public void setXmlText(String xmlText) {
        this.xmlText = xmlText;
    }

    public float getXmlWidth() {
        return xmlWidth;
    }

    public void setXmlWidth(float xmlWidth) {
        this.xmlWidth = xmlWidth;
    }
    
    
}
