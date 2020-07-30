package ru.griat.rcse.visualisation;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.griat.rcse.entity.Cluster;
import ru.griat.rcse.entity.Trajectory;
import ru.griat.rcse.entity.TrajectoryPoint;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static ru.griat.rcse.misc.Utils.INPUT_FILE_DIR;
import static ru.griat.rcse.misc.Utils.INPUT_IMG_EXTENSION;
import static ru.griat.rcse.misc.Utils.OUTPUT_IMG_DIR;
import static ru.griat.rcse.misc.Utils.getFileDir;

public class DisplayImage {

    private final static Logger LOGGER = LoggerFactory.getLogger(DisplayImage.class.getName());

    private Integer i = 0;
    private static int color = Color.RED.getRGB();
    private static int[] clusterColors = {
            Color.RED.getRGB(),
            Color.BLUE.getRGB(),
            Color.CYAN.getRGB(),
            Color.PINK.getRGB(),
            Color.YELLOW.getRGB(),
            Color.GREEN.getRGB(),
            Color.WHITE.getRGB(),
            Color.ORANGE.getRGB(),
            Color.MAGENTA.getRGB(),
            Color.LIGHT_GRAY.getRGB(),
            Color.GRAY.getRGB(),
            Color.BLACK.getRGB(),
            Color.DARK_GRAY.getRGB(),
    };

    public void displayAndSaveClusters(String inputFileName, String outputFileName, String subDir,
                                       java.util.List<Cluster> clusters, boolean save) throws IOException {
        BufferedImage img = ImageIO.read(new File(getFileDir(INPUT_FILE_DIR, inputFileName)));

        drawClusters(img, clusters);
        displayImage(img);
        if (save) saveImage(outputFileName, subDir, img);
    }

    public void displayAndSave(String inputFileName, String outputFileName, String subDir,
                               java.util.List<Trajectory> trajectories, boolean save) throws IOException {
        BufferedImage img = ImageIO.read(new File(getFileDir(INPUT_FILE_DIR, inputFileName)));

        drawTrajectories(img, trajectories);
        displayImage(img);
        if (save)
            saveImage(StringUtils.isNotEmpty(outputFileName) ? outputFileName : inputFileName, subDir, img);
    }

    public void displayAndSave(String fileName, TrajectoryPoint point, boolean save) throws IOException {
        BufferedImage img = ImageIO.read(new File(getFileDir(INPUT_FILE_DIR, fileName)));

        drawBoldTrajectoryPoint(img, point);
        displayImage(img);
        if (save)
            saveImage(fileName, null, img);
    }

    private void displayImage(BufferedImage img) {
        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(img.getWidth(), img.getHeight());

        ImageIcon icon = new ImageIcon(img);

        JLabel lbl = new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    private void saveImage(String fileName, String subDir, BufferedImage img) throws IOException {
        File output = new File(getFileDir(
                OUTPUT_IMG_DIR,
                StringUtils.isNotEmpty(subDir) ? subDir + "/" + fileName : fileName));
        ImageIO.write(img, INPUT_IMG_EXTENSION, output);
    }

    /**
     * Draws trajectories on an image
     *
     * @param img          output image to display trajectories
     * @param trajectories array of input trajectories
     */
    private void drawTrajectories(BufferedImage img, java.util.List<Trajectory> trajectories) {
        trajectories.forEach(t -> {
                    t.getTrajectoryPoints().forEach(tp ->
                            drawBoldTrajectoryPoint(img, tp));
                    increaseI();
                }
        );
        this.i = 0;
        trajectories.forEach(t -> {
            t.getKeyPoints().forEach(kp -> {
                drawExtraBoldTrajectoryPoint(img, kp);
            });
            increaseI();
        });
    }

    /**
     * Draws trajectories on an image
     *
     * @param img      output image to display clustered trajectories
     * @param clusters array of input clusters
     */
    private void drawClusters(BufferedImage img, java.util.List<Cluster> clusters) {
        clusters.forEach(c -> {
            c.getTrajectories().forEach(t -> {
                t.getTrajectoryPoints().forEach(tp ->
                        drawBoldTrajectoryPoint(img, tp));
            });
            c.getTrajectories().forEach(t -> {
                t.getKeyPoints().forEach(kp -> {
                    drawExtraBoldTrajectoryPoint(img, kp);
                });
            });
            increaseI();
        });
    }

    private void increaseI() {
        if (this.i < clusterColors.length - 1)
            this.i++;
        else
            this.i = 0;
    }

    /**
     * Adds a trajectory point pixel to image with red color.
     * Pixel takes place of 3*3 surrounding pixels
     *
     * @param img output image to add trajectory point on to
     * @param tp  trajectory point with pixel coordinates
     */
    private void drawBoldTrajectoryPoint(BufferedImage img, TrajectoryPoint tp) {
        if (tp.getX() + 2 >= img.getWidth() || tp.getY() + 2 >= img.getHeight()) {
//            LOGGER.error("Out of image borders: (" + tp.getX() + ", " + tp.getY() + ")");
            return;
        }

        for (int i = -1; i < 1; i++) {
            for (int j = -1; j < 1; j++) {
                img.setRGB(tp.getX() + i, tp.getY() + j, clusterColors[this.i]);
            }
        }
    }

    private void drawExtraBoldTrajectoryPoint(BufferedImage img, TrajectoryPoint tp) {
        if (tp.getX() + 3 >= img.getWidth() || tp.getY() + 3 >= img.getHeight()) {
            drawBoldTrajectoryPoint(img, tp);
//            LOGGER.error("Out of image borders: (" + tp.getX() + ", " + tp.getY() + ")");
            return;
        }
        for (int i = -2; i < 3; i++) {
            for (int j = -2; j < 3; j++) {
                try {
                    img.setRGB(tp.getX() + i, tp.getY() + j, clusterColors[this.i]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println(e);
                }
            }
        }
    }

}
