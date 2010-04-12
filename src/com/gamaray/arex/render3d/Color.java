package com.gamaray.arex.render3d;

public class Color {
    public static int rgb(int red, int green, int blue) {
        return argb(0xff, red, green, blue);
    }

    public static int argb(int alpha, int red, int green, int blue) {
        int color = 0;
        color |= blue << 0;
        color |= green << 8;
        color |= red << 16;
        color |= alpha << 24;

        return color;
    }

    public static int alpha(int color) {
        return (color >> 24) & 0xFF;
    }

    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int blue(int color) {
        return (color >> 0) & 0xFF;
    }
}
