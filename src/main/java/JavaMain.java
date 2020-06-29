import entity.Trajectory;
import exception.TrajectoriesParserException;

import java.io.IOException;
import java.util.List;

public class JavaMain {

    private static final String FILE_DIR = "/Users/aygulmardanova/IdeaProjects/ILMENAU/mt-anomalies/src/main/resources/input/";
    private static final String FILE_NAME = "4.txt";

    public static void main(String[] args) throws IOException, TrajectoriesParserException {
        List<Trajectory> trajectories = new TrajectoriesParser().parseTxt(FILE_DIR + FILE_NAME);
        System.out.println(trajectories.size());
    }
}
