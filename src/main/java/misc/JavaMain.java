package misc;

import entity.Trajectory;
import entity.TrajectoryPoint;
import exception.TrajectoriesParserException;
import clustering.Clustering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parsing.TrajectoriesParser;
import visualisation.DisplayImage;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import static misc.Utils.*;

public class JavaMain {

    private final static Logger LOGGER = LoggerFactory.getLogger(JavaMain.class.getName());

    private static Clustering clustering;

    public static void main(String[] args) throws IOException, TrajectoriesParserException {

        for (String input: INPUT_FILE_NAMES) {
            List<Trajectory> trajectories = parseTrajectories(Utils.getFileName(input));
            clustering = new Clustering(trajectories);
            printInputBorders(trajectories);
            displayImage(Utils.getImgFileName(input), trajectories);
//            for (Trajectory t1 : trajectories) {
//                for (Trajectory t2 : trajectories) {
//                    if (t1 != t2) {
//                        calcDist(t1, t2);
//                    }
//                }
//            }
        }
    }

    private static List<Trajectory> parseTrajectories(String fileName) throws IOException, TrajectoriesParserException {
        LOGGER.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        LOGGER.info("Trajectories parsing from (" + fileName + ") started");
        List<Trajectory> trajectories = new TrajectoriesParser().parseTxt(Utils.getFileDir(Utils.INPUT_FILE_DIR, fileName));
        LOGGER.info("Total amount of trajectories: " + trajectories.size());
        return trajectories;

    }

    private static void displayImage(String fileName, List<Trajectory> trajectories) throws IOException {
        new DisplayImage().displayAndSave(fileName, trajectories);
    }

    private static double calcDist(Trajectory t1, Trajectory t2) {
        LOGGER.info("-----");
        double dist = clustering.calcLCSSDist(t1, t2);

        if (dist != 1) {
            LOGGER.info("Calculating distance between trajectories: " +
                    "\n1) " + t1 + "; " +
                    "\n2) " + t2);
            LOGGER.info("dist(" + t1.getId() + ", " + t2.getId() + ") = " + dist);
        }

        return dist;
    }

    private static void printInputBorders(List<Trajectory> trajectories) {
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

        LOGGER.info("borders for X: (" + minX + ", " + maxX + ")");
        LOGGER.info("borders for Y: (" + minY + ", " + maxY + ")");
    }

}
