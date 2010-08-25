package com.gamaray.arex.gui;

import android.view.View;


public interface DrawWindow {
    public int getWidth();

    public int getHeight();

    public void setFill(boolean fill);

    public void setColor(int c);

    public void drawLine(float x1, float y1, float x2, float y2);

    public void drawRectangle(float x, float y, float width, float height);

    public void drawCircle(float x, float y, float radius);

    public void drawBitmap(Bitmap bmp, float x, float y, float rotation, float scale);

    public void drawBitmap(int[] bmp, float x, float y, int width, int height, float rotation, float scale);

    public void drawObject(Drawable obj, float x, float y, float rotation, float scale);

    public void setFontSize(float size);

    public float getTextWidth(String txt);

    public float getTextAscent();

    public float getTextDescent();

    public float getTextLeading();

    public void drawText(float x, float y, String text);
    
    public void drawUrlWindow(String url,float x,float y);
}
