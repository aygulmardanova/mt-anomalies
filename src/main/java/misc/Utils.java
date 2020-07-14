package misc;

public class Utils {

    public static final String INPUT_FILE_DIR = "/Users/aygulmardanova/IdeaProjects/ILMENAU/mt-anomalies/src/main/resources/input/";
    public static final String OUTPUT_IMG_DIR = "/Users/aygulmardanova/IdeaProjects/ILMENAU/mt-anomalies/src/main/resources/output/";

    public static final String[] INPUT_FILE_NAMES = {"1", "2", "3", "4"};
    public static final String[] INPUT_FILE_NAMES_FIRST = {"1"};
    public static final String INPUT_FILE_EXTENSION = "txt";
    public static final String INPUT_IMG_EXTENSION = "jpg";

    public static String getImgFileName(String name) {
        return name + "." + INPUT_IMG_EXTENSION;
    }

    public static String getFileName(String name) {
        return name + "." + INPUT_FILE_EXTENSION;
    }
}
