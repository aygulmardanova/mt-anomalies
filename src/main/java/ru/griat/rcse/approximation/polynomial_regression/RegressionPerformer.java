package ru.griat.rcse.approximation.polynomial_regression;

import org.apache.commons.math3.analysis.solvers.BaseAbstractUnivariateSolver;
import org.apache.commons.math3.analysis.solvers.BisectionSolver;
import org.apache.commons.math3.analysis.solvers.LaguerreSolver;
import org.apache.commons.math3.exception.NoBracketingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.griat.rcse.entity.Trajectory;
import ru.griat.rcse.entity.TrajectoryPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static ru.griat.rcse.misc.Utils.IMAGE_MAX_X;
import static ru.griat.rcse.misc.Utils.IMAGE_MAX_Y;
import static ru.griat.rcse.misc.Utils.IMAGE_MIN_X;
import static ru.griat.rcse.misc.Utils.IMAGE_MIN_Y;
import static ru.griat.rcse.misc.Utils.MAX_KP_COUNT;
import static ru.griat.rcse.misc.Utils.TIME_STEP;
import static ru.griat.rcse.misc.Utils.displayRegressionTrajectories;

public class RegressionPerformer {

    private final static Logger LOGGER = LoggerFactory.getLogger(RegressionPerformer.class.getName());

    public void performRegression(List<Trajectory> trajectories, String input) throws IOException {
        double[] t, x, y;
        int minDegree = 3;
        int degreeMargin = 2;
        double thresholdR2 = 0.98;
        double minR2forX = 1.0, minR2forY = 1.0;
        int minR2forXid = -1, minR2forYid = -1;

        for (int tId = 0; tId < trajectories.size(); tId++) {
            PolynomialRegression regressionX = null;
            PolynomialRegression regressionY = null;

            Trajectory currentTr = trajectories.get(tId);
            t = currentTr.getTrajectoryPoints().stream()
                    .mapToDouble(TrajectoryPoint::getTime).toArray();
            x = currentTr.getTrajectoryPoints().stream()
                    .mapToDouble(TrajectoryPoint::getX).toArray();
            y = currentTr.getTrajectoryPoints().stream()
                    .mapToDouble(TrajectoryPoint::getY).toArray();

            for (int degree = minDegree; degree < minDegree + degreeMargin; degree++) {
                if (regressionX == null || regressionX.R2() < thresholdR2)
                    regressionX = new PolynomialRegression(t, x, degree);
                if (regressionY == null || regressionY.R2() < thresholdR2)
                    regressionY = new PolynomialRegression(t, y, degree);
            }

            currentTr.setRegressionX(regressionX);
            currentTr.setRegressionY(regressionY);

            if (regressionX.R2() < minR2forX) {
                minR2forX = regressionX.R2();
                minR2forXid = tId;
            }
            if (regressionY.R2() < minR2forY) {
                minR2forY = regressionY.R2();
                minR2forYid = tId;
            }
            calculateKeyPoints(currentTr);
//            printRegressionResults(currentTr, t, x, y, input);
        }
//        printStatistics(trajectories, input, minDegree, minR2forX, minR2forY, minR2forXid, minR2forYid);

        List<Trajectory> copies = new ArrayList<>();
        for (Trajectory traj : trajectories) {
            List<TrajectoryPoint> tpCopy = traj.getTrajectoryPoints().stream().map(tp ->
                    new TrajectoryPoint(
                            (int) Math.round(traj.getRegressionX().predict(tp.getTime())),
                            (int) Math.round(traj.getRegressionY().predict(tp.getTime())),
                            tp.getTime()
                    )).collect(toList());
            Trajectory trCopy = new Trajectory(traj.getId() * 100, tpCopy);
            trCopy.setRegressionX(traj.getRegressionX());
            trCopy.setRegressionY(traj.getRegressionY());
            trCopy.setKeyPoints(traj.getKeyPoints());
            copies.add(traj);
//            copies.add(trCopy);
        }
        displayRegressionTrajectories(input, null, copies);
//        displayRegressionTrajectories(input, null, trajectories.stream().filter(tr -> List.of(300, 575, 2).contains(tr.getId())).collect(toList()));
//        displayRegressionTrajectories(input, null, copies.stream().filter(tr -> tr.getRegressionX().degree() == 4 && tr.getRegressionY().degree() == 4).collect(toList()));
//        System.out.println(trajectories.stream().filter(tr -> tr.getKeyPoints().size() == 3).collect(toList()).size());
        System.out.println("min: " + trajectories.stream().mapToInt(tr -> tr.getKeyPoints().size()).min());
        System.out.println("max: " + trajectories.stream().mapToInt(tr -> tr.getKeyPoints().size()).max());
        System.out.println("avg: " + trajectories.stream().mapToInt(tr -> tr.getKeyPoints().size()).average());
        displayRegressionTrajectories(input, null, copies.stream().filter(c -> c.getKeyPoints().size() == 3).collect(toList()));
    }

    private void calculateKeyPoints(Trajectory currentTr) {
        if (currentTr.length() < MAX_KP_COUNT) {
            currentTr.setKeyPoints(currentTr.getTrajectoryPoints().stream().map(TrajectoryPoint::clone).collect(toList()));
            return;
        }

        Polynomial polynomialX = currentTr.getRegressionX().toPolynomial();
        Polynomial diffX1 = polynomialX.derivative();
        Polynomial diffX2 = diffX1.derivative();

        Polynomial polynomialY = currentTr.getRegressionY().toPolynomial();
        Polynomial diffY1 = polynomialY.derivative();
        Polynomial diffY2 = diffY1.derivative();

//        LaguerreSolver, BisectionSolver,
        BaseAbstractUnivariateSolver bisectionSolver = new BisectionSolver();
        BaseAbstractUnivariateSolver laguerreSolver = new LaguerreSolver();
        for (Polynomial diff : List.of(diffX1, diffX2, diffY1, diffY2)) {
            Double prevRes = null;
            Double res;
            for (BaseAbstractUnivariateSolver solver : List.of(bisectionSolver, laguerreSolver)) {
                try {
                    res = solver.solve(30000, diff,
                            currentTr.getTrajectoryPoints().stream().mapToInt(TrajectoryPoint::getTime).min().getAsInt(),
                            currentTr.getTrajectoryPoints().stream().mapToInt(TrajectoryPoint::getTime).max().getAsInt(),
                            currentTr.getTrajectoryPoints().stream().mapToInt(TrajectoryPoint::getTime).min().getAsInt() + 1
                    );
//                    add key points, which are solutions of derivative equations
                    if (prevRes == null || Math.round(prevRes) != Math.round(res)) {
                        currentTr.addKeyPoint(new TrajectoryPoint(
                                (int) Math.round(currentTr.getRegressionX().predict(res)),
                                (int) Math.round(currentTr.getRegressionY().predict(res)),
                                (int) Math.round(res)), null);
                        prevRes = res;
                    }
                } catch (NoBracketingException ignored) {}
            }
        }

        double minX = IMAGE_MAX_X, maxX = IMAGE_MIN_X, minY = IMAGE_MAX_Y, maxY = IMAGE_MIN_Y;
        int tForMinX = 0, tForMaxX = 0, tForMinY = 0, tForMaxY = 0;
        for (int time : currentTr.getTrajectoryPoints().stream().mapToInt(TrajectoryPoint::getTime).boxed().collect(toList())) {
            double predictedX = currentTr.getRegressionX().predict(time);
            double predictedY = currentTr.getRegressionY().predict(time);
            if (predictedX < minX) {
                minX = predictedX;
                tForMinX = time;
            }
            if (predictedX > maxX) {
                maxX = predictedX;
                tForMaxX = time;
            }
            if (predictedY < minY) {
                minY = predictedY;
                tForMinY = time;
            }
            if (predictedY > maxY) {
                maxY = predictedY;
                tForMaxY = time;
            }
        }

//        add border key points
        for (int tt : List.of(tForMinX, tForMaxX, tForMinY, tForMaxY)) {
            currentTr.addKeyPoint(new TrajectoryPoint(
                    (int) Math.round(currentTr.getRegressionX().predict(tt)),
                    (int) Math.round(currentTr.getRegressionY().predict(tt)),
                    tt), null);
        }

        int stTime = currentTr.get(0).getTime();
        int endTime = currentTr.get(currentTr.length() - 1).getTime();
//            add first point
        if (currentTr.getKeyPoints().stream().noneMatch(kp -> kp.getTime() < currentTr.get(2).getTime()))
            currentTr.addKeyPoint(new TrajectoryPoint(
                    (int) Math.round(currentTr.getRegressionX().predict(stTime)),
                    (int) Math.round(currentTr.getRegressionY().predict(stTime)),
                    stTime), null);
//            add last point
        if (currentTr.getKeyPoints().stream().noneMatch(kp -> endTime - kp.getTime() < 2 * TIME_STEP))
            currentTr.addKeyPoint(new TrajectoryPoint(
                    (int) Math.round(currentTr.getRegressionX().predict(endTime)),
                    (int) Math.round(currentTr.getRegressionY().predict(endTime)),
                    endTime), null);

/*
//        if small amount of trajectory points
//        and trajectory length is more than 2 times bigger than amount of key points:
        if (currentTr.getKeyPoints().size() < MAX_KP_COUNT && currentTr.length() >= currentTr.getKeyPoints().size()) {
            int diff = MAX_KP_COUNT - currentTr.getKeyPoints().size();
            double interval = (currentTr.length() - 3) * 1.0 / diff;
            for (int i = 0; i < diff; i++) {
                int tt = currentTr.get((int) Math.round(1 + interval * i)).getTime();
                Integer bonusTT = (i < diff - 1) ? currentTr.get((int) Math.round(1 + interval * (i + 1))).getTime() : null;
                currentTr.addKeyPoint(new TrajectoryPoint(
                        (int) Math.round(currentTr.getRegressionX().predict(tt)),
                        (int) Math.round(currentTr.getRegressionY().predict(tt)),
                        tt), bonusTT);
            }
        }
*/
    }

    private static void printRegressionResults(Trajectory currentTr,
                                               double[] t, double[] x, double[] y,
                                               String input) throws IOException {
        System.out.println("---Model for X---");
        System.out.println(currentTr.getRegressionX());
        System.out.println("---Model for Y---");
        System.out.println(currentTr.getRegressionY());
    }

    private static void printStatistics(List<Trajectory> trajectories, String input, int minDegree,
                                        double minR2forX, double minR2forY,
                                        int minR2forXid, int minR2forYid) throws IOException {
        double avgR2forX = trajectories.stream().mapToDouble(tr -> tr.getRegressionX().R2()).average().getAsDouble();
        double avgR2forY = trajectories.stream().mapToDouble(tr -> tr.getRegressionY().R2()).average().getAsDouble();

        LOGGER.info("min R2 for X is: {}", minR2forX);
        LOGGER.info("avg R2 for X is: {}", avgR2forX);
        LOGGER.info("min R2 for Y is: {}", minR2forY);
        LOGGER.info("avg R2 for Y is: {}", avgR2forY);

        List<Trajectory> trajectoriesPol3 = trajectories.stream()
                .filter(tr ->
                        tr.getRegressionX().degree() == minDegree && tr.getRegressionY().degree() == minDegree)
                .collect(toList());
        List<Trajectory> trajectoriesPol4 = trajectories.stream()
                .filter(tr ->
                        tr.getRegressionX().degree() == minDegree + 1 || tr.getRegressionY().degree() == minDegree + 1)
                .collect(toList());
//        displayRegressionTrajectories(input, "pol-3", trajectoriesPol3);
//        displayRegressionTrajectories(input, "pol-4", trajectoriesPol4);

//        System.out.println("min speed for pol3: " + trajectoriesPol3.stream()
//                .mapToDouble(Trajectory::getAvgSpeed)
//                .min().getAsDouble());
//        System.out.println("avg speed for pol3: " + trajectoriesPol3.stream()
//                .mapToDouble(Trajectory::getAvgSpeed)
//                .average().getAsDouble());
//        System.out.println("max speed for pol3: " + trajectoriesPol3.stream()
//                .mapToDouble(Trajectory::getAvgSpeed)
//                .max().getAsDouble());
//
//        System.out.println("min speed for pol4: " + trajectoriesPol4.stream()
//                .mapToDouble(Trajectory::getAvgSpeed)
//                .min().getAsDouble());
//        System.out.println("avg speed for pol4: " + trajectoriesPol4.stream()
//                .mapToDouble(Trajectory::getAvgSpeed)
//                .average().getAsDouble());
//        System.out.println("max speed for pol4: " + trajectoriesPol4.stream()
//                .mapToDouble(Trajectory::getAvgSpeed)
//                .max().getAsDouble());
//
//        System.out.println("min accel for pol3: " + trajectoriesPol3.stream()
//                .mapToDouble(Trajectory::getAvgAcceleration)
//                .min().getAsDouble());
//        System.out.println("avg accel for pol3: " + trajectoriesPol3.stream()
//                .mapToDouble(Trajectory::getAvgAcceleration)
//                .average().getAsDouble());
//        System.out.println("max accel for pol3: " + trajectoriesPol3.stream()
//                .mapToDouble(Trajectory::getAvgAcceleration)
//                .max().getAsDouble());
//
//        System.out.println("min accel for pol4: " + trajectoriesPol4.stream()
//                .mapToDouble(Trajectory::getAvgAcceleration)
//                .min().getAsDouble());
//        System.out.println("avg accel for pol4: " + trajectoriesPol4.stream()
//                .mapToDouble(Trajectory::getAvgAcceleration)
//                .average().getAsDouble());
//        System.out.println("max accel for pol4: " + trajectoriesPol4.stream()
//                .mapToDouble(Trajectory::getAvgAcceleration)
//                .max().getAsDouble());
    }

}
