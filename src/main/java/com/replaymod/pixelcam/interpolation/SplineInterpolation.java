package com.replaymod.pixelcam.interpolation;

import com.replaymod.pixelcam.path.Position;

import java.lang.reflect.Field;
import java.util.*;

public class SplineInterpolation implements Interpolation<Position> {

    protected Field[] fields;

    protected Vector<Position> points = new Vector<>();
    protected List<Vector<Cubic>> cubics = Collections.emptyList();

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
    public void prepare() {
        if(fields == null) {
            throw new IllegalStateException("At least one Keyframe has to be added before preparing");
        }

        if(fields.length <= 0) {
            throw new IllegalStateException("The passed KeyframeValue class" +
                    " has to contain at least one Field");
        }

        if(!points.isEmpty()) {
            cubics = new ArrayList<>(fields.length);
            for (Field field : fields) {
                Vector<Cubic> vec = new Vector<>();
                cubics.add(vec);
                try {
                    calcNaturalCubic(points, field, vec);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            throw new IllegalStateException("At least one Value needs to be added" +
                    " before preparing this Spline");
        }
    }

    @Override
    public void applyPoint(float position, Position toEdit) {
        Vector<Cubic> first = cubics.get(0);
        position = position * first.size();
        int cubicNum = (int) Math.min(first.size() - 1, position);
        float cubicPos = (position - cubicNum);

        int i = 0;
        for(Field f : fields) {
            try {
                f.set(toEdit, cubics.get(i).get(cubicNum).eval(cubicPos));
            } catch(Exception e) {
                e.printStackTrace();
            }
            i++;
        }
    }

    public void calcNaturalCubic(List valueCollection, Field val, Collection<Cubic> cubicCollection) throws IllegalArgumentException, IllegalAccessException {
        int num = valueCollection.size() - 1;

        double[] gamma = new double[num + 1];
        double[] delta = new double[num + 1];
        double[] D = new double[num + 1];

        int i;
        /*
               We solve the equation
	          [2 1       ] [D[0]]   [3(x[1] - x[0])  ]
	          |1 4 1     | |D[1]|   |3(x[2] - x[0])  |
	          |  1 4 1   | | .  | = |      .         |
	          |    ..... | | .  |   |      .         |
	          |     1 4 1| | .  |   |3(x[n] - x[n-2])|
	          [       1 2] [D[n]]   [3(x[n] - x[n-1])]

	          by using row operations to convert the matrix to upper triangular
	          and then back sustitution.  The D[i] are the derivatives at the knots.
		 */
        gamma[0] = 1.0f / 2.0f;
        for(i = 1; i < num; i++) {
            gamma[i] = 1.0f / (4.0f - gamma[i - 1]);
        }
        gamma[num] = 1.0f / (2.0f - gamma[num - 1]);

        Double p0 = val.getDouble(valueCollection.get(0));
        Double p1 = val.getDouble(valueCollection.get(1));

        delta[0] = 3.0f * (p1 - p0) * gamma[0];
        for(i = 1; i < num; i++) {
            p0 = val.getDouble(valueCollection.get(i - 1));
            p1 = val.getDouble(valueCollection.get(i + 1));
            delta[i] = (3.0f * (p1 - p0) - delta[i - 1]) * gamma[i];
        }

        p0 = val.getDouble(valueCollection.get(num - 1));
        p1 = val.getDouble(valueCollection.get(num));

        delta[num] = (3.0f * (p1 - p0) - delta[num - 1]) * gamma[num];

        D[num] = delta[num];
        for(i = num - 1; i >= 0; i--) {
            D[i] = delta[i] - gamma[i] * D[i + 1];
        }

        cubicCollection.clear();

        for(i = 0; i < num; i++) {
            p0 = val.getDouble(valueCollection.get(i));
            p1 = val.getDouble(valueCollection.get(i + 1));

            cubicCollection.add(new Cubic(
                            p0,
                            D[i],
                            3 * (p1 - p0) - 2 * D[i] - D[i + 1],
                            2 * (p0 - p1) + D[i] + D[i + 1]
                    )
            );
        }
    }

    public class Cubic {
        private double a, b, c, d;

        public Cubic(double p0, double d2, double e, double f) {
            this.a = p0;
            this.b = d2;
            this.c = e;
            this.d = f;
        }

        public double eval(double u) {
            return (((d * u) + c) * u + b) * u + a;
        }
    }

    @Override
    public InterpolationType getInterpolationType() {
        return InterpolationType.SPLINE;
    }
}