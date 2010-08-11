package com.gamaray.arex.context;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.gamaray.arex.ARXDownload;
import com.gamaray.arex.ARXRender;
import com.gamaray.arex.geo.GeoPoint;
import com.gamaray.arex.gui.Bitmap;
import com.gamaray.arex.io.ARXHttpInputStream;
import com.gamaray.arex.render3d.Matrix3D;
import com.gamaray.arex.xml.Element;

public interface ARXContext {
    public ARXDownload getARXDownload();

    public ARXRender getARXRender();

    public void getRotationMatrix(Matrix3D dest);

    public void getCurrentLocation(GeoPoint dest);

    public String getLaunchUrl();

    public Bitmap createBitmap(InputStream is) throws Exception;

    public Element parseXML(InputStream is) throws Exception;

    public ARXHttpInputStream getHttpGETInputStream(String url) throws Exception;

    public ARXHttpInputStream getHttpPOSTInputStream(String url, String params) throws Exception;

//    public void returnHttpInputStream(ARXHttpInputStream is) throws Exception;

    public InputStream getResourceInputStream(String name) throws Exception;

    public void returnResourceInputStream(InputStream is) throws Exception;

    public boolean gpsEnabled();

    public boolean gpsSignalReceived();

    public float getDeclination();

    public boolean isCompassAccuracyLow();

    public void playSound(String url) throws Exception;

    public void showWebPage(String url) throws Exception;

    public long getRandomLong();

    public double getRandomDouble();

    public Map<String,Object> getPrefs();

    public void setPrefs(Map<String,Object> prefs);
}
