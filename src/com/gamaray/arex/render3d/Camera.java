package com.gamaray.arex.render3d;

import java.util.Arrays;

public class Camera {
    public static float DEFAULT_VIEW_ANGLE = (float) Math.toRadians(45);
    public int width, height;
    public boolean buffersInititalized = false;
    public int buf[];
    public float zbuf[];
    public int leftBound[], rightBound[];
    public float leftDepth[], rightDepth[];

    public Matrix3D transform = new Matrix3D();
    public Vector3D location = new Vector3D();

    float viewAngle;
    float distanceToCamera;

    public Camera(int width, int height) {
        this(width, height, true);
    }

    public Camera(int width, int height, boolean initBuffers) {
        this.width = width;
        this.height = height;

        transform.setToIdentity();
        location.setTo(0, 0, 0);

        if (initBuffers) {
            buf = new int[width * height];
            zbuf = new float[width * height];
            leftBound = new int[height];
            rightBound = new int[height];
            rightDepth = new float[height];
            leftDepth = new float[height];
            buffersInititalized = true;
        }
    }

    public void setViewAngle(float viewAngle) {
        this.viewAngle = viewAngle;
        this.distanceToCamera = (this.width / 2) / (float) Math.tan(viewAngle / 2);
    }

    public void setViewAngle(int width, int height, float viewAngle) {
        this.viewAngle = viewAngle;
        this.distanceToCamera = (width / 2) / (float) Math.tan(viewAngle / 2);
    }

    public void clearBuffers() {
        Arrays.fill(buf, Color.argb(0, 0, 0, 0));
        Arrays.fill(zbuf, Float.NEGATIVE_INFINITY);
    }

    public void render(Object3D obj) {
        float vertexArray[] = obj.mesh.vertices;
        int colorArray[] = obj.mesh.colors;
        short faceArray[] = obj.mesh.faces;
        short faceColorArray[] = obj.mesh.faceColors;
        float planeNormals[] = obj.mesh.planeNormals;
        int faceArrayCount = obj.mesh.faces.length;

        int height = this.height;
        int width = this.width;
        float distanceToCamera = this.distanceToCamera;
        int buf[] = this.buf;
        float zbuf[] = this.zbuf;
        int leftBound[] = this.leftBound;
        int rightBound[] = this.rightBound;
        float rightDepth[] = this.leftDepth;
        float leftDepth[] = this.rightDepth;

        float camA1 = transform.a1;
        float camA2 = transform.a2;
        float camA3 = transform.a3;
        float camB1 = transform.b1;
        float camB2 = transform.b2;
        float camB3 = transform.b3;
        float camC1 = transform.c1;
        float camC2 = transform.c2;
        float camC3 = transform.c3;

        float camX = location.x;
        float camY = location.y;
        float camZ = location.z;

        float objA1 = obj.transform.a1;
        float objA2 = obj.transform.a2;
        float objA3 = obj.transform.a3;
        float objB1 = obj.transform.b1;
        float objB2 = obj.transform.b2;
        float objB3 = obj.transform.b3;
        float objC1 = obj.transform.c1;
        float objC2 = obj.transform.c2;
        float objC3 = obj.transform.c3;

        float objX = obj.location.x;
        float objY = obj.location.y;
        float objZ = obj.location.z;

        int x, y;
        float depth;
        float tmpX, tmpY, tmpZ, tmpD;
        float dx, dy, dd;
        float gradient, depthGradient;
        int startY, endY;
        int minX, minY, maxX, maxY;

        for (int fIdx = 0; fIdx < faceArrayCount; fIdx += 3) {
            // Indexes
            short v1Idx = faceArray[fIdx + 0];
            short v2Idx = faceArray[fIdx + 1];
            short v3Idx = faceArray[fIdx + 2];
            short cIdx = faceColorArray[fIdx / 3];

            // Color
            int color = colorArray[cIdx];

            // Object transform
            int v1IdxInt = v1Idx * 3;
            float v1X = vertexArray[v1IdxInt + 0];
            float v1Y = vertexArray[v1IdxInt + 1];
            float v1Z = vertexArray[v1IdxInt + 2];
            tmpX = objA1 * v1X + objA2 * v1Y + objA3 * v1Z;
            tmpY = objB1 * v1X + objB2 * v1Y + objB3 * v1Z;
            tmpZ = objC1 * v1X + objC2 * v1Y + objC3 * v1Z;
            v1X = tmpX + objX;
            v1Y = tmpY + objY;
            v1Z = tmpZ + objZ;

            int v2IdxInt = v2Idx * 3;
            float v2X = vertexArray[v2IdxInt + 0];
            float v2Y = vertexArray[v2IdxInt + 1];
            float v2Z = vertexArray[v2IdxInt + 2];
            tmpX = objA1 * v2X + objA2 * v2Y + objA3 * v2Z;
            tmpY = objB1 * v2X + objB2 * v2Y + objB3 * v2Z;
            tmpZ = objC1 * v2X + objC2 * v2Y + objC3 * v2Z;
            v2X = tmpX + objX;
            v2Y = tmpY + objY;
            v2Z = tmpZ + objZ;

            int v3IdxInt = v3Idx * 3;
            float v3X = vertexArray[v3IdxInt + 0];
            float v3Y = vertexArray[v3IdxInt + 1];
            float v3Z = vertexArray[v3IdxInt + 2];
            tmpX = objA1 * v3X + objA2 * v3Y + objA3 * v3Z;
            tmpY = objB1 * v3X + objB2 * v3Y + objB3 * v3Z;
            tmpZ = objC1 * v3X + objC2 * v3Y + objC3 * v3Z;
            v3X = tmpX + objX;
            v3Y = tmpY + objY;
            v3Z = tmpZ + objZ;

            int pnIdx = fIdx;
            float pnX = planeNormals[pnIdx + 0];
            float pnY = planeNormals[pnIdx + 1];
            float pnZ = planeNormals[pnIdx + 2];
            tmpX = objA1 * pnX + objA2 * pnY + objA3 * pnZ;
            tmpY = objB1 * pnX + objB2 * pnY + objB3 * pnZ;
            tmpZ = objC1 * pnX + objC2 * pnY + objC3 * pnZ;
            pnX = tmpX;
            pnY = tmpY;
            pnZ = tmpZ;

            // Backface culling
            if (!(pnX * (camX - v1X) + pnY * (camY - v1Y) + pnZ * (camZ - v1Z) >= 0)) {
                continue;
            }

            // Camera transform
            v1X -= camX;
            v1Y -= camY;
            v1Z -= camZ;
            tmpX = camA1 * v1X + camA2 * v1Y + camA3 * v1Z;
            tmpY = camB1 * v1X + camB2 * v1Y + camB3 * v1Z;
            tmpZ = camC1 * v1X + camC2 * v1Y + camC3 * v1Z;
            v1X = tmpX;
            v1Y = tmpY;
            v1Z = tmpZ;

            v2X -= camX;
            v2Y -= camY;
            v2Z -= camZ;
            tmpX = camA1 * v2X + camA2 * v2Y + camA3 * v2Z;
            tmpY = camB1 * v2X + camB2 * v2Y + camB3 * v2Z;
            tmpZ = camC1 * v2X + camC2 * v2Y + camC3 * v2Z;
            v2X = tmpX;
            v2Y = tmpY;
            v2Z = tmpZ;

            v3X -= camX;
            v3Y -= camY;
            v3Z -= camZ;
            tmpX = camA1 * v3X + camA2 * v3Y + camA3 * v3Z;
            tmpY = camB1 * v3X + camB2 * v3Y + camB3 * v3Z;
            tmpZ = camC1 * v3X + camC2 * v3Y + camC3 * v3Z;
            v3X = tmpX;
            v3Y = tmpY;
            v3Z = tmpZ;

            // XY Plane Clip
            if (v1Z > -1f || v2Z > -1f || v3Z > -1f) {
                continue;
            }

            // Project
            float v1XP = distanceToCamera * v1X / -v1Z;
            float v1YP = distanceToCamera * v1Y / -v1Z;
            v1XP = v1XP + width / 2;
            v1YP = -v1YP + height / 2;

            float v2XP = distanceToCamera * v2X / -v2Z;
            float v2YP = distanceToCamera * v2Y / -v2Z;
            v2XP = v2XP + width / 2;
            v2YP = -v2YP + height / 2;

            float v3XP = distanceToCamera * v3X / -v3Z;
            float v3YP = distanceToCamera * v3Y / -v3Z;
            v3XP = v3XP + width / 2;
            v3YP = -v3YP + height / 2;

            // Scan convert
            minX = 0;
            maxX = width - 1;
            minY = 0;
            maxY = height - 1;

            Arrays.fill(leftBound, Integer.MAX_VALUE);
            Arrays.fill(rightBound, 0);

            float lineX1 = 0, lineY1 = 0, lineD1 = 0;
            float lineX2 = 0, lineY2 = 0, lineD2 = 0;
            int minStartY = Integer.MAX_VALUE, maxEndY = 0;

            for (int i = 0; i < 3; i++) {
                if (i == 0) {
                    lineX1 = v1XP;
                    lineY1 = v1YP;
                    lineD1 = v1Z;
                    lineX2 = v2XP;
                    lineY2 = v2YP;
                    lineD2 = v2Z;
                } else if (i == 1) {
                    lineX1 = v2XP;
                    lineY1 = v2YP;
                    lineD1 = v2Z;
                    lineX2 = v3XP;
                    lineY2 = v3YP;
                    lineD2 = v3Z;
                } else if (i == 2) {
                    lineX1 = v3XP;
                    lineY1 = v3YP;
                    lineD1 = v3Z;
                    lineX2 = v1XP;
                    lineY2 = v1YP;
                    lineD2 = v1Z;
                }

                if (lineY1 > lineY2) {
                    tmpX = lineX1;
                    tmpY = lineY1;
                    tmpD = lineD1;
                    lineX1 = lineX2;
                    lineY1 = lineY2;
                    lineD1 = lineD2;
                    lineX2 = tmpX;
                    lineY2 = tmpY;
                    lineD2 = tmpD;
                }

                startY = Math.max(ceil(lineY1), minY);
                endY = Math.min(ceil(lineY2), maxY);
                dx = lineX2 - lineX1;
                dy = lineY2 - lineY1;
                dd = lineD2 - lineD1;

                minStartY = Math.min(minStartY, startY);
                maxEndY = Math.max(maxEndY, endY);

                if (dy != 0) {
                    gradient = dx / dy;
                    depthGradient = dd / dy;

                    for (y = startY; y < endY; y++) {
                        x = ceil(lineX1 + (y - lineY1) * gradient);
                        x = Math.min(maxX + 1, Math.max(x, minX));
                        depth = lineD1 + (y - lineY1) * depthGradient;

                        if (x < leftBound[y]) {
                            leftBound[y] = x;
                            leftDepth[y] = depth;
                        }

                        if (x > rightBound[y]) {
                            rightBound[y] = x;
                            rightDepth[y] = depth;
                        }
                    }
                }
            }

            // Draw pixels
            for (y = minStartY; y < maxEndY; y++) {
                int leftBoundVal = leftBound[y];
                int rightBoundVal = rightBound[y];

                if (leftBoundVal != Integer.MAX_VALUE && leftBoundVal != rightBoundVal) {
                    dx = (float) (leftBoundVal - rightBoundVal);
                    depthGradient = (leftDepth[y] - rightDepth[y]) / dx;

                    depth = leftDepth[y];
                    for (x = leftBoundVal; x < rightBoundVal; x++) {
                        depth += depthGradient;

                        int idx = x + y * width;
                        if (depth > zbuf[idx]) {
                            buf[idx] = color;
                            zbuf[idx] = depth;
                        }
                    }
                }
            }
        }
    }

    public int ceil(float f) {
        return (f > 0) ? ((int) f + 1) : ((int) f);
    }

    public void projectPoint(Vector3D orgPoint, Vector3D prjPoint) {
        prjPoint.x = distanceToCamera * orgPoint.x / -orgPoint.z;
        prjPoint.y = distanceToCamera * orgPoint.y / -orgPoint.z;
        prjPoint.z = orgPoint.z;
        prjPoint.x = prjPoint.x + width / 2;
        prjPoint.y = -prjPoint.y + height / 2;
    }

    public float projectLength(float len, float z) {
        return Math.abs(distanceToCamera * len / z);
    }

    public String toString() {
        return "CAM(" + width + "," + height + ")";
    }
}
