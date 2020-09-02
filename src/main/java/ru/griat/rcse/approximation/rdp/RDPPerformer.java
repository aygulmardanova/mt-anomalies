package ru.griat.rcse.approximation.rdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.griat.rcse.approximation.rdp.RDPReducer;
import ru.griat.rcse.entity.Trajectory;

import java.io.IOException;
import java.util.List;

import static ru.griat.rcse.misc.Utils.displayRdpTrajectories;

public class RDPPerformer {

    private final static Logger LOGGER = LoggerFactory.getLogger(RDPPerformer.class.getName());

    public void performRDP(List<Trajectory> trajectories, String input) throws IOException {
        double epsilon = 0.5;
        for (int tId = 0; tId < trajectories.size(); tId++) {
            Trajectory currentTr = trajectories.get(tId);

            currentTr.setRdpPoints(RDPReducer.reduce(currentTr.getTrajectoryPoints(), epsilon));
            System.out.println("orig: " + currentTr.length() + ", rdp: " + currentTr.getRdpPoints().size());
        }

        displayRdpTrajectories(input, null, trajectories);
        System.out.println("min: " + trajectories.stream().mapToInt(tr -> tr.getRdpPoints().size()).min());
        System.out.println("max: " + trajectories.stream().mapToInt(tr -> tr.getRdpPoints().size()).max());
        System.out.println("avg: " + trajectories.stream().mapToInt(tr -> tr.getRdpPoints().size()).average());

    }
}
