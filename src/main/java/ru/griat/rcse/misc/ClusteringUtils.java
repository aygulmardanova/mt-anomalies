package ru.griat.rcse.misc;

import ru.griat.rcse.entity.Cluster;
import ru.griat.rcse.entity.Trajectory;
import ru.griat.rcse.entity.TrajectoryPoint;

import java.util.List;

import static ru.griat.rcse.misc.Utils.getTrajectoryPoints;

public class ClusteringUtils {

    /**
     * For each trajectory in @trajectories calc the adaptive epsilonX and epsilonY
     * for each trajectory point (key points for regression, and rdp points for RDP, RDP_N algorithms)
     *
     * @param trajectories list of trajectories to calculate Euclidean distances between each pair
     */
    public static void calcEuclDistancesToCP(List<Trajectory> trajectories, TrajectoryPoint cameraPoint, int maxX, int minX, int maxY, int minY) {
        trajectories.forEach(tr ->
                getTrajectoryPoints(tr).forEach(kp -> {
                    double cpDist = kp.distanceTo(cameraPoint);
                    kp.setEpsilons(cpDist, maxX, minX, maxY, minY);
                })
        );
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

}
