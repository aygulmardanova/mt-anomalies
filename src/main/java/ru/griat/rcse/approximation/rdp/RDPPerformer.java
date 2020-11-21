package ru.griat.rcse.approximation.rdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.griat.rcse.entity.Trajectory;
import ru.griat.rcse.misc.enums.ApproximationMethod;
import ru.griat.rcse.misc.enums.ClusteringMethod;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static ru.griat.rcse.misc.ApproximationUtils.calcAdditionalRdpPoints;
import static ru.griat.rcse.misc.ApproximationUtils.positionalErrors;
import static ru.griat.rcse.misc.ApproximationUtils.printTrajectoriesLengthsStatistics;
import static ru.griat.rcse.misc.ApproximationUtils.removeRedundantPoints;
import static ru.griat.rcse.misc.Utils.APPROXIMATION_METHOD;
import static ru.griat.rcse.misc.Utils.CLUSTERING_METHOD;
import static ru.griat.rcse.misc.Utils.RDP_COUNT;
import static ru.griat.rcse.misc.Utils.RDP_EPSILON;
import static ru.griat.rcse.misc.Utils.displayRdpTrajectories;
import static ru.griat.rcse.misc.Utils.displayTrajectories;
import static ru.griat.rcse.misc.Utils.sortTrajectoryPoints;

public class RDPPerformer {

    private final static Logger LOGGER = LoggerFactory.getLogger(RDPPerformer.class.getName());

    public void performRDP(List<Trajectory> trajectories, String input) throws IOException {
//        long distStart = System.currentTimeMillis();
        for (Trajectory currentTr : trajectories) {
            currentTr.setRdpPoints(APPROXIMATION_METHOD == ApproximationMethod.RDP
                    ? RDPReducer.reduce(currentTr.getTrajectoryPoints(), RDP_EPSILON)
                    : RDPReducer.reduceToN(currentTr.getTrajectoryPoints(), RDP_COUNT));
            calcAdditionalRdpPoints(currentTr);
            sortTrajectoryPoints(currentTr);
        }

//        long distEnd = System.currentTimeMillis();
//        LOGGER.info("Total {} approximation time {}", APPROXIMATION_METHOD, distEnd - distStart);
//        LOGGER.info("Total {} approximation time per traj {}", APPROXIMATION_METHOD, (distEnd - distStart) / trajectories.size());

//        displayRdpTrajectories(input, null, trajectories);
//        displayRdpTrajectories(input, null, trajectories.stream().filter(tr -> tr.getRdpPoints().size() == 7).collect(Collectors.toList()));

        positionalErrors(trajectories);
        printTrajectoriesLengthsStatistics(trajectories);
        System.out.println(trajectories.stream().filter(tr -> tr.getRdpPoints().size() == 7).count());

//        decrease approximation points count
        if (CLUSTERING_METHOD != ClusteringMethod.DBSCAN)
            removeRedundantPoints(trajectories, 2);

        printTrajectoriesLengthsStatistics(trajectories);
    }

}
