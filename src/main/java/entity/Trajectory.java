package entity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Trajectory {

    private List<TrajectoryPoint> trajectoryPoints;

    public Trajectory() {
    }

    public Trajectory(List<TrajectoryPoint> trajectoryPoints) {
        this.trajectoryPoints = trajectoryPoints;
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
                "(" +
                trajectoryPoints.stream().map(tp -> tp.getX() + ", " + tp.getY()).collect(Collectors.joining("), (")) +
                ")" +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(trajectoryPoints);
    }
}
