package ru.griat.rcse.clustering;

import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.griat.rcse.entity.Cluster;
import ru.griat.rcse.entity.Trajectory;
import ru.griat.rcse.entity.TrajectoryPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.griat.rcse.misc.ClusteringUtils.flattenTrajectories;
import static ru.griat.rcse.misc.Utils.DBSCAN_EPS;
import static ru.griat.rcse.misc.Utils.DBSCAN_MIN_PTS;
import static ru.griat.rcse.misc.Utils.INPUT_FILE_NAMES_FIRST;
import static ru.griat.rcse.misc.Utils.displayTrajectories;
import static ru.griat.rcse.misc.Utils.getImgFileName;
import static ru.griat.rcse.misc.Utils.getTrajectoryPoints;

public class DBSCANClustering implements Clustering {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBSCANClustering.class.getName());

    private final List<Cluster> clusters;

    public DBSCANClustering() {
        clusters = new ArrayList<>();
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
        LOGGER.info("total clustered trajectories sum {}", trajectories.size());

        trajectories = trajectories.stream().filter(tr -> getTrajectoryPoints(tr).size() > 7).collect(Collectors.toList());
        flattenTrajectories(trajectories);
        LOGGER.info("total clustered trajectories sum {}", trajectories.size());

        DBSCANClusterer<Trajectory> clusterer = new DBSCANClusterer<>(210, 8);
        List<org.apache.commons.math3.ml.clustering.Cluster<Trajectory>> dbscanClusters = clusterer.cluster(trajectories);
        dbscanClusters.forEach(cl -> clusters.add(new Cluster(clusters.size(), cl.getPoints())));
        LOGGER.info("output clusters {}", dbscanClusters.size());
        LOGGER.info("total clustered trajectories sum {}", dbscanClusters.stream().mapToInt(cl -> cl.getPoints().size()).sum());

        try {
            displayTrajectories(getImgFileName(INPUT_FILE_NAMES_FIRST[0]), trajectories.stream().filter(tr -> clusters.stream().noneMatch(cl -> cl.getTrajectories().contains(tr))).collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return clusters;
    }

    @Override
    public void classifyTrajectories(List<Trajectory> inputTrajectories) throws IOException {

    }

}
