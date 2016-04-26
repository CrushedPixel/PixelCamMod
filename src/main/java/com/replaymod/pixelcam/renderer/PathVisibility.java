package com.replaymod.pixelcam.renderer;

public enum PathVisibility {
    BOTH, SPLINE, LINEAR, NONE;

    public PathVisibility next() {
        int i = ordinal() + 1;
        if(i >= values().length) i = 0;
        return values()[i];
    }
}
