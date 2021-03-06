package ru.griat.rcse.clustering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.griat.rcse.entity.Cluster;
import ru.griat.rcse.entity.Trajectory;
import ru.griat.rcse.entity.TrajectoryPoint;
import ru.griat.rcse.misc.ClusteringUtils;
import ru.griat.rcse.visualisation.DisplayImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;
import static ru.griat.rcse.misc.ClusteringUtils.calcEuclDistancesToCP;
import static ru.griat.rcse.misc.ClusteringUtils.head;
import static ru.griat.rcse.misc.Utils.IS_ADAPTIVE;
import static ru.griat.rcse.misc.Utils.LINKAGE_METHOD;
import static ru.griat.rcse.misc.Utils.OUTPUT_CLUSTERS_COUNT;
import static ru.griat.rcse.misc.Utils.STATIC_COEFF;
import static ru.griat.rcse.misc.Utils.getImgFileName;
import static ru.griat.rcse.misc.Utils.getTrajectoryPoints;

public class HierarchicalClustering implements Clustering {

    private static final Logger LOGGER = LoggerFactory.getLogger(HierarchicalClustering.class.getName());

    private List<Cluster> clusters;

    private Double[][] trajLCSSDistances;
    private Double[][] clustLCSSDistances;
    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private TrajectoryPoint cameraPoint;

    @Override
    public TrajectoryPoint getCameraPoint() {
        return cameraPoint;
    }

    public HierarchicalClustering(List<Trajectory> trajectories) {
        clusters = new ArrayList<>();
        trajLCSSDistances = new Double[trajectories.size()][trajectories.size()];
        clustLCSSDistances = new Double[trajectories.size()][trajectories.size()];
    }

    public Double[][] getTrajLCSSDistances() {
        return trajLCSSDistances;
    }

    public void setTrajLCSSDistances(Double[][] trajLCSSDistances) {
        this.trajLCSSDistances = trajLCSSDistances;
        for (int i = 0; i < trajLCSSDistances.length; i++) {
            System.arraycopy(trajLCSSDistances[i], 0, clustLCSSDistances[i], 0, trajLCSSDistances.length);
        }
    }

    public void setBorders(int minX, int maxX, int minY, int maxY, List<Trajectory> trajectories) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.cameraPoint = new TrajectoryPoint((int) Math.round(0.25 * maxX), (int) Math.round(0.95 * maxY));
        calcEuclDistancesToCP(trajectories, cameraPoint, maxX, minX, maxY, minY);
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
    @Override
    public List<Cluster> cluster(List<Trajectory> trajectories) {
        initClusters(trajectories);
        whileCluster(OUTPUT_CLUSTERS_COUNT);

        ClusteringUtils.printClusters(clusters);
        ClusteringUtils.validateClusters(clusters, clustLCSSDistances, trajLCSSDistances);
        long start = System.currentTimeMillis();
        ClusteringUtils.classifyClusters(clusters);
        modelClusters();
        long end = System.currentTimeMillis();
        LOGGER.info("total classification time {}", end - start);
        LOGGER.info("{} clusters in total", clusters.size());
        return clusters;
    }

    /**
     * Trajectories classification
     * Based on using defined clusters' models
     * - compares input trajectory with each cluster by calculating the LCSS distance to the cluster model
     * - finds the closest cluster taking into consideration threshold value *defining the maximum allowed distance)
     * - - no cluster was found -> trajectory is anomalous (unknown behavior)
     * - - cluster is found -> depends on the label of the cluster
     *
     * @param inputTrajectories A list of input trajectories to be classified
     *
     * Classifies each input trajectory and print the classification result
     */
    @Override
    public void classifyTrajectories(List<Trajectory> inputTrajectories) throws IOException {
        double lcssMax = 0.85;
        List<Trajectory> anomalousTrajectories = new ArrayList<>();
        inputTrajectories.forEach(it -> {
            final double[] minLcss = {1.0};
            final Cluster[] closestCluster = {null};
            calcEuclDistancesToCP(inputTrajectories, cameraPoint, maxX, minX, maxY, minY);

            System.out.println(String.format("------tr %s-----", it.getId()));
            clusters.forEach(cl -> {
                double curLcss = calcDist(it, cl.getClusterModel());
                if (curLcss < minLcss[0]) {
                    minLcss[0] = curLcss;
                    closestCluster[0] = cl;
                }
                System.out.println(String.format("dist to cl %s = %.2f", cl.getId(), curLcss));
            });
            if (closestCluster[0] == null || minLcss[0] > lcssMax) {
                System.out.println("anomalous trajectory");
                anomalousTrajectories.add(it);
            } else {
                System.out.println(String.format("closest cl is %s", closestCluster[0].getId()));
                System.out.println(closestCluster[0].getNormal() ? "normal trajectory" : "anomalous trajectory");
            }
        });
        new DisplayImage().displayAndSave(getImgFileName("1"), null, null, anomalousTrajectories, false);
    }

    public void initClusters(List<Trajectory> trajectories) {
//    initialisation
        trajectories.forEach(trajectory ->
                clusters.add(new Cluster(trajectory.getId(), trajectory)));
    }

    /**
     * stopPoint - desired number of clusters to stop:
     * if null - stop when 1 cluster is left
     * if no joins are possible, stop.
     */
    public void whileCluster(Integer stopPoint) {
        if (stopPoint == null)
            stopPoint = 1;
        int numOfClusters = clusters.size();
        int id1;
        int id2;
        double minClustDist;
        while (numOfClusters > stopPoint) {
            id1 = -1;
            id2 = -1;
            minClustDist = Double.MAX_VALUE;
            for (int i1 = 0; i1 < clusters.size(); i1++) {
                for (int i2 = i1 + 1; i2 < clusters.size(); i2++) {
                    if (i1 != i2
                            && clustLCSSDistances[clusters.get(i1).getId()][clusters.get(i2).getId()] != null
                            && clustLCSSDistances[clusters.get(i1).getId()][clusters.get(i2).getId()] <= minClustDist
//                            && !containsAbsolutelyDifferentTraj(clusters.get(i1), clusters.get(i2), trajLCSSDistances)
                    ) {
//                        FIXME: for normal clustering uncomment lines
//                        if (clusters.size() > 25 && !containsAbsolutelyDifferentTraj(clusters.get(i1), clusters.get(i2), trajLCSSDistances)
//                                || clusters.size() <= 50 && clustLCSSDistances[clusters.get(i1).getId()][clusters.get(i2).getId()] <= 0.91
//                                || clusters.size() <= 25) {
                        minClustDist = clustLCSSDistances[clusters.get(i1).getId()][clusters.get(i2).getId()];
                        id1 = i1;
                        id2 = i2;
//                        }
                    }
                }
            }
            if (id1 < 0 || id2 < 0) {
                break;
            }
//            join i1 and i2 clusters, add i1 traj-es to cluster i2
//            recalculate D for i1 and i2 lines -> set i2 line all to NULLs
//            remove i2 from 'clusters'

            clusters.get(id1).appendTrajectories(clusters.get(id2).getTrajectories());
            recalcClustersDistMatrix(id1, id2);
            clusters.remove(id2);

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
    @Override
    public Double calcDist(Trajectory t1, Trajectory t2) {
        int m = getTrajectoryPoints(t1).size();
        int n = getTrajectoryPoints(t2).size();

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
        List<TrajectoryPoint> trajectoryPoints1 = getTrajectoryPoints(t1);
        List<TrajectoryPoint> trajectoryPoints2 = getTrajectoryPoints(t2);
        int m = trajectoryPoints1.size();
        int n = trajectoryPoints2.size();

        if (m == 0 || n == 0) {
            return 0.0;
        }

//        calculate adaptive
        if (IS_ADAPTIVE) {
            TrajectoryPoint tp1 = trajectoryPoints1.get(m - 1);
            TrajectoryPoint tp2 = trajectoryPoints2.get(n - 1);
            epsilonX = getEpsilonX(tp1, tp2);
            epsilonY = getEpsilonY(tp1, tp2);
        }

//      check last trajectory point (of each trajectory-part recursively)
//      according to [8]: delta and epsilon as thresholds for X- and Y-axes respectively
//      Then the abscissa difference and ordinate difference are less than thresholds (they are relatively close to each other)
//      they are considered similar and LCSS distance is increased by 1
        if (abs(trajectoryPoints1.get(m - 1).getX() - trajectoryPoints2.get(n - 1).getX()) < epsilonX
                && abs(trajectoryPoints1.get(m - 1).getY() - trajectoryPoints2.get(n - 1).getY()) < epsilonY
                && abs(m - n) <= delta) {
            return 1 + calcLCSS(head(t1), head(t2), delta, epsilonX, epsilonY);
        } else {
            return max(
                    calcLCSS(head(t1), t2, delta, epsilonX, epsilonY),
                    calcLCSS(t1, head(t2), delta, epsilonX, epsilonY)
            );
        }
    }

    /**
     * calc δ
     *
     * @param m length of first trajectory
     * @param n length of second trajectory
     * @return δ value
     */
    private Double getDelta(int m, int n) {
        return 0.2 * min(m, n);
    }

    /**
     * calc ε for X
     *
     * @param m length of first trajectory
     * @param n length of second trajectory
     * @return ε value
     */
    private Double getEpsilonX(int m, int n) {
        return STATIC_COEFF * (maxX - minX);
    }

    private Double getEpsilonX(TrajectoryPoint tp1, TrajectoryPoint tp2) {
        switch (LINKAGE_METHOD) {
            case SINGLE:
                return Math.min(tp1.getEpsilonX(), tp2.getEpsilonX());
            case AVERAGE:
                return (tp1.getEpsilonX() + tp2.getEpsilonX()) / 2;
            case MAXIMUM:
                return Math.max(tp1.getEpsilonX(), tp2.getEpsilonX());
        }
        return STATIC_COEFF * (maxX - minX);
    }

    /**
     * calc ε for Y
     *
     * @param m length of first trajectory
     * @param n length of second trajectory
     * @return ε value
     */
    private Double getEpsilonY(int m, int n) {
        return STATIC_COEFF * (maxY - minY);
    }

    private Double getEpsilonY(TrajectoryPoint tp1, TrajectoryPoint tp2) {
        switch (LINKAGE_METHOD) {
            case SINGLE:
                return Math.min(tp1.getEpsilonY(), tp2.getEpsilonY());
            case AVERAGE:
                return (tp1.getEpsilonY() + tp2.getEpsilonY()) / 2;
            case MAXIMUM:
                return Math.max(tp1.getEpsilonY(), tp2.getEpsilonY());
        }
        return STATIC_COEFF * (maxY - minY);
    }

    /**
     * At each step calc a distance matrix btwn clusters
     * Merge two clusters with a min dist -> requires an update of the dist matrix
     * because of the implementation: clusterId1 < clusterId2
     *
     * @param clusterId1 index of left joined cluster in clusters list (remained cluster)
     * @param clusterId2 index of right joined cluster in clusters list (removed cluster)
     */
    private void recalcClustersDistMatrix(int clusterId1, int clusterId2) {
        for (int i = 0; i < clusters.size(); i++) {
            for (int j = i + 1; j < clusters.size(); j++) {
                clustLCSSDistances[clusters.get(i).getId()][clusters.get(j).getId()] =
                        calcClustersDist(clusters.get(i), clusters.get(j));
            }
        }
//        for (int i = 0; i < clusterId1; i++) {
//            clustLCSSDistances[clusters.get(i).getId()][clusterId1] = calcClustersDist(clusters.get(i), clusters.get(clusterId1), method);
//        }
//        for (int j = clusterId2; j < clusters.size(); j++) {
//            clustLCSSDistances[clusterId2][clusters.get(j).getId()] = calcClustersDist(clusters.get(clusterId2), clusters.get(j), method);
//        }
        clustLCSSDistances[clusters.get(clusterId1).getId()][clusters.get(clusterId2).getId()] = null;
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
        double dist = 0.0;
        switch (LINKAGE_METHOD) {
            case SINGLE: {
                dist = Double.MAX_VALUE;
                for (Trajectory trajectory1 : cluster1.getTrajectories()) {
                    for (Trajectory trajectory2 : cluster2.getTrajectories()) {
                        Double lcssDist = trajLCSSDistances[trajectory1.getId()][trajectory2.getId()];
                        if (lcssDist != null && lcssDist < dist)
                            dist = lcssDist;
                    }
                }
                break;
            }
            case AVERAGE: {
                int count = 0;
                for (Trajectory trajectory1 : cluster1.getTrajectories()) {
                    for (Trajectory trajectory2 : cluster2.getTrajectories()) {
                        Double lcssDist = trajLCSSDistances[trajectory1.getId()][trajectory2.getId()];
                        if (lcssDist != null) {
                            dist += lcssDist;
                            count++;
                        }
                    }
                }
                dist = dist / count;
                break;
            }
            case MAXIMUM: {
                dist = Double.MIN_VALUE;
                for (Trajectory trajectory1 : cluster1.getTrajectories()) {
                    for (Trajectory trajectory2 : cluster2.getTrajectories()) {
                        Double lcssDist = trajLCSSDistances[trajectory1.getId()][trajectory2.getId()];
                        if (lcssDist != null && lcssDist > dist)
                            dist = lcssDist;
                    }
                }
                break;
            }
        }
        return dist;
    }

    private void modelClusters() {
        for (Cluster c : clusters) {
            if (c.getTrajectories().size() == 1) {
                c.setClusterModel(c.getTrajectories().get(0));
                continue;
            }
            Trajectory model = null;
            double avg = Double.MAX_VALUE;
            for (Trajectory t : c.getTrajectories()) {
                double sum = 0.0;
                for (Trajectory t1 : c.getTrajectories()) {
                    if (t1 != t)
                        sum += t.getId() < t1.getId() ? trajLCSSDistances[t.getId()][t1.getId()] : trajLCSSDistances[t1.getId()][t.getId()];
                }
                double curAvg = sum / (c.getTrajectories().size() - 1);
                if (curAvg < avg) {
                    model = t;
                    avg = curAvg;
                }
            }
            c.setClusterModel(model);
        }
    }

}
