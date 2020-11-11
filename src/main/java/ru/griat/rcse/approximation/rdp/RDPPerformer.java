package ru.griat.rcse.approximation.rdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.griat.rcse.entity.Trajectory;
import ru.griat.rcse.entity.TrajectoryPoint;
import ru.griat.rcse.misc.enums.ApproximationMethod;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import static ru.griat.rcse.misc.ApproximationUtils.calcAdditionalRdpPoints;
import static ru.griat.rcse.misc.ApproximationUtils.positionalErrors;
import static ru.griat.rcse.misc.ApproximationUtils.printTrajectoriesLengthsStatistics;
import static ru.griat.rcse.misc.Utils.APPROXIMATION_METHOD;
import static ru.griat.rcse.misc.Utils.RDP_COUNT;
import static ru.griat.rcse.misc.Utils.RDP_EPSILON;
import static ru.griat.rcse.misc.Utils.sortTrajectoryPoints;

public class RDPPerformer {

    private final static Logger LOGGER = LoggerFactory.getLogger(RDPPerformer.class.getName());

    public void performRDP(List<Trajectory> trajectories, String input) throws IOException {
        for (Trajectory currentTr : trajectories) {
            currentTr.setRdpPoints(APPROXIMATION_METHOD == ApproximationMethod.RDP
                    ? RDPReducer.reduce(currentTr.getTrajectoryPoints(), RDP_EPSILON)
                    : RDPReducer.reduceToN(currentTr.getTrajectoryPoints(), RDP_COUNT));
            calcAdditionalRdpPoints(currentTr);
            sortTrajectoryPoints(currentTr);
        }

//        displayRdpTrajectories(input, null, trajectories);
//        displayRdpTrajectories(input, null, trajectories.stream().filter(tr -> tr.getRdpPoints().size() <= 2).collect(Collectors.toList()));

        positionalErrors(trajectories);
        printTrajectoriesLengthsStatistics(trajectories);
        double coeff = 2;

//        decrease approximation points count
        for (Trajectory currentTr : trajectories) {
            if (currentTr.length() > 12 || currentTr.getRdpPoints().size() < 6)
                continue;
            double totalDist = currentTr.totalDist();
//            average inter-points distance in the original trajectory
            double avgDistUnit = totalDist / currentTr.getTrajectoryPoints().size();
//            don't consider first and last approximation points
            for (int i = 1; i < currentTr.getRdpPoints().size() - 1; i++) {
                TrajectoryPoint currentTP = currentTr.getRdpPoints().get(i);
                if (currentTP.distanceTo(currentTr.get(i - 1)) < coeff * avgDistUnit
                        || currentTP.distanceTo(currentTr.get(i + 1)) < coeff * avgDistUnit) {
//                    LOGGER.info(MessageFormat.format("RDP point {0} was deleted", i));
                    currentTr.getRdpPoints().remove(currentTP);
                }
            }
        }
        printTrajectoriesLengthsStatistics(trajectories);
    }

}
