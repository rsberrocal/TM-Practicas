import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

public class Encoder {
    public final static String BUILDER_FNAME = "builder_skeleton.gz";
    public final static String VIDEO_FNAME = "video.zip";
    public final static String COMPRESSED_FNAME = "compressed.tar";
    private static int blockSize = 8;
    private ArrayList<Byte> builder;
    private ArrayList<BufferedImage> raw;
    private final int cols;
    private final int rows;
    private final float quality;
    private final short gop;
    private final short brick;    // breaking block size
    private final short offset;   // Defines Block neighbourhood to seek/match

    public Encoder(ArrayList video, float quality, short gop, short block_size, short offset) {
        this.raw = video;
        this.quality = quality;
        this.gop = gop;
        this.brick = block_size;
        this.rows = raw.get(0).getWidth() / brick;
        this.cols = raw.get(0).getHeight() / brick;
        this.offset = offset;
    }

    private BufferedImage[] chunkFrame(BufferedImage frame) {
        int chunks = rows * cols;
        int count = 0;

        BufferedImage imgs[] = new BufferedImage[chunks]; //Image array to hold image chunks
        for (int y = 0; y < frame.getHeight(); y += this.brick) {
            for (int x = 0; x < frame.getWidth(); x += this.brick) {
                imgs[count] = frame.getSubimage(x, y, this.brick, this.brick);
                count++;

            }
        }
        return imgs;
    }

    private double compareImages(BufferedImage im1, BufferedImage im2) {
        assert (im1.getHeight() == im2.getHeight() && im1.getWidth() == im2.getWidth());
        double variation = 0.0;
        for (int y = 0; y < im1.getHeight(); y++) {
            for (int x = 0; x < im1.getWidth(); x++) {
                variation += compareARGB(im1.getRGB(x, y), im2.getRGB(x, y)) / Math.sqrt(3);
            }
        }
        return variation / (im1.getWidth() * im1.getHeight());
    }

    private double compareARGB(int rgb1, int rgb2) {
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

    private void serializeShort(short n) {
        this.builder.add((byte) (n & 0xff));
        this.builder.add((byte) ((n >> 8) & 0xff));
    }

    private void serializeCoord(short x, short y) {
        serializeShort(x);
        serializeShort(y);
    }

    public void saveZip(File file) {
        // guardar en zip
    }

    private double meanValue(BufferedImage image) {
        Raster raster = image.getRaster();
        double sum = 0.0;

        for (int y = 0; y < image.getHeight(); ++y)
            for (int x = 0; x < image.getWidth(); ++x)
                sum += raster.getSample(x, y, 0);

        return sum / (image.getWidth() * image.getHeight());
    }

    private boolean templateMatching(int h, BufferedImage template, BufferedImage pframe,
                                     ArrayList<int[]> coords) {

        int xmin, xmax, ymin, ymax;
        ymin = (h % rows) * brick - offset;
        if (ymin < 0) ymin = 0;

        ymax = brick * ((h % rows) + 1) + offset;
        if (ymax > raw.get(0).getWidth()) ymax = raw.get(0).getWidth();

        xmin = (h / rows) * brick - offset;
        if (xmin < 0) xmin = 0;

        xmax = brick * ((h / rows) + 1) + offset;
        if (xmax > raw.get(0).getHeight()) xmax = raw.get(0).getHeight();

        //System.out.println("Brick "+h+": Coordenades: of search"+xmin+" "+xmax+" "+ymin+" "+ymax);

        for (int i = ymin; i <= ymax - brick; i++) {
            for (int j = xmin; j <= xmax - brick; j++) {
                /* matching evaluation */

                double corr = compareImages(pframe.getSubimage(i, j, this.brick, this.brick), template);
                if (corr < this.quality) {
                    //System.out.println("\t\tINDEXED brick @("+i+","+j+") with corr:"+corr );
                    int[] coord = {i, j};
                    coords.add(coord);
                    serializeCoord((short) i, (short) j);
                    serializeShort((short) h);
                    //System.out.println("\t\t(x, y) " + coord[0] + ", " + coord[1] + " / h " + h);
                    return true;
                }
            }
        }
        return false;
    }

    private void setPatchColor(BufferedImage pframe, ArrayList<int[]> coords) {
        int x, y;
        for (int[] coord : coords) {
            x = coord[0];
            y = coord[1];
            // Compute average patch color
            int[] colors = pframe.getRGB(x, y, this.brick, this.brick, null, 0, this.brick);
            int r = 0;
            int b = 0;
            int g = 0;
            for (int c : colors) {
                r += ((c >> 16) & 0xFF);
                g += ((c >> 8) & 0xFF);
                b += (c & 0xFF);
            }
            int R = r / colors.length;
            int G = g / colors.length;
            int B = b / colors.length;
            //System.out.println("\tAVG PATCH RGB: " + R + " , " + G + " , " + B);

            // Set color patch
            int[] rgbArray = new int[(this.brick - 2) * (this.brick - 2)];
            Color c = new Color(R, G, B);
            //Color c = new Color(255,255,255);
            Arrays.fill(rgbArray, c.getRGB());
            pframe.setRGB(++x, ++y, this.brick - 2, this.brick - 2, rgbArray, 0, 0);
        }
    }

////////////////////////////////////////////////////
//               GZIP compression                 //
////////////////////////////////////////////////////

    private boolean compressGzipFile(byte[] data) {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(data.length);
            try {
                GZIPOutputStream gos;
                gos = new GZIPOutputStream(byteStream, false);
                try {
                    gos.write(data, 0, data.length);
                } finally {
                    gos.close();
                }
            } finally {
                byteStream.close();
            }

            byte[] compressedData = byteStream.toByteArray();
            FileOutputStream fileStream = new FileOutputStream(BUILDER_FNAME);
            try {
                fileStream.write(compressedData);
            } finally {
                try {
                    fileStream.close();
                } catch (Exception e) {
                    /* We should probably delete the file now? */
                    return false;
                }
            }
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    private byte[] toByteArray(ArrayList<Byte> in) {
        int n = in.size();
        byte ret[] = new byte[n];
        for (int i = 0; i < n; i++) ret[i] = in.get(i);
        return ret;
    }

    public static void encode(BufferedImage input,int seekRange) {
        System.out.println("Encoding image");
        short matches;
        ArrayList<int[]> coords;
        BufferedImage[] iframe = null;
        DCT dct =  new DCT();// class to do the operation


        int width = input.getWidth();
        int height = input.getHeight();
        // tiles are 8x8
        for (int i = 0; i < height; i += blockSize) {
            for (int j = 0; j < width; j += blockSize) {
                // Create tiles
                BufferedImage tile = input.getSubimage(i, j, blockSize, blockSize);
                int[][] aux = new int[blockSize][blockSize];
                // Get info
                for (int x = 0; x < tile.getWidth(); x++) {
                    for (int y = 0; y < tile.getHeight(); y++) {
                        aux[x][y] = tile.getRGB(x, y);
                    }
                }
                // calculate DCT
                aux = dct.applyDCT(aux);


            }
        }
    }
}
