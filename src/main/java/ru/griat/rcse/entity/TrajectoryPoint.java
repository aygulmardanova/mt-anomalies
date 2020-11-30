package ru.griat.rcse.entity;

import java.util.Objects;

import static ru.griat.rcse.misc.Utils.ADAPT_COEFF_X;
import static ru.griat.rcse.misc.Utils.ADAPT_COEFF_Y;

public class TrajectoryPoint implements Cloneable {

    private int x;
    private int y;
    private int time;

    private double cpDist;
    private double epsilonX;
    private double epsilonY;

    public TrajectoryPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public TrajectoryPoint(int x, int y, int time) {
        this.x = x;
        this.y = y;
        this.time = time;
    }

    @Override
    public String toString() {
        return "TrajectoryPoint{" +
                "x=" + this.x +
                ", y=" + this.y +
                ", timestamp=" + this.time +
                '}';
    }

    @Override
    public TrajectoryPoint clone() {
        try {
            return (TrajectoryPoint) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getTime() {
        return this.time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public double getCpDist() {
        return cpDist;
    }

    public void setCpDist(double cpDist) {
        this.cpDist = cpDist;
    }

    public double getEpsilonX() {
        return epsilonX;
    }

    public void setEpsilonX(double epsilonX) {
        this.epsilonX = epsilonX;
    }

    public double getEpsilonY() {
        return epsilonY;
    }

    public void setEpsilonY(double epsilonY) {
        this.epsilonY = epsilonY;
    }

    /**
     * Calculates the Euclidean distance between two trajectory points
     *
     * @param other     second trajectory point
     * @return          Euclidean distance between 'this' and 'other' trajectory points
     */
    public double distanceTo(TrajectoryPoint other) {
        if (this == other) {
            return 0;
        }
        double d = Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2);
        return Math.sqrt(d);
    }

    public void setEpsilons(double cpDist, int maxX, int minX, int maxY, int minY) {
        setCpDist(cpDist);
//        double epsilonX = ADAPT_COEFF_X * (maxX - minX) / cpDist;
        double epsilonX = (maxX - minX) / Math.sqrt(cpDist);
        setEpsilonX(cpDist < 250 ? 250.0 : epsilonX);
//        double epsilonY = ADAPT_COEFF_Y * (maxY - minY) / cpDist;
        double epsilonY = (maxY - minY) / Math.sqrt(cpDist);
        setEpsilonY(cpDist < 200 ? 150.0 : epsilonY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrajectoryPoint that = (TrajectoryPoint) o;
        return Objects.equals(x, that.x) &&
                Objects.equals(y, that.y) &&
                Objects.equals(time, that.time);
    }

    public boolean equalsSpatially(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrajectoryPoint that = (TrajectoryPoint) o;
        return Objects.equals(x, that.x) &&
                Objects.equals(y, that.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, time);
    }
}
