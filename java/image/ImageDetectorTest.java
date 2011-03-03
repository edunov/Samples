package image;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

/**
 * @author "Sergey Edunov"
 * @version 2/18/11
 */
public class ImageDetectorTest extends JFrame {

    private JPanel c1 = new JPanel();
    private JPanel c2 = new JPanel();
    private JPanel c3 = new JPanel();
    private BufferedImage i1, i2, i3;
    private int W;
    private int H;

    public static void main(String[] args) throws IOException {
        if (args.length < 1){
            System.out.println("Please provide input image");
            System.exit(0);
        }
        String image = args[0];
        File fImage = new File(image);
        if (!fImage.exists()) {
            System.out.println("Image file " + fImage.getAbsolutePath() + " does not exists");
            System.exit(0);
        }
        BufferedImage bi = ImageIO.read(fImage);


        ColorCannyEdgeDetector cced = new ColorCannyEdgeDetector(bi);
        BufferedImage biCCED = cced.findEdges();

        CannyEdgeDetector ced = new CannyEdgeDetector(bi);
        BufferedImage biCED = ced.findEdges();

        ImageDetectorTest idt = new ImageDetectorTest();
        idt.start(bi, biCED, biCCED);


    }



    public void start(BufferedImage bi, BufferedImage bi2, BufferedImage bi3){
        W = bi.getWidth();
        H = bi.getHeight();
        setLayout(new GridLayout(1, 3));
        setSize(W * 3 + 10, H + 5);
        i1 = bi;
        i2 = bi2;
        i3 = bi3;
        c1.setSize(W, H);
        c2.setSize(W, H);
        c3.setSize(W, H);
        add(c1);
        add(c2);
        add(c3);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

    }

    @Override
    public void paint(Graphics g) {
        c1.getGraphics().drawImage(i1, 0, 0, this);
        c2.getGraphics().drawImage(i2, 0, 0, this);
        c3.getGraphics().drawImage(i3, 0, 0, this);
    }







}
