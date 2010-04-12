package com.gamaray.arex;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Random;

import android.app.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.ContentResolver;

import android.database.Cursor;

import android.net.Uri;

import android.os.Bundle;

import android.media.MediaPlayer;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.view.MotionEvent;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.hardware.Camera;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import static android.hardware.SensorManager.SENSOR_DELAY_FASTEST;
import static android.hardware.SensorManager.SENSOR_DELAY_UI;
import static android.hardware.SensorManager.SENSOR_DELAY_NORMAL;
import static android.hardware.SensorManager.SENSOR_MAGNETIC_FIELD;
import static android.hardware.SensorManager.SENSOR_ACCELEROMETER;

import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Location;

import android.util.Log;

import com.gamaray.arex.*;
import com.gamaray.arex.render3d.*;
import com.gamaray.arex.gui.*;
import com.gamaray.arex.geo.*;
import com.gamaray.arex.xml.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.res.AssetManager;

public class ARExplorer extends Activity implements SensorEventListener, LocationListener
{
	boolean fatalError = false;
	String fatalErrorMsg;
	Exception fatalErrorEx;
	
	CameraView cameraView;
	AugmentedView augmentedView;
	
	static boolean initialized = false;
	static AndroidARXContext ctx;
	static AndroidDrawWindow drawWindow;
	static ARXView view;
	Thread downloadThread;
	Thread renderThread;
    
	public static final int TOOL_BAR_HEIGHT = 26;
	
	float RTmp[] = new float[9];
	float R[] = new float[9];
	float I[] = new float[9];
	float grav[] = new float[3];
	float mag[] = new float[3];
	
	private SensorManager sensorMgr;
	private List<Sensor> sensors;
	private Sensor sensorGrav, sensorMag;
	private LocationManager locationMgr;
	boolean gpsEnabled = false;
	int gpsUpdates = 0;
	
	int rotationHistIdx = 0;
	Matrix3D rotationTmp = new Matrix3D();
	Matrix3D rotationFinal = new Matrix3D();
	Matrix3D rotationSmooth = new Matrix3D();
	Matrix3D rotationHist[] = new Matrix3D[60];
	Matrix3D m1 = new Matrix3D();
	Matrix3D m2 = new Matrix3D();
	Matrix3D m3 = new Matrix3D();
	Matrix3D m4 = new Matrix3D();
	
	public void doError(Exception ex1)
	{
		if (!fatalError)
		{
			fatalError = true;
			fatalErrorMsg = ex1.toString();
			fatalErrorEx = ex1;
			
			ex1.printStackTrace();
			Log.d("gamaray", "FATAL ERROR", ex1);
			
			try
			{
				// SPECIAL CODE
			}
			catch (Exception ex2)
			{
				ex2.printStackTrace();
			}
		}
		
		try
		{
			augmentedView.invalidate();
		}
		catch (Exception ignore)
		{
			
		}
	}
	
	public void killOnError() throws Exception
	{
		if (fatalError) throw new Exception();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        try
        {	
        	killOnError();
        		
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        cameraView = new CameraView(this);
	        augmentedView = new AugmentedView(this);
	        setContentView(cameraView);
	        addContentView(augmentedView, 
	        	new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	        
	        if (!initialized)
	        {
	        	ctx = new AndroidARXContext(this);
				ctx.downloadManager = new ARXDownload(ctx);
				ctx.renderManager = new ARXRender(ctx);
	        	drawWindow = new AndroidDrawWindow();
	        	view = new ARXView(ctx);
	        	
	        	initialized = true;
	        }
        }
        catch (Exception ex)
        {
        	doError(ex);
        }
    }
    
    @Override
	protected void onPause() 
	{
		super.onPause();
		
		try
		{
			try { sensorMgr.unregisterListener(this, sensorGrav); } catch (Exception ignore) { }
			try { sensorMgr.unregisterListener(this, sensorMag); } catch (Exception ignore) { }
			sensorMgr = null;
			
			try { locationMgr.removeUpdates(this);  } catch (Exception ignore) { }
			try { locationMgr = null; } catch (Exception ignore) { }
			
			try { ctx.downloadManager.stop(); } catch (Exception ignore) { }
			try { ctx.renderManager.stop(); } catch (Exception ignore) { }
			ARXHttpInputStream.killMode = true;
			
			if (fatalError)
			{
				Log.d("gamaray", "CALLING FINISH");
				finish();
			}
        }
        catch (Exception ex)
        {
        	doError(ex);
        }
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		
		try
		{
        	killOnError();
        	
			ctx.appCtx = this;
			view.doLaunch();
			view.purgeEvents();
	        
	        double angleX, angleY;
	        
	        angleX = Math.toRadians(-90);
	    	m1.setTo(
	    		1f, 0f, 0f,
	    		0f,(float) Math.cos(angleX), (float) -Math.sin(angleX),
	    		0f,(float) Math.sin(angleX), (float) Math.cos(angleX));
			
			angleX = Math.toRadians(-90);
	    	angleY = Math.toRadians(-90);
	    	m2.setTo(
	    		1f, 0f, 0f,
	    		0f,(float) Math.cos(angleX), (float) -Math.sin(angleX),
	    		0f,(float) Math.sin(angleX), (float) Math.cos(angleX));
	    	m3.setTo(
	    		(float) Math.cos(angleY), 0f, (float) Math.sin(angleY),
	    		0f, 1f, 0f,
	    		(float) -Math.sin(angleY), 0f, (float) Math.cos(angleY));
	    	
	    	m4.setToIdentity();
	    	
	    	for (int i = 0; i < rotationHist.length; i++)
	    	{
	    		rotationHist[i] = new Matrix3D();
	    	}
			
			sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
	
			sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
			if (sensors.size() > 0) { sensorGrav = sensors.get(0); }
	
			sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
			if (sensors.size() > 0) { sensorMag = sensors.get(0); }
	
			sensorMgr.registerListener(
				this, 
				sensorGrav,
				SENSOR_DELAY_FASTEST);
			sensorMgr.registerListener(
				this, 
				sensorMag,
				SENSOR_DELAY_FASTEST);
			
			try
			{	
				locationMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
				locationMgr.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 
					100, 
					1, 
					this);
					
				gpsEnabled = locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
				
				Location lastFix = null;
				try
				{	
					lastFix = locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					if (lastFix == null) lastFix = locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				}
				catch (Exception ex2)
				{
					ex2.printStackTrace();
				}
				
				synchronized(ctx.curLoc)
				{
					if (lastFix != null)
					{
						ctx.curLoc.lat = lastFix.getLatitude();
						ctx.curLoc.lon = lastFix.getLongitude();
						ctx.curLoc.alt = lastFix.getAltitude();
					}
					else
					{
						ctx.curLoc.lat = 45.0;
						ctx.curLoc.lon = -85.0;
						ctx.curLoc.alt = 0.0;
					}
				}
				
				GeomagneticField gmf = new GeomagneticField(
					(float) ctx.curLoc.lat, (float) ctx.curLoc.lon, (float) ctx.curLoc.alt, 
					System.currentTimeMillis());
					
				angleY = Math.toRadians(-gmf.getDeclination());
		    	m4.setTo(
		    		(float) Math.cos(angleY), 0f, (float) Math.sin(angleY),
		    		0f, 1f, 0f,
		    		(float) -Math.sin(angleY), 0f, (float) Math.cos(angleY));
				ctx.declination = gmf.getDeclination();
			}
			catch (Exception ex)
			{
				Log.d("gamaray", "GPS Initialize Error", ex);
			}
			
			ARXHttpInputStream.killMode = false;
			
			downloadThread = new Thread(ctx.downloadManager);
			downloadThread.start();
			
			renderThread = new Thread(ctx.renderManager);
			renderThread.start();
        }
        catch (Exception ex)
        {
        	doError(ex);
        	
        	try
        	{
        		if (sensorMgr != null)
        		{
	        		sensorMgr.unregisterListener(this, sensorGrav);
					sensorMgr.unregisterListener(this, sensorMag);
					sensorMgr = null;
        		}
				
				if (locationMgr != null)
				{
					locationMgr.removeUpdates(this);
					locationMgr = null;
				}
				
				if (ctx != null)
				{
					if (ctx.downloadManager != null) ctx.downloadManager.stop();
					if (ctx.renderManager != null) ctx.renderManager.stop();
			
					ARXHttpInputStream.killMode = true;
				}
			}
			catch (Exception ignore)
			{
				
			}
        }
	}
	
	ArrayList menuArray = new ArrayList(); 
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) 
    {
		try
		{
        	killOnError();
        	
			menu.clear();
			menuArray.clear();
			
			view.createMenuEvent(menuArray);
			for (int i = 0; i < menuArray.size(); i++)
			{
				com.gamaray.arex.gui.MenuItem menuItem = (com.gamaray.arex.gui.MenuItem) menuArray.get(i);
				
				menu.add(0, menuItem.id, i, menuItem.txt);	
			}
			
        	return true;
		}
        catch (Exception ex)
        {
        	doError(ex);
        	
        	return super.onPrepareOptionsMenu(menu);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	try
    	{
        	killOnError();
        	
        	view.menuEvent(item.getItemId());

        	return true;
    	}
        catch (Exception ex)
        {
        	doError(ex);
        	
        	return super.onOptionsItemSelected(item);
        }
    }
	
	public void onAccuracyChanged(Sensor s, int accuracy) 
	{
		try
		{
        	killOnError();
        	
			if (s.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			{
				if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW)
				{
					ctx.lowCompassAccuracy = true;
				}
				else
				{
					ctx.lowCompassAccuracy = false;
				}
			}
		}
        catch (Exception ex)
        {
        	doError(ex);
        }
	}

	public void onSensorChanged(SensorEvent evt) 
	{
		try
		{
        	killOnError();
        	
			if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) 
			{
				grav[0] = evt.values[0];
				grav[1] = evt.values[1];
				grav[2] = evt.values[2];
				
				augmentedView.postInvalidate();
			}
			else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)  
			{
				mag[0] = evt.values[0];
				mag[1] = evt.values[1];
				mag[2] = evt.values[2];
				
				augmentedView.postInvalidate();
			}
			
			SensorManager.getRotationMatrix(RTmp,I,grav,mag);
			SensorManager.remapCoordinateSystem(RTmp, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z, R);
			
			rotationTmp.setTo(
				R[0], R[1], R[2],
				R[3], R[4], R[5],
				R[6], R[7], R[8] );
			
			rotationFinal.setToIdentity();
	    	rotationFinal.multiply(m4);
	    	rotationFinal.multiply(m1);
			rotationFinal.multiply(rotationTmp);
	    	rotationFinal.multiply(m3);
	    	rotationFinal.multiply(m2);
	    	rotationFinal.invert(); // TODO: use transpose() instead
	    	
	    	rotationHist[rotationHistIdx].setTo(rotationFinal);
	    	rotationHistIdx++;
	    	if (rotationHistIdx >= rotationHist.length) rotationHistIdx = 0;
	    	
	    	rotationSmooth.setTo(
	    		0f, 0f, 0f,
	    		0f, 0f, 0f,
	    		0f, 0f, 0f);
	    	for (int i = 0; i < rotationHist.length; i++)
	    	{
	    		rotationSmooth.add(rotationHist[i]);
	    	}
	    	rotationSmooth.multiply(1 / (float) rotationHist.length);
	    	
			synchronized(ctx.rotationMatrix)
			{
				ctx.rotationMatrix.setTo(rotationSmooth);
			}
		}
        catch (Exception ex)
        {
        	doError(ex);
        }
	}
	
	public boolean onTouchEvent(MotionEvent me)
	{
		try
		{
        	killOnError();
        	
			float xPress = me.getX();
			float yPress = me.getY() - TOOL_BAR_HEIGHT;
			
			view.clickEvent(xPress, yPress);
		
			return true;
		}
        catch (Exception ex)
        {
        	doError(ex);
        	
        	return super.onTouchEvent(me);
        }
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		try
		{
        	killOnError();
        	
			if (view.isInfoBoxOpen() && keyCode == KeyEvent.KEYCODE_BACK)
			{
				view.keyEvent(keyCode);
				
				return true;
			}
			
			return super.onKeyDown(keyCode, event);
		}
        catch (Exception ex)
        {
        	doError(ex);
        	
        	return super.onKeyDown(keyCode, event);
        }
	}
	
	public void onProviderDisabled(String provider)
	{
		gpsEnabled = locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	
	public void onProviderEnabled(String provider)
	{
		gpsEnabled = locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		
	}
	
	public void onLocationChanged(Location location)
	{
		try
		{
        	killOnError();
        	
			if (LocationManager.GPS_PROVIDER.equals(location.getProvider()))
			{
				synchronized(ctx.curLoc)
				{
					ctx.curLoc.lat = location.getLatitude();
					ctx.curLoc.lon = location.getLongitude();
					ctx.curLoc.alt = location.getAltitude();
				}
				
				gpsUpdates++;
			}
		}
        catch (Exception ex)
        {
        	doError(ex);
        }
	}
}

class CameraView extends SurfaceView implements SurfaceHolder.Callback 
{
	ARExplorer app;
	SurfaceHolder holder;
    Camera camera;

    CameraView(Context context) 
    {
        super(context);
        
        try
        {
	        app = (ARExplorer) context;
        	
	        holder = getHolder();
	        holder.addCallback(this);
	        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        catch (Exception ex)
        {
        	
        }
    }

    public void surfaceCreated(SurfaceHolder holder) 
    {
    	try
    	{
    		if (camera != null)
    		{
        		try { camera.stopPreview(); } catch (Exception ignore) { }
		    	try { camera.release(); } catch (Exception ignore) { }
		        camera = null;
    		}
    		
        	camera = Camera.open();
        	camera.setPreviewDisplay(holder);
    	}
    	catch(Exception ex)
    	{
    		try
    		{
    			if (camera != null)
	    		{
	        		try { camera.stopPreview(); } catch (Exception ignore) { }
		    		try { camera.release(); } catch (Exception ignore) { }
		        	camera = null;
	    		}
    		}
    		catch (Exception ignore)
    		{
    			
    		}
    	}
    }

    public void surfaceDestroyed(SurfaceHolder holder) 
    {
    	try
    	{
    		if (camera != null)
    		{
        		try { camera.stopPreview(); } catch (Exception ignore) { }
	    		try { camera.release(); } catch (Exception ignore) { }
	        	camera = null;
    		}
    	}
        catch (Exception ex)
        {
        	ex.printStackTrace();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) 
    {
    	try
    	{
        	camera.startPreview();
    	}
        catch (Exception ex)
        {
        	ex.printStackTrace();
        }
    }
}

class AugmentedView extends View 
{
	ARExplorer app;
        
	public AugmentedView(Context context) 
	{
		super(context);
        	
        try
        {
        	app = (ARExplorer) context;
        	
        	app.killOnError();
        }
        catch (Exception ex)
        {
        	app.doError(ex);
        }
	}
	
	@Override
	protected void onDraw(Canvas canvas) 
	{
		try
		{
			if (app.fatalError)
			{
				Paint errPaint = new Paint();
				errPaint.setColor(Color.RED);
				errPaint.setTextSize(16);
				
				canvas.drawText("ERROR: ", 10, 20, errPaint);
				canvas.drawText("" + app.fatalErrorMsg, 10, 40, errPaint);
				
				return;
			}
			
        	app.killOnError();
			
			app.drawWindow.width = canvas.getWidth();
			app.drawWindow.height = canvas.getHeight() - app.TOOL_BAR_HEIGHT;
			app.drawWindow.canvas = canvas;
		
			if (!app.view.isInit())
			{
				app.view.init(app.drawWindow.width, app.drawWindow.height);
			}
			
			app.view.draw(app.drawWindow);
		}
		catch (Exception ex)
		{
			app.doError(ex);
		}
	}
}

class AndroidDrawWindow implements DrawWindow
{
	Canvas canvas;
	int width, height;
    Paint paint = new Paint();
    Paint bmpPaint = new Paint();
	
	public AndroidDrawWindow()
	{
        paint.setTextSize(16);
    	paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public void setFill(boolean fill)
	{
		if (fill)
			paint.setStyle(Paint.Style.FILL);
		else
			paint.setStyle(Paint.Style.STROKE);
	}
	
	public void setColor(int c)
	{
		paint.setColor(c);
	}
	
	public void drawLine(float x1, float y1, float x2, float y2)
	{
		canvas.drawLine(x1, y1 ,x2 ,y2 , paint);
	}
	
	public void drawRectangle(float x, float y, float width, float height)
	{
		canvas.drawRect(x, y, x+width, y+height, paint);
	}
	
	public void drawCircle(float x, float y, float radius)
	{
		canvas.drawCircle(x, y, radius, paint);
	}
	
	public void drawText(float x, float y, String text)
	{
		canvas.drawText(text, x, y, paint);
	}
	
	public void drawBitmap(com.gamaray.arex.gui.Bitmap bmp, float x, float y, float rotation, float scale)
	{
		canvas.save();
		canvas.translate(x + bmp.getWidth()/2, y + bmp.getHeight()/2);
		canvas.rotate(rotation);
		canvas.scale(scale,scale);
		canvas.translate(-(bmp.getWidth()/2), -(bmp.getHeight()/2));
		canvas.drawBitmap(((AndroidBitmap) bmp).bmp, 0, 0, bmpPaint);
		canvas.restore();
	}
	
	public void drawBitmap(int[] bmp, float bmpX, float bmpY, int width, int height, float rotation, float scale)
	{
		canvas.save();
		canvas.translate(bmpX + width/2, bmpY + height/2);
		canvas.rotate(rotation);
		canvas.scale(scale,scale);
		canvas.translate(-(width/2), -(height/2));
		canvas.drawBitmap(bmp, 0, width, 0, 0, width, height, true, bmpPaint);
		canvas.restore();
	}
	
	public void drawObject(Drawable obj, float x, float y, float rotation, float scale)
	{
		canvas.save();
		canvas.translate(x + obj.getWidth()/2, y + obj.getHeight()/2);
		canvas.rotate(rotation);
		canvas.scale(scale,scale);
		canvas.translate(-(obj.getWidth()/2), -(obj.getHeight()/2));
		obj.draw(this);
		canvas.restore();
	}
	
	public float getTextWidth(String txt)
	{
		return paint.measureText(txt);
	}
	
	public float getTextAscent()
	{
		return -paint.ascent();
	}
	
	public float getTextDescent()
	{
		return paint.descent();
	}
	
	public float getTextLeading()
	{
		return 0;
	}
	
	public void setFontSize(float size)
	{
		paint.setTextSize(size);
	}
}

class AndroidARXContext implements ARXContext
{
	ARExplorer appCtx;
	
	Random rand;
	
	ARXDownload downloadManager;
	ARXRender renderManager;
	
	GeoPoint curLoc = new GeoPoint();
	Matrix3D rotationMatrix = new Matrix3D();
	
	boolean lowCompassAccuracy = false;
	float declination = 0f;
	
	public AndroidARXContext(Context appCtx)
	{
		this.appCtx = (ARExplorer) appCtx;
		
		rotationMatrix.setToIdentity();
		
		int locationHash = 0;
		try
		{
			LocationManager locationMgr = (LocationManager) appCtx.getSystemService(appCtx.LOCATION_SERVICE);
			Location lastFix = locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (lastFix == null) lastFix = locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			
			if (lastFix != null) locationHash = ("HASH_" + lastFix.getLatitude() + "_" + lastFix.getLongitude()).hashCode();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		rand = new Random(System.currentTimeMillis() + locationHash);
	}
	
	public boolean gpsEnabled()
	{
		return appCtx.gpsEnabled;
	}
	
	public boolean gpsSignalReceived()
	{
		return (appCtx.gpsUpdates > 0);
	}
	
	public ARXDownload getARXDownload()
	{
		return downloadManager;
	}
	
	public ARXRender getARXRender()
	{
		return renderManager;
	}
	
	public float getDeclination()
	{
		return declination;
	}
	
	public boolean isCompassAccuracyLow()
	{
		return lowCompassAccuracy;
	}
	
	public String getLaunchUrl()
	{
		Intent intent = ((Activity) appCtx).getIntent();
		
		if (intent.getAction().equals(intent.ACTION_VIEW))
    	{
    		return intent.getData().toString();
    	}
    	else
    	{
    		return "";
    	}
	}
	
	public void getRotationMatrix(Matrix3D dest)
	{
		synchronized(rotationMatrix)
		{
			dest.setTo(rotationMatrix);
		}
	}
	
	public void getCurrentLocation(GeoPoint dest)
	{
		synchronized(curLoc)
		{
			dest.lat = curLoc.lat;
			dest.lon = curLoc.lon;
			dest.alt = curLoc.alt;
		}
	}
	
	public com.gamaray.arex.gui.Bitmap createBitmap(InputStream is)
		throws Exception
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bmp = BitmapFactory.decodeStream(is, null, options);
        
        if (bmp == null) throw new Exception("Error creating bitmap");
        
        return new AndroidBitmap(bmp);
	}
	
	public com.gamaray.arex.xml.Element parseXML(InputStream is)
		throws Exception
	{
	    AndroidXMLHandler handler = new AndroidXMLHandler();
	            
        SAXParserFactory spf = null;
        SAXParser sp = null;
                
        spf = SAXParserFactory.newInstance();
        if (spf != null) 
        {
            sp = spf.newSAXParser();
            sp.parse(is, handler);
        }
		
		return handler.root;
	}
	
	public ARXHttpInputStream getHttpGETInputStream(String urlStr) throws Exception
	{
		InputStream is = null;
		HttpURLConnection conn = null;
		
		if (urlStr.startsWith("gamaray://"))
		{
			urlStr = "http://" + urlStr.substring(10,urlStr.length());
			Log.d("gamaray", "urlStr="+urlStr);	
		}
		
		if (urlStr.startsWith("content://")) return getContentInputStream(urlStr, null);
		
		try
		{
	        URL url = new URL(urlStr);
	        conn = (HttpURLConnection) url.openConnection();
	        conn.setReadTimeout(10000);
	        conn.setConnectTimeout(10000);
	        
	        is = conn.getInputStream();
	        ARXHttpInputStream arxis = new AndroidHttpInputStream(conn, is, conn.getContentLength());
	        
	        return arxis;
		}
		catch (Exception ex)
		{
			try { is.close(); } catch (Exception ignore) { }
			try { conn.disconnect(); } catch (Exception ignore) { }
			
			throw ex;
		}
	}
	
	public ARXHttpInputStream getHttpPOSTInputStream(String urlStr, String params) throws Exception
	{
		InputStream is = null;
		OutputStream os = null;
		HttpURLConnection conn = null;
		
		if (urlStr.startsWith("gamaray://"))
		{
			urlStr = "http://" + urlStr.substring(10,urlStr.length());
			Log.d("gamaray", "urlStr="+urlStr);	
		}
		
		if (urlStr.startsWith("content://")) return getContentInputStream(urlStr, params);
		
		try
		{
	        URL url = new URL(urlStr);
	        conn = (HttpURLConnection) url.openConnection();
	        conn.setReadTimeout(10000);
	        conn.setConnectTimeout(10000);
	        
	        if (params != null)
	        {
				conn.setDoOutput(true);
				os = conn.getOutputStream();
		        OutputStreamWriter wr = new OutputStreamWriter(os);
		        wr.write(params);
		        wr.close();
	        }
	        
	        is = conn.getInputStream();
	        ARXHttpInputStream arxis = new AndroidHttpInputStream(conn, is, conn.getContentLength());
	        	
	        return arxis;
		}
		catch (Exception ex)
		{
			try { is.close(); } catch (Exception ignore) { }
			try { os.close(); } catch (Exception ignore) { }
			try { conn.disconnect(); } catch (Exception ignore) { }
			
			if (conn != null && conn.getResponseCode() == 405)
			{
				return getHttpGETInputStream(urlStr);
			}
			else
			{
				throw ex;
			}
		}
	}
	
	public ARXHttpInputStream getContentInputStream(String urlStr, String params) throws Exception
	{
		ContentResolver cr = appCtx.getContentResolver();
		Cursor cur = cr.query(Uri.parse(urlStr), null, params, null, null);
		
		cur.moveToFirst();
		int mode = cur.getInt(cur.getColumnIndex("MODE"));
		
		if (mode == 1)
		{
        	String result = cur.getString(cur.getColumnIndex("RESULT"));
        	cur.deactivate();
        	
    		return new ARXHttpInputStream(new ByteArrayInputStream(result.getBytes()), result.length());
		}
		else
		{
        	cur.deactivate();
        	
			throw new Exception("Invalid content:// mode " + mode);
		}
	}
	
	public void returnHttpInputStream(ARXHttpInputStream is) throws Exception
	{
		if (is != null)
		{
			is.close();
		}
	}
	
	public InputStream getResourceInputStream(String name) throws Exception
	{
		AssetManager mgr = appCtx.getAssets();
		
		return mgr.open(name);
	}
	
	public void returnResourceInputStream(InputStream is) throws Exception
	{
		if (is != null) is.close();	
	}
	
	public void playSound(String url) throws Exception
	{	
		//MediaPlayer mp = new MediaPlayer();
	    //mp.setDataSource("http://worldonefive.s3.amazonaws.com/raygun.wav");
	    //mp.prepare();
	    //mp.start();
	}
	
	public void showWebPage(String url) throws Exception
	{
		appCtx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); 
	}
	
	public long getRandomLong()
	{
		return rand.nextLong();
	}
	
	public double getRandomDouble()
	{
		return rand.nextDouble();
	}
	
	public HashMap getPrefs()
	{
		SharedPreferences settings = appCtx.getSharedPreferences("ARX_PREFS", 0);
		
		return new HashMap(settings.getAll());
	}
	
	public void setPrefs(HashMap prefs)
	{
		SharedPreferences settings = appCtx.getSharedPreferences("ARX_PREFS", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		
		for (Iterator itr = prefs.keySet().iterator(); itr.hasNext();)
		{
			String key = (String) itr.next();
			editor.putString(key, (String) prefs.get(key));
		}
		
		editor.commit();
	}
}

class AndroidHttpInputStream extends ARXHttpInputStream
{
	HttpURLConnection conn;
	
	public AndroidHttpInputStream(HttpURLConnection conn, InputStream is, int contentLength)
	{
		super(new BufferedInputStream(is), contentLength);
		
		this.conn = conn;
	}
	
	public void close()
		throws IOException
	{
		super.close();
		
		conn.disconnect();
	}
}

class AndroidXMLHandler extends DefaultHandler
{
	Element root = new Element();
	Element cur;
	
	public AndroidXMLHandler()
	{
		root.parentElement = null;
		root.isRoot = true;
		cur = root;
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) 
    {
    	Element newElem = new Element();
    	newElem.name = localName;
    	newElem.parentElement = cur;
    	for (int i = 0; i < attributes.getLength(); i++)
        {
        	newElem.attribs.put(attributes.getLocalName(i), attributes.getValue(i));
        }
    	
    	cur.childElements.add(newElem);
    	cur = newElem;
    }
    
    public void endElement(String uri, String localName, String qName) 
    {
    	cur = cur.parentElement;
    }
    
    public void characters(char[] ch, int start, int length) 
    {
    	if (cur.content == null) 
    		cur.content = String.valueOf(ch, start, length);
    	else
    		cur.content += String.valueOf(ch, start, length);
    }
}

class AndroidBitmap implements com.gamaray.arex.gui.Bitmap
{
	Bitmap bmp;	
		
	public AndroidBitmap(Bitmap bmp)
	{
		this.bmp = bmp;
	}
	
	public float getWidth()
	{
		return bmp.getWidth();
	}
	
	public float getHeight()
	{
		return bmp.getHeight();
	}
	
	public void draw(DrawWindow dw)
	{
		dw.drawBitmap(this, 0, 0, 0, 1);
	}
}