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
        double[] t;
        double[] x;
        double[] y;
        int polynomialDegree = 3;
        PolynomialRegression regressionX;
        PolynomialRegression regressionY;

        int tId = 101;
        Trajectory currentTr = trajectories.get(tId);
        int firstTime = currentTr.getTrajectoryPoints().get(0).getTime();
        for (TrajectoryPoint tp: currentTr.getTrajectoryPoints()) {
            tp.setTime(tp.getTime() + 1 - firstTime);
        }
        t = currentTr.getTrajectoryPoints().stream().mapToDouble(TrajectoryPoint::getTime).toArray();
        x = currentTr.getTrajectoryPoints().stream().mapToDouble(TrajectoryPoint::getX).toArray();
        y = currentTr.getTrajectoryPoints().stream().mapToDouble(TrajectoryPoint::getY).toArray();
        regressionX = new PolynomialRegression(t, x, polynomialDegree);
        regressionY = new PolynomialRegression(t, y, polynomialDegree);

        System.out.println("---Model for X---");
        System.out.println(regressionX);
        regressionX.printPredictedResults(t, x);
        System.out.println("---Model for Y---");
        System.out.println(regressionY);
        regressionY.printPredictedResults(t, y);

        List<TrajectoryPoint> tpCopy = trajectories.get(tId).getTrajectoryPoints().stream().map(tp ->
                new TrajectoryPoint((int) Math.round(regressionX.predict(tp.getTime())), (int) Math.round(regressionY.predict(tp.getTime())), tp.getTime())).collect(toList());
        Trajectory trCopy = new Trajectory(1200, tpCopy);
        displayRegressionTrajectories(input, List.of(currentTr, trCopy));
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

    private static void displayRegressionTrajectories(String fileName, List<Trajectory> trajectories) throws IOException {
        new DisplayImage().displayAndSave(Utils.getImgFileName(fileName),
                Utils.getImgFileName(fileName + "-" + trajectories.get(0).getId()), "regression-results", trajectories, false);
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
