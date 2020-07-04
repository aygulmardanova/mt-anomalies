package entity;

import java.util.Objects;

public class TrajectoryPoint {

    private long x;
    private long y;
    private long time;

    @Override
    public String toString() {
        return "TrajectoryPoint{" +
                "x=" + this.x +
                ", y=" + this.y +
                ", timestamp=" + this.time +
                '}';
    }

    public TrajectoryPoint(long x, long y) {
        this.x = x;
        this.y = y;
    }

    public long getX() {
        return this.x;
    }

    public void setX(long x) {
        this.x = x;
    }

    public long getY() {
        return this.y;
    }

    public void setY(long y) {
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
