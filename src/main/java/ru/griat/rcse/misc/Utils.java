package ru.griat.rcse.misc;

import ru.griat.rcse.entity.Cluster;
import ru.griat.rcse.entity.Trajectory;
import ru.griat.rcse.entity.TrajectoryPoint;
import ru.griat.rcse.misc.enums.ApproximationMethod;
import ru.griat.rcse.misc.enums.LinkageMethod;
import ru.griat.rcse.visualisation.DisplayImage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class Utils {

    private static final Path MAIN_PATH = Paths.get(".").normalize().toAbsolutePath();
    private static final Path RESOURCES_PATH = Paths.get(MAIN_PATH.toString(),
            "src", "main", "resources");
    public static final Path INPUT_FILE_DIR = Paths.get(RESOURCES_PATH.toString(), "input");
    public static final Path OUTPUT_IMG_DIR = Paths.get(RESOURCES_PATH.toString(), "output");
    public static final Path CSV_DIR = Paths.get(RESOURCES_PATH.toString(), "csv");

    public static final String EXPERIMENT_ID = "exp11";
    public static final String[] INPUT_FILE_NAMES = {"1", "2", "3", "4"};
    public static final String[] INPUT_FILE_NAMES_FIRST = {"1"};
    public static final String INPUT_FILE_EXTENSION = "txt";
    public static final String INPUT_IMG_EXTENSION = "jpg";
    public static final String CSV_EXTENSION = "csv";

//    time between frames in seconds (here 0.01 sec)
    public static final double INTER_FRAME_TIME = 0.01;

    public static final int IMAGE_MIN_X = 0;
    public static final int IMAGE_MAX_X = 1280;
    public static final int IMAGE_MIN_Y = 0;
    public static final int IMAGE_MAX_Y = 720;

    public static final int MIN_LENGTH = 10;
    public static final int MIN_TOTAL_DIST = 80;
    public static final int MAX_KP_COUNT = 9;
    public static final int TIME_STEP = 5;

    public static final LinkageMethod LINKAGE_METHOD = LinkageMethod.AVERAGE;
    public static final ApproximationMethod APPROXIMATION_METHOD = ApproximationMethod.REGRESSION;
    public static final boolean IS_ADAPTIVE = true;
    public static final double STATIC_COEFF = 0.15;
    public static final double ADAPT_COEFF = 20.0;
    public static final int OUTPUT_CLUSTERS_COUNT = 9;
    public static final double RDP_EPSILON = 10.5;
    public static final int RDP_COUNT = MAX_KP_COUNT;

    public static String getImgFileName(String name) {
        return name + "." + INPUT_IMG_EXTENSION;
    }

    public static String getFileName(String name) {
        return name + "." + INPUT_FILE_EXTENSION;
    }

    public static String getFileDir(Path path, String fileName) {
        return Paths.get(path.toString(), fileName).toString();
    }

//    fileName = 1 / 2 / 3 / 4
    public static String getCsvDir(String experimentId, String fileName) throws IOException {
        Path csvPath = Paths.get(CSV_DIR.toString(), experimentId);
        new File(csvPath.toString()).mkdirs();

        csvPath = Paths.get(csvPath.toString(), fileName + "." + CSV_EXTENSION);
        if (!csvPath.toFile().exists())
            csvPath.toFile().createNewFile();

        return csvPath.toString();
    }

    public static List<TrajectoryPoint> getTrajectoryPoints(Trajectory trajectory) {
        switch (APPROXIMATION_METHOD) {
            case NONE:
                return trajectory.getTrajectoryPoints();
            case REGRESSION:
                return trajectory.getKeyPoints();
            case RDP:
            case RDP_N:
                return trajectory.getRdpPoints();
        }
        return Collections.emptyList();
    }

    public static void sortTrajectoryPoints(Trajectory trajectory) {
        switch (APPROXIMATION_METHOD) {
            case REGRESSION:
                trajectory.setKeyPoints(trajectory.getKeyPoints().stream().sorted(Comparator.comparing(TrajectoryPoint::getTime)).collect(toList()));
            case RDP:
            case RDP_N:
                trajectory.setRdpPoints(trajectory.getRdpPoints().stream().sorted(Comparator.comparing(TrajectoryPoint::getTime)).collect(toList()));
        }
    }

    public static boolean checkTPValidity(TrajectoryPoint tp) {
        return tp.getX() >= IMAGE_MIN_X && tp.getX() <= IMAGE_MAX_X
                && tp.getY() >= IMAGE_MIN_Y && tp.getY() <= IMAGE_MAX_Y;
    }

    public static void displayClusters(String fileName, List<Cluster> clusters, boolean save) throws IOException {
        new DisplayImage().displayAndSaveClusters(fileName, "res" + fileName, "clustering-results/" + EXPERIMENT_ID, clusters, save);
    }

    public static void displayTrajectories(String fileName, List<Trajectory> trajectories) throws IOException {
        new DisplayImage().displayAndSave(fileName, "", "initial-data", trajectories, false);
    }

    public static void displayRegressionTrajectories(String fileName, String subName, List<Trajectory> trajectories) throws IOException {
        new DisplayImage().displayAndSave(getImgFileName(fileName),
                getImgFileName(fileName + "-" + subName), "regression-results", trajectories, false);
    }

    public static void displayRdpTrajectories(String fileName, String subName, List<Trajectory> trajectories) throws IOException {
        new DisplayImage().displayAndSave(getImgFileName(fileName),
                getImgFileName(fileName + "-" + subName), "rdp-results", trajectories, false);
    }

    public static void displayTrajectories(String fileName, List<Trajectory> trajectories, List<Integer> indexes) throws IOException {
        new DisplayImage().displayAndSave(fileName, null, null, indexes.stream().map(trajectories::get).collect(toList()), false);
    }

    public static List<Trajectory> filterTrajectories(List<Trajectory> trajectories) {
        return trajectories.stream().filter(tr -> tr.length() > MIN_LENGTH && tr.totalDist() >= MIN_TOTAL_DIST).collect(toList());
    }

    public static List<Trajectory> copyTrajectories(List<Trajectory> trajectories) {
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
            trCopy.setRdpPoints(traj.getRdpPoints());
            copies.add(traj);
            copies.add(trCopy);
        }
        return copies;
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
