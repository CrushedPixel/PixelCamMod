package com.replaymod.pixelcam.path;

import com.replaymod.pixelcam.interpolation.Interpolation;
import com.replaymod.pixelcam.interpolation.LinearInterpolation;
import com.replaymod.pixelcam.interpolation.SplineInterpolation;

import java.util.LinkedList;
import java.util.List;

public class CameraPath {

    private final List<Position> points = new LinkedList<>();

    public int addPoint(Position position, int index) {
        if(index < 0) points.add(position);
        else points.add(index, position);

        if(index < 0) return points.size()-1;
        return index;
    }

    public void clear() {
        points.clear();
    }

    public void removePoint(int index) {
        points.remove(index);
    }

    public int getPointCount() {
        return points.size();
    }

    public Position getPoint(int index) {
        return points.get(index);
    }

    public Interpolation<Position> getInterpolation(InterpolationType type) {
        Interpolation<Position> interpolation =
                (type == InterpolationType.LINEAR || points.size() < 3)
                        ? new LinearInterpolation() : new SplineInterpolation();

        for(Position pos : points) {
            interpolation.addPoint(pos);
        }

        interpolation.prepare();

        return interpolation;
    }

    public enum InterpolationType {
        LINEAR, SPLINE
    }

}
