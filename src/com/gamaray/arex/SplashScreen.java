package com.gamaray.arex;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

public class SplashScreen extends Activity {
    private static final int SPLASHTIME = 3000;
    protected Handler exitHandler = null;
    protected Runnable exitRunnable = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.splashscreen);
        // Runnable exiting the splash screen and launching the menu
        exitRunnable = new Runnable() {
            public void run() {
                exitSplash();
            }
        };
        // Run the exitRunnable in in _splashTime ms
        exitHandler = new Handler();
        exitHandler.postDelayed(exitRunnable, SPLASHTIME);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Remove the exitRunnable callback from the handler queue
            exitHandler.removeCallbacks(exitRunnable);
            // Run the exit code manually
            exitSplash();
        }
        return true;
    }

    private void exitSplash() {
        finish();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        startActivity(new Intent("com.gamaray.arex.SplashScreen.ARExplorer"));
    }
}