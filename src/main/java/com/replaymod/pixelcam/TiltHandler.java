package com.replaymod.pixelcam;

public class TiltHandler {

    private static float tilt;

    public static float getTilt() {
        return tilt;
    }

    public static void changeTilt(float diff) {
        tilt += diff;
    }

    public static void resetTilt() {
        tilt = 0;
    }

    public static void setTilt(float newTilt) {
        tilt = newTilt;
    }

}
