package com.gamaray.arex;

import java.util.LinkedList;
import java.util.Queue;

import com.gamaray.arex.render3d.Matrix3D;

public class RotationCalculator {

    private static final int MAX_HISTORY_LENGHT=60;
    
    private Queue<Matrix3D> rotationHist = new LinkedList<Matrix3D>();
    private final Matrix3D m1 = new Matrix3D();
    private final Matrix3D m2 = new Matrix3D();
    private final Matrix3D m3 = new Matrix3D();
    private final Matrix3D m4 = new Matrix3D();

    public RotationCalculator() {
        double angleX, angleY;

        angleX = Math.toRadians(-90);
        m1.setTo(1f, 0f, 0f, 0f, (float) Math.cos(angleX), (float) -Math.sin(angleX), 0f, (float) Math.sin(angleX),
                (float) Math.cos(angleX));

        angleX = Math.toRadians(-90);
        angleY = Math.toRadians(-90);
        m2.setTo(1f, 0f, 0f, 0f, (float) Math.cos(angleX), (float) -Math.sin(angleX), 0f, (float) Math.sin(angleX),
                (float) Math.cos(angleX));
        m3.setTo((float) Math.cos(angleY), 0f, (float) Math.sin(angleY), 0f, 1f, 0f, (float) -Math.sin(angleY), 0f,
                (float) Math.cos(angleY));

        m4.setToIdentity();

    }

    public void updateM4(float declination) {
        double angleY = Math.toRadians(-declination);
        m4.setTo((float) Math.cos(angleY), 0f, (float) Math.sin(angleY), 0f, 1f, 0f, (float) -Math.sin(angleY), 0f,
                (float) Math.cos(angleY));
    }

    public Matrix3D calculateSmooth(float r[]) {
        Matrix3D rotationTmp = new Matrix3D();
        Matrix3D rotationFinal = new Matrix3D();

        rotationTmp.setTo(r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7], r[8]);

        rotationFinal.setToIdentity();
        rotationFinal.multiply(m4);
        rotationFinal.multiply(m1);
        rotationFinal.multiply(rotationTmp);
        rotationFinal.multiply(m3);
        rotationFinal.multiply(m2);
        rotationFinal.invert(); // TODO: use transpose() instead

        if (rotationFinal.isValid()){
            rotationHist.add(rotationFinal);
            if (rotationHist.size()>MAX_HISTORY_LENGHT){
                rotationHist.poll();
            }
        }
        
        
        return Matrix3D.average(rotationHist);
    }

}
