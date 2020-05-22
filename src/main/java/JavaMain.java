import entity.Trajectory;
import exception.TrajectoriesParserException;

import java.io.IOException;
import java.util.List;

public class JavaMain {

    private static final String FILE_NAME = "/Users/aygulmardanova/IdeaProjects/ILMENAU/mt-anomalies/src/main/resources/input/0.txt";

    public static void main(String[] args) throws IOException, TrajectoriesParserException {
        List<Trajectory> trajectories = new TrajectoriesParser2().parseTxt(FILE_NAME);
        System.out.println(trajectories.size());
    }
}
