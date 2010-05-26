package com.gamaray.arex.gui;

import android.graphics.Bitmap;

public class AndroidBitmap implements com.gamaray.arex.gui.Bitmap {
    private final Bitmap bmp;

    public AndroidBitmap(Bitmap bmp) {
        this.bmp = bmp;
    }

    public float getWidth() {
        return bmp.getWidth();
    }

    public float getHeight() {
        return bmp.getHeight();
    }

    public void draw(DrawWindow dw) {
        dw.drawBitmap(this, 0, 0, 0, 1);
    }
    
    public Bitmap getBmp() {
        return bmp;
    }
}