import entity.Trajectory;
import entity.TrajectoryPoint;
import exception.TrajectoriesParserException;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/*
* stop symbols:
* if meet number - read until ']' or ',' or ')'
* [ - check for next, if [ - check for next, if [ - isX=true, if value - save x,
* */
public class TrajectoriesParser {

    private int openingSqBracketNumber;
    private int closingSqBracketNumber;
    private int indexOfT;

    private StringBuilder x;
    private StringBuilder y;
    private StringBuilder t;

    private List<TrajectoryPoint> trajectoryPoints;
    private List<Trajectory> trajectories;

    public TrajectoriesParser() {
        openingSqBracketNumber = 0;
        closingSqBracketNumber = 0;
        indexOfT = 0;

        x = new StringBuilder();
        y = new StringBuilder();
        t = new StringBuilder();

        trajectoryPoints = new ArrayList<>();
        trajectories = new ArrayList<>();
    }


    public List<Trajectory> parseTxt(String fileName) throws FileNotFoundException, TrajectoriesParserException {

        File file = new File(FilenameUtils.normalize(fileName));
        Scanner input = new Scanner(file);
        while(input.hasNext()) {
            String nextToken = input.next();
            while (nextToken.equals("[")) {
                openingSqBracketNumber++;
                nextToken = input.next();
            }
            if (nextToken.equals("(")) {
                readCoordinates(input);
            }
            if (input.next().equals(",") && input.next().equals("[")) {
                readTime(input);
            } else {
                throw new TrajectoriesParserException("After coordinates array with timestamps was expected");
            }
        }

        input.close();
        return trajectories;
    }

    private void readCoordinates(Scanner input) {
        String nextToken = input.next();
        while(!nextToken.equals(",")) {
            x.append(nextToken);
            nextToken = input.next();
        }
        while(!nextToken.equals(")")) {
            y.append(nextToken);
            nextToken = input.next();
        }
        processTrajectoryPoint();
    }

    private void readTime(Scanner input) throws TrajectoriesParserException {
        String nextToken = input.next();
        while(!nextToken.equals("]")) {
            while(!nextToken.equals(",") && !nextToken.equals("]")) {
                t.append(nextToken);
                nextToken = input.next();
            }
//            update trajectoryPoint at current index with a timestamp
            trajectoryPoints.get(indexOfT).setTimestamp(Integer.valueOf(t.toString()));
            indexOfT++;
            t = new StringBuilder();
            nextToken = input.next();
        }
        if (!input.next().equals("]"))
            throw new TrajectoriesParserException("Closing square bracket ']' expected after trajectories array");
//        at this point, one trajectory is processed and closed.
//        Next token must be comma or third closing square bracket
        trajectories.add(new Trajectory(trajectoryPoints));
    }

    private void processTrajectoryPoint() {
        TrajectoryPoint point = new TrajectoryPoint();
        point.setX(Integer.valueOf(x.toString()));
        point.setY(Integer.valueOf(y.toString()));
        trajectoryPoints.add(point);

        x = new StringBuilder();
        y = new StringBuilder();
    }

}
