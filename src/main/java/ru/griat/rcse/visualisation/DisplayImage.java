package ru.griat.rcse.visualisation;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.griat.rcse.entity.Cluster;
import ru.griat.rcse.entity.Trajectory;
import ru.griat.rcse.entity.TrajectoryPoint;
import ru.griat.rcse.misc.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static ru.griat.rcse.misc.Utils.INPUT_IMG_EXTENSION;
import static ru.griat.rcse.misc.Utils.OUTPUT_IMG_DIR;

public class DisplayImage {

    private final static Logger LOGGER = LoggerFactory.getLogger(DisplayImage.class.getName());

    private Integer i = 0;
    private static int color = Color.RED.getRGB();
    private static int[] clusterColors = {
            Color.YELLOW.getRGB(),
            Color.BLUE.getRGB(),
            Color.CYAN.getRGB(),
            Color.PINK.getRGB(),
            Color.RED.getRGB(),
            Color.GREEN.getRGB(),
            Color.WHITE.getRGB(),
            Color.ORANGE.getRGB(),
            Color.MAGENTA.getRGB(),
            Color.LIGHT_GRAY.getRGB(),
            Color.GRAY.getRGB(),
            Color.BLACK.getRGB(),
            Color.DARK_GRAY.getRGB(),
    };

    public void displayAndSaveClusters(String fileName, String subDir, java.util.List<Cluster> clusters) throws IOException {
        BufferedImage img = ImageIO.read(new File(Utils.getFileDir(Utils.INPUT_FILE_DIR, fileName)));

        drawClusters(img, clusters);
        displayImage(img);
        saveImage(fileName, subDir, img);
    }

    public void displayAndSave(String fileName, java.util.List<Trajectory> trajectories) throws IOException {
        BufferedImage img = ImageIO.read(new File(Utils.getFileDir(Utils.INPUT_FILE_DIR, fileName)));

        drawTrajectories(img, trajectories);
        displayImage(img);
//        saveImage(fileName, img);
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
        File output = new File(Utils.getFileDir(
                OUTPUT_IMG_DIR,
                StringUtils.isNotEmpty(subDir) ? subDir + "/res" + fileName : fileName));
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
            LOGGER.error("Out of image borders: (" + tp.getX() + ", " + tp.getY() + ")");
            return;
        }

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                img.setRGB(tp.getX() + i, tp.getY() + j, clusterColors[this.i]);
            }
        }
    }

}
