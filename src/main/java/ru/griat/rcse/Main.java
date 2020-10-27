package ru.griat.rcse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.griat.rcse.clustering.HierarchicalClustering;
import ru.griat.rcse.csv.CSVProcessing;
import ru.griat.rcse.entity.Cluster;
import ru.griat.rcse.entity.Trajectory;
import ru.griat.rcse.exception.TrajectoriesParserException;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static ru.griat.rcse.misc.ApproximationUtils.performApproximation;
import static ru.griat.rcse.misc.ParseUtils.parseTrajectories;
import static ru.griat.rcse.misc.ParseUtils.setInputBorders;
import static ru.griat.rcse.misc.Utils.*;

public class Main {

    private final static Logger LOGGER = LoggerFactory.getLogger(Main.class.getName());

    private static HierarchicalClustering clustering;

    public static void main(String[] args) throws IOException, TrajectoriesParserException {

        for (String input : INPUT_FILE_NAMES_FIRST) {
            List<Trajectory> trajectories = parseTrajectories(getFileName(input));
            List<Trajectory> initialTrajectories = trajectories;
            Double[][] trajLCSSDistances;

            trajectories = filterTrajectories(trajectories);

            performApproximation(trajectories, input);
            displayTrajectories(getImgFileName(input), trajectories.stream().filter(tr -> tr.getRegressionX().degree() < 2 || tr.getRegressionY().degree() < 2).collect(toList()));

            clustering = new HierarchicalClustering(initialTrajectories);
            setInputBorders(initialTrajectories, clustering);

            calcDistances(trajectories);
            trajLCSSDistances = clustering.getTrajLCSSDistances();
            new CSVProcessing().writeCSV(trajLCSSDistances, 0, initialTrajectories.size(), 0, initialTrajectories.size(), EXPERIMENT_ID, input);

            trajLCSSDistances = new Double[initialTrajectories.size()][initialTrajectories.size()];
            new CSVProcessing().readCSV(trajLCSSDistances, EXPERIMENT_ID, input);
            clustering.setTrajLCSSDistances(trajLCSSDistances);

            List<Cluster> clusters = clustering.cluster(trajectories);
            displayClusters(getImgFileName(input), clusters.stream().filter(cl -> !cl.getNormal()).collect(toList()), false);
            displayClusters(getImgFileName(input), clusters, false);

            List<Trajectory> inputTrajectories = trajectories.stream().filter(tr -> List.of(100, 101).contains(tr.getId())).collect(toList());
            clustering.classifyTrajectories(inputTrajectories);
            clustering.classifyTrajectories(generateTestTrajectories(clustering.getCameraPoint()));
        }
    }

    private static void calcDistances(List<Trajectory> trajectories) {
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

        if (!Double.valueOf(1.0).equals(dist)) {
            LOGGER.info("Calculating distance between trajectories: " +
                    "\n1) " + t1 + "; " +
                    "\n2) " + t2);
            LOGGER.info("dist(" + t1.getId() + ", " + t2.getId() + ") = " + dist);
        }

        return dist;
    }

}
