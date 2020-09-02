package ru.griat.rcse.approximation.rdp;

import ru.griat.rcse.entity.TrajectoryPoint;

import java.util.ArrayList;
import java.util.List;

/*
 * from https://github.com/LukaszWiktor/series-reducer
 */
public class RDPReducer {

    /**
     * Reduces number of points in given series using Ramer-Douglas-Peucker algorithm.
     *
     * @param points  initial, ordered list of points {@link TrajectoryPoint}
     * @param epsilon allowed margin of the resulting curve, has to be > 0
     */
    public static List<TrajectoryPoint> reduce(List<TrajectoryPoint> points, double epsilon) {
        if (epsilon < 0) {
            throw new IllegalArgumentException("Epsilon cannot be less then 0.");
        }
        double furthestPointDistance = 0.0;
        int furthestPointIndex = 0;
        Line line = new Line(points.get(0), points.get(points.size() - 1));
        for (int i = 1; i < points.size() - 1; i++) {
            double distance = line.distance(points.get(i));
            if (distance > furthestPointDistance) {
                furthestPointDistance = distance;
                furthestPointIndex = i;
            }
        }
        if (furthestPointDistance > epsilon) {
            List<TrajectoryPoint> reduced1 = reduce(points.subList(0, furthestPointIndex + 1), epsilon);
            List<TrajectoryPoint> reduced2 = reduce(points.subList(furthestPointIndex, points.size()), epsilon);
            List<TrajectoryPoint> result = new ArrayList<>(reduced1);
            result.addAll(reduced2.subList(1, reduced2.size()));
            return result;
        } else {
            return line.asList();
        }
    }


}
