package ru.griat.rcse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.griat.rcse.clustering.Clustering;
import ru.griat.rcse.clustering.DBSCANClustering;
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

    private static Clustering clustering;

    public static void main(String[] args) throws IOException, TrajectoriesParserException {

        for (String input : INPUT_FILE_NAMES_FIRST) {
            List<Trajectory> trajectories = parseTrajectories(getFileName(input));
            List<Trajectory> initialTrajectories = trajectories;

            trajectories = filterTrajectories(trajectories);
            LOGGER.info("Total approximated trajectories count: {}", trajectories.size());

            performApproximation(trajectories, input);

            switch (CLUSTERING_METHOD) {
                case HIERARCHICAL:
                    Double[][] trajLCSSDistances;
                    clustering = new HierarchicalClustering(initialTrajectories);
                    setInputBorders(initialTrajectories, (HierarchicalClustering) clustering);

                    if (true)
                        return;
                    calcDistances(trajectories);
                    trajLCSSDistances = ((HierarchicalClustering) clustering).getTrajLCSSDistances();
                    new CSVProcessing().writeCSV(trajLCSSDistances, 0, initialTrajectories.size(), 0, initialTrajectories.size(), EXPERIMENT_ID, input);

                    trajLCSSDistances = new Double[initialTrajectories.size()][initialTrajectories.size()];
                    new CSVProcessing().readCSV(trajLCSSDistances, EXPERIMENT_ID, input);
                    ((HierarchicalClustering) clustering).setTrajLCSSDistances(trajLCSSDistances);
                    break;
                case DBSCAN:
                    clustering = new DBSCANClustering();
            }

//            long clStart = System.currentTimeMillis();
            List<Cluster> clusters = clustering.cluster(trajectories);
//            long clEnd = System.currentTimeMillis();
//            LOGGER.info("Total clustering time is: {} milliseconds", clEnd - clStart);
            displayClusters(getImgFileName(input), clusters.stream().filter(cl -> !cl.getNormal()).collect(toList()), false);
            displayClusters(getImgFileName(input), clusters, false);

//            List<Trajectory> inputTrajectories = trajectories.stream().filter(tr -> List.of(100, 101).contains(tr.getId())).collect(toList());
//            clustering.classifyTrajectories(inputTrajectories);
//            clustering.classifyTrajectories(generateTestTrajectories(clustering.getCameraPoint()));
        }
    }

    private static void calcDistances(List<Trajectory> trajectories) {
        int i = 0;
        for (Trajectory t1 : trajectories) {
            for (Trajectory t2 : trajectories) {
                if (t1.getId() != t2.getId() && t1.getId() < t2.getId()) {
                    logCalcDist(t1, t2);
                    i++;
                }
            }
        }
        LOGGER.info("Total trajectories pairs count is {}", i);
    }

    private static double logCalcDist(Trajectory t1, Trajectory t2) {
//        LOGGER.info("-----");
        double dist = clustering.calcDist(t1, t2);

//        if (!Double.valueOf(1.0).equals(dist)) {
//            LOGGER.info("Calculating distance between trajectories: " +
//                    "\n1) " + t1 + "; " +
//                    "\n2) " + t2);
//            LOGGER.info("dist(" + t1.getId() + ", " + t2.getId() + ") = " + dist);
//        }

        return dist;
    }

}
