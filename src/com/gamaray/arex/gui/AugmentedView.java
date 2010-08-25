package com.gamaray.arex.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.gamaray.arex.ARExplorer;
import com.gamaray.arex.event.EventBus;
import com.gamaray.arex.event.OrientationChangeEvent;

public class AugmentedView extends View implements OrientationChangeEvent.Handler{
    private ARExplorer app;

    public AugmentedView(Context context) {
        super(context);

        try {
            app = (ARExplorer) context;

            app.killOnError();
        } catch (Exception ex) {
            app.doError(ex);
        }
        EventBus.get().register(OrientationChangeEvent.TYPE, this);
        
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            if (app.isFatalError()) {
                Paint errPaint = new Paint();
                errPaint.setColor(Color.RED);
                errPaint.setTextSize(16);

                canvas.drawText("ERROR: ", 10, 20, errPaint);
                canvas.drawText("" + app.getFatalErrorMsg(), 10, 40, errPaint);

                return;
            }

            app.killOnError();

            ARExplorer.getDrawWindow().setWidth(canvas.getWidth());
            ARExplorer.getDrawWindow().setHeight(canvas.getHeight() - ARExplorer.TOOL_BAR_HEIGHT);
            ARExplorer.getDrawWindow().setCanvas(canvas);

            if (!ARExplorer.getView().isInit()) {
                ARExplorer.getView().init(ARExplorer.getDrawWindow().getWidth(), ARExplorer.getDrawWindow().getHeight());
            }

            ARExplorer.getView().draw(ARExplorer.getDrawWindow());
        } catch (Exception ex) {
            app.doError(ex);
        }
    }

    @Override
    public void onOrientationChange(OrientationChangeEvent p) {
        postInvalidate();        
    }
}
