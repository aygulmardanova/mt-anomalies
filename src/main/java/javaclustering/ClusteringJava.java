package javaclustering;

import entity.Cluster;
import entity.Trajectory;
import entity.TrajectoryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.lang.Math.*;
import static java.util.stream.Collectors.toList;

public class ClusteringJava {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusteringJava.class.getName());

    /*
     * Stores clusters in a list.
     *
     *
     */
    private List<Map<Integer, Cluster>> clusters = new ArrayList<>();

    private Double[][] lcssDistances;
    private long minX;
    private long maxX;
    private long minY;
    private long maxY;

    public ClusteringJava(List<Trajectory> trajectories) {
        lcssDistances = new Double[trajectories.size()][trajectories.size()];
    }

    public void setBorders(long minX, long maxX, long minY, long maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    /**
     * Single linkage
     * <p>
     * Implementation of Agglomerative Hierarchical Clustering of trajectories
     * \\Initialisation :
     * Initialize the clusters with one trajectory to each cluster
     * <p>
     * \\WHILE Process:
     * while number of clusters is greater than 1 do
     * - Calculate the similarity matrix D between pairs of clusters
     * - Find the smallest LCSS distance between clusters in D
     * - Merge the two clusters with minimum LCSS distance in a single cluster
     * - Remove the two clusters
     * end while
     *
     * @param trajectories A database of trajectories
     * @return Clusters of trajectories
     */
    public void cluster(List<Trajectory> trajectories) {
//    initialisation
        final int[] i = {0};
        trajectories.forEach(trajectory -> {
            clusters.add(new HashMap<Integer, Cluster>() {{
                put(i[0], new Cluster(trajectory));
            }});
            i[0]++;
        });
    }

    /**
     * Calculates LCSS distance for two input trajectories
     * Smaller the LCSS distance - the better (0.0 - equal trajectories)
     *
     * @param t1 first trajectory
     * @param t2 second trajectory
     * @return LCSS distance for t1 and t2
     */
    public Double calcLCSSDist(Trajectory t1, Trajectory t2) {
        int m = t1.length();
        int n = t2.length();

        double delta = getDelta(m, n);
        double epsilonX = getEpsilonX(m, n);
        double epsilonY = getEpsilonY(m, n);

        double dist = 1 - calcLCSS(t1, t2, delta, epsilonX, epsilonY) / min(m, n);
        lcssDistances[t1.getId()][t2.getId()] = dist;
        return dist;
    }


    /**
     * Calculates LCSS for two input trajectories
     * Bigger the LCSS - the better
     *
     * @param t1       first trajectory
     * @param t2       second trajectory
     * @param delta    δ parameter: how far we can look in time to match a given point from one T to a point in another T
     * @param epsilonX ε parameter: the size of proximity in which to look for matches on X-coordinate
     * @param epsilonY ε parameter: the size of proximity in which to look for matches on Y-coordinate
     * @return LCSS for t1 and t2
     */
    private Double calcLCSS(Trajectory t1, Trajectory t2, Double delta, Double epsilonX, Double epsilonY) {
        int m = t1.length();
        int n = t2.length();

        if (m == 0 || n == 0) {
            return 0.0;
        }

//      according to [8]: delta and epsilon as thresholds for X- and Y-axes respectively
//      Then the abscissa difference and ordinate difference are less than thresholds (they are relatively close to each other)
//      they are considered similar and LCSS distance is increased by 1
        else if (abs(t1.get(m - 1).getX() - t2.get(n - 1).getX()) < epsilonX
                && abs(t1.get(m - 1).getY() - t2.get(n - 1).getY()) < epsilonY
                && abs(m - n) <= delta) {
            return 1 + calcLCSS(rest(t1), rest(t2), delta, epsilonX, epsilonY);
        } else {
            return max(
                    calcLCSS(rest(t1), t2, delta, epsilonX, epsilonY),
                    calcLCSS(t1, rest(t2), delta, epsilonX, epsilonY)
            );
        }
    }

    /**
     * Calculates shortened trajectory by excluding last trajectory point
     *
     * @param t trajectory
     * @return trajectory without last trajectory point
     */
    private Trajectory rest(Trajectory t) {
        List<TrajectoryPoint> tpClone = t.getTrajectoryPoints().stream()
                .map(TrajectoryPoint::clone).collect(toList());
        tpClone.remove(t.length() - 1);
        return new Trajectory(t.getId(), tpClone);
    }

    /**
     * calc δ
     *
     * @param m length of first trajectory
     * @param n length of second trajectory
     * @return δ value
     */
    private Double getDelta(int m, int n) {
        return 0.5 * min(m, n);
    }

    /**
     * calc ε for X
     *
     * @param m length of first trajectory
     * @param n length of second trajectory
     * @return ε value
     */
    private Double getEpsilonX(int m, int n) {
        return 0.1 * (maxX - minX);
    }

    /**
     * calc ε for Y
     *
     * @param m length of first trajectory
     * @param n length of second trajectory
     * @return ε value
     */
    private Double getEpsilonY(int m, int n) {
        return 0.1 * (maxY - minY);
    }

    /**
     * At each step calc a distance matrix btwn clusters
     * Merge two clusters with a min dist -> requires an update of the dist matrix
     *
     * @param level which level of hierarchical tree of clusters to work with
     */
    private void calcClustersDistMatrix(int level) {

    }

    /**
     * Calculates inter-clusters distance for two input clusters
     * using 'single-link' linkage method:
     * the between-cluster distance == the min distance btwn two trajectories in the two clusters
     *
     * @param cluster1 first cluster
     * @param cluster2 second cluster
     * @return distance between clusters
     */
    private Double calcClustersDist(Cluster cluster1, Cluster cluster2) {
        double dist = Double.MAX_VALUE;
        for (Trajectory trajectory1 : cluster1.getTrajectories()) {
            for (Trajectory trajectory2 : cluster2.getTrajectories()) {
                double lcssDist = calcLCSSDist(trajectory1, trajectory2);
                if (lcssDist < dist)
                    dist = lcssDist;
            }
        }
        return dist;
    }


}
