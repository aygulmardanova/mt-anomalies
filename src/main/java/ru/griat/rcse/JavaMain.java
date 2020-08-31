package ru.griat.rcse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.griat.rcse.approximation.polynomial_regression.RegressionPerformer;
import ru.griat.rcse.clustering.Clustering;
import ru.griat.rcse.csv.CSVProcessing;
import ru.griat.rcse.entity.Cluster;
import ru.griat.rcse.entity.Trajectory;
import ru.griat.rcse.entity.TrajectoryPoint;
import ru.griat.rcse.exception.TrajectoriesParserException;
import ru.griat.rcse.parsing.TrajectoriesParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static ru.griat.rcse.misc.Utils.*;

public class JavaMain {

    private final static Logger LOGGER = LoggerFactory.getLogger(JavaMain.class.getName());

    private static Clustering clustering;
    private static RegressionPerformer regression;

    public static void main(String[] args) throws IOException, TrajectoriesParserException {
        regression = new RegressionPerformer();

        for (String input : INPUT_FILE_NAMES_FIRST) {
            List<Trajectory> trajectories = parseTrajectories(getFileName(input));
//            List<Trajectory> trajectories2 = parseTrajectories(getFileName("2"));
            List<Trajectory> initialTrajectories = trajectories;
            Double[][] trajLCSSDistances;
//            displayTrajectories(getImgFileName(input), trajectories);
//            displayTrajectories(getImgFileName(input), trajectories.stream().filter(tr -> tr.length() <= MIN_LENGTH || tr.totalDist() < MIN_TOTAL_DIST).collect(toList()));

            trajectories = trajectories.stream().filter(tr -> tr.length() > MIN_LENGTH && tr.totalDist() >= MIN_TOTAL_DIST).collect(toList());
//            displayTrajectories(getImgFileName(input), trajectories);

            regression.performRegression(trajectories, input);
//            regression.performRegression(trajectories2, input);
//            displayTrajectories(getImgFileName(input), trajectories, List.of(0, 2, 6, 8, 13, 70, 176, 180));

            clustering = new Clustering(initialTrajectories);
            setInputBorders(initialTrajectories);

//            calcDistances(trajectories, 0, 0, 0, 0);
//            trajLCSSDistances = clustering.getTrajLCSSDistances();
//            new CSVProcessing().writeCSV(trajLCSSDistances, 0, initialTrajectories.size(), 0, initialTrajectories.size(), EXPERIMENT_ID, input);

            trajLCSSDistances = new Double[initialTrajectories.size()][initialTrajectories.size()];
            new CSVProcessing().readCSV(trajLCSSDistances, EXPERIMENT_ID, input);
            clustering.setTrajLCSSDistances(trajLCSSDistances);

            List<Cluster> clusters = clustering.cluster(trajectories);
//            for (int i = 0; i < clusters.size(); i++) {
//                displayClusters(getImgFileName(input), clusters.subList(i, i + 1), false);
//            }
//            displayClusters(getImgFileName(input), clusters.stream().filter(cl -> !cl.getNormal()).collect(toList()), false);
            displayClusters(getImgFileName(input), clusters, false);

//            List<Integer> trIds = List.of(100);
//            List<Trajectory> inputTrajectories = trajectories.stream().filter(tr -> trIds.contains(tr.getId())).collect(toList());
//            clustering.classifyTrajectories(initialTrajectories.stream().filter(tr -> tr.length() <= MIN_LENGTH || tr.totalDist() < MIN_TOTAL_DIST).collect(toList()));
//            clustering.classifyTrajectories(inputTrajectories);

//            inputTrajectories = generateTestTrajectories2(trajectories);
//            clustering.classifyTrajectories(inputTrajectories);
        }
    }

    private static List<Trajectory> generateTestTrajectories2(List<Trajectory> trajectories) throws IOException {
        int l1 = 30;
        TrajectoryPoint cp = clustering.getCameraPoint();
        List<TrajectoryPoint> tpList1 = new ArrayList<>();
        int coeffX = 30;
        int coeffY = 10;
        for (int i = 0; i < l1; i++) {
//            tpList1.add(new TrajectoryPoint(cp.getX() + i * 10, cp.getY() + i * 10, i * TIME_STEP));
            tpList1.add(new TrajectoryPoint(cp.getX() + i * coeffX, cp.getY() - i * coeffY, i * TIME_STEP));
        }
        Trajectory t1 = new Trajectory(0, tpList1);

        List<Trajectory> tList = new ArrayList<>(List.of(t1));
        regression.performRegression(tList, null);
//        t1.setTrajectoryPoints(t1.getKeyPoints());
        return tList;
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
        clustering.setBorders(minX, maxX, minY, maxY, trajectories);

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
