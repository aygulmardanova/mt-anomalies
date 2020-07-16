package ru.griat.rcse.csv;

import ru.griat.rcse.misc.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CSVProcessing {

    private final static String CSV_DELIMITER = ";";

    public void writeCSV(Double[][] trajLCSSDistances, int startI, int startJ, int endI, int endJ, String experimentId, String fileName) throws IOException {
        FileWriter csvWriter = new FileWriter(Utils.getCsvDir(experimentId, fileName));

        for (int i = startI; i <= endI; i++) {
            for (int j = startJ; j <= endJ; j++) {
                csvWriter.append(String.join(CSV_DELIMITER, String.valueOf(i), String.valueOf(j), trajLCSSDistances[i][j]+ "\n"));
            }
        }
        csvWriter.flush();
        csvWriter.close();
    }

    public void readCSV(Double[][] trajLCSSDistances, String experimentId, String fileName) throws IOException {
        String csvPath = Utils.getCsvDir(experimentId, fileName);
        String row;
        File csvFile = new File(csvPath);
        if (csvFile.isFile()) {
            BufferedReader csvReader = new BufferedReader(new FileReader(csvPath));
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(CSV_DELIMITER);
                trajLCSSDistances[Integer.parseInt(data[0])][Integer.parseInt(data[1])] = Double.valueOf(data[2]);
            }
            csvReader.close();
        }
    }

}
