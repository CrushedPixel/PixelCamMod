/*
 * This file is part of PixelCam Mod, licensed under the Apache License, Version 2.0.
 *
 * Copyright (c) 2016 CrushedPixel <http://crushedpixel.eu>
 * Copyright (c) contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.replaymod.pixelcam.path;

import com.replaymod.pixelcam.interpolation.Interpolation;
import com.replaymod.pixelcam.interpolation.InterpolationType;
import com.replaymod.pixelcam.interpolation.LinearInterpolation;
import com.replaymod.pixelcam.interpolation.SplineInterpolation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CameraPath {

    private final List<Position> points = new LinkedList<>();

    private Interpolation<Position> interpolation;

    public int addPoint(Position position, int index) {
        if(index < 0) points.add(position);
        else points.add(index, position);

        interpolation = null;

        if(index < 0) return points.size()-1;
        return index;
    }

    public void clear() {
        points.clear();
        interpolation = null;
    }

    public void removePoint(int index) {
        points.remove(index);
        interpolation = null;
    }

    public int getPointCount() {
        return points.size();
    }

    public List<Position> getPoints() {
        return new ArrayList<>(points);
    }

    public Position getPoint(int index) {
        return points.get(index);
    }

    public Interpolation<Position> getInterpolation(InterpolationType type) {

        if(interpolation == null || (interpolation.getInterpolationType() != type && points.size() > 2)) {
            interpolation = (type == InterpolationType.LINEAR || points.size() < 3)
                            ? new LinearInterpolation() : new SplineInterpolation();

            for(Position pos : points) {
                interpolation.addPoint(pos);
            }

            interpolation.prepare();
        }

        return interpolation;
    }

}
