package ru.griat.rcse.approximation.polynomial_regression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.griat.rcse.entity.Trajectory;
import ru.griat.rcse.entity.TrajectoryPoint;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static ru.griat.rcse.approximation.ApproximationUtils.addFirstAndLastPoints;
import static ru.griat.rcse.approximation.ApproximationUtils.calcAdditionalKeyPoints;
import static ru.griat.rcse.approximation.ApproximationUtils.calculateDiffEquationSolutions;
import static ru.griat.rcse.approximation.ApproximationUtils.positionalErrors;
import static ru.griat.rcse.approximation.ApproximationUtils.printTrajectoriesLengthsStatistics;
import static ru.griat.rcse.misc.Utils.IMAGE_MAX_X;
import static ru.griat.rcse.misc.Utils.IMAGE_MAX_Y;
import static ru.griat.rcse.misc.Utils.IMAGE_MIN_X;
import static ru.griat.rcse.misc.Utils.IMAGE_MIN_Y;
import static ru.griat.rcse.misc.Utils.MAX_KP_COUNT;
import static ru.griat.rcse.misc.Utils.copyTrajectories;
import static ru.griat.rcse.misc.Utils.displayRegressionTrajectories;

public class RegressionPerformer {

    private final static Logger LOGGER = LoggerFactory.getLogger(RegressionPerformer.class.getName());

    public void performRegression(List<Trajectory> trajectories, String input) throws IOException {
        double[] t, x, y;
        int minDegree = 3;
        int degreeMargin = 2;
        double thresholdR2 = 0.98;
        double minR2forX = 1.0, minR2forY = 1.0;

        for (Trajectory trajectory : trajectories) {
            PolynomialRegression regressionX = null;
            PolynomialRegression regressionY = null;

            t = trajectory.getTrajectoryPoints().stream()
                    .mapToDouble(TrajectoryPoint::getTime).toArray();
            x = trajectory.getTrajectoryPoints().stream()
                    .mapToDouble(TrajectoryPoint::getX).toArray();
            y = trajectory.getTrajectoryPoints().stream()
                    .mapToDouble(TrajectoryPoint::getY).toArray();

            for (int degree = minDegree; degree < minDegree + degreeMargin; degree++) {
                if (regressionX == null || regressionX.R2() < thresholdR2)
                    regressionX = new PolynomialRegression(t, x, degree);
                if (regressionY == null || regressionY.R2() < thresholdR2)
                    regressionY = new PolynomialRegression(t, y, degree);
            }

            trajectory.setRegressionX(regressionX);
            trajectory.setRegressionY(regressionY);

            if (regressionX.R2() < minR2forX) {
                minR2forX = regressionX.R2();
            }
            if (regressionY.R2() < minR2forY) {
                minR2forY = regressionY.R2();
            }
            calculateKeyPoints(trajectory);
//            printRegressionResults(currentTr);
        }
        positionalErrors(trajectories);
//        printStatistics(trajectories, input, minDegree, minR2forX, minR2forY);
        printTrajectoriesLengthsStatistics(trajectories);

//        List<Trajectory> copies = copyTrajectories(trajectories);
//        displayRegressionTrajectories(input, null, copies);
    }

    private void calculateKeyPoints(Trajectory currentTr) {
        if (currentTr.length() < MAX_KP_COUNT) {
            currentTr.setKeyPoints(currentTr.getTrajectoryPoints().stream().map(TrajectoryPoint::clone).collect(toList()));
            return;
        }

        calculateDiffEquationSolutions(currentTr);
        printKeyPointsStatisticsAndAddBorderPoints(currentTr);
        addFirstAndLastPoints(currentTr);
        calcAdditionalKeyPoints(currentTr);
    }

    private static void printRegressionResults(Trajectory currentTr) throws IOException {
        System.out.println("---Model for X---");
        System.out.println(currentTr.getRegressionX());
        System.out.println("---Model for Y---");
        System.out.println(currentTr.getRegressionY());
    }

    private static void printStatistics(List<Trajectory> trajectories, String input, int minDegree,
                                        double minR2forX, double minR2forY) throws IOException {
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

    private static void printKeyPointsStatisticsAndAddBorderPoints(Trajectory currentTr) {
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
    }

}
