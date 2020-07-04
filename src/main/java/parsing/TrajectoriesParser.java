package parsing;

import entity.Trajectory;
import entity.TrajectoryPoint;
import exception.TrajectoriesParserException;
import org.apache.commons.io.FilenameUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/*
 * stop symbols:
 * if meet number - read until ']' or ',' or ')'
 * [ - check for next, if [ - check for next, if [ - isX=true, if value - save x,
 * */
public class TrajectoriesParser {

    private int openingSqBracketNumber;
    private int closingSqBracketNumber;

    private boolean trajectoryStarted = false;
    private boolean trajectoryCoordinatesStarted = false;
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

    public List<Trajectory> parseTxt(String fileName) throws IOException, TrajectoriesParserException {

        InputStream reader = new FileInputStream(FilenameUtils.normalize(fileName));
        int intch;
        while ((intch = reader.read()) != -1) {
            char nextChar = (char) intch;
            while ((nextChar == ',' || nextChar == ' '))
                nextChar = (char) reader.read();
            while (nextChar == '[') {
                increaseOpeningSqBracketsCount();
                nextChar = (char) reader.read();
            }
            while (trajectoryCoordinatesStarted) {
                if (nextChar == '(') {
                    readCoordinates(reader);
                }
                nextChar = (char) reader.read();
                if (nextChar == ']') {
                    increaseClosingSqBracketsCount();
                }
            }
            nextChar = (char) reader.read();
            while ((nextChar == ',' || nextChar == ' '))
                nextChar = (char) reader.read();
            if (trajectoryStarted) {
                if (nextChar == '[') {
                    increaseOpeningSqBracketsCount();
                    readTime(reader);
                } else {
                    throw new TrajectoriesParserException("After coordinates array with timestamps was expected");
                }
                finishProcessingTrajectory();
            }
        }

        reader.close();
        return trajectories;
    }

    private void processBracketsCount() {
        if (openingSqBracketNumber == 1) {
            trajectoryStarted = false;
            trajectoryCoordinatesStarted = false;
        }
        if (openingSqBracketNumber == 2) {
            trajectoryStarted = true;
            trajectoryCoordinatesStarted = false;
        }
        if (openingSqBracketNumber == 3) {
            trajectoryCoordinatesStarted = true;
        }
    }

    private void readCoordinates(InputStream reader) throws IOException {
        char nextChar = (char) reader.read();
        while (nextChar != ',') {
            if (nextChar >= '0' && nextChar <= '9')
                x.append(nextChar);
            nextChar = (char) reader.read();
        }
        while (nextChar != ')') {
            if (nextChar >= '0' && nextChar <= '9')
                y.append(nextChar);
            nextChar = (char) reader.read();
        }
        processTrajectoryPoint();
    }

    private void readTime(InputStream reader) throws IOException {
        char nextChar = (char) reader.read();
        while (nextChar != ']') {
            while (nextChar != ',' && nextChar != ']') {
                t.append(nextChar);
                nextChar = (char) reader.read();
            }
            if (nextChar == ']') {
                increaseClosingSqBracketsCount();
            }
//            update trajectoryPoint at current index with a timestamp
            trajectoryPoints.get(indexOfT).setTime(Integer.valueOf(t.toString().trim()));
            indexOfT++;
            t = new StringBuilder();
            nextChar = (char) reader.read();
        }
    }

    private void increaseOpeningSqBracketsCount() {
        openingSqBracketNumber++;
        closingSqBracketNumber--;
        processBracketsCount();
    }

    private void increaseClosingSqBracketsCount() {
        openingSqBracketNumber--;
        closingSqBracketNumber++;
        processBracketsCount();
    }

    private void finishProcessingTrajectory() {
        trajectories.add(new Trajectory(trajectoryPoints));
        trajectoryPoints = new ArrayList<>();
        indexOfT = 0;
        trajectoryStarted = false;
        increaseClosingSqBracketsCount();
    }

    private void processTrajectoryPoint() {
        TrajectoryPoint point = new TrajectoryPoint(
                Integer.valueOf(x.toString().trim()),
                Integer.valueOf(y.toString().trim())
        );
        trajectoryPoints.add(point);

        x = new StringBuilder();
        y = new StringBuilder();
    }

}
