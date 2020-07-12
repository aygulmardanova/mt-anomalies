package entity;

import java.util.ArrayList;
import java.util.List;

public class Cluster {

    private int id;

    private int level;

    private List<Trajectory> trajectories;

    public Cluster() {
    }

    public Cluster(Trajectory trajectory) {
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<Trajectory> getTrajectories() {
        return trajectories;
    }

    public void setTrajectories(List<Trajectory> trajectories) {
        this.trajectories = trajectories;
    }

    public void appendTrajectory(Trajectory trajectory) {
        this.trajectories.add(trajectory);
    }

    public void appendTrajectories(List<Trajectory> trajectories) {
        this.trajectories.addAll(trajectories);
    }

}
