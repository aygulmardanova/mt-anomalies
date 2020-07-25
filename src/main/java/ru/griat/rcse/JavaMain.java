package ru.griat.rcse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.griat.rcse.approximation.PolynomialRegression;
import ru.griat.rcse.clustering.Clustering;
import ru.griat.rcse.csv.CSVProcessing;
import ru.griat.rcse.entity.Cluster;
import ru.griat.rcse.entity.Trajectory;
import ru.griat.rcse.entity.TrajectoryPoint;
import ru.griat.rcse.exception.TrajectoriesParserException;
import ru.griat.rcse.misc.Utils;
import ru.griat.rcse.parsing.TrajectoriesParser;
import ru.griat.rcse.visualisation.DisplayImage;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static ru.griat.rcse.misc.Utils.INPUT_FILE_NAMES_FIRST;

public class JavaMain {

    private final static Logger LOGGER = LoggerFactory.getLogger(JavaMain.class.getName());
    private static final String EXPERIMENT_ID = "exp1";

    private static Clustering clustering;

    public static void main(String[] args) throws IOException, TrajectoriesParserException {

        for (String input : INPUT_FILE_NAMES_FIRST) {
            List<Trajectory> trajectories = parseTrajectories(Utils.getFileName(input));
            List<Trajectory> initialTrajectories = trajectories;
            Double[][] trajLCSSDistances;

            clustering = new Clustering(initialTrajectories);
            setInputBorders(initialTrajectories);

            performRegression(trajectories, input);

//            trajectories = initialTrajectories.stream()
//                    .filter(tr ->
//                            getIndexesOfTrajWithLengthLessThan(initialTrajectories, 15).contains(tr.getId()))
//                    .collect(toList());
//            displayTrajectories(Utils.getImgFileName(input), trajectories);

//            calcDistances(trajectories, 11, 13, 0, 100);

//            trajLCSSDistances = clustering.getTrajLCSSDistances();
//            new CSVProcessing().writeCSV(trajLCSSDistances, 0, 624, 0, 624, EXPERIMENT_ID, input);

//            trajLCSSDistances = new Double[initialTrajectories.size()][initialTrajectories.size()];
//            new CSVProcessing().readCSV(trajLCSSDistances, EXPERIMENT_ID, input);
//            clustering.setTrajLCSSDistances(trajLCSSDistances);
//            displayTrajectories(Utils.getImgFileName(input), trajectories, filterTrajWithDistLessThan(trajectories, trajLCSSDistances, 1.0));

//            List<Cluster> clusters = clustering.cluster(trajectories);
//            displayClusters(Utils.getImgFileName(input), clusters);
        }
    }

    private static void performRegression(List<Trajectory> trajectories, String input) throws IOException {
        double[] t, x, y;
        int minDegree = 3;
        int degreeMargin = 2;
        double thresholdR2 = 0.97;
        double minR2forX = 1.0, minR2forY = 1.0;
        int minR2forXid = -1, minR2forYid = -1;

        for (int tId = 0; tId < trajectories.size(); tId++) {
//        for (int tId = 100; tId < 101; tId++) {
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
            printRegressionResults(currentTr, regressionX, regressionY, t, x, y, input);
        }
        double avgR2forX = trajectories.stream().mapToDouble(tr -> tr.getRegressionX().R2()).average().getAsDouble();
        double avgR2forY = trajectories.stream().mapToDouble(tr -> tr.getRegressionY().R2()).average().getAsDouble();

        LOGGER.info("min R2 for X is for trajectory {}: {}", minR2forXid, minR2forX);
        LOGGER.info("avg R2 for X is: {}", avgR2forX);
        LOGGER.info("min R2 for Y is for trajectory {}: {}", minR2forYid, minR2forY);
        LOGGER.info("avg R2 for Y is: {}", avgR2forY);

        List<Trajectory> trajectoriesPol3 = trajectories.stream()
                .filter(tr ->
                        tr.getRegressionX().degree() == minDegree && tr.getRegressionY().degree() == minDegree)
                .collect(toList());
        List<Trajectory> trajectoriesPol4 = trajectories.stream()
                .filter(tr ->
                        tr.getRegressionX().degree() == minDegree + 1 && tr.getRegressionY().degree() == minDegree + 1
                                && tr.getRegressionX().degree() < minDegree + 2 && tr.getRegressionY().degree() < minDegree + 2)
                .collect(toList());
        displayRegressionTrajectories(input, "pol-3", trajectoriesPol3);
        displayRegressionTrajectories(input, "pol-4", trajectoriesPol4);

        System.out.println("avg speed for pol3: " + trajectoriesPol3.stream()
                .mapToDouble(Trajectory::getAvgSpeed)
                .average().getAsDouble());
        System.out.println("avg speed for pol4: " + trajectoriesPol4.stream()
                .mapToDouble(Trajectory::getAvgSpeed)
                .average().getAsDouble());
    }

    private static List<Trajectory> parseTrajectories(String fileName) throws IOException, TrajectoriesParserException {
        LOGGER.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        LOGGER.info("Trajectories parsing from (" + fileName + ") started");
        List<Trajectory> trajectories = new TrajectoriesParser().parseTxt(Utils.getFileDir(Utils.INPUT_FILE_DIR, fileName));
        LOGGER.info("Total amount of trajectories: " + trajectories.size());
        return trajectories;
    }

    private static void calcDistances(List<Trajectory> trajectories, int start1, int end1, int start2, int end2) {
        for (Trajectory t1 : trajectories) {
            for (Trajectory t2 : trajectories) {
                if (t1.getId() != t2.getId() && t1.getId() < t2.getId()) {
                    logCalcDist(t1, t2);
                }

//                if (t1.getId() != t2.getId() && t1.getId() >= start1 && t1.getId() < end1
//                        && t2.getId() >= start2 && t2.getId() < end2) {
//                    calcDist(t1, t2);
//                }
            }
        }
    }

    private static void displayClusters(String fileName, List<Cluster> clusters) throws IOException {
        new DisplayImage().displayAndSaveClusters(fileName, "res" + fileName, "clustering-results/" + EXPERIMENT_ID, clusters, false);
    }

    private static void displayTrajectories(String fileName, List<Trajectory> trajectories) throws IOException {
        new DisplayImage().displayAndSave(fileName, "", "initial-data", trajectories, false);
    }

    private static void displayRegressionTrajectories(String fileName, String subName, List<Trajectory> trajectories) throws IOException {
        new DisplayImage().displayAndSave(Utils.getImgFileName(fileName),
                Utils.getImgFileName(fileName + "-" + subName), "regression-results", trajectories, false);
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
                                               PolynomialRegression regressionX, PolynomialRegression regressionY,
                                               double[] t, double[] x, double[] y,
                                               String input) throws IOException {
        System.out.println("---Model for X---");
        System.out.println(regressionX);
        regressionX.printPredictedResults(t, x);
        System.out.println("---Model for Y---");
        System.out.println(regressionY);
        regressionY.printPredictedResults(t, y);

        List<TrajectoryPoint> tpCopy = currentTr.getTrajectoryPoints().stream().map(tp ->
                new TrajectoryPoint(
                        (int) Math.round(regressionX.predict(tp.getTime())),
                        (int) Math.round(regressionY.predict(tp.getTime())),
                        tp.getTime()
                )).collect(toList());
//        List<TrajectoryPoint> tpCopy = IntStream
//                .range(currentTr.getTrajectoryPoints().get(0).getTime(), currentTr.getTrajectoryPoints().get(currentTr.length() - 1).getTime() + 1)
//                .boxed().mapToDouble(time -> Double.parseDouble(time + "")).boxed()
//                .map(time -> new TrajectoryPoint(
//                        (int) Math.round(regressionX.predict(time)),
//                        (int) Math.round(regressionY.predict(time)),
//                        (int) Math.round(time)))
//                .collect(toList());

        Trajectory trCopy = new Trajectory(currentTr.getId() * 100, tpCopy);
        displayRegressionTrajectories(input, currentTr.getId() + "", List.of(trCopy, currentTr));
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
