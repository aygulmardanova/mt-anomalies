package entity;

import dbis.stark.STObject;
import org.locationtech.jts.geom.Geometry;
import utils.STUtils;

import java.util.Objects;

public class TrajectoryPoint1 extends STObject {

    private long time;

    @Override
    public String toString() {
        return "TrajectoryPoint1{" +
                "x=" + getX() +
                ", y=" + getY() +
                ", timestamp=" + getTime() +
                '}';
    }

    public TrajectoryPoint1(Geometry g, long x, long y) {
        super(g, null);
        STUtils.createSTObject(x, y);
    }

    public long getX() {
        return STUtils.getX(this);
//        return Long.valueOf(String.valueOf(super.getGeo().getCoordinate().getX()));
    }

    public long getY() {
        return STUtils.getY(this);
//        return Long.valueOf(String.valueOf(super.getGeo().getCoordinate().getY()));
    }

    public long getTime() {
        return super.getStart().get().value();
    }

    public double distanceTo(TrajectoryPoint1 other) {
        if (this == other) {
            return 0;
        }
        double d = Math.pow(this.getX() - other.getX(), 2) + Math.pow(this.getY() - other.getY(), 2);
        return Math.sqrt(d);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrajectoryPoint1 that = (TrajectoryPoint1) o;
        return Objects.equals(getX(), that.getX()) &&
                Objects.equals(getY(), that.getY()) &&
                Objects.equals(getTime(), that.getTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY(), getTime());
    }
}
