package com.gamaray.arex.context;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.gamaray.arex.ARExplorer;
import com.gamaray.arex.ARXDownload;
import com.gamaray.arex.ARXLocationManager;
import com.gamaray.arex.ARXRender;
import com.gamaray.arex.geo.GeoPoint;
import com.gamaray.arex.gui.AndroidBitmap;
import com.gamaray.arex.io.ARXHttpInputStream;
import com.gamaray.arex.io.AndroidHttpInputStream;
import com.gamaray.arex.loader.FlushedInputStream;
import com.gamaray.arex.render3d.Matrix3D;
import com.gamaray.arex.xml.AndroidXMLHandler;

public class AndroidARXContext implements ARXContext {

    private final ARExplorer appCtx;
    private final ARXLocationManager locationManager;

    private Random rand=new Random(System.currentTimeMillis());

    private final ARXDownload downloadManager;
    private final ARXRender renderManager;

    // private GeoPoint curLoc = new GeoPoint();
    private Matrix3D rotationMatrix = new Matrix3D();

    // private boolean lowCompassAccuracy = false;
    // private float declination = 0f;

    public AndroidARXContext(Context appCtx, ARXLocationManager locationManager) {
        this.appCtx = (ARExplorer) appCtx;
        downloadManager = new ARXDownload(this);
        renderManager = new ARXRender(this);

        this.locationManager = locationManager;
        // rotationMatrix.setToIdentity();
        //
        // int locationHash = 0;
        // try {
        // LocationManager locationMgr = (LocationManager)
        // appCtx.getSystemService(Context.LOCATION_SERVICE);
        // Location lastFix =
        // locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        // if (lastFix == null) {
        // lastFix =
        // locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        // } else {
        // locationHash = ("HASH_" + lastFix.getLatitude() + "_" +
        // lastFix.getLongitude()).hashCode();
        // }
        // } catch (Exception ex) {
        // ex.printStackTrace();
        // }

        // rand = new Random(System.currentTimeMillis() + locationManager.getl);
    }

    // public boolean gpsEnabled() {
    // return appCtx.isGpsEnabled();
    // }
    //
    // public boolean gpsSignalReceived() {
    // return (appCtx.getGpsUpdates() > 0);
    // }

    public ARXDownload getARXDownload() {
        return downloadManager;
    }

    public ARXRender getARXRender() {
        return renderManager;
    }

    public float getDeclination() {
        return locationManager.getDeclination();
    }

    public boolean isCompassAccuracyLow() {
        return locationManager.isLowCompassAccuracy();
    }

    public String getLaunchUrl() {
        Intent intent = ((Activity) appCtx).getIntent();

        if (intent.getAction().equals(Intent.ACTION_VIEW)) {
            return intent.getData().toString();
        } else {
            return "";
        }
    }

//    @Override
//    public void getRotationMatrix(Matrix3D dest) {
//        // TODO:very odd seems to be a setter rather than a getter
//        synchronized (rotationMatrix) {
//            dest.setTo(rotationMatrix);
//        }
//    }

    @Override
    public Matrix3D getRotationMatrix() {
        return rotationMatrix;
    }

    // public void getCurrentLocation(GeoPoint dest) {
    // Location location = locationManager.getCurrentLocation();
    // dest.lat = location.getLatitude();
    // dest.lon = location.getLongitude();
    // dest.alt = location.getAltitude();
    // }

    public com.gamaray.arex.gui.Bitmap createBitmap(InputStream is) throws Exception {
        Bitmap bmp = BitmapFactory.decodeStream(new FlushedInputStream(is), null, null);

        if (bmp == null) {
            throw new Exception("Error creating bitmap");
        }
        return new AndroidBitmap(bmp);
    }

    public com.gamaray.arex.xml.Element parseXML(InputStream is) throws Exception {
        AndroidXMLHandler handler = new AndroidXMLHandler();

        SAXParserFactory spf = null;
        SAXParser sp = null;

        spf = SAXParserFactory.newInstance();
        if (spf != null) {
            sp = spf.newSAXParser();
            sp.parse(is, handler);
        }

        return handler.getRoot();
    }

    public ARXHttpInputStream getHttpGETInputStream(String urlStr) throws Exception {
        InputStream is = null;
        HttpURLConnection conn = null;

        if (urlStr.startsWith("gamaray://")) {
            urlStr = "http://" + urlStr.substring(10, urlStr.length());
            Log.d("gamaray", "urlStr=" + urlStr);
        }

        if (urlStr.startsWith("content://"))
            return getContentInputStream(urlStr, null);

        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(10000);

            is = conn.getInputStream();
            ARXHttpInputStream arxis = new AndroidHttpInputStream(conn, is, conn.getContentLength());

            return arxis;
        } catch (Exception ex) {
            try {
                is.close();
            } catch (Exception ignore) {
            }
            try {
                conn.disconnect();
            } catch (Exception ignore) {
            }

            throw ex;
        }
    }

    public ARXHttpInputStream getHttpPOSTInputStream(String urlStr, String params) throws Exception {
        InputStream is = null;
        OutputStream os = null;
        HttpURLConnection conn = null;

        if (urlStr.startsWith("gamaray://")) {
            urlStr = "http://" + urlStr.substring(10, urlStr.length());
            Log.d("gamaray", "urlStr=" + urlStr);
        }

        if (urlStr.startsWith("content://"))
            return getContentInputStream(urlStr, params);

        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(10000);

            if (params != null) {
                conn.setDoOutput(true);
                os = conn.getOutputStream();
                OutputStreamWriter wr = new OutputStreamWriter(os);
                wr.write(params);
                wr.close();
            }

            is = conn.getInputStream();
            ARXHttpInputStream arxis = new AndroidHttpInputStream(conn, is, conn.getContentLength());

            return arxis;
        } catch (Exception ex) {
            try {
                is.close();
            } catch (Exception ignore) {
            }
            try {
                os.close();
            } catch (Exception ignore) {
            }
            try {
                conn.disconnect();
            } catch (Exception ignore) {
            }

            if (conn != null && conn.getResponseCode() == 405) {
                return getHttpGETInputStream(urlStr);
            } else {
                throw ex;
            }
        }
    }

    public ARXHttpInputStream getContentInputStream(String urlStr, String params) throws Exception {
        ContentResolver cr = appCtx.getContentResolver();
        Cursor cur = cr.query(Uri.parse(urlStr), null, params, null, null);

        cur.moveToFirst();
        int mode = cur.getInt(cur.getColumnIndex("MODE"));

        if (mode == 1) {
            String result = cur.getString(cur.getColumnIndex("RESULT"));
            cur.deactivate();

            return new ARXHttpInputStream(new ByteArrayInputStream(result.getBytes()), result.length());
        } else {
            cur.deactivate();

            throw new Exception("Invalid content:// mode " + mode);
        }
    }

    public InputStream getResourceInputStream(String name) throws Exception {
        AssetManager mgr = appCtx.getAssets();

        return mgr.open(name);
    }

    public void returnResourceInputStream(InputStream is) throws Exception {
        if (is != null)
            is.close();
    }

    public void playSound(String url) throws Exception {
        // MediaPlayer mp = new MediaPlayer();
        // mp.setDataSource("http://worldonefive.s3.amazonaws.com/raygun.wav");
        // mp.prepare();
        // mp.start();
    }

    public void showWebPage(String url) throws Exception {
        appCtx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    public long getRandomLong() {
        return rand.nextLong();
    }

    public double getRandomDouble() {
        return rand.nextDouble();
    }

    @Override
    public Map<String, Object> getPrefs() {
        SharedPreferences settings = appCtx.getSharedPreferences("ARX_PREFS", 0);

        return new HashMap<String, Object>(settings.getAll());
    }

    public void setPrefs(Map<String, Object> prefs) {
        SharedPreferences settings = appCtx.getSharedPreferences("ARX_PREFS", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();

        for (String key : prefs.keySet()) {
            if (prefs.get(key) != null) {
                editor.putString(key, prefs.get(key).toString());
            }
        }

        editor.commit();
    }

    public ARXDownload getDownloadManager() {
        return downloadManager;
    }

    public ARXRender getRenderManager() {
        return renderManager;
    }

    public GeoPoint getCurrentLocation() {
        Location location = locationManager.getCurrentLocation();
        return new GeoPoint(location.getLatitude(), location.getLongitude(), location.getAltitude());
    }

    @Override
    public boolean gpsEnabled() {
        return locationManager.isGpsEnabled();
    }

    @Override
    public boolean gpsSignalReceived() {
        return locationManager.getGpsUpdates()>0;
    }

}
