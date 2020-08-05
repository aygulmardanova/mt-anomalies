package ru.griat.rcse.entity;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class Cluster {

    private int id;

    private List<Trajectory> trajectories;

    private Trajectory clusterModel;

    public Cluster() {
    }

    public Cluster(Trajectory trajectory) {
        this.trajectories = new ArrayList<>();
        this.trajectories.add(trajectory);
    }

    public Cluster(int id, Trajectory trajectory) {
        this.id = id;
        this.trajectories = new ArrayList<>();
        this.trajectories.add(trajectory);
    }

    public Cluster(List<Trajectory> trajectories) {
        this.trajectories = trajectories;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Trajectory> getTrajectories() {
        return trajectories;
    }

    public void setTrajectories(List<Trajectory> trajectories) {
        this.trajectories = trajectories;
    }

    public Trajectory getClusterModel() {
        return clusterModel;
    }

    public void setClusterModel(Trajectory clusterModel) {
        this.clusterModel = clusterModel;
    }

    public void appendTrajectory(Trajectory trajectory) {
        this.trajectories.add(trajectory);
    }

    public void appendTrajectories(List<Trajectory> trajectories) {
        this.trajectories.addAll(trajectories);
    }

    @Override
    public String toString() {
        return "Cluster " + id + ": {" +
                "trajectories=(" + trajectories.stream()
                .map(tr -> String.valueOf(tr.getId()))
                .collect(joining("), (")) +
                ")}";
    }
}
