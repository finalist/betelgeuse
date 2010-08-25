package com.gamaray.arex.gui;

import android.R;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.Button;

public class AndroidDrawWindow implements DrawWindow {
    private Canvas canvas;
    private int width;
    private int height;
    private Paint paint = new Paint();
    private Paint bmpPaint = new Paint();
    private final Activity activity;

    public AndroidDrawWindow(Activity activity) {
        paint.setTextSize(16);
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        this.activity=activity;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setFill(boolean fill) {
        if (fill)
            paint.setStyle(Paint.Style.FILL);
        else
            paint.setStyle(Paint.Style.STROKE);
    }

    public void setColor(int c) {
        paint.setColor(c);
    }

    public void drawLine(float x1, float y1, float x2, float y2) {
        canvas.drawLine(x1, y1, x2, y2, paint);
    }

    public void drawRectangle(float x, float y, float width, float height) {
        canvas.drawRect(x, y, x + width, y + height, paint);
    }

    public void drawCircle(float x, float y, float radius) {
        canvas.drawCircle(x, y, radius, paint);
    }

    public void drawText(float x, float y, String text) {
        canvas.drawText(text, x, y, paint);
    }

    public void drawBitmap(com.gamaray.arex.gui.Bitmap bmp, float x, float y, float rotation, float scale) {
        canvas.save();
        canvas.translate(x + bmp.getWidth() / 2, y + bmp.getHeight() / 2);
        canvas.rotate(rotation);
        canvas.scale(scale, scale);
        canvas.translate(-(bmp.getWidth() / 2), -(bmp.getHeight() / 2));
        canvas.drawBitmap(((AndroidBitmap) bmp).getBmp(), 0, 0, bmpPaint);
        canvas.restore();
    }

    public void drawBitmap(int[] bmp, float bmpX, float bmpY, int width, int height, float rotation, float scale) {
        canvas.save();
        canvas.translate(bmpX + width / 2, bmpY + height / 2);
        canvas.rotate(rotation);
        canvas.scale(scale, scale);
        canvas.translate(-(width / 2), -(height / 2));
        canvas.drawBitmap(bmp, 0, width, 0, 0, width, height, true, bmpPaint);
        canvas.restore();
    }

    public void drawObject(Drawable obj, float x, float y, float rotation, float scale) {
        canvas.save();
        canvas.translate(x + obj.getWidth() / 2, y + obj.getHeight() / 2);
        canvas.rotate(rotation);
        canvas.scale(scale, scale);
        canvas.translate(-(obj.getWidth() / 2), -(obj.getHeight() / 2));
        obj.draw(this);
        canvas.restore();
    }

    public float getTextWidth(String txt) {
        return paint.measureText(txt);
    }

    public float getTextAscent() {
        return -paint.ascent();
    }

    public float getTextDescent() {
        return paint.descent();
    }

    public float getTextLeading() {
        return 0;
    }

    public void setFontSize(float size) {
        paint.setTextSize(size);
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void drawUrlWindow(String url, float x, float y) {

//        WebView webView = new WebView(activity);
//        webView.loadUrl(url);
//        
//        activity.addContentView(webView, new LayoutParams(new Float(x).intValue(), new Float(y).intValue()));
        
    }

    
    
}