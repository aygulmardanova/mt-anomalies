package ru.griat.rcse.misc;

import ru.griat.rcse.entity.TrajectoryPoint;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {

    private static final Path MAIN_PATH = Paths.get(".").normalize().toAbsolutePath();
    private static final Path RESOURCES_PATH = Paths.get(MAIN_PATH.toString(),
            "src", "main", "resources");
    public static final Path INPUT_FILE_DIR = Paths.get(RESOURCES_PATH.toString(), "input");
    public static final Path OUTPUT_IMG_DIR = Paths.get(RESOURCES_PATH.toString(), "output");
    public static final Path CSV_DIR = Paths.get(RESOURCES_PATH.toString(), "csv");

    public static final String[] INPUT_FILE_NAMES = {"1", "2", "3", "4"};
    public static final String[] INPUT_FILE_NAMES_FIRST = {"2"};
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

    public static final double STATIC_COEFF = 0.1;
    public static final double ADAPT_COEFF = 20.0;
    public static final int OUTPUT_CLUSTERS_COUNT = 20;

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

    public static boolean checkTPValidity(TrajectoryPoint tp) {
        return tp.getX() >= IMAGE_MIN_X && tp.getX() <= IMAGE_MAX_X
                && tp.getY() >= IMAGE_MIN_Y && tp.getY() <= IMAGE_MAX_Y;
    }
}
