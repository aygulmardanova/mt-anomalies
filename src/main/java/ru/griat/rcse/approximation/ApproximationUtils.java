package ru.griat.rcse.approximation;

import org.apache.commons.math3.analysis.solvers.BaseAbstractUnivariateSolver;
import org.apache.commons.math3.analysis.solvers.BisectionSolver;
import org.apache.commons.math3.analysis.solvers.LaguerreSolver;
import org.apache.commons.math3.exception.NoBracketingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.griat.rcse.approximation.polynomial_regression.Polynomial;
import ru.griat.rcse.approximation.polynomial_regression.RegressionPerformer;
import ru.griat.rcse.approximation.rdp.Line;
import ru.griat.rcse.approximation.rdp.RDPPerformer;
import ru.griat.rcse.entity.Trajectory;
import ru.griat.rcse.entity.TrajectoryPoint;
import ru.griat.rcse.misc.enums.ApproximationMethod;

import java.io.IOException;
import java.util.List;
import java.util.OptionalInt;

import static ru.griat.rcse.misc.Utils.APPROXIMATION_METHOD;
import static ru.griat.rcse.misc.Utils.MAX_KP_COUNT;
import static ru.griat.rcse.misc.Utils.TIME_STEP;

public class ApproximationUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApproximationUtils.class.getName());

    public static void performApproximation(List<Trajectory> trajectories, String input) throws IOException {
        switch (APPROXIMATION_METHOD) {
            case NONE:
                break;
            case REGRESSION:
                new RegressionPerformer().performRegression(trajectories, input);
                break;
            case RDP:
            case RDP_N:
                new RDPPerformer().performRDP(trajectories, input);
                break;
        }
    }

    public static void calculateDiffEquationSolutions(Trajectory currentTr) {

        Polynomial polynomialX = currentTr.getRegressionX().toPolynomial();
        Polynomial diffX1 = polynomialX.derivative();
        Polynomial diffX2 = diffX1.derivative();

        Polynomial polynomialY = currentTr.getRegressionY().toPolynomial();
        Polynomial diffY1 = polynomialY.derivative();
        Polynomial diffY2 = diffY1.derivative();

//        LaguerreSolver, BisectionSolver,
//        TODO: try to add steps and obtain more equation solutions (use START_VALUE for the solver)
        BaseAbstractUnivariateSolver bisectionSolver = new BisectionSolver();
        BaseAbstractUnivariateSolver laguerreSolver = new LaguerreSolver();
        for (Polynomial diff : List.of(diffX1, diffX2, diffY1, diffY2)) {
            Double prevRes = null;
            Double res;
            for (BaseAbstractUnivariateSolver solver : List.of(bisectionSolver, laguerreSolver)) {
                try {
                    res = solver.solve(30000, diff,
                            currentTr.getTrajectoryPoints().stream().mapToInt(TrajectoryPoint::getTime).min().getAsInt(),
                            currentTr.getTrajectoryPoints().stream().mapToInt(TrajectoryPoint::getTime).max().getAsInt(),
                            currentTr.getTrajectoryPoints().stream().mapToInt(TrajectoryPoint::getTime).min().getAsInt() + 1
                    );
//                    add key points, which are solutions of derivative equations
                    if (prevRes == null || Math.round(prevRes) != Math.round(res)) {
                        currentTr.addKeyPoint(new TrajectoryPoint(
                                (int) Math.round(currentTr.getRegressionX().predict(res)),
                                (int) Math.round(currentTr.getRegressionY().predict(res)),
                                (int) Math.round(res)), null);
                        prevRes = res;
                    }
                } catch (NoBracketingException ignored) {}
            }
        }
    }

    public static void addFirstAndLastPoints(Trajectory currentTr) {
        int stTime = currentTr.get(0).getTime();
        int endTime = currentTr.get(currentTr.length() - 1).getTime();
//            add first point
        if (currentTr.getKeyPoints().stream().noneMatch(kp -> kp.getTime() < currentTr.get(2).getTime()))
            currentTr.addKeyPoint(new TrajectoryPoint(
                    (int) Math.round(currentTr.getRegressionX().predict(stTime)),
                    (int) Math.round(currentTr.getRegressionY().predict(stTime)),
                    stTime), null);
//            add last point
        if (currentTr.getKeyPoints().stream().noneMatch(kp -> endTime - kp.getTime() < 2 * TIME_STEP))
            currentTr.addKeyPoint(new TrajectoryPoint(
                    (int) Math.round(currentTr.getRegressionX().predict(endTime)),
                    (int) Math.round(currentTr.getRegressionY().predict(endTime)),
                    endTime), null);
    }

    public static void calcAdditionalKeyPoints(Trajectory currentTr) {
//        if small amount of trajectory points
//        and trajectory length is more than 2 times bigger than amount of key points:
        if (currentTr.getKeyPoints().size() < MAX_KP_COUNT && currentTr.length() >= currentTr.getKeyPoints().size()) {
            int diff = MAX_KP_COUNT - currentTr.getKeyPoints().size();
            double interval = (currentTr.length() - 3) * 1.0 / diff;
            for (int i = 0; i < diff; i++) {
                int tt = currentTr.get((int) Math.round(1 + interval * i)).getTime();
                Integer bonusTT = (i < diff - 1) ? currentTr.get((int) Math.round(1 + interval * (i + 1))).getTime() : null;
                currentTr.addKeyPoint(new TrajectoryPoint(
                        (int) Math.round(currentTr.getRegressionX().predict(tt)),
                        (int) Math.round(currentTr.getRegressionY().predict(tt)),
                        tt), bonusTT);
            }
        }
    }

    public static void calcAdditionalRdpPoints(Trajectory currentTr) {
//        if small amount of trajectory points
//        and trajectory length is more than 2 times bigger than amount of key points:
        if (currentTr.getRdpPoints().size() < MAX_KP_COUNT && currentTr.length() >= currentTr.getRdpPoints().size()) {
            int diff = MAX_KP_COUNT - currentTr.getRdpPoints().size();
            double interval = (currentTr.length() - 3) * 1.0 / diff;
            for (int i = 0; i < diff; i++) {
                int tt = currentTr.get((int) Math.round(1 + interval * i)).getTime();
                Integer bonusTT = (i < diff - 1) ? currentTr.get((int) Math.round(1 + interval * (i + 1))).getTime() : null;
                currentTr.addRDPPoint(currentTr.getTrajectoryPoints().stream().filter(tp -> tp.getTime() == tt).findFirst().get(), bonusTT);
            }
        }
    }

    public static void positionalErrors(List<Trajectory> trajectories) {
        double minPosError = Double.MAX_VALUE;
        double maxPosError = Double.MIN_VALUE;
        double avgPosError = 0.0;
        for (Trajectory currentTr : trajectories) {
            double currentPosError = ApproximationUtils.positionalError(currentTr);
            if (currentPosError < minPosError)
                minPosError = currentPosError;
            if (currentPosError > maxPosError)
                maxPosError = currentPosError;
            avgPosError += currentPosError;
        }

        System.out.println("min positional error: " + minPosError);
        System.out.println("max positional error: " + maxPosError);
        System.out.println("avg positional error: " + avgPosError / trajectories.size());
    }

    public static double positionalError(Trajectory trajectory) {
        List<TrajectoryPoint> original = trajectory.getTrajectoryPoints();
        List<TrajectoryPoint> simplified = APPROXIMATION_METHOD.equals(ApproximationMethod.REGRESSION)
                ? trajectory.getKeyPoints()
                : trajectory.getRdpPoints();

        final double[] error = {0.0};
        if (original.size() == simplified.size())
            return error[0];

        original.forEach(op -> error[0] += calcDist(op, simplified));
        return error[0];
    }

    public static double calcDist(TrajectoryPoint op, List<TrajectoryPoint> simplified) {
//            find the respective simplifying line (defined by left and right RDP points)
        OptionalInt rightTime = simplified.stream().filter(rpTime -> rpTime.getTime() >= op.getTime()).mapToInt(TrajectoryPoint::getTime).min();
        OptionalInt leftTime = simplified.stream().filter(rpTime -> rpTime.getTime() <= op.getTime()).mapToInt(TrajectoryPoint::getTime).max();
        if (rightTime.isEmpty() || leftTime.isEmpty()) {
            LOGGER.error("No surrounding RDP point found, while each original point must have 2 surroinding RDP points.");
            return 0.0;
        }
        if (rightTime.getAsInt() == leftTime.getAsInt())
            return 0.0;
        TrajectoryPoint rightTP = simplified.stream().filter(rp -> rp.getTime() == rightTime.getAsInt()).findFirst().get();
        TrajectoryPoint leftTP = simplified.stream().filter(rp -> rp.getTime() == leftTime.getAsInt()).findFirst().get();
        if (rightTP.equalsSpatially(op) || leftTP.equalsSpatially(op))
            return 0.0;
        return new Line(leftTP, rightTP).distance(op);
    }

    public static void printTrajectoriesLengthsStatistics(List<Trajectory> trajectories) {
        System.out.println("min length: " + trajectories.stream().mapToInt(tr -> getSimplifiedPoints(tr).size()).min());
        System.out.println("max length: " + trajectories.stream().mapToInt(tr -> getSimplifiedPoints(tr).size()).max());
        System.out.println("avg length: " + trajectories.stream().mapToInt(tr -> getSimplifiedPoints(tr).size()).average());
    }

    public static List<TrajectoryPoint> getSimplifiedPoints(Trajectory trajectory) {
        return APPROXIMATION_METHOD.equals(ApproximationMethod.REGRESSION)
                ? trajectory.getKeyPoints()
                : trajectory.getRdpPoints();
    }
}
