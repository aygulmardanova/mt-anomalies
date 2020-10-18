package ru.griat.rcse.approximation.rdp;

import ru.griat.rcse.entity.TrajectoryPoint;

import java.util.Arrays;
import java.util.List;

public class Line {

    private final TrajectoryPoint start;
    private final TrajectoryPoint end;

    private final double dx;
    private final double dy;
    private final double sxey;
    private final double exsy;
    private final double length;

    public Line(TrajectoryPoint start, TrajectoryPoint end) {
        this.start = start;
        this.end = end;
        dx = start.getX() - end.getX();
        dy = start.getY() - end.getY();
        sxey = start.getX() * end.getY();
        exsy = end.getX() * start.getY();
        length = Math.sqrt(dx * dx + dy * dy);
    }

    public List<TrajectoryPoint> asList() {
        return Arrays.asList(start, end);
    }

    public double distance(TrajectoryPoint tp) {
        if (length == 0.0) {
            double curdx = start.getX() - tp.getX();
            double curdy = start.getY() - tp.getY();
            return Math.sqrt(curdx * curdx + curdy * curdy);
        }
        return Math.abs(dy * tp.getX() - dx * tp.getY() + sxey - exsy) / length;
    }

}
