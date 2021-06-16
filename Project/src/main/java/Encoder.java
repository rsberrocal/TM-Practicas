import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import org.javatuples.Pair;
import org.javatuples.Triplet;

public class Encoder {

    /**
     * Genera las teselas de una imagen
     * @param image Imagen a teselar
     * @param blockSizeX Mida en los ejes de las X
     * @param blockSizeY Mida en los ejes de las Y
     * @return Devuelve una lista con una tupla de 3 donde tenemos la imagen teselada y las coords en X, Y de donde empieza
     */
    private static ArrayList<Triplet<BufferedImage, Integer, Integer>> getTiles(BufferedImage image, int blockSizeX, int blockSizeY) {
        ArrayList<Triplet<BufferedImage, Integer, Integer>> res = new ArrayList<>(); // Lista donde estara el resultado
        // Recorremos todas las teselas de la imagen
        for (int y = 0; y < image.getHeight(); y += blockSizeY) {
            for (int x = 0; x < image.getWidth(); x += blockSizeX) {
                // Añadimos una subimagen y sus coords
                res.add(Triplet.with(image.getSubimage(x, y, blockSizeX, blockSizeY), x, y));
            }
        }
        return res;
    }

    /**
     * Devuelve el valor de comparacion entre dos imagenes
     * @param im1 Imagen de origen
     * @param im2 Imagen de destino
     * @return
     */
    private static double compareImages(BufferedImage im1, BufferedImage im2) {
        // Primero mira si coinciden de tamaño
        assert (im1.getHeight() == im2.getHeight() && im1.getWidth() == im2.getWidth());
        double variation = 0.0;
        // Recorremos todos los pixeles de la imagen de origen
        for (int y = 0; y < im1.getHeight(); y++) {
            for (int x = 0; x < im1.getWidth(); x++) {
                // Comparamos cada pixel de la imagen 1 con la imagen 2
                variation += compareARGB(im1.getRGB(x, y), im2.getRGB(x, y)) / Math.sqrt(3);
            }
        }
        return variation / (im1.getWidth() * im1.getHeight());
    }

    /**
     * Compara los valores ARGB de dos pixeles
     * @param rgb1 Pixel origen
     * @param rgb2 Pixel destino
     * @return
     */
    private static double compareARGB(int rgb1, int rgb2) {
        double r1 = ((rgb1 >> 16) & 0xFF) / 255.0;
        double r2 = ((rgb2 >> 16) & 0xFF) / 255.0;
        double g1 = ((rgb1 >> 8) & 0xFF) / 255.0;
        double g2 = ((rgb2 >> 8) & 0xFF) / 255.0;
        double b1 = (rgb1 & 0xFF) / 255.0;
        double b2 = (rgb2 & 0xFF) / 255.0;
        double a1 = ((rgb1 >> 24) & 0xFF) / 255.0;
        double a2 = ((rgb2 >> 24) & 0xFF) / 255.0;

        // Si hay transparencia, los valores alpha haran una diferencia, en caso de tener un png
        return a1 * a2 * Math.sqrt((r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2));
    }

    /**
     * A partir de una imagen, recorre todos los pixeles y calcula su media
     * @param image
     * @return
     */
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

    /**
     * Encodea la imagen original
     * @param input imagen original
     * @param imageCompare imagen a comparar
     * @param seekRange rango
     * @param blockSizeX tileX
     * @param blockSizeY tileY
     * @param quality Calidad
     * @return imagen encodeada
     */
    public static Pair<BufferedImage, ArrayList<ArrayList<Pair<Integer, Integer>>>> encode(BufferedImage input, BufferedImage imageCompare, int seekRange, int blockSizeX, int blockSizeY, double quality) {
        // Flag para marcar el match
        boolean found = false;
        // Lista de tiles
        ArrayList<Triplet<BufferedImage, Integer, Integer>> tilesInput = getTiles(input, blockSizeX, blockSizeY);
        ArrayList<Triplet<BufferedImage, Integer, Integer>> tilesCompare = getTiles(imageCompare, blockSizeX, blockSizeY);
        // Lista con la informacion de los tiles
        ArrayList<ArrayList<Pair<Integer, Integer>>> tileInfo = new ArrayList<>();
        // Resultado
        BufferedImage result = input;
        // Iteramos todos los tiles de las dos imagenes
        for (Triplet<BufferedImage, Integer, Integer> tileInput : tilesInput) {
            ArrayList<Pair<Integer, Integer>> tilesValues = new ArrayList<>();
            for (Triplet<BufferedImage, Integer, Integer> tileCompare : tilesCompare) {
                // Si la comparacion es menor que la calidad, tenemos un match
                if (compareImages(tileInput.getValue0(), tileCompare.getValue0()) < quality) {
                    // Iteramos por todo el tile para poner el valor medio
                    int val = meanValue(tileInput.getValue0()).getRGB();
                    for (int i = tileInput.getValue1(); i < tileInput.getValue1() + blockSizeX; i++) {
                        for (int j = tileInput.getValue2(); i < tileInput.getValue2() + blockSizeY; i++) {
                            result.setRGB(i, j, val);
                        }
                    }
                    found = true;
                    // Nos guardamos la informacion de la tile para luego hacer decode
                    Pair<Integer, Integer> pair = Pair.with(tileInput.getValue1(), tileInput.getValue2());
                    tilesValues.add(pair);
                    break;// salimos del bucle
                }
            }
            //Añadimos la info
            tileInfo.add(tilesValues);
            // Con estas 3 lineas va el triple de rapido
            /*if (found){
                break;
            }*/
        }
        // Devolvemos una tupla con la imagen y con la informacion del tile
        return Pair.with(result, tileInfo);
    }
}
