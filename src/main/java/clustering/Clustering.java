package clustering;

import entity.Cluster;
import entity.Trajectory;
import entity.TrajectoryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.*;
import static java.util.stream.Collectors.toList;

public class Clustering {

    private static final Logger LOGGER = LoggerFactory.getLogger(Clustering.class.getName());

    /*
     * Stores clusters in a list.
     *
     *
     */
    private List<Map<Integer, Cluster>> clusters = new ArrayList<>();
    private List<Cluster> clusterIds = new ArrayList<>();

    private Double[][] trajLCSSDistances;
    private Double[][] clustLCSSDistances;
    private int minX;
    private int maxX;
    private int minY;
    private int maxY;

    public Clustering(List<Trajectory> trajectories) {
        trajLCSSDistances = new Double[trajectories.size()][trajectories.size()];
        clustLCSSDistances = new Double[trajectories.size()][trajectories.size()];
    }

    public void setBorders(int minX, int maxX, int minY, int maxY) {
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
    public List<Cluster> cluster(List<Trajectory> trajectories) {
        initClusters(trajectories);
        whileCluster(10);
        return clusterIds;
    }

    public void initClusters(List<Trajectory> trajectories) {
//    initialisation
        trajectories.forEach(trajectory ->
                clusterIds.add(new Cluster(trajectory)));
    }


    /**
     * stopPoint - desired number of clusters to stop:
     * if null - stop when 1 cluster is left
     *      if no joins are possible, stop.
     */
    public void whileCluster(Integer stopPoint) {
        if (stopPoint == null)
            stopPoint = 1;
        int numOfClusters = clustLCSSDistances.length;
        double minClustDist = Double.MAX_VALUE;
        while (numOfClusters > stopPoint) {
            List<Integer> ids = clusterIds.stream().map(Cluster::getId).collect(toList());
            int id1 = -1;
            int id2 = -1;
            for (Integer i1: ids) {
                for (Integer i2: ids) {
                    if (!i1.equals(i2) && clustLCSSDistances[i1][i2] != null && clustLCSSDistances[i1][i2] < minClustDist) {
                        minClustDist = clustLCSSDistances[i1][i2];
                        id1 = i1;
                        id2 = i2;
                    }
                }
            }
//            join i1 and i2 clusters, add i1 traj-es to cluster i2
            clusterIds.get(id1).appendTrajectories(clusterIds.get(id2).getTrajectories());
//            remove i2 from 'clusterIds' and 'clusters'
            clusterIds.remove(id2);
//            recalculate D for i1 and i2 lines -> set i2 line all to NULLs
            

            numOfClusters--;
        }
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
        trajLCSSDistances[t1.getId()][t2.getId()] = dist;
        clustLCSSDistances[t1.getId()][t2.getId()] = dist;
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

//      check last trajectory point (of each trajectory-part recursively)
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
                Double lcssDist = trajLCSSDistances[trajectory1.getId()][trajectory2.getId()];
                if (lcssDist != null && lcssDist < dist)
                    dist = lcssDist;
            }
        }
        return dist;
    }

    private void joinClusters() {

    }


}
