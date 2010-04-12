package com.gamaray.arex.render3d;

public class Vector3D {
    public float x;
    public float y;
    public float z;

    public Vector3D() {
        this(0, 0, 0);
    }

    public Vector3D(Vector3D v) {
        this(v.x, v.y, v.z);
    }

    public Vector3D(float x, float y, float z) {
        setTo(x, y, z);
    }

    public boolean equals(Object obj) {
        Vector3D v = (Vector3D) obj;
        return (v.x == x && v.y == y && v.z == z);
    }

    public boolean equals(float x, float y, float z) {
        return (this.x == x && this.y == y && this.z == z);
    }

    public String toString() {
        return "<" + x + ", " + y + ", " + z + ">";
    }

    public void setTo(Vector3D v) {
        setTo(v.x, v.y, v.z);
    }

    public void setTo(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }

    public void subtract(float x, float y, float z) {
        add(-x, -y, -z);
    }

    public void add(Vector3D v) {
        add(v.x, v.y, v.z);
    }

    public void subtract(Vector3D v) {
        add(-v.x, -v.y, -v.z);
    }

    public void multiply(float s) {
        x *= s;
        y *= s;
        z *= s;
    }

    public void divide(float s) {
        x /= s;
        y /= s;
        z /= s;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public void normalize() {
        divide(length());
    }

    public float getDotProduct(Vector3D v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public void setToCrossProduct(Vector3D u, Vector3D v) {
        float x = u.y * v.z - u.z * v.y;
        float y = u.z * v.x - u.x * v.z;
        float z = u.x * v.y - u.y * v.x;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void transform(Matrix3D m) {
        float xTemp = m.a1 * x + m.a2 * y + m.a3 * z;
        float yTemp = m.b1 * x + m.b2 * y + m.b3 * z;
        float zTemp = m.c1 * x + m.c2 * y + m.c3 * z;

        x = xTemp;
        y = yTemp;
        z = zTemp;
    }
}
