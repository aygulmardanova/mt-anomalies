package ru.griat.rcse.approximation.rdp;

import ru.griat.rcse.entity.Trajectory;
import ru.griat.rcse.misc.enums.ApproximationMethod;

import java.io.IOException;
import java.util.List;

import static ru.griat.rcse.misc.ApproximationUtils.calcAdditionalRdpPoints;
import static ru.griat.rcse.misc.ApproximationUtils.positionalErrors;
import static ru.griat.rcse.misc.ApproximationUtils.printTrajectoriesLengthsStatistics;
import static ru.griat.rcse.misc.Utils.APPROXIMATION_METHOD;
import static ru.griat.rcse.misc.Utils.RDP_COUNT;
import static ru.griat.rcse.misc.Utils.RDP_EPSILON;
import static ru.griat.rcse.misc.Utils.sortTrajectoryPoints;

public class RDPPerformer {

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
    }
}
