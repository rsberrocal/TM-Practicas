import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ImagePane extends GridPane implements Initializable {
    @FXML
    public Label inputTxt;

    @FXML
    public ImageView imageContainer;

    @FXML
    public CheckBox encodeCheck;

    @FXML
    public CheckBox decodeCheck;

    @FXML
    public CheckBox negativeCheck;

    @FXML
    public CheckBox binarizationCheck;

    @FXML
    public CheckBox saturationCheck;

    @FXML
    public CheckBox edgeDCheck;

    @FXML
    public CheckBox averagingCheck;

    @FXML
    public Label binaritzationValue;

    @FXML
    public Label saturationValue;

    @FXML
    public Label edgDValue;

    @FXML
    public Label averagingValue;

    @FXML
    public Label fpsValue;

    @FXML
    public Label nTilesValue;

    @FXML
    public Label seekRangeValue;

    @FXML
    public Label GOPValue;

    @FXML
    public Label QualityValue;


    public String pathDir = "output/images/";

    public static Map<String, BufferedImage> imatgesMap = new HashMap<>();
    public ArrayList<BufferedImage> imagesBuffered;
    public ArrayList<File> images;

    public void getDataFromZip() {
        this.images = new ArrayList<>();
        this.imagesBuffered = new ArrayList<>();
        File dir = new File(pathDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            FileInputStream fis = new FileInputStream(inputTxt.getText());
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry zipEntry = zis.getNextEntry();
            //buffer for read and write data to file
            byte[] buffer = new byte[1024];
            System.out.println("Inicio de Unzipping");
            while (zipEntry != null) {
                String name = zipEntry.getName();
                if (!zipEntry.isDirectory()) {
                    //System.out.println("Unzipping " + name);
                    File newFile = new File(pathDir + File.separator + name);
                    //create directories for sub directories in zip
                    new File(newFile.getParent()).mkdirs();

                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    Image image = new Image(newFile.toURI().toString());
                    BufferedImage imatgeModi = SwingFXUtils.fromFXImage(image, null); // crea bufferedimage
                    this.imagesBuffered.add(imatgeModi);
                    this.images.add(newFile);
                } else {
                    System.out.println("Unzipping dir " + name);
                    File newDir = new File(pathDir + File.separator + name);
                    newDir.mkdir();
                }
                //close this ZipEntry
                zis.closeEntry();
                zipEntry = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
            System.out.println("Final Unzipping");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return file name without extension
     *
     * @param file file to get name
     * @return name without extension
     */
    private static String getName(File file) {
        String fileName = "";
        try {
            if (file != null && file.exists()) {// See if file exists
                String name = file.getName();// Get name
                fileName = name.replaceFirst("[.][^.]+$", ""); // Remove the extension
            }
        } catch (Exception e) {
            e.printStackTrace();
            fileName = "";
        }
        return fileName;
    }

    /**
     * Save all the image in a zip
     */
    public void saveOnZip() {
        // Create zip with output
        long startZip = System.currentTimeMillis();
        File newZip = new File(Main.output);
        try {
            FileOutputStream fos = new FileOutputStream(newZip);
            ZipOutputStream zipOutputStream = new ZipOutputStream(fos);

            System.out.println("Saving on zip... " + Main.output);
            int i = 0;
            for (BufferedImage img : imagesBuffered) {
                String name = getName(images.get(i)) + ".jpeg";
                ZipEntry entry = new ZipEntry(name);
                zipOutputStream.putNextEntry(entry);
                ImageIO.write(img, "jpeg", zipOutputStream);
                i++;
            }
            zipOutputStream.finish();
            zipOutputStream.close();
            long endZip = System.currentTimeMillis() - startZip;
            System.out.println("All saved at " + endZip + "ms");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Show all the images on imagesBuffered.
     * If batch mode is selected we just stop the program.
     */
    public void showImages() {
        if (!Main.hasBatch) {
            new Thread() {
                public void run() {
                    // Iterate over all the images and show it
                    for (int i = 0; i < imagesBuffered.size(); i++) {
                        int finalI = i;
                        Platform.runLater(() -> {

                            imageContainer.setImage(SwingFXUtils.toFXImage(imagesBuffered.get(finalI), null));

                        });
                        try {
                            int pepe = 1000 / Main.FPS;
                            Thread.sleep(pepe);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (i == imagesBuffered.size() - 1) {
                            i = 0;                          // aixo fa que no pari
                        }
                    }
                }
            }.start();
        } else {
            System.exit(0);
        }
    }

    public void changeFilterView() {
        encodeCheck.setSelected(Main.hasNegative);
        encodeCheck.setDisable(true);
        decodeCheck.setSelected(Main.hasNegative);
        decodeCheck.setDisable(true);
        negativeCheck.setSelected(Main.hasNegative);
        negativeCheck.setDisable(true);
        binarizationCheck.setSelected(Main.hasBinarisation);
        binarizationCheck.setDisable(true);
        saturationCheck.setSelected(Main.hasSaturation);
        saturationCheck.setDisable(true);
        edgeDCheck.setSelected(Main.hasEdgeDetection);
        edgeDCheck.setDisable(true);
        averagingCheck.setSelected(Main.hasAveraging);
        averagingCheck.setDisable(true);
        nTilesValue.setText(String.valueOf(Main.NTILES));
        seekRangeValue.setText(String.valueOf(Main.SEEKRANGE));
        GOPValue.setText(String.valueOf(Main.GOP));
        QualityValue.setText(String.valueOf(Main.QUALITY));
        binaritzationValue.setText(String.valueOf(Main.binarisationValue));
        saturationValue.setText(String.valueOf(Main.SATVALUE));
        edgDValue.setText(String.valueOf(Main.EDGEDETECTH));
        averagingValue.setText(String.valueOf(Main.avaragingValue));
        fpsValue.setText(String.valueOf(Main.FPS));
        encodeCheck.setSelected(Main.hasEncode);
        decodeCheck.setSelected(Main.hasDecode);
    }

    public void doEncode() {
        long startEncode = System.currentTimeMillis();
        int aux = Main.GOP;
        for (int i = 0; i < imagesBuffered.size(); i++) {
            if (aux == Main.GOP) {
                aux = 1;
            } else {
                imagesBuffered.set(i, Encoder.encode(imagesBuffered.get(i), imagesBuffered.get(i - aux), 0, 8, 8, 80.0));
                aux++;
            }
        }
        long endEncode = System.currentTimeMillis() - startEncode;
        System.out.println("Encoded all at " + endEncode + " ms");
    }

    public void doDecode() {
        long startDecode = System.currentTimeMillis();
        int aux = Main.GOP;
        int num = 0;
        for (int i = 0; i < imagesBuffered.size(); i++) {
            if
            if (aux == Main.GOP) {
                aux = 1;
            } else {
                imagesBuffered.set(i, Encoder.encode(imagesBuffered.get(i), imagesBuffered.get(i - aux), 0, 8, 8, 80.0));
                aux++;
            }
        }
        long endDecode = System.currentTimeMillis() - startDecode;
        System.out.println("Encoded all at " + endDecode + " ms");
    }

    public BufferedImage seleccioFiltres(BufferedImage a) {
        if (Main.hasEdgeDetection) {
            a = filtreEdgeDetection(a, Main.EDGEDETECTH);
        }
        if (Main.hasBinarisation) {
            a = filtreBinarisation(a, Main.binarisationValue);
        }
        if (Main.hasAveraging) {
            a = filtreAveraging(a, Main.avaragingValue);
        }
        if (Main.hasNegative) {
            a = filtreNegatiu(a);
        }
        if (Main.hasSaturation) {
            a = filtresaturacio(a, Main.SATVALUE);
        }

        return a;
    }

    public BufferedImage filtreNegatiu(BufferedImage imatge) {
        WritableRaster raster = imatge.copyData(null);
        ColorModel colorM = imatge.getColorModel();
        boolean alpha = colorM.isAlphaPremultiplied();
        BufferedImage bImage = new BufferedImage(colorM, raster, alpha, null);
        for (int y = 0; y < imatge.getHeight(); y++) {
            for (int x = 0; x < imatge.getWidth(); x++) {
                int pixel = imatge.getRGB(x, y);  // An integer pixel in the default RGB color model and default sRGB colorspace
                int a = (pixel >> 24) & 0xff;
                // RGB
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                // sobrescritura del pixel
                pixel = (a << 24) | (255 - red << 16) | (255 - green << 8) | 255 - blue;
                // i li assignem el nou valor a la imatge
                bImage.setRGB(x, y, pixel);
            }
        }
        return bImage;
    }

    public BufferedImage filtreAveraging(BufferedImage imatge, int avNum) {
        // Mitja R, G, B
        int[] mitjaColor = new int[3];
        // Creació de la tesela a on podrem modificar els pixels
        WritableRaster raster = imatge.copyData(null);
        WritableRaster tesela = raster.createWritableChild(imatge.getMinX(), imatge.getMinY(), imatge.getWidth(), imatge.getHeight(), 0, 0, null);
        for (int x = 0; x < imatge.getWidth() - 1; x++) {
            for (int y = 0; y < imatge.getHeight() - 1; y++) { // x i y de la imatge
                int r = 0;
                int g = 0;
                int b = 0;
                // utilitzem l'average number per la finestra interior
                for (int t = -avNum; t <= avNum; t++) {
                    for (int z = -avNum; z <= avNum; z++) {
                        // t i z serviran de coordenades a dins de la finestra, pero abans hem de comprovar que la finestra no estigui fora del limit imatge
                        if ((y + (t) < imatge.getHeight()) && (x + (z) < imatge.getWidth()) && (y + (t) >= 0 && x + (z) >= 0)) {
                            int pixel = imatge.getRGB(x, y); // An integer pixel in the default RGB color model and default sRGB colorspace
                            // RGB
                            r += (pixel & 0x00ff0000) >> 16;
                            g += (pixel & 0x0000ff00) >> 8;
                            b += pixel & 0x000000ff;
                        }
                    }
                }
                int espai = (avNum - (-avNum) + 1) * (avNum - (-avNum) + 1);
                // For every channel, compute the new pixel value
                mitjaColor[0] = (int) (r / espai);
                mitjaColor[1] = (int) (g / espai);
                mitjaColor[2] = (int) (b / espai);
                // Apliquem els nous colors en el raster
                raster.setPixel(x, y, mitjaColor);
            }
        }
        // un cop creada la imatge, les fem bufferedImage a traves del color model i el alpha i la retornem
        ColorModel colorM = imatge.getColorModel();
        boolean alpha = imatge.isAlphaPremultiplied();
        BufferedImage subImage = new BufferedImage(colorM, tesela, alpha, null);
        return subImage;
    }

    public BufferedImage filtreBinarisation(BufferedImage imatge, int binNum) {
        // Creació de la tesela a on podrem modificar els pixels
        WritableRaster raster = imatge.copyData(null);
        WritableRaster tesela = raster.createWritableChild(imatge.getMinX(), imatge.getMinY(), imatge.getWidth(), imatge.getHeight(), 0, 0, null);

        // RGB per cada dos colors
        int[] black = {0, 0, 0};
        int[] white = {255, 255, 255};
        // Aquesta variable sera la mitja de cada pixel per decidir si és blanc o negre
        double mitja = 0;

        for (int x = 0; x < imatge.getWidth(); x++) {
            for (int y = 0; y < imatge.getHeight(); y++) {
                int pixel = imatge.getRGB(x, y);
                int red = (pixel & 0x00ff0000) >> 16;
                int green = (pixel & 0x0000ff00) >> 8;
                int blue = pixel & 0x000000ff;
                mitja = (red + green + blue) / 3; // extreu la mitja
                if (mitja <= binNum) {
                    raster.setPixel(x, y, black); // seteja els pixels amb negre
                } else {
                    raster.setPixel(x, y, white); // seteja els pixels amb blanc
                }
            }
        }
        //Create the binarized image and return it
        BufferedImage result = new BufferedImage(imatge.getColorModel(), tesela, imatge.isAlphaPremultiplied(), null);
        return result;
    }

    public BufferedImage filtreEdgeDetection(BufferedImage a, int edgeDist) {
        Color white = new Color(255, 255, 255);
        Color black = new Color(0, 0, 0);

        Color topPixel = null;
        Color lowerPixel = null;

        double topIntensity;
        double lowerIntensity;

        for (int y = 0; y < a.getHeight() - 1; y++) {
            for (int x = 0; x < a.getWidth(); x++) {

                topPixel = new Color(a.getRGB(x, y));
                lowerPixel = new Color(a.getRGB(x, y + 1));

                topIntensity = (topPixel.getRed() + topPixel.getGreen() + topPixel.getBlue()) / 3;
                lowerIntensity = (lowerPixel.getRed() + lowerPixel.getGreen() + lowerPixel.getBlue()) / 3;

                if (Math.abs(topIntensity - lowerIntensity) < edgeDist) {
                    a.setRGB(x, y, white.getRGB());
                } else {
                    a.setRGB(x, y, black.getRGB());
                }
            }
        }
        return a;
    }

    public BufferedImage filtresaturacio(BufferedImage imatge, double s) {
        double RW = 0.3086;
        double RG = 0.6084;
        double RB = 0.0820;

        final double a = (1 - s) * RW + s;
        final double b = (1 - s) * RW;
        final double c = (1 - s) * RW;
        final double d = (1 - s) * RG;
        final double e = (1 - s) * RG + s;
        final double f = (1 - s) * RG;
        final double g = (1 - s) * RB;
        final double h = (1 - s) * RB;
        final double i = (1 - s) * RB + s;

        final int width = imatge.getWidth();
        final int height = imatge.getHeight();
        final double[] red = new double[width * height];
        final double[] green = new double[width * height];
        final double[] blue = new double[width * height];

        final WritableRaster raster = imatge.getRaster();
        raster.getSamples(0, 0, width, height, 0, red);
        raster.getSamples(0, 0, width, height, 1, green);
        raster.getSamples(0, 0, width, height, 2, blue);

        for (int x = 0; x < red.length; x++) {
            final double r0 = red[x];
            final double g0 = green[x];
            final double b0 = blue[x];
            red[x] = a * r0 + d * g0 + g * b0;
            green[x] = b * r0 + e * g0 + h * b0;
            blue[x] = c * r0 + f * g0 + i * b0;
        }

        raster.setSamples(0, 0, width, height, 0, red);
        raster.setSamples(0, 0, width, height, 1, green);
        raster.setSamples(0, 0, width, height, 2, blue);

        return imatge;
    }

    private void setFilters() {
        for (int i = 0; i < imagesBuffered.size(); i++) {
            this.imagesBuffered.set(i, this.seleccioFiltres(imagesBuffered.get(i)));
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        inputTxt.setText(Main.input);
        getDataFromZip();

        if (Main.status == 0) {  //NO ENCODE NO DECODE
            setFilters();
        } else if (Main.status == 1) {  //SI ENCODE NO DECODE
            setFilters();
            doEncode();
        } else if (Main.status == 2) {  //NO ENCODE SI DECODE

        } else if (Main.status == 3) {  //SI ENCODE SI DECODE
            setFilters();
            doEncode();
        }
        changeFilterView();
        showImages();
        saveOnZip();
        System.out.println(Main.input);
    }
}
