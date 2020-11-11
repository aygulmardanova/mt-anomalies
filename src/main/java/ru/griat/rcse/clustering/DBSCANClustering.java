package ru.griat.rcse.clustering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.griat.rcse.entity.Cluster;
import ru.griat.rcse.entity.Trajectory;
import ru.griat.rcse.entity.TrajectoryPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DBSCANClustering implements Clustering {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBSCANClustering.class.getName());

    private List<Cluster> clusters;

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
        return clusters;
    }

    @Override
    public void classifyTrajectories(List<Trajectory> inputTrajectories) throws IOException {

    }

}
