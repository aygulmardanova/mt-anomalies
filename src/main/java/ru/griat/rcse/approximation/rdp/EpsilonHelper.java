package ru.griat.rcse.approximation.rdp;

import ru.griat.rcse.entity.TrajectoryPoint;

import java.util.Arrays;
import java.util.List;

/*
* from https://github.com/LukaszWiktor/series-reducer
*/
public class EpsilonHelper {

    /**
     * For each 3 consecutive points in the list this function calculates the distance
     * from the middle point to a line defined by the first and third point.
     * <p>
     * The result may be used to find a proper epsilon by calculating
     * maximum {@link #max(double[])} or average {@link #avg(double[])} from
     * all deviations.
     */
    public static double[] deviations(List<TrajectoryPoint> points) {
        double[] deviations = new double[Math.max(0, points.size() - 2)];
        for (int i = 2; i < points.size(); i++) {
            TrajectoryPoint p1 = points.get(i - 2);
            TrajectoryPoint p2 = points.get(i - 1);
            TrajectoryPoint p3 = points.get(i);
            double dev = new Line(p1, p3).distance(p2);
            deviations[i - 2] = dev;
        }
        return deviations;
    }

    public static double sum(double[] values) {
        return Arrays.stream(values).sum();
    }

    public static double avg(double[] values) {
        return Arrays.stream(values).average().orElse(0.0);
    }

    public static double max(double[] values) {
        return Arrays.stream(values).max().orElse(0.0);
    }
}