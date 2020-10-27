package ru.griat.rcse.entity;

import ru.griat.rcse.approximation.polynomial_regression.PolynomialRegression;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static ru.griat.rcse.misc.Utils.*;

public class Trajectory implements Cloneable {

    private int id;

    private List<TrajectoryPoint> trajectoryPoints;

    private PolynomialRegression regressionX;
    private PolynomialRegression regressionY;

    private List<TrajectoryPoint> keyPoints;
    private List<TrajectoryPoint> rdpPoints;

    private double avgSpeed;

    private double avgAcceleration;

    public Trajectory(int id, List<TrajectoryPoint> trajectoryPoints) {
        this.id = id;
        this.trajectoryPoints = trajectoryPoints;
        calcSpeed();
        calcAcceleration();
        this.keyPoints = new ArrayList<>();
        this.rdpPoints = new ArrayList<>();
    }

    public Trajectory(int id, List<TrajectoryPoint> trajectoryPoints, double avgSpeed, double avgAcceleration) {
        this.id = id;
        this.trajectoryPoints = trajectoryPoints;
        this.avgSpeed = avgSpeed;
        this.avgAcceleration = avgAcceleration;
        this.keyPoints = new ArrayList<>();
        this.rdpPoints = new ArrayList<>();
    }

    @Override
    public Trajectory clone() {
        List<TrajectoryPoint> tpClone = this.getTrajectoryPoints().stream()
                .map(TrajectoryPoint::clone).collect(toList());
        List<TrajectoryPoint> kpClone = this.getKeyPoints().stream()
                .map(TrajectoryPoint::clone).collect(toList());
        List<TrajectoryPoint> rdpClone = this.getRdpPoints().stream()
                .map(TrajectoryPoint::clone).collect(toList());
        Trajectory tClone = new Trajectory(this.getId(), tpClone, this.getAvgSpeed(), this.getAvgAcceleration());
        tClone.setKeyPoints(kpClone);
        tClone.setRdpPoints(rdpClone);
        return tClone;
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

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public double getAvgAcceleration() {
        return avgAcceleration;
    }

    public PolynomialRegression getRegressionX() {
        return regressionX;
    }

    public void setRegressionX(PolynomialRegression regressionX) {
        this.regressionX = regressionX;
    }

    public PolynomialRegression getRegressionY() {
        return regressionY;
    }

    public void setRegressionY(PolynomialRegression regressionY) {
        this.regressionY = regressionY;
    }

    public List<TrajectoryPoint> getKeyPoints() {
        return keyPoints;
    }

    public void setKeyPoints(List<TrajectoryPoint> keyPoints) {
        this.keyPoints = keyPoints;
    }

    public void addKeyPoint(TrajectoryPoint keyPoint, Integer bonusTT) {
        if (keyPoint.getTime() > this.get(length() - 1).getTime())
            return;
        if (this.length() > MAX_KP_COUNT && this.keyPoints.stream().anyMatch(thisKP -> Math.abs(thisKP.getTime() - keyPoint.getTime()) < 1 * TIME_STEP)) {
            if (bonusTT == null)
                return;
//            int newTime = keyPoint.getTime() + 2 * TIME_STEP;
            this.addKeyPoint(new TrajectoryPoint(
                    (int) Math.round(this.regressionX.predict(bonusTT)),
                    (int) Math.round(this.regressionY.predict(bonusTT)),
                    bonusTT
            ), null);
            return;
        }
        if (!keyPoints.contains(keyPoint) && checkTPValidity(keyPoint))
            this.keyPoints.add(keyPoint);
    }

    public List<TrajectoryPoint> getRdpPoints() {
        return rdpPoints;
    }

    public void setRdpPoints(List<TrajectoryPoint> rdpPoints) {
        this.rdpPoints = rdpPoints;
    }

    public void addRDPPoint(TrajectoryPoint rdpPoint, Integer bonusTT) {
        if (rdpPoint.getTime() > this.get(length() - 1).getTime())
            return;
        if (this.length() > MAX_KP_COUNT && this.keyPoints.stream().anyMatch(thisKP -> Math.abs(thisKP.getTime() - rdpPoint.getTime()) < 1 * TIME_STEP)) {
            if (bonusTT == null)
                return;
            this.addRDPPoint(this.trajectoryPoints.stream().filter(tp -> tp.getTime() == bonusTT).findFirst().get(), null);
            return;
        }
        if (!rdpPoints.contains(rdpPoint) && checkTPValidity(rdpPoint))
            this.rdpPoints.add(rdpPoint.clone());
    }

    public Integer length() {
        return this.trajectoryPoints.size();
    }

    public TrajectoryPoint get(int index) {
        return this.trajectoryPoints.get(index);
    }

    public double totalDist() {
        return this.get(0).distanceTo(this.get(length() - 1));
    }
    /**
     * Calculates average speed in 'pixels per sec'
     * pixels / sec
     */
    public void calcSpeed() {
        double dist = 0;
        double time = 0;
        for (int i = 0; i < this.length() - 1; i++) {
            dist += get(i).distanceTo(get(i + 1));
        }
//        since it is known that 1ms between frames - calc number of frames * inter_frame_time
        time = (get(length() - 1).getTime() - get(0).getTime()) * INTER_FRAME_TIME;

        avgSpeed = dist / time;
    }

    /**
     * Calculates constant average acceleration in 'pixels^2 per sec' based on first and last speeds
     * pixels / sec^2
     */
    public void calcAcceleration() {
        double firstSpeed = get(0).distanceTo(get(1)) / ((get(1).getTime() - get(0).getTime()) * INTER_FRAME_TIME);
        double lastSpeed = get(length() - 2).distanceTo(get(length() - 1))
                / ((get(length() - 1).getTime() - get(length() - 2).getTime()) * INTER_FRAME_TIME);
        avgAcceleration = (firstSpeed + lastSpeed) / ((get(length() - 1).getTime() - get(0).getTime()) * INTER_FRAME_TIME);
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
                "length=" + length() + ", " +
                "speed=" + avgSpeed + " (pix per sec), " +
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
