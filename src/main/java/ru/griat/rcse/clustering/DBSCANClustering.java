package ru.griat.rcse.clustering;

import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.griat.rcse.entity.Cluster;
import ru.griat.rcse.entity.Trajectory;
import ru.griat.rcse.entity.TrajectoryPoint;
import ru.griat.rcse.misc.ClusteringUtils;
import ru.griat.rcse.misc.ParseUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.griat.rcse.misc.Utils.DBSCAN_EPS;
import static ru.griat.rcse.misc.Utils.DBSCAN_MIN_PTS;
import static ru.griat.rcse.misc.Utils.LINKAGE_METHOD;
import static ru.griat.rcse.misc.Utils.getTrajectoryPoints;

public class DBSCANClustering implements Clustering {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBSCANClustering.class.getName());

    private final List<Cluster> clusters;
    private final int initialSize;

    public DBSCANClustering(int initialSize) {
        clusters = new ArrayList<>();
        this.initialSize = initialSize;
    }

    @Override
    public TrajectoryPoint getCameraPoint() {
        throw new UnsupportedOperationException("getCameraPoint() method is not supported in DBSCAN clustering");
    }

    @Override
    public Double calcDist(Trajectory t1, Trajectory t2) {
        return null;
    }

    @Override
    public List<Cluster> cluster(List<Trajectory> trajectories) {
        LOGGER.info("total total trajectories sum {}", trajectories.size());

        trajectories = trajectories.stream().filter(tr -> getTrajectoryPoints(tr).size() > 7).collect(Collectors.toList());
        ClusteringUtils.flattenTrajectories(trajectories);
        LOGGER.info("total flattened trajectories sum {}", trajectories.size());

        DBSCANClusterer<Trajectory> clusterer = new DBSCANClusterer<>(DBSCAN_EPS, DBSCAN_MIN_PTS);
        List<org.apache.commons.math3.ml.clustering.Cluster<Trajectory>> dbscanClusters = clusterer.cluster(trajectories);
        dbscanClusters.forEach(cl -> clusters.add(new Cluster(clusters.size(), cl.getPoints())));
        LOGGER.info("output clusters {}", dbscanClusters.size());
        LOGGER.info("total clustered trajectories sum {}", dbscanClusters.stream().mapToInt(cl -> cl.getPoints().size()).sum());

        validateClusters(trajectories);

        return clusters;
    }

//        DI calculation
    private void validateClusters(List<Trajectory> trajectories) {
        Double[][] trajLCSSDistances = calcTrajLCSSDistances(trajectories);
        Double[][] clustLCSSDistances = calcClustLCSSDistances(trajLCSSDistances);
        ClusteringUtils.validateClusters(clusters, clustLCSSDistances, trajLCSSDistances);
    }

    @Override
    public void classifyTrajectories(List<Trajectory> inputTrajectories) {

    }

    private Double[][] calcTrajLCSSDistances(List<Trajectory> trajectories) {
        Double[][] trajLCSSDistances = new Double[initialSize][initialSize];

        HierarchicalClustering hc = new HierarchicalClustering(trajectories);
        ParseUtils.setInputBorders(trajectories, hc);
        trajectories.sort(Comparator.comparing(Trajectory::getId));

        for (Trajectory t1 : trajectories) {
            for (Trajectory t2 : trajectories) {
                if (t1.getId() != t2.getId() && t1.getId() < t2.getId()) {
                    double dist = hc.calcDist(t1, t2);
                    trajLCSSDistances[t1.getId()][t2.getId()] = dist;
                }
            }
        }
        return trajLCSSDistances;
    }

    private Double[][] calcClustLCSSDistances(Double[][] trajLCSSDistances) {
        Double[][] clustLCSSDistances = new Double[clusters.size()][clusters.size()];
        for (Cluster cluster1 : clusters) {
            for (Cluster cluster2 : clusters) {
                clustLCSSDistances[cluster1.getId()][cluster2.getId()] =
                        calcClustersDist(cluster1, cluster2, trajLCSSDistances);
            }
        }
        return clustLCSSDistances;
    }

    private Double calcClustersDist(Cluster cluster1, Cluster cluster2, Double[][] trajLCSSDistances) {
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

                        if (lcssDist == null)
                            lcssDist = trajLCSSDistances[trajectory2.getId()][trajectory1.getId()];
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

}
