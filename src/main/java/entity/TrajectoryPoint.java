package entity;

import java.util.Objects;

public class TrajectoryPoint implements Cloneable {

    private int x;
    private int y;
    private long time;

    public TrajectoryPoint(int x, int y) {
        this.x = x;
        this.y = y;
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

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double distanceTo(TrajectoryPoint other) {
        if (this == other) {
            return 0;
        }
        double d = Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2);
        return Math.sqrt(d);
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

    @Override
    public int hashCode() {
        return Objects.hash(x, y, time);
    }
}
