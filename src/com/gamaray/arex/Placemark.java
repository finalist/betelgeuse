package com.gamaray.arex;

import com.gamaray.arex.context.ARXContext;
import com.gamaray.arex.geo.GeoPoint;
import com.gamaray.arex.geo.GeoUtil;
import com.gamaray.arex.gui.Bitmap;
import com.gamaray.arex.gui.DrawWindow;
import com.gamaray.arex.gui.Drawable;
import com.gamaray.arex.gui.TextBlock;
import com.gamaray.arex.gui.Vector2D;
import com.gamaray.arex.render3d.Camera;
import com.gamaray.arex.render3d.Color;
import com.gamaray.arex.render3d.Matrix3D;
import com.gamaray.arex.render3d.Mesh3D;
import com.gamaray.arex.render3d.Object3D;
import com.gamaray.arex.render3d.Vector3D;

abstract public class Placemark {
    // XML properties
    String xmlId;
    String xmlName;
    String xmlAssetId;
    String xmlLocationId;
    boolean xmlShowInRadar;
    String xmlOnPress;
    String xmlShowOnFocus;
    float xmlLocX, xmlLocY, xmlLocZ;
    GeoPoint xmlGeoLoc = new GeoPoint();

    // State properties
    float locX, locY, locZ;
    GeoPoint geoLoc = new GeoPoint();
    Object3D obj = new Object3D();

    // Pointer to asset
    Asset asset;

    // Pointer to layer
    Layer layer;

    // Draw properties
    boolean isVisible, isCenterVisible, isLookingAt;
    float distToCenter;
    Vector3D centerMark = new Vector3D();
    Vector3D signPostMark = new Vector3D();
    Vector3D originMark = new Vector3D();
    static float BLOB_OUTER = 20, BLOB_INNER = 16, BLOB_WORK = 4;
    static int blobOuterColor = Color.rgb(0, 0, 255), blobInnerColor = Color.rgb(100, 100, 255), blobWorkColor = Color
            .rgb(50, 50, 255);
    static int errOuterColor = Color.rgb(255, 0, 0), errInnerColor = Color.rgb(255, 100, 100);

    // Temp properties
    Vector3D tmp1 = new Vector3D();
    Vector3D tmp2 = new Vector3D();
    Vector3D loc = new Vector3D();
    Vector3D origin = new Vector3D(0, 0, 0);
    Vector3D upVector = new Vector3D(0, 1, 0);
    Vector2D pressPt = new Vector2D();

    abstract void updateState(GeoPoint curGPSFix, long time);

    abstract void prepareDraw(Camera cam);

    abstract void draw(DrawWindow dw);

    abstract boolean press(float x, float y, ARXContext ctx, ARXState state);

    void calcCenterMark(Vector3D originalPoint, Camera viewCam) {
        tmp1.setTo(originalPoint);
        tmp1.transform(obj.transform);
        tmp1.add(obj.location);
        tmp1.subtract(viewCam.location);
        tmp1.transform(viewCam.transform);
        viewCam.projectPoint(tmp1, tmp2);
        centerMark.setTo(tmp2);
    }

    void calcSignPostMark(Vector3D originalPoint, Camera viewCam) {
        tmp1.setTo(originalPoint);
        tmp1.add(obj.location);
        tmp1.subtract(viewCam.location);
        tmp1.transform(viewCam.transform);
        viewCam.projectPoint(tmp1, tmp2);
        signPostMark.setTo(tmp2);
    }

    void calcOriginMark(Vector3D originalPoint, Camera viewCam) {
        tmp1.setTo(originalPoint);
        tmp1.transform(obj.transform);
        tmp1.add(obj.location);
        tmp1.subtract(viewCam.location);
        tmp1.transform(viewCam.transform);
        viewCam.projectPoint(tmp1, tmp2);
        originMark.setTo(tmp2);
    }

    void drawDistance(DrawWindow dw) {
        if (layer.distancesVisible) {
            dw.setFontSize(12);
            String txt = ARXUtil.formatDistance(obj.location.length());
            float padw = 4, padh = 2;
            float w = dw.getTextWidth(txt) + padw * 2;
            float h = dw.getTextAscent() + dw.getTextDescent() + padh * 2;

            dw.setColor(Color.rgb(0, 0, 0));
            dw.setFill(true);
            dw.drawRectangle(centerMark.x - w / 2, centerMark.y - h / 2, w, h);
            dw.setColor(Color.rgb(255, 255, 255));
            dw.setFill(false);
            dw.drawRectangle(centerMark.x - w / 2, centerMark.y - h / 2, w, h);
            dw.drawText(padw + centerMark.x - w / 2, padh + dw.getTextAscent() + centerMark.y - h / 2, txt);
        }
    }

    void determineVisibility(Camera viewCam) {
        isVisible = false;
        isCenterVisible = false;
        isLookingAt = false;
        distToCenter = Float.MAX_VALUE;

        if (centerMark.z < -1f) {
            isVisible = true;

            if (ARXUtil.pointInRectangle(centerMark.x, centerMark.y, 0, 0, viewCam.width, viewCam.height)) {
                isCenterVisible = true;

                float xDist = centerMark.x - viewCam.width / 2;
                float yDist = centerMark.y - viewCam.height / 2;
                float dist = xDist * xDist + yDist * yDist;

                distToCenter = (float) Math.sqrt(dist);

                if (dist < 50 * 50) {
                    isLookingAt = true;
                }
            }
        }
    }
}

class Placemark3D extends Placemark {
    // XML properties
    float xmlRotX, xmlRotY, xmlRotZ;
    float xmlScale;

    // State properties
    float rotX, rotY, rotZ;
    float scale;

    Matrix3D rotXMatrix = new Matrix3D(), rotYMatrix = new Matrix3D(), rotZMatrix = new Matrix3D();
    Matrix3D scaleMatrix = new Matrix3D();

    // Render properties
    static float angleThreshold = (float) Math.cos(Math.toRadians(5));
    static float distThreshold = 0.1f;
    static int timeThreshold;
    long lastRender = 0;
    boolean stale = true;
    int nextRenderStatus = ARXState.NOT_STARTED;
    String renderJobId;
    RenderJobResult renderResult;

    static int INITIAL = 0, ASSET_DOWNLOADING = 1, RENDER_READY = 2, ERROR = 3;
    int drawMode = INITIAL;

    public Placemark3D() {

    }

    void updateState(GeoPoint curGPSFix, long time) {
        // Do updates
        geoLoc.setTo(xmlGeoLoc);
        rotX = xmlRotX;
        rotY = xmlRotY;
        rotZ = xmlRotZ;
        scale = xmlScale;
        locX = xmlLocX;
        locY = xmlLocY;
        locZ = xmlLocZ;

        // Set up Object3D
        loc.setTo(locX, locY, locZ);
        scaleMatrix.setToScale(scale);
        rotXMatrix.setToRotationX((float) Math.toRadians(rotX));
        rotYMatrix.setToRotationY((float) Math.toRadians(rotY));
        rotZMatrix.setToRotationZ((float) Math.toRadians(rotZ));
        obj.transform.setToIdentity();
        obj.transform.multiply(rotYMatrix);
        obj.transform.multiply(rotXMatrix);
        obj.transform.multiply(rotZMatrix);
        obj.transform.multiply(scaleMatrix);
        GeoUtil.convertGeoToVector(curGPSFix, geoLoc, obj.location);
        if (layer.xmlRelativeAltitude)
            obj.location.y = (float) geoLoc.alt;
        obj.location.add(loc);

        if (asset.downloadStatus == ARXState.READY) {
            obj.mesh = (Mesh3D) asset.downloadResult.obj;
        }

        if (!stale && drawMode == RENDER_READY) {
            Vector3D rl = renderResult.renderLocation;
            Vector3D cl = obj.location;

            tmp1.setTo(rl);
            tmp1.normalize();
            tmp2.setTo(cl);
            tmp2.normalize();
            float dot = tmp1.getDotProduct(tmp2);

            float distRL = rl.length();
            float distCL = cl.length();
            float delta = Math.abs(distRL - distCL);
            float ratio = delta / distRL;

            if (dot < angleThreshold || ratio > 0.1)
                stale = true;
        }
    }

    void prepareDraw(Camera viewCam) {
        if (drawMode == RENDER_READY) {
            calcCenterMark(renderResult.center, viewCam);
            calcSignPostMark(renderResult.signPost, viewCam);
        } else {
            calcCenterMark(origin, viewCam);
            calcSignPostMark(upVector, viewCam);
        }

        determineVisibility(viewCam);
    }

    void draw(DrawWindow dw) {
        if (isVisible) {
            if (drawMode == RENDER_READY) {
                float renderAngle = ARXUtil.getAngle(renderResult.centerX, renderResult.centerY,
                        renderResult.signPostX, renderResult.signPostY);
                float currentAngle = ARXUtil.getAngle(centerMark.x, centerMark.y, signPostMark.x, signPostMark.y);

                dw.drawBitmap(renderResult.cam.buf, centerMark.x - renderResult.centerX, centerMark.y -
                        renderResult.centerY, renderResult.cam.width, renderResult.cam.height, currentAngle -
                        renderAngle, renderResult.scaleFactor);

                drawDistance(dw);
            } else if (drawMode == ASSET_DOWNLOADING) {
                dw.setFill(true);
                dw.setColor(blobOuterColor);
                dw.drawCircle(centerMark.x, centerMark.y, BLOB_OUTER);
                dw.setColor(blobInnerColor);
                dw.drawCircle(centerMark.x, centerMark.y, BLOB_INNER);

                if (asset.isDownloadJobActive) {
                    float pct = asset.downloadJobPctComplete;
                    float rad = (BLOB_INNER - BLOB_WORK) * pct + BLOB_WORK;
                    dw.setColor(blobWorkColor);
                    dw.drawCircle(centerMark.x, centerMark.y, rad);
                }
            } else if (drawMode == ERROR) {
                dw.setFill(true);
                dw.setColor(errOuterColor);
                dw.drawCircle(centerMark.x, centerMark.y, BLOB_OUTER);
                dw.setColor(errInnerColor);
                dw.drawCircle(centerMark.x, centerMark.y, BLOB_INNER);
            } else {
                dw.setFill(true);
                dw.setColor(blobOuterColor);
                dw.drawCircle(centerMark.x, centerMark.y, BLOB_OUTER);
                dw.setColor(blobInnerColor);
                dw.drawCircle(centerMark.x, centerMark.y, BLOB_INNER);
            }
        }
    }

    boolean press(float x, float y, ARXContext ctx, ARXState state) {
        boolean evtHandled = false;

        if (asset.downloadStatus == ARXState.READY && xmlOnPress != null) {
            float xDist = centerMark.x - x;
            float yDist = centerMark.y - y;
            float dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);

            if (dist < 20) {
                evtHandled = state.handleEvent(ctx, xmlId, xmlOnPress);
            }
        }

        return evtHandled;
    }

    public void updateRenders(Camera cam, ARXContext ctx) {
        if (asset.downloadStatus == ARXState.READY) {
            if (asset.downloadResult.error) {
                drawMode = ERROR;
            } else {
                if (nextRenderStatus == ARXState.NOT_STARTED) {
                    requestNewRender(cam, ctx);
                } else if (nextRenderStatus == ARXState.JOB_SUBMITTED) {
                    if (ctx.getARXRender().isJobComplete(renderJobId)) {
                        renderResult = ctx.getARXRender().getJobResult(renderJobId);

                        if (renderResult.error) {
                            nextRenderStatus = ARXState.NOT_STARTED;
                            lastRender = 0;
                            stale = true;

                            drawMode = ERROR;
                        } else {
                            nextRenderStatus = ARXState.READY;
                            lastRender = System.currentTimeMillis();
                            stale = false;

                            drawMode = RENDER_READY;
                        }
                    }
                } else if (nextRenderStatus == ARXState.READY) {
                    requestNewRender(cam, ctx);

                    drawMode = RENDER_READY;
                }
            }
        } else {
            drawMode = ASSET_DOWNLOADING;
        }
    }

    private void requestNewRender(Camera cam, ARXContext ctx) {
        if (!isCenterVisible || !stale)
            return;

        Object3D reqObj = new Object3D();
        reqObj.mesh = obj.mesh;
        reqObj.location.setTo(obj.location);
        reqObj.transform.setTo(obj.transform);

        tmp1.setTo(reqObj.mesh.center);
        tmp1.transform(reqObj.transform);
        tmp1.add(reqObj.location);

        RenderJobRequest renderRequest = new RenderJobRequest();
        renderRequest.obj = reqObj;
        renderRequest.camLocation = new Vector3D(0f, 0f, 0f);
        renderRequest.camTransform = new Matrix3D();
        renderRequest.camTransform.setToLookAt(renderRequest.camLocation, tmp1);
        renderRequest.screenHeight = cam.height;
        renderRequest.screenWidth = cam.width;
        renderRequest.viewAngle = Camera.DEFAULT_VIEW_ANGLE;
        renderJobId = ctx.getARXRender().submitJob(renderRequest);

        nextRenderStatus = ARXState.JOB_SUBMITTED;
    }

    public void copyRenderState(Placemark3D dest) {
        Placemark3D src = this;

        if (src.drawMode == RENDER_READY) {
            if (dest.asset.xmlUrl != null && src.asset.xmlUrl != null && dest.asset.xmlUrl.equals(src.asset.xmlUrl)) {
                if (dest.transformEqual(src)) {
                    dest.nextRenderStatus = ARXState.READY;
                    dest.renderResult = src.renderResult;
                    dest.obj = src.obj;
                    dest.lastRender = src.lastRender;
                    dest.stale = src.stale;
                    dest.drawMode = src.drawMode;
                }
            }
        }
    }

    boolean transformEqual(Placemark3D dest) {
        Placemark3D src = this;

        return (dest.xmlScale == src.xmlScale && dest.xmlRotX == src.xmlRotX && dest.xmlRotY == src.xmlRotY && dest.xmlRotZ == src.xmlRotZ);
    }
}

abstract class PlacemarkDrawable extends Placemark {
    String xmlAnchor;

    AnchorUtil anchoredObj = new AnchorUtil();

    void updateState(GeoPoint curGPSFix, long time) {
        // Do updates
        locX = xmlLocX;
        locY = xmlLocY;
        locZ = xmlLocZ;
        geoLoc.setTo(xmlGeoLoc);

        // Set up Object3D
        loc.setTo(locX, locY, locZ);
        obj.transform.setToIdentity();
        GeoUtil.convertGeoToVector(curGPSFix, geoLoc, obj.location);
        if (layer.xmlRelativeAltitude)
            obj.location.y = (float) geoLoc.alt;
        obj.location.add(loc);
    }

    void prepareDraw(Camera viewCam) {
        calcCenterMark(origin, viewCam);
        calcSignPostMark(upVector, viewCam);

        determineVisibility(viewCam);
    }

    boolean isPressValid(float x, float y) {
        float currentAngle = ARXUtil.getAngle(centerMark.x, centerMark.y, signPostMark.x, signPostMark.y);

        pressPt.x = x - centerMark.x;
        pressPt.y = y - centerMark.y;
        pressPt.rotate(Math.toRadians(-(currentAngle + 90)));
        pressPt.x += centerMark.x;
        pressPt.y += centerMark.y;

        float objX = centerMark.x + anchoredObj.x - anchoredObj.getWidth() / 2;
        float objY = centerMark.y + anchoredObj.y - anchoredObj.getHeight() / 2;
        float objW = anchoredObj.getWidth() / 2;
        float objH = anchoredObj.getHeight() / 2;

        if (pressPt.x > objX && pressPt.x < objX + objW && pressPt.y > objY && pressPt.y < objY + objH) {
            return true;
        } else {
            return false;
        }
    }
}

class PlacemarkImg extends PlacemarkDrawable {
    void draw(DrawWindow dw) {
        if (isVisible) {
            if (asset.downloadStatus == ARXState.READY) {
                if (asset.downloadResult.error) {
                    dw.setFill(true);
                    dw.setColor(errOuterColor);
                    dw.drawCircle(centerMark.x, centerMark.y, BLOB_OUTER);
                    dw.setColor(errInnerColor);
                    dw.drawCircle(centerMark.x, centerMark.y, BLOB_INNER);
                } else {
                    float currentAngle = ARXUtil.getAngle(centerMark.x, centerMark.y, signPostMark.x, signPostMark.y);

                    Bitmap bmp = (Bitmap) asset.downloadResult.obj;

                    anchoredObj.prepare(bmp, xmlAnchor);

                    dw.drawObject(anchoredObj, centerMark.x - anchoredObj.getWidth() / 2, centerMark.y -
                            anchoredObj.getHeight() / 2, currentAngle + 90, 1);

                    drawDistance(dw);
                }
            } else {
                dw.setFill(true);
                dw.setColor(blobOuterColor);
                dw.drawCircle(centerMark.x, centerMark.y, BLOB_OUTER);
                dw.setColor(blobInnerColor);
                dw.drawCircle(centerMark.x, centerMark.y, BLOB_INNER);

                if (asset.isDownloadJobActive) {
                    float pct = asset.downloadJobPctComplete;
                    float rad = (BLOB_INNER - BLOB_WORK) * pct + BLOB_WORK;
                    dw.setColor(blobWorkColor);
                    dw.drawCircle(centerMark.x, centerMark.y, rad);
                }
            }
        }
    }

    boolean press(float x, float y, ARXContext ctx, ARXState state) {
        boolean evtHandled = false;

        if (asset.downloadStatus == ARXState.READY && xmlOnPress != null) {
            if (isPressValid(x, y)) {
                evtHandled = state.handleEvent(ctx, xmlId, xmlOnPress);
            }
        }

        return evtHandled;
    }
}

class PlacemarkTxt extends PlacemarkDrawable {
    public String xmlText;
    TextBlock textBlock;

    void draw(DrawWindow dw) {
        if (textBlock == null) {
            textBlock = new TextBlock(xmlText, 12, 160, dw);
        }

        if (isVisible) {
            float currentAngle = ARXUtil.getAngle(centerMark.x, centerMark.y, signPostMark.x, signPostMark.y);

            anchoredObj.prepare(textBlock, xmlAnchor);

            dw.drawObject(anchoredObj, centerMark.x - anchoredObj.getWidth() / 2, centerMark.y -
                    anchoredObj.getHeight() / 2, currentAngle + 90, 1);
        }
    }

    boolean press(float x, float y, ARXContext ctx, ARXState state) {
        boolean evtHandled = false;

        if (xmlOnPress != null) {
            if (isPressValid(x, y)) {
                evtHandled = state.handleEvent(ctx, xmlId, xmlOnPress);
            }
        }

        return evtHandled;
    }
}

class AnchorUtil implements Drawable {
    float x, y, w, h;
    float width, height;
    Drawable obj;

    public void prepare(Drawable drawObj, String anchorType) {
        obj = drawObj;
        w = obj.getWidth();
        h = obj.getHeight();

        if (anchorType.equals("BR")) {
            x = 0;
            y = 0;
        } else if (anchorType.equals("BC")) {
            x = w / 2;
            y = 0;
        } else if (anchorType.equals("BL")) {
            x = w;
            y = 0;
        } else if (anchorType.equals("CR")) {
            x = 0;
            y = h / 2;
        } else if (anchorType.equals("CC")) {
            x = w / 2;
            y = h / 2;
        } else if (anchorType.equals("CL")) {
            x = w;
            y = h / 2;
        } else if (anchorType.equals("TR")) {
            x = 0;
            y = h;
        } else if (anchorType.equals("TC")) {
            x = w / 2;
            y = h;
        } else if (anchorType.equals("TL")) {
            x = w;
            y = h;
        } else {
            x = w / 2;
            y = 0;
        }

        width = w * 2;
        height = h * 2;
    }

    public void draw(DrawWindow dw) {
        dw.drawObject(obj, x, y, 0, 1);
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
