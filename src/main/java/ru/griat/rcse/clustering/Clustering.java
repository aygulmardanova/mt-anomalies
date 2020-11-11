package ru.griat.rcse.clustering;

import ru.griat.rcse.entity.Cluster;
import ru.griat.rcse.entity.Trajectory;
import ru.griat.rcse.entity.TrajectoryPoint;

import java.io.IOException;
import java.util.List;

public interface Clustering {

    TrajectoryPoint getCameraPoint();

    Double calcDist(Trajectory t1, Trajectory t2);

    List<Cluster> cluster(List<Trajectory> trajectories);

    void classifyTrajectories(List<Trajectory> inputTrajectories) throws IOException;

}
