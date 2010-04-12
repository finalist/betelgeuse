package com.gamaray.arex;

import java.io.InputStream;

import java.util.HashMap;

import com.gamaray.arex.render3d.*;
import com.gamaray.arex.gui.*;
import com.gamaray.arex.geo.*;
import com.gamaray.arex.xml.*;

public interface ARXContext 
{
	public ARXDownload getARXDownload();
	public ARXRender getARXRender();
	
	public void getRotationMatrix(Matrix3D dest);
	public void getCurrentLocation(GeoPoint dest);
	
	public String getLaunchUrl();
	
	public Bitmap createBitmap(InputStream is) throws Exception;
	public Element parseXML(InputStream is) throws Exception;
	
	public ARXHttpInputStream getHttpGETInputStream(String url) throws Exception;
	public ARXHttpInputStream getHttpPOSTInputStream(String url, String params) throws Exception;
	public void returnHttpInputStream(ARXHttpInputStream is) throws Exception;
	
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
	
	public HashMap getPrefs();
	public void setPrefs(HashMap prefs);
}
