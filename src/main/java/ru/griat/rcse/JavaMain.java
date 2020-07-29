package ru.griat.rcse;

import org.apache.commons.math3.analysis.solvers.BaseAbstractUnivariateSolver;
import org.apache.commons.math3.analysis.solvers.BisectionSolver;
import org.apache.commons.math3.analysis.solvers.LaguerreSolver;
import org.apache.commons.math3.exception.NoBracketingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.griat.rcse.approximation.Polynomial;
import ru.griat.rcse.approximation.PolynomialRegression;
import ru.griat.rcse.clustering.Clustering;
import ru.griat.rcse.csv.CSVProcessing;
import ru.griat.rcse.entity.Cluster;
import ru.griat.rcse.entity.Trajectory;
import ru.griat.rcse.entity.TrajectoryPoint;
import ru.griat.rcse.exception.TrajectoriesParserException;
import ru.griat.rcse.parsing.TrajectoriesParser;
import ru.griat.rcse.visualisation.DisplayImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static ru.griat.rcse.misc.Utils.IMAGE_MAX_X;
import static ru.griat.rcse.misc.Utils.IMAGE_MAX_Y;
import static ru.griat.rcse.misc.Utils.IMAGE_MIN_X;
import static ru.griat.rcse.misc.Utils.IMAGE_MIN_Y;
import static ru.griat.rcse.misc.Utils.INPUT_FILE_DIR;
import static ru.griat.rcse.misc.Utils.INPUT_FILE_NAMES_FIRST;
import static ru.griat.rcse.misc.Utils.getFileDir;
import static ru.griat.rcse.misc.Utils.getFileName;
import static ru.griat.rcse.misc.Utils.getImgFileName;

public class JavaMain {

    private final static Logger LOGGER = LoggerFactory.getLogger(JavaMain.class.getName());
    private static final String EXPERIMENT_ID = "exp2";

    private static Clustering clustering;

    public static void main(String[] args) throws IOException, TrajectoriesParserException {

        for (String input : INPUT_FILE_NAMES_FIRST) {
            List<Trajectory> trajectories = parseTrajectories(getFileName(input));
            List<Trajectory> initialTrajectories = trajectories;
            Double[][] trajLCSSDistances;
//            displayTrajectories(getImgFileName(input), trajectories);

            int minLength = 10;
            int minTotalDist = 80;
//            displayTrajectories(getImgFileName(input), trajectories.stream().filter(tr -> tr.length() <= minLength || tr.totalDist() < minTotalDist).collect(toList()));

            trajectories = trajectories.stream().filter(tr -> tr.length() > minLength && tr.totalDist() >= minTotalDist).collect(toList());

//            int st = 269;
//            trajectories = trajectories.subList(st, st + 1);
//            displayTrajectories(getImgFileName(input), trajectories);
            clustering = new Clustering(initialTrajectories);
            setInputBorders(initialTrajectories);
//            displayTrajectories(getImgFileName(input), trajectories);

//            displayTrajectories(getImgFileName(input), trajectories.subList(150, 152));
            performRegression(trajectories, input);
            displayTrajectories(getImgFileName(input), trajectories);

//            trajectories = initialTrajectories.stream()
//                    .filter(tr ->
//                            getIndexesOfTrajWithLengthLessThan(initialTrajectories, 15).contains(tr.getId()))
//                    .collect(toList());
//            displayTrajectories(getImgFileName(input), trajectories);

            calcDistances(trajectories, 0, 0, 0, 0);

//            trajLCSSDistances = clustering.getTrajLCSSDistances();
//            new CSVProcessing().writeCSV(trajLCSSDistances, 0, trajectories.size(), 0, trajectories.size(), EXPERIMENT_ID, input);

//            trajLCSSDistances = new Double[initialTrajectories.size()][initialTrajectories.size()];
//            new CSVProcessing().readCSV(trajLCSSDistances, EXPERIMENT_ID, input);
//            clustering.setTrajLCSSDistances(trajLCSSDistances);
//            displayTrajectories(getImgFileName(input), trajectories, filterTrajWithDistLessThan(trajectories, trajLCSSDistances, 1.0));

            List<Cluster> clusters = clustering.cluster(trajectories);
//            int clSt = 0;
//            displayClusters(getImgFileName(input), clusters.subList(clSt, clSt + 1), false);
            displayClusters(getImgFileName(input), clusters, false);
        }
    }

    private static void performRegression(List<Trajectory> trajectories, String input) throws IOException {
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
        printStatistics(trajectories, input, minDegree, minR2forX, minR2forY, minR2forXid, minR2forYid);

//        List<Trajectory> copies = new ArrayList<>();
//        for (Trajectory traj : trajectories) {
//            List<TrajectoryPoint> tpCopy = traj.getTrajectoryPoints().stream().map(tp ->
//                    new TrajectoryPoint(
//                            (int) Math.round(traj.getRegressionX().predict(tp.getTime())),
//                            (int) Math.round(traj.getRegressionY().predict(tp.getTime())),
//                            tp.getTime()
//                    )).collect(toList());
//            Trajectory trCopy = new Trajectory(traj.getId() * 100, tpCopy);
//            trCopy.setRegressionX(traj.getRegressionX());
//            trCopy.setRegressionY(traj.getRegressionY());
////            trCopy.setKeyPoints(traj.getKeyPoints());
//            copies.add(traj);
//            copies.add(trCopy);
//        }
//        displayRegressionTrajectories(input, null, trajectories);
//        displayRegressionTrajectories(input, null, trajectories.stream().filter(tr -> List.of(300, 575, 2).contains(tr.getId())).collect(toList()));
//        displayRegressionTrajectories(input, null, copies.stream().filter(tr -> tr.getRegressionX().degree() == 4 && tr.getRegressionY().degree() == 4).collect(toList()));
//        System.out.println(trajectories.stream().filter(tr -> tr.getKeyPoints().size() == 3).collect(toList()).size());
        System.out.println(trajectories.stream().mapToInt(tr -> tr.getKeyPoints().size()).min());
        System.out.println(trajectories.stream().mapToInt(tr -> tr.getKeyPoints().size()).max());
    }

    private static void calculateKeyPoints(Trajectory currentTr) {
        int maxKPCount = 11;
        if (currentTr.length() < maxKPCount) {
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
            Double res1 = null, res2 = null;
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
                                (int) Math.round(res)));
                        prevRes = res;
                    }
                } catch (NoBracketingException nbe) {
                }
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
                    tt));
        }

        int timeStep = 5;
        int stTime = currentTr.get(0).getTime();
        int endTime = currentTr.get(currentTr.length() - 1).getTime();
//            add first point
        if (currentTr.getKeyPoints().stream().noneMatch(kp -> kp.getTime() < currentTr.get(2).getTime()))
            currentTr.addKeyPoint(new TrajectoryPoint(
                    (int) Math.round(currentTr.getRegressionX().predict(stTime)),
                    (int) Math.round(currentTr.getRegressionY().predict(stTime)),
                    stTime));
//            add last point
        if (currentTr.getKeyPoints().stream().noneMatch(kp -> endTime - kp.getTime() < 2 * timeStep))
            currentTr.addKeyPoint(new TrajectoryPoint(
                    (int) Math.round(currentTr.getRegressionX().predict(endTime)),
                    (int) Math.round(currentTr.getRegressionY().predict(endTime)),
                    endTime));

//        if small amount of trajectory points
//        and trajectory length is more than 2 times bigger than amount of key points:
        if (currentTr.getKeyPoints().size() < maxKPCount && currentTr.length() >= currentTr.getKeyPoints().size()) {
            int diff = maxKPCount - currentTr.getKeyPoints().size();
            double interval = (currentTr.length() - 3) * 1.0 / diff;
            for (int i = 0; i < diff; i++) {
                int tt = currentTr.get((int) Math.round(1 + interval * i)).getTime();
                currentTr.addKeyPoint(new TrajectoryPoint(
                        (int) Math.round(currentTr.getRegressionX().predict(tt)),
                        (int) Math.round(currentTr.getRegressionY().predict(tt)),
                        tt));
            }
        }
    }

    private static List<Trajectory> parseTrajectories(String fileName) throws IOException, TrajectoriesParserException {
        LOGGER.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        LOGGER.info("Trajectories parsing from (" + fileName + ") started");
        List<Trajectory> trajectories = new TrajectoriesParser().parseTxt(getFileDir(INPUT_FILE_DIR, fileName));
        LOGGER.info("Total amount of trajectories: " + trajectories.size());
        return trajectories;
    }

    private static void calcDistances(List<Trajectory> trajectories, int start1, int end1, int start2, int end2) {
        for (Trajectory t1 : trajectories) {
            for (Trajectory t2 : trajectories) {
                if (t1.getId() != t2.getId() && t1.getId() < t2.getId()) {
                    logCalcDist(t1, t2);
                }

            }
        }
    }

    private static void displayClusters(String fileName, List<Cluster> clusters, boolean save) throws IOException {
        new DisplayImage().displayAndSaveClusters(fileName, "res" + fileName, "clustering-results/" + EXPERIMENT_ID, clusters, save);
    }

    private static void displayTrajectories(String fileName, List<Trajectory> trajectories) throws IOException {
        new DisplayImage().displayAndSave(fileName, "", "initial-data", trajectories, false);
    }

    private static void displayRegressionTrajectories(String fileName, String subName, List<Trajectory> trajectories) throws IOException {
        new DisplayImage().displayAndSave(getImgFileName(fileName),
                getImgFileName(fileName + "-" + subName), "regression-results", trajectories, false);
    }

    private static void displayTrajectories(String fileName, List<Trajectory> trajectories, List<Integer> indexes) throws IOException {
        new DisplayImage().displayAndSave(fileName, null, null, indexes.stream().map(trajectories::get).collect(toList()), false);
    }

    private static double logCalcDist(Trajectory t1, Trajectory t2) {
        LOGGER.info("-----");
        double dist = clustering.calcLCSSDist(t1, t2);

        if (dist != 1) {
            LOGGER.info("Calculating distance between trajectories: " +
                    "\n1) " + t1 + "; " +
                    "\n2) " + t2);
        }
        LOGGER.info("dist(" + t1.getId() + ", " + t2.getId() + ") = " + dist);

        return dist;
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

    private static void setInputBorders(List<Trajectory> trajectories) {
        int minX = trajectories.stream()
                .mapToInt(tr ->
                        tr.getTrajectoryPoints().stream()
                                .mapToInt(TrajectoryPoint::getX)
                                .min().orElseThrow(NoSuchElementException::new))
                .min().orElseThrow(NoSuchElementException::new);
        int minY = trajectories.stream()
                .mapToInt(tr ->
                        tr.getTrajectoryPoints().stream()
                                .mapToInt(TrajectoryPoint::getY)
                                .min().orElseThrow(NoSuchElementException::new))
                .min().orElseThrow(NoSuchElementException::new);
        int maxX = trajectories.stream()
                .mapToInt(tr ->
                        tr.getTrajectoryPoints().stream()
                                .mapToInt(TrajectoryPoint::getX)
                                .max().orElseThrow(NoSuchElementException::new))
                .max().orElseThrow(NoSuchElementException::new);
        int maxY = trajectories.stream()
                .mapToInt(tr ->
                        tr.getTrajectoryPoints().stream()
                                .mapToInt(TrajectoryPoint::getY)
                                .max().orElseThrow(NoSuchElementException::new))
                .max().orElseThrow(NoSuchElementException::new);
        clustering.setBorders(minX, maxX, minY, maxY);

//        LOGGER.info("borders for X: (" + minX + ", " + maxX + ")");
//        LOGGER.info("borders for Y: (" + minY + ", " + maxY + ")");
    }

    private static List<Integer> filterTrajWithDistLessThan(List<Trajectory> trajectories, Double[][] trajLCSSDistances,
                                                            Double max) {
        return IntStream.range(1, trajectories.size())
                .filter(ind ->
                        trajLCSSDistances[0][ind] < max)
                .boxed().collect(toList());
    }

    private static List<Integer> getIndexesOfTrajWithLengthLessThan(List<Trajectory> trajectories, Integer maxLength) {
        return IntStream.range(0, trajectories.size()).boxed()
                .filter(ind ->
                        trajectories.get(ind).length() < maxLength)
                .collect(toList());
    }

}
