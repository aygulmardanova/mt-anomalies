import entity.Trajectory;
import entity.TrajectoryPoint;
import exception.TrajectoriesParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parsing.TrajectoriesParser;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

public class JavaMain {

    private final static Logger LOGGER = LoggerFactory.getLogger(JavaMain.class.getName());

    private static final String FILE_DIR = "/Users/aygulmardanova/IdeaProjects/ILMENAU/mt-anomalies/src/main/resources/input/";
//    private static final String FILE_NAME = "4.txt";
    private static final String[] FILE_NAMES = {"1.txt", "2.txt", "3.txt", "4.txt"};

    public static void main(String[] args) throws IOException, TrajectoriesParserException {

        for (String FILE_NAME: FILE_NAMES) {
            LOGGER.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
            LOGGER.info("Trajectories parsing from (" + FILE_NAME + ") started");
            List<Trajectory> trajectories = new TrajectoriesParser().parseTxt(FILE_DIR + FILE_NAME);
            LOGGER.info("Total amount of trajectories: " + trajectories.size());

            printInputBorders(trajectories);

//        int i1 = 0;
//        int i2 = 3;
//
//        LOGGER.info("Calculating distance between trajectories: " +
//                "\n1) " + trajectories.get(i1) + "; " +
//                "\n2) " + trajectories.get(i2));
//
//        LOGGER.info(trajectories.get(i1).getTrajectoryPoints().size() + "");
//        LOGGER.info(trajectories.get(i2).getTrajectoryPoints().size() + "");
//        ClusteringJava clusteringJava = new ClusteringJava();
//        double dist = clusteringJava.calcLCSSDist(trajectories.get(i1), trajectories.get(i2));
//        LOGGER.info(dist + "");

        }
    }

    private static void printInputBorders(List<Trajectory> trajectories) {
        long minX = trajectories.stream()
                .mapToLong(tr ->
                        tr.getTrajectoryPoints().stream()
                                .mapToLong(TrajectoryPoint::getX)
                                .min().orElseThrow(NoSuchElementException::new))
                .min().orElseThrow(NoSuchElementException::new);
        long minY = trajectories.stream()
                .mapToLong(tr ->
                        tr.getTrajectoryPoints().stream()
                                .mapToLong(TrajectoryPoint::getY)
                                .min().orElseThrow(NoSuchElementException::new))
                .min().orElseThrow(NoSuchElementException::new);
        long maxX = trajectories.stream()
                .mapToLong(tr ->
                        tr.getTrajectoryPoints().stream()
                                .mapToLong(TrajectoryPoint::getX)
                                .max().orElseThrow(NoSuchElementException::new))
                .max().orElseThrow(NoSuchElementException::new);
        long maxY = trajectories.stream()
                .mapToLong(tr ->
                        tr.getTrajectoryPoints().stream()
                                .mapToLong(TrajectoryPoint::getY)
                                .max().orElseThrow(NoSuchElementException::new))
                .max().orElseThrow(NoSuchElementException::new);

        LOGGER.info("borders for X: (" + minX + ", " + maxX + ")");
        LOGGER.info("borders for Y: (" + minY + ", " + maxY + ")");
    }

}
