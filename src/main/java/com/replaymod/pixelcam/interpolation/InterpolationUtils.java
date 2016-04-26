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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class InterpolationUtils {

    public static List<Field> getFieldsToInterpolate(Class clazz) {
        List<Field> fields = new ArrayList<>();
        for(Field f : clazz.getDeclaredFields()) {
            if(f.isAnnotationPresent(Interpolate.class)) fields.add(f);
        }

        if(clazz.getSuperclass() != Object.class) {
            fields.addAll(getFieldsToInterpolate(clazz.getSuperclass()));
        }

        return fields;
    }

    /**
     * Note: I invented the word "Euler break". If there are any better suggestions, let me know.
     * @param first The previous, fixed Rotation value
     * @param second The new Rotation value
     * @param eulerBreak The Euler break, e.g. 180 for Minecraft's Camera Yaw
     * @return The new Rotation value, modified to make the Interpolation algorithms
     * find the closest path between two Euler Rotation values
     */
    public static double fixEulerRotation(double first, double second, int eulerBreak) {
        if(first == second) return first;

        //converting the values to values between 0 and 359,
        //essentially moving the euler break to 0
        double normalizedFirst = (first + eulerBreak) % 360;
        double normalizedSecond = (second + eulerBreak) % 360;

        //the difference between the rotation values
        //if using the "conventional" path
        double pathDifference = Math.abs(normalizedSecond-normalizedFirst);

        int factor = normalizedSecond > normalizedFirst ? 1 : -1;

        //if the "conventional" path takes more than half the rotation,
        //use the path crossing the euler break
        if(pathDifference > 180) {
            //invert the path difference to rotate in the other direction
            pathDifference = -1*(360-pathDifference);
        }

        return first + factor*pathDifference;
    }

}
