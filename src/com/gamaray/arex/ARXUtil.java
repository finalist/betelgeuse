package com.gamaray.arex;

class ARXUtil {
    static synchronized void db(String msg) {
        System.out.println(">> " + System.currentTimeMillis() + "," + Thread.currentThread().getName() + "," + msg);
    }

    public static String parseAction(String action) {
        return (action.substring(action.indexOf(':') + 1, action.length())).trim();
    }

    public static String formatDistance(float meters) {
        if (meters < 1000) {
            return ((int) meters) + "m";
        } else if (meters < 10000) {
            return formatDecimal(meters / 1000f, 1) + "km";
        } else {
            return ((int) (meters / 1000f)) + "km";
        }
    }

    public static String formatDecimal(float val, int dec) {
        int factor = (int) Math.pow(10, dec);

        int front = (int) (val + 0.5f);
        int back = (int) Math.abs(val * ((float) factor) + 0.5f) % factor;

        return front + "." + back;
    }

    public static boolean rectTriIntersect(float r_x, float r_y, float r_w, float r_h, float A_x, float A_y, float B_x,
            float B_y, float C_x, float C_y) {
        float rp1_x, rp1_y, rp2_x, rp2_y, rp3_x, rp3_y, rp4_x, rp4_y;

        // Is any point on triangle inside rectangle (CCL)
        if (A_x > r_x && A_x < r_x + r_w && A_y > r_y && A_y < r_y + r_h)
            return true;
        if (B_x > r_x && B_x < r_x + r_w && B_y > r_y && B_y < r_y + r_h)
            return true;
        if (C_x > r_x && C_x < r_x + r_w && C_y > r_y && C_y < r_y + r_h)
            return true;

        // Are all points on triangle outside one edge of rectangle (CCL)
        if (A_x < r_x && B_x < r_x && C_x < r_x)
            return false;
        if (A_y < r_y && B_y < r_y && C_y < r_y)
            return false;
        if (A_x > r_x + r_w && B_x > r_x + r_w && C_x > r_x + r_w)
            return false;
        if (A_y > r_y + r_h && B_y > r_y + r_h && C_y > r_y + r_h)
            return false;

        rp1_x = r_x;
        rp1_y = r_y;
        rp2_x = r_x + r_w;
        rp2_y = r_y;
        rp3_x = r_x + r_w;
        rp3_y = r_y + r_h;
        rp4_x = r_x;
        rp4_y = r_y + r_h;

        // Are any points on rectangle inside triangle
        if (pointInTriangle(rp1_x, rp1_y, A_x, A_y, B_x, B_y, C_x, C_y))
            return true;
        if (pointInTriangle(rp2_x, rp2_y, A_x, A_y, B_x, B_y, C_x, C_y))
            return true;
        if (pointInTriangle(rp3_x, rp3_y, A_x, A_y, B_x, B_y, C_x, C_y))
            return true;
        if (pointInTriangle(rp4_x, rp4_y, A_x, A_y, B_x, B_y, C_x, C_y))
            return true;

        // Do any lines intersect
        if (lineIntersect(rp1_x, rp1_y, rp2_x, rp2_y, A_x, A_y, B_x, B_y))
            return true;
        if (lineIntersect(rp2_x, rp2_y, rp3_x, rp3_y, A_x, A_y, B_x, B_y))
            return true;
        if (lineIntersect(rp3_x, rp3_y, rp4_x, rp4_y, A_x, A_y, B_x, B_y))
            return true;
        if (lineIntersect(rp4_x, rp4_y, rp1_x, rp1_y, A_x, A_y, B_x, B_y))
            return true;

        if (lineIntersect(rp1_x, rp1_y, rp2_x, rp2_y, B_x, B_y, C_x, C_y))
            return true;
        if (lineIntersect(rp2_x, rp2_y, rp3_x, rp3_y, B_x, B_y, C_x, C_y))
            return true;
        if (lineIntersect(rp3_x, rp3_y, rp4_x, rp4_y, B_x, B_y, C_x, C_y))
            return true;
        if (lineIntersect(rp4_x, rp4_y, rp1_x, rp1_y, B_x, B_y, C_x, C_y))
            return true;

        if (lineIntersect(rp1_x, rp1_y, rp2_x, rp2_y, C_x, C_y, A_x, A_y))
            return true;
        if (lineIntersect(rp2_x, rp2_y, rp3_x, rp3_y, C_x, C_y, A_x, A_y))
            return true;
        if (lineIntersect(rp3_x, rp3_y, rp4_x, rp4_y, C_x, C_y, A_x, A_y))
            return true;
        if (lineIntersect(rp4_x, rp4_y, rp1_x, rp1_y, C_x, C_y, A_x, A_y))
            return true;

        return false;
    }

    public static boolean lineIntersect(float begin__x_, float begin__y_, float end__x_, float end__y_,
            float other_line_begin__x_, float other_line_begin__y_, float other_line_end__x_, float other_line_end__y_) {
        float denom = ((other_line_end__y_ - other_line_begin__y_) * (end__x_ - begin__x_)) -
                ((other_line_end__x_ - other_line_begin__x_) * (end__y_ - begin__y_));

        float nume_a = ((other_line_end__x_ - other_line_begin__x_) * (begin__y_ - other_line_begin__y_)) -
                ((other_line_end__y_ - other_line_begin__y_) * (begin__x_ - other_line_begin__x_));

        float nume_b = ((end__x_ - begin__x_) * (begin__y_ - other_line_begin__y_)) -
                ((end__y_ - begin__y_) * (begin__x_ - other_line_begin__x_));

        if (denom == 0.0f) {
            if (nume_a == 0.0f && nume_b == 0.0f) {
                // COINCIDENT
                // TODO: check if "intersects"
                return false;
            }

            // PARALLEL
            return false;
        }

        float ua = nume_a / denom;
        float ub = nume_b / denom;

        if (ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f) {
            // Get the intersection point.
            // intersection_x_ = begin__x_ + ua*(end__x_ - begin__x_);
            // intersection_y_ = begin__y_ + ua*(end__y_ - begin__y_);

            return true;
        }

        return false;
    }

    public static boolean pointInRectangle(float P_x, float P_y, float r_x, float r_y, float r_w, float r_h) {
        return (P_x > r_x && P_x < r_x + r_w && P_y > r_y && P_y < r_y + r_h);
    }

    public static boolean pointInTriangle(float P_x, float P_y, float A_x, float A_y, float B_x, float B_y, float C_x,
            float C_y) {
        // Compute vectors
        float v0_x = C_x - A_x;
        float v0_y = C_y - A_y;
        float v1_x = B_x - A_x;
        float v1_y = B_y - A_y;
        float v2_x = P_x - A_x;
        float v2_y = P_y - A_y;

        // Compute dot products
        float dot00 = dot(v0_x, v0_y, v0_x, v0_y);
        float dot01 = dot(v0_x, v0_y, v1_x, v1_y);
        float dot02 = dot(v0_x, v0_y, v2_x, v2_y);
        float dot11 = dot(v1_x, v1_y, v1_x, v1_y);
        float dot12 = dot(v1_x, v1_y, v2_x, v2_y);

        // Compute barycentric coordinates
        float invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
        float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        float v = (dot00 * dot12 - dot01 * dot02) * invDenom;

        // Check if point is in triangle
        return (u > 0) && (v > 0) && (u + v < 1);
    }

    public static float dot(float p1_x, float p1_y, float p2_x, float p2_y) {
        return p1_x * p2_x + p1_y * p2_y;
    }

    public static void fillRect(int buf[], int bufWidth, int bufHeight, int rx, int ry, int width, int height, int color) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int bufIdx = (rx + x) + (ry + y) * bufWidth;
                if (bufIdx >= 0 && bufIdx < buf.length)
                    buf[bufIdx] = color;
            }
        }
    }

    public static void drawRect(int buf[], int bufWidth, int bufHeight, int rx, int ry, int width, int height, int color) {
        drawHLine(buf, bufWidth, bufHeight, rx, ry, width, color);
        drawHLine(buf, bufWidth, bufHeight, rx, ry + height - 1, width, color);
        drawVLine(buf, bufWidth, bufHeight, rx, ry, height, color);
        drawVLine(buf, bufWidth, bufHeight, rx + width - 1, ry, height, color);
    }

    public static void drawVLine(int buf[], int bufWidth, int bufHeight, int x, int y, int len, int color) {
        for (int i = 0; i < len; i++) {
            int bufIdx = (x) + (y + i) * bufWidth;
            if (bufIdx >= 0 && bufIdx < buf.length)
                buf[bufIdx] = color;
        }
    }

    public static void drawHLine(int buf[], int bufWidth, int bufHeight, int x, int y, int len, int color) {
        for (int i = 0; i < len; i++) {
            int bufIdx = (x + i) + (y) * bufWidth;
            if (bufIdx >= 0 && bufIdx < buf.length)
                buf[bufIdx] = color;
        }
    }

    public static float getAngle(float center_x, float center_y, float post_x, float post_y) {
        float tmpv_x = post_x - center_x;
        float tmpv_y = post_y - center_y;
        float d = (float) Math.sqrt(tmpv_x * tmpv_x + tmpv_y * tmpv_y);
        float cos = tmpv_x / d;
        float angle = (float) Math.toDegrees(Math.acos(cos));

        angle = (tmpv_y < 0) ? angle * -1 : angle;

        return angle;
    }
}
