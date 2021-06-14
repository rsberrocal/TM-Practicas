import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ImagePane extends GridPane implements Initializable {
    @FXML
    public Label inputTxt;

    @FXML
    public ImageView imageContainer;

    @FXML
    public CheckBox negativeCheck;

    @FXML
    public CheckBox saturationCheck;

    @FXML
    public Label binaritzationValue;

    @FXML
    public Label averagingValue;

    @FXML
    public Label edgDValue;

    @FXML
    public Label fpsValue;

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

    public void saveOnZip() {
        // FileOutputStream fos = new FileOutputStream("");
    }

    public void showImages() {
        if(!Main.hasBatch){
            new Thread() {
                public void run() {
                    changeFilterView();
                    for (int i = 0; i<imagesBuffered.size();i++) {
                        int finalI = i;
                        Platform.runLater(() -> {

                            imageContainer.setImage(SwingFXUtils.toFXImage(imagesBuffered.get(finalI), null ));

                        });
                        try {
                            int pepe = 1000 / Main.FPS;
                            Thread.sleep(pepe);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (i==imagesBuffered.size()-1){
                            i = 0;                          // aixo fa que no pari
                        }
                    }
                }
            }.start();
            // System.out.println(images.get(0).getName());
        }else{
            System.exit(0);
        }
    }

    public void changeFilterView() {
        negativeCheck.setSelected(Main.hasNegative);
        negativeCheck.setDisable(true);
        saturationCheck.setSelected(Main.hasSaturation);
        saturationCheck.setDisable(true);
        fpsValue.setText(String.valueOf(Main.FPS));
        edgDValue.setText(String.valueOf(Main.EDGEDETECTH));
        binaritzationValue.setText(String.valueOf(Main.binarisationValue));
        averagingValue.setText(String.valueOf(Main.avaragingValue));
    }

    public void doEncode() {
        // 1. read images is done before
        // 2. make filters
        // 3. convert images to JPEG if they are not,
        for (BufferedImage img : imagesBuffered) {
            //Image image = new Image(img.toURI().toString());
            //BufferedImage imatgeModi = SwingFXUtils.fromFXImage(image, null); // crea bufferedimage
            // need to encodeEncoder.
            // encode ( img)
        }

    }

    public BufferedImage seleccioFiltres(BufferedImage a) {
        if(Main.hasEdgeDetection){
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
        }if(Main.hasSaturation){
            //a = filtreSaturation(a);
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
        Color white = new Color(255,255,255);
        Color black = new Color(0,0,0);

        Color topPixel = null;
        Color lowerPixel = null;

        double topIntensity;
        double lowerIntensity;

        for(int y = 0; y < a.getHeight()-1; y++){
            for(int x = 0; x < a.getWidth(); x++){

                topPixel = new Color(a.getRGB(x,y));
                lowerPixel = new Color(a.getRGB(x,y+1));

                topIntensity =  (topPixel.getRed() + topPixel.getGreen() + topPixel.getBlue()) / 3;
                lowerIntensity =  (lowerPixel.getRed() + lowerPixel.getGreen() + lowerPixel.getBlue()) / 3;

                if(Math.abs(topIntensity - lowerIntensity) < edgeDist){
                    a.setRGB(x,y, white.getRGB());
                }else{
                    a.setRGB(x,y, black.getRGB());
                }
            }
        }
        return a;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        inputTxt.setText(Main.input);
        getDataFromZip();

        if (Main.status == 0){  //NO ENCODE NO DECODE
            for(int i = 0; i< imagesBuffered.size();i++){
                this.imagesBuffered.set(i,this.seleccioFiltres(imagesBuffered.get(i)));
            }
        }else if(Main.status == 1){  //SI ENCODE NO DECODE

        }else if(Main.status == 2){  //NO ENCODE SI DECODE

        }else if(Main.status == 3){  //SI ENCODE SI DECODE

        }

        showImages();
        System.out.println(Main.input);
    }
}
