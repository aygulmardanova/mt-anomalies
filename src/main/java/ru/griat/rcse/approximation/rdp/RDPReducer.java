package ru.griat.rcse.approximation.rdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.griat.rcse.entity.TrajectoryPoint;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static ru.griat.rcse.misc.ApproximationUtils.calcDist;

/*
 * from https://github.com/LukaszWiktor/series-reducer
 */
public class RDPReducer {

    private final static Logger LOGGER = LoggerFactory.getLogger(RDPReducer.class.getName());

    /**
     * Reduces number of points in given series using Ramer-Douglas-Peucker algorithm.
     *
     * @param points  initial, ordered list of points {@link TrajectoryPoint}
     * @param epsilon allowed margin of the resulting curve, has to be > 0
     */
    public static List<TrajectoryPoint> reduce(List<TrajectoryPoint> points, double epsilon) {
//        TODO: how to evaluate?
//        findProperEpsilon(points);

        if (epsilon < 0) {
            throw new IllegalArgumentException("Epsilon cannot be less then 0.");
        }
        double furthestPointDistance = 0.0;
        int furthestPointIndex = 0;
        Line line = new Line(points.get(0), points.get(points.size() - 1));
//        between all points find the point with the max distance from the simplifying line
        for (int i = 1; i < points.size() - 1; i++) {
            double distance = line.distance(points.get(i));
            if (distance > furthestPointDistance) {
                furthestPointDistance = distance;
                furthestPointIndex = i;
            }
        }
//        if chosen point is farther than the defined epsilon, then add it to output simplified trajectory points list
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

    /**
     * Reduces number of points in given series using Ramer-Douglas-Peucker N algorithm to the total amount of given 'count'.
     *
     * @param points    initial, ordered list of points {@link TrajectoryPoint}
     * @param count     allowed margin of the resulting curve, has to be > 0
     */
    public static List<TrajectoryPoint> reduceToN(List<TrajectoryPoint> points, int count) {
//        TODO: how to evaluate?
//        findProperEpsilon(points);
        List<TrajectoryPoint> pointsCopy = points.stream()
                .map(TrajectoryPoint::clone).collect(toList())
                .subList(1, points.size() - 1);

        if (count < 2) {
            throw new IllegalArgumentException("Points count cannot be less then 2.");
        }
        double furthestPointDistance = 0.0;
        TrajectoryPoint furthestPoint = null;

        List<TrajectoryPoint> simplified = new ArrayList<>();
        simplified.add(points.get(0));
        simplified.add(points.get(points.size() - 1));

        while (simplified.size() < count) {
//            for each original point from pointsCopy -> define line segment, calc the distance
            for (TrajectoryPoint point: pointsCopy) {
                double dist = calcDist(point, simplified);
                if (dist > furthestPointDistance) {
                    furthestPointDistance = dist;
                    furthestPoint = point;
                }
            }
            if (furthestPoint == null)
                break;
            simplified.add(furthestPoint);
            pointsCopy.remove(furthestPoint);
            furthestPointDistance = 0.0;
            furthestPoint = null;
        }

        return simplified;
    }

    private static void findProperEpsilon(List<TrajectoryPoint> points) {
        double[] deviations = EpsilonHelper.deviations(points);
        System.out.println("max deviations: " + EpsilonHelper.max(deviations));
        System.out.println("avg deviations: " + EpsilonHelper.avg(deviations));
    }

}
