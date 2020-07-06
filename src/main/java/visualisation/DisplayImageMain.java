package visualisation;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static misc.Constants.FILE_DIR;
import static misc.Constants.IMG_FILE_NAMES;

public class DisplayImageMain {

    public static void main(String[] args) throws IOException {

        BufferedImage img = ImageIO.read(new File(FILE_DIR + IMG_FILE_NAMES[0]));
        ImageIcon icon = new ImageIcon(img);
        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(img.getWidth(), img.getHeight());
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}
