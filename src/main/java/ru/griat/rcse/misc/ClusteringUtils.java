package ru.griat.rcse.misc;

import com.google.common.math.Quantiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.griat.rcse.entity.Cluster;
import ru.griat.rcse.entity.Trajectory;
import ru.griat.rcse.entity.TrajectoryPoint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static ru.griat.rcse.misc.Utils.MAX_KP_COUNT;
import static ru.griat.rcse.misc.Utils.getTrajectoryPoints;

public class ClusteringUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(ClusteringUtils.class.getName());

    /**
     * For each trajectory in @trajectories calc the adaptive epsilonX and epsilonY
     * for each trajectory point (key points for regression, and rdp points for RDP, RDP_N algorithms)
     *
     * @param trajectories list of trajectories to calculate Euclidean distances between each pair
     */
    public static void calcEuclDistancesToCP(List<Trajectory> trajectories, TrajectoryPoint cameraPoint, int maxX, int minX, int maxY, int minY) {
//        final double[] minCPDist = {Double.MAX_VALUE};
//        final double[] minEpsX = {Double.MAX_VALUE};
//        final double[] minEpsY = {Double.MAX_VALUE};
//        final double[] maxCPDist = {0.0};
//        final double[] maxEpsX = {0.0};
//        final double[] maxEpsY = {0.0};
//        final double[] sumCPDist = { 0.0 };
//        final double[] sumEpsX = { 0.0 };
//        final double[] sumEpsY = { 0.0 };
//        final int[] count = {0};
        trajectories.forEach(tr ->
                getTrajectoryPoints(tr).forEach(kp -> {
                    double cpDist = kp.distanceTo(cameraPoint);
                    kp.setEpsilons(cpDist, maxX, minX, maxY, minY);

//                    if (cpDist > maxCPDist[0])
//                        maxCPDist[0] = cpDist;
//                    if (cpDist < minCPDist[0])
//                        minCPDist[0] = cpDist;
//
//                    if (kp.getEpsilonX() > maxEpsX[0])
//                        maxEpsX[0] = kp.getEpsilonX();
//                    if (kp.getEpsilonY() > maxEpsY[0])
//                        maxEpsY[0] = kp.getEpsilonY();
//                    if (kp.getEpsilonX() < minEpsX[0])
//                        minEpsX[0] = kp.getEpsilonX();
//                    if (kp.getEpsilonY() > minEpsY[0])
//                        minEpsY[0] = kp.getEpsilonY();
////                    sumCPDist[0] += cpDist;
//                    sumEpsX[0] += kp.getEpsilonX();
//                    sumEpsY[0] += kp.getEpsilonY();
//                    count[0]++;
                })
        );
//        LOGGER.info("minCPDist is {}", minCPDist[0]);
//        LOGGER.info("avgCPDist is {}", sumCPDist[0] / count[0]);
//        LOGGER.info("maxCPDist is {}", maxCPDist[0]);
//        LOGGER.info("minEpsX is {}", minEpsX[0]);
//        LOGGER.info("avgEpsX is {}", sumEpsX[0] / count[0]);
//        LOGGER.info("maxEpsX is {}", maxEpsX[0]);
//        LOGGER.info("minEpsY is {}", minEpsY[0]);
//        LOGGER.info("avgEpsY is {}", sumEpsY[0] / count[0]);
//        LOGGER.info("maxEpsY is {}", maxEpsY[0]);
    }

    public static boolean containsAbsolutelyDifferentTraj(Cluster c1, Cluster c2, Double[][] trajLCSSDistances) {
        for (Trajectory t1 : c1.getTrajectories()) {
            for (Trajectory t2 : c2.getTrajectories()) {
                if (trajLCSSDistances[t1.getId()][t2.getId()] != null && trajLCSSDistances[t1.getId()][t2.getId()] == 1.0)
                    return true;
            }
        }
        return false;
    }

    /**
     * Calculates shortened trajectory by excluding last trajectory point
     *
     * @param t trajectory
     * @return trajectory without last trajectory point
     */
    public static Trajectory head(Trajectory t) {
        Trajectory tClone = t.clone();
        getTrajectoryPoints(tClone).remove(getTrajectoryPoints(tClone).size() - 1);
        return tClone;
    }

    /**
     * Flatten all the given trajectories by representing each trajectory as a list of points:
     * {(x1, y1), (x2, y2), ... (xn, yn)} --> {x1, y1, x2, y2, ..., xn, yn}
     * All the input trajectories must be of the same length
     *
     * @param trajectories list of trajectories
     */
    public static void flattenTrajectories(List<Trajectory> trajectories) {
        boolean diffLength = trajectories.stream().anyMatch(tr -> getTrajectoryPoints(tr).size() != MAX_KP_COUNT);
        if (diffLength) {
            throw new UnsupportedOperationException("DBSCAN can not be applied to vectors of different length");
        }
        for (Trajectory trajectory : trajectories) {
            List<TrajectoryPoint> keyPoints = getTrajectoryPoints(trajectory);
            double[] tps = new double[2 * keyPoints.size()];
            for (int i = 0; i < keyPoints.size(); i++) {
                tps[2 * i] = keyPoints.get(i).getX();
                tps[2 * i + 1] = keyPoints.get(i).getY();
            }
            trajectory.setPoint(tps);
        }
    }

    public static void printClusters(List<Cluster> clusters) {
        for (Cluster cluster : clusters) {
            LOGGER.info(cluster.toString());
        }
    }

    public static void classifyClusters(List<Cluster> clusters) {
        List<Integer> cardinalities = clusters.stream().map(cl -> cl.getTrajectories().size()).sorted().collect(Collectors.toList());
        double limit = Quantiles.quartiles().index(1).compute(cardinalities);
        clusters.forEach(cl -> {
            cl.setNormal(cl.getTrajectories().size() >= limit);
        });
    }

    /**
     * Dunn's Validity Index (DI) = dist_min / diam_max
     * dist_min = min inter-cluster distance (minimum distance between two clusters;
     * single-linkage -> min distance between two trajectories from two clusters)
     * diam_max = max intra-cluster distance (maximum distance between two farthermost trajectories)
     */
    public static void validateClusters(List<Cluster> clusters, Double[][] clustLCSSDistances, Double[][] trajLCSSDistances) {
        clusters.forEach(cluster -> cluster.getTrajectories().sort(Comparator.comparing(Trajectory::getId)));

        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < clusters.size(); i++) {
            for (int j = i + 1; j < clusters.size(); j++) {
                if (clustLCSSDistances[clusters.get(i).getId()][clusters.get(j).getId()] < minDist)
                    minDist = clustLCSSDistances[clusters.get(i).getId()][clusters.get(j).getId()];
            }
        }

        double maxDiam = clusters.stream().mapToDouble(cluster -> {
            double maxDist = 0;
            for (int i = 0; i < cluster.getTrajectories().size(); i++) {
                for (int j = i + 1; j < cluster.getTrajectories().size(); j++) {
                    if (trajLCSSDistances[cluster.getTrajectories().get(i).getId()][cluster.getTrajectories().get(j).getId()] > maxDist)
                        maxDist = trajLCSSDistances[cluster.getTrajectories().get(i).getId()][cluster.getTrajectories().get(j).getId()];
                }
            }
            return maxDist;
        }).max().getAsDouble();

        double DI = minDist / maxDiam;
        LOGGER.info(String.format("DI = %.2f", DI));
    }

}
