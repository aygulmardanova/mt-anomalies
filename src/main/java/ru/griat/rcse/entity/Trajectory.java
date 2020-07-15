package ru.griat.rcse.entity;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

public class Trajectory {

    private int id;

    private List<TrajectoryPoint> trajectoryPoints;

    public Trajectory() {
    }

    public Trajectory(List<TrajectoryPoint> trajectoryPoints) {
        this.trajectoryPoints = trajectoryPoints;
    }

    public Trajectory(int id, List<TrajectoryPoint> trajectoryPoints) {
        this.id = id;
        this.trajectoryPoints = trajectoryPoints;
    }

    public int getId() {
        return id;
    }

    public List<TrajectoryPoint> getTrajectoryPoints() {
        return trajectoryPoints;
    }

    public void setTrajectoryPoints(List<TrajectoryPoint> trajectoryPoints) {
        this.trajectoryPoints = trajectoryPoints;
    }

    public Integer length() {
        return this.trajectoryPoints.size();
    }

    public TrajectoryPoint get(int index) {
        return this.trajectoryPoints.get(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trajectory that = (Trajectory) o;
        if (trajectoryPoints.size() != that.trajectoryPoints.size()) return false;
        return trajectoryPoints.stream()
                .allMatch(trajectoryPoint ->
                        that.trajectoryPoints.stream()
                                .anyMatch(t -> t.equals(trajectoryPoint)));
    }

    @Override
    public String toString() {
        return "Trajectory{" +
                "id=" + id + ", " +
                "length=" + trajectoryPoints.size() + ", " +
                "(" +
                trajectoryPoints.stream()
                        .map(tp -> tp.getX() + ", " + tp.getY())
                        .collect(joining("), (")) +
                ")" +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(trajectoryPoints);
    }
}
