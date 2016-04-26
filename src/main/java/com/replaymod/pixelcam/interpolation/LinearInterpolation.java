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
package com.replaymod.pixelcam.interpolation;

import com.replaymod.pixelcam.path.Position;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class LinearInterpolation implements Interpolation<Position> {

    private Field[] fields;

    protected List<Position> points = new ArrayList<>();

    @Override
    public void prepare() {}

    @Override
    public void addPoint(Position point) {
        double normalizedYaw = (point.getYaw() + 180) % 360;
        double normalizedTilt = (point.getTilt()) % 360;

        if(!points.isEmpty()) {
            Position last = points.get(points.size() - 1);

            double yaw = InterpolationUtils.fixEulerRotation(last.getYaw(), point.getYaw(), 180);
            double tilt = InterpolationUtils.fixEulerRotation(last.getTilt(), point.getTilt(), 0);

            point.setYaw(yaw);
            point.setTilt(tilt);
        } else {
            point.setYaw(normalizedYaw - 180);
            point.setTilt(normalizedTilt);
        }

        points.add(point);

        if(fields == null) {
            List<Field> fields = InterpolationUtils.getFieldsToInterpolate(point.getClass());
            this.fields = fields.toArray(new Field[fields.size()]);
        }
    }

    @Override
    public void applyPoint(float position, Position toEdit) {
        if(fields == null) {
            throw new IllegalStateException("At least one Keyframe has to be added before preparing");
        }

        if(fields.length <= 0) {
            throw new IllegalStateException("The passed KeyframeValue class" +
                    " has to contain at least one Field");
        }

        //first, get previous and next T for given position
        float relative = position * (points.size()-1);
        int previousIndex = (int)Math.floor(relative);
        int nextIndex = (int)Math.ceil(relative);
        float percentage = relative - previousIndex;

        Position previous = points.get(previousIndex);
        Position next = points.get(nextIndex);

        for(Field f : fields) {
            try {
                f.set(toEdit, getInterpolatedValue(f.getDouble(previous), f.getDouble(next), percentage));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private double getInterpolatedValue(double val1, double val2, float perc) {
        return val1 + ((val2 - val1) * perc);
    }

    @Override
    public InterpolationType getInterpolationType() {
        return InterpolationType.LINEAR;
    }

}