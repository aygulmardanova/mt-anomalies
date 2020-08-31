package ru.griat.rcse.approximation.rdp;

import ru.griat.rcse.entity.TrajectoryPoint;

import java.util.Arrays;
import java.util.List;

public class Line {

    private TrajectoryPoint start;
    private TrajectoryPoint end;

    private double dx;
    private double dy;
    private double sxey;
    private double exsy;
    private double length;

    public Line(TrajectoryPoint start, TrajectoryPoint end) {
        this.start = start;
        this.end = end;
        dx = start.getX() - end.getX();
        dy = start.getY() - end.getY();
        sxey = start.getX() * end.getY();
        exsy = end.getX() * start.getY();
        length = Math.sqrt(dx * dx + dy * dy);
    }

    @SuppressWarnings("unchecked")
    public List<TrajectoryPoint> asList() {
        return Arrays.asList(start, end);
    }

    double distance(TrajectoryPoint tp) {
        return Math.abs(dy * tp.getX() - dx * tp.getY() + sxey - exsy) / length;
    }

}
