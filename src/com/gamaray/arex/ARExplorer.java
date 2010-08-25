package com.gamaray.arex;

import static android.hardware.SensorManager.SENSOR_DELAY_FASTEST;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;

import com.gamaray.arex.context.AndroidARXContext;
import com.gamaray.arex.gui.AndroidDrawWindow;
import com.gamaray.arex.gui.AugmentedView;
import com.gamaray.arex.gui.CameraView;
import com.gamaray.arex.gui.MenuItem;
import com.gamaray.arex.io.ARXHttpInputStream;

public class ARExplorer extends Activity{
    private boolean fatalError = false;
    private String fatalErrorMsg;
    Exception fatalErrorEx;

    CameraView cameraView;
    AugmentedView augmentedView;

    static boolean initialized = false;
    static AndroidARXContext ctx;
    private static AndroidDrawWindow drawWindow;
    private static ARXView view;
    Thread downloadThread;
    Thread renderThread;
    private PowerManager.WakeLock wakelock; 

    public static final int TOOL_BAR_HEIGHT = 26;

//    private SensorManager sensorMgr;
//    private List<Sensor> sensors;
//    private Sensor sensorGrav, sensorMag;

    
//    private String currentLocationProvider;
//    private boolean gpsEnabled = false;
//    private int gpsUpdates = 0;

    
    private ARXLocationManager locationManager=new ARXLocationManager(this);
    
    List<MenuItem> menuArray = new ArrayList<MenuItem>();


    public void doError(Exception ex1) {
        if (!fatalError) {
            fatalError = true;
            fatalErrorMsg = ex1.toString();
            fatalErrorEx = ex1;

            ex1.printStackTrace();
            Log.d("gamaray", "FATAL ERROR", ex1);

            try {
                // SPECIAL CODE
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }

        try {
            augmentedView.invalidate();
        } catch (Exception ignore) {

        }
    }

    public void killOnError() throws Exception {
        if (fatalError)
            throw new Exception();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            killOnError();
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            cameraView = new CameraView(this);
            augmentedView = new AugmentedView(this);
            setContentView(cameraView);
            addContentView(augmentedView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            if (!initialized) {
                ctx = new AndroidARXContext(this,locationManager);
                drawWindow = new AndroidDrawWindow(this);
                view = new ARXView(ctx);

                initialized = true;
            }
        } catch (Exception ex) {
            doError(ex);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (wakelock!=null){
            wakelock.release();
        }
        
        
        try {
//            try {
//                sensorMgr.unregisterListener(this, sensorGrav);
//            } catch (Exception ignore) {
//            }
//            try {
//                sensorMgr.unregisterListener(this, sensorMag);
//            } catch (Exception ignore) {
//            }
//            sensorMgr = null;

            locationManager.stop();
            
            try {
                ctx.getDownloadManager().stop();
            } catch (Exception ignore) {
            }
            try {
                ctx.getRenderManager().stop();
            } catch (Exception ignore) {
            }
            ARXHttpInputStream.setKillMode(true);

            if (fatalError) {
                Log.d("gamaray", "CALLING FINISH");
                finish();
            }
        } catch (Exception ex) {
            doError(ex);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            killOnError();

            view.doLaunch();
            view.purgeEvents();

//            double angleX, angleY;
//
//            angleX = Math.toRadians(-90);
//            m1.setTo(1f, 0f, 0f, 0f, (float) Math.cos(angleX), (float) -Math.sin(angleX), 0f, (float) Math.sin(angleX),
//                    (float) Math.cos(angleX));
//
//            angleX = Math.toRadians(-90);
//            angleY = Math.toRadians(-90);
//            m2.setTo(1f, 0f, 0f, 0f, (float) Math.cos(angleX), (float) -Math.sin(angleX), 0f, (float) Math.sin(angleX),
//                    (float) Math.cos(angleX));
//            m3.setTo((float) Math.cos(angleY), 0f, (float) Math.sin(angleY), 0f, 1f, 0f, (float) -Math.sin(angleY), 0f,
//                    (float) Math.cos(angleY));
//
//            m4.setToIdentity();
//
//            for (int i = 0; i < rotationHist.length; i++) {
//                rotationHist[i] = new Matrix3D();
//            }

//            sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
//
//            sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
//            if (sensors.size() > 0) {
//                sensorGrav = sensors.get(0);
//            }
//
//            sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
//            if (sensors.size() > 0) {
//                sensorMag = sensors.get(0);
//            }
//
//            sensorMgr.registerListener(this, sensorGrav, SENSOR_DELAY_FASTEST);
//            sensorMgr.registerListener(this, sensorMag, SENSOR_DELAY_FASTEST);

            try {
                
                if (wakelock==null){
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    wakelock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "ARExplorer");
                }
                wakelock.acquire();

                
                locationManager.start();
            
            } catch (Exception ex) {
                Log.d("gamaray", "GPS Initialize Error", ex);
            }

            ARXHttpInputStream.setKillMode(false);

            downloadThread = new Thread(ctx.getDownloadManager());
            downloadThread.start();

            renderThread = new Thread(ctx.getRenderManager());
            renderThread.start();
        } catch (Exception ex) {
            doError(ex);

            try {
//                if (sensorMgr != null) {
//                    sensorMgr.unregisterListener(this, sensorGrav);
//                    sensorMgr.unregisterListener(this, sensorMag);
//                    sensorMgr = null;
//                }

                locationManager.stop();
                
                if (ctx != null) {
                    if (ctx.getDownloadManager() != null)
                        ctx.getDownloadManager().stop();
                    if (ctx.getRenderManager() != null)
                        ctx.getRenderManager().stop();

                    ARXHttpInputStream.setKillMode(true);
                }
            } catch (Exception ignore) {

            }
        }
    }
    
//    private void unregisterLocationProvider(){
//        if (currentLocationProvider != null) {
//            LocationManager locationMgr = (LocationManager) getSystemService(LOCATION_SERVICE);            
//            locationMgr.removeUpdates(this);
//            currentLocationProvider = null;
//        }
//    }
    
//    private void updateLocationProvider(){
//        LocationManager locationMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
//        
//        Criteria criteria=new Criteria();
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);
//        
//        
//        
//        String newProvider=locationMgr.getBestProvider(criteria, true);
//        if (!newProvider.equals(currentLocationProvider)){
//            locationMgr.removeUpdates(this);
//            locationMgr.requestLocationUpdates(newProvider, 100, 1, this);
//            currentLocationProvider=newProvider;
//        }
//
//        gpsEnabled = currentLocationProvider.equals(LocationManager.GPS_PROVIDER);
//
//        Location lastFix = locationMgr.getLastKnownLocation(currentLocationProvider);
//
//        synchronized (ctx.getCurLoc()) {
//            if (lastFix != null) {
//                ctx.getCurLoc().lat = lastFix.getLatitude();
//                ctx.getCurLoc().lon = lastFix.getLongitude();
//                ctx.getCurLoc().alt = lastFix.getAltitude();
//            } else {
//                ctx.getCurLoc().lat = 45.0;
//                ctx.getCurLoc().lon = -85.0;
//                ctx.getCurLoc().alt = 0.0;
//            }
//        }
//
//        GeomagneticField gmf = new GeomagneticField((float) ctx.getCurLoc().lat, (float) ctx.getCurLoc().lon,
//                (float) ctx.getCurLoc().alt, System.currentTimeMillis());
//
////        double angleY = Math.toRadians(-gmf.getDeclination());
////        m4.setTo((float) Math.cos(angleY), 0f, (float) Math.sin(angleY), 0f, 1f, 0f, (float) -Math.sin(angleY),
////                0f, (float) Math.cos(angleY));
//        rotationCalculator.updateM4(gmf.getDeclination());
//        ctx.setDeclination(gmf.getDeclination());   
//    }
    

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        try {
            killOnError();

            menu.clear();
            menuArray.clear();

            view.createMenuEvent(menuArray);
            for (int i = 0; i < menuArray.size(); i++) {
                com.gamaray.arex.gui.MenuItem menuItem = (com.gamaray.arex.gui.MenuItem) menuArray.get(i);

                menu.add(0, menuItem.id, i, menuItem.txt);
            }

            return true;
        } catch (Exception ex) {
            doError(ex);

            return super.onPrepareOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        try {
            killOnError();

            view.menuEvent(item.getItemId());

            return true;
        } catch (Exception ex) {
            doError(ex);

            return super.onOptionsItemSelected(item);
        }
    }

//    public void onAccuracyChanged(Sensor s, int accuracy) {
//        try {
//            killOnError();
//
//            if (s.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//                if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
//                    ctx.setLowCompassAccuracy(true);
//                } else {
//                    ctx.setLowCompassAccuracy(false);
//                }
//            }
//        } catch (Exception ex) {
//            doError(ex);
//        }
//    }

//    public void onSensorChanged(SensorEvent evt) {
//        try {
//            killOnError();
//
//            float grav[] = new float[3];
//            float mag[] = new float[3];
//
//            if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//                grav = evt.values;
//                augmentedView.postInvalidate();
//            } else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//                mag=evt.values;
//                augmentedView.postInvalidate();
//            }
//            float R[] = new float[9];
//            float I[] = new float[9];
//            float RTmp[] = new float[9];
//
//            SensorManager.getRotationMatrix(RTmp, I, grav, mag);
//            SensorManager.remapCoordinateSystem(RTmp, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z, R);
//
////            calculateSmooth(R);
////            
////            rotationTmp.setTo(R[0], R[1], R[2], R[3], R[4], R[5], R[6], R[7], R[8]);
////
////            rotationFinal.setToIdentity();
////            rotationFinal.multiply(m4);
////            rotationFinal.multiply(m1);
////            rotationFinal.multiply(rotationTmp);
////            rotationFinal.multiply(m3);
////            rotationFinal.multiply(m2);
////            rotationFinal.invert(); // TODO: use transpose() instead
////
////            rotationHist[rotationHistIdx].setTo(rotationFinal);
////            rotationHistIdx++;
////            if (rotationHistIdx >= rotationHist.length)
////                rotationHistIdx = 0;
////
////            rotationSmooth.setTo(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);
////            for (int i = 0; i < rotationHist.length; i++) {
////                rotationSmooth.add(rotationHist[i]);
////            }
////            rotationSmooth.multiply(1 / (float) rotationHist.length);
//
//            synchronized (ctx.getRotationMatrix()) {
//                ctx.getRotationMatrix().setTo(rotationCalculator.calculateSmooth(R));
//            }
//        } catch (Exception ex) {
//            doError(ex);
//        }
//    }

    public boolean onTouchEvent(MotionEvent me) {
        try {
            killOnError();

            float xPress = me.getX();
            float yPress = me.getY() - TOOL_BAR_HEIGHT;

            view.clickEvent(xPress, yPress);

            return true;
        } catch (Exception ex) {
            doError(ex);

            return super.onTouchEvent(me);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            killOnError();

            if (view.isInfoBoxOpen() && keyCode == KeyEvent.KEYCODE_BACK) {
                view.keyEvent(keyCode);

                return true;
            }

            return super.onKeyDown(keyCode, event);
        } catch (Exception ex) {
            doError(ex);

            return super.onKeyDown(keyCode, event);
        }
    }

    public boolean isFatalError() {
        return fatalError;
    }

    public String getFatalErrorMsg() {
        return fatalErrorMsg;
    }

    public static AndroidDrawWindow getDrawWindow() {
        return drawWindow;
    }

    public static ARXView getView() {
        return view;
    }
}