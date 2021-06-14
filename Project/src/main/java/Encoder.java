import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import org.javatuples.Triplet;

public class Encoder {

    public Encoder() {
    }

    private static ArrayList<Triplet<BufferedImage, Integer, Integer>> getTiles(BufferedImage image, int blockSizeX, int blockSizeY) {
        int tiles = (image.getHeight() / blockSizeY) * (image.getWidth() / blockSizeX); // get num of tiles
        int count = 0;
        ArrayList<Triplet<BufferedImage, Integer, Integer>> res = new ArrayList<>();
        for (int y = 0; y < image.getHeight(); y += blockSizeY) {
            for (int x = 0; x < image.getWidth(); x += blockSizeX) {
                // Get every tile and saved it as bufferedImage
                res.add(Triplet.with(image.getSubimage(x, y, blockSizeX, blockSizeY), x, y));
            }
        }
        return res;
    }

    private static double compareImages(BufferedImage im1, BufferedImage im2) {
        assert (im1.getHeight() == im2.getHeight() && im1.getWidth() == im2.getWidth());
        double variation = 0.0;
        for (int y = 0; y < im1.getHeight(); y++) {
            for (int x = 0; x < im1.getWidth(); x++) {
                variation += compareARGB(im1.getRGB(x, y), im2.getRGB(x, y)) / Math.sqrt(3);
            }
        }
        return variation / (im1.getWidth() * im1.getHeight());
    }

    private static double compareARGB(int rgb1, int rgb2) {
        double r1 = ((rgb1 >> 16) & 0xFF) / 255.0;
        double r2 = ((rgb2 >> 16) & 0xFF) / 255.0;
        double g1 = ((rgb1 >> 8) & 0xFF) / 255.0;
        double g2 = ((rgb2 >> 8) & 0xFF) / 255.0;
        double b1 = (rgb1 & 0xFF) / 255.0;
        double b2 = (rgb2 & 0xFF) / 255.0;
        double a1 = ((rgb1 >> 24) & 0xFF) / 255.0;
        double a2 = ((rgb2 >> 24) & 0xFF) / 255.0;

        // if there is transparency, the alpha values will make difference smaller
        return a1 * a2 * Math.sqrt((r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2));
    }

    private static Color meanValue(BufferedImage image) {
        int r = 0, g = 0, b = 0;
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                Color pix = new Color(image.getRGB(i, j));
                r += pix.getRed();
                g += pix.getGreen();
                b += pix.getBlue();
            }
        }
        r = r / (image.getWidth() * image.getHeight());
        g = g / (image.getWidth() * image.getHeight());
        b = b / (image.getWidth() * image.getHeight());

        return new Color(r, g, b);
    }

    public static BufferedImage encode(BufferedImage input, BufferedImage imageCompare, int seekRange, int blockSizeX, int blockSizeY, double quality) {
        System.out.println("Encoding image");
        boolean found = false;
        ArrayList<Triplet<BufferedImage, Integer, Integer>> tilesInput = getTiles(input, blockSizeX, blockSizeY);
        ArrayList<Triplet<BufferedImage, Integer, Integer>> tilesCompare = getTiles(imageCompare, blockSizeX, blockSizeY);

        BufferedImage result = input;

        for (Triplet<BufferedImage, Integer, Integer> tileInput : tilesInput) {
            for (Triplet<BufferedImage, Integer, Integer> tileCompare : tilesCompare) {
                double comp = compareImages(tileInput.getValue0(), tileCompare.getValue0());
                if (compareImages(tileInput.getValue0(), tileCompare.getValue0()) < quality) {
                    for (int i = tileInput.getValue1(); i < tileInput.getValue1() + blockSizeX; i++){
                        for (int j = tileInput.getValue2(); i < tileInput.getValue2() + blockSizeY; i++){
                            result.setRGB(i, j, meanValue(tileInput.getValue0()).getRGB());
                        }
                    }
                    found = true;
                    break;
                }
            }
            if (found){
                break;
            }
        }

        return result;
    }
}
