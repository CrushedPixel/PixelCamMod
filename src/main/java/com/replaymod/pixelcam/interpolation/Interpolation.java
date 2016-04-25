package com.replaymod.pixelcam.interpolation;

public interface Interpolation<T> {

    void prepare();

    void applyPoint(float position, T toEdit);

    void addPoint(T pos);

}