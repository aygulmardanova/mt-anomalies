package visualisation;

import entity.Trajectory;
import entity.TrajectoryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DisplayImage {

    private final static Logger LOGGER = LoggerFactory.getLogger(DisplayImage.class.getName());

    private static int color = Color.RED.getRGB();
    private static int[] clusterColors = {
            Color.BLACK.getRGB(),
            Color.BLUE.getRGB(),
            Color.CYAN.getRGB(),
            Color.DARK_GRAY.getRGB(),
            Color.GRAY.getRGB(),
            Color.GREEN.getRGB(),
            Color.LIGHT_GRAY.getRGB(),
            Color.MAGENTA.getRGB(),
            Color.ORANGE.getRGB(),
            Color.PINK.getRGB(),
            Color.RED.getRGB(),
            Color.WHITE.getRGB(),
            Color.YELLOW.getRGB(),

    };

    public void display(String file, java.util.List<Trajectory> trajectories) throws IOException {
        BufferedImage img = ImageIO.read(new File(file));
        ImageIcon icon = new ImageIcon(img);

        drawTrajectories(img, trajectories);

        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(img.getWidth(), img.getHeight());

        JLabel lbl = new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    /**
     * Draws trajectories on an image
     *
     * @param img output image to display trajectories
     * @param trajectories array of input trajectories
     */
    private void drawTrajectories(BufferedImage img,  java.util.List<Trajectory> trajectories) {
        trajectories.forEach(t ->
                t.getTrajectoryPoints().forEach(tp ->
                        drawBoldTrajectoryPoint(img, tp))
        );
    }

    /**
     * Adds a trajectory point pixel to image with red color.
     * Pixel takes place of 3*3 surrounding pixels
     *
     * @param img output image to add trajectory point on to
     * @param tp trajectory point with pixel coordinates
     */
    private void drawBoldTrajectoryPoint(BufferedImage img, TrajectoryPoint tp) {
        if (tp.getX() + 2 >= img.getWidth() || tp.getY() + 2 >= img.getHeight()) {
            LOGGER.error("Out of image borders: (" + tp.getX() + ", " + tp.getY() + ")");
            return;
        }

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                img.setRGB(tp.getX() + i, tp.getY() + j, color);
            }
        }
    }

}
