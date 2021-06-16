import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.apache.commons.io.FileUtils;
import org.javatuples.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
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
    public ImageView imageContainer2;

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
    public Label nTilesXValue;
    @FXML
    public Label nTilesYValue;

    @FXML
    public Label seekRangeValue;

    @FXML
    public Label GOPValue;

    @FXML
    public Label QualityValue;


    public String pathDir = "output/images/";

    public static Map<String, BufferedImage> imatgesMap = new HashMap<>();
    public ArrayList<BufferedImage> imagesBuffered;
    public ArrayList<BufferedImage> imagesBufferedDecode;
    public ArrayList<BufferedImage> imagesBufferedNoDecode;
    public ArrayList<File> images;
    private ArrayList<ArrayList<Pair<Integer, Integer>>> listTilesInfo;
    private File tileInfo;

    public void getDataFromZip() {
        this.images = new ArrayList<>();
        this.imagesBuffered = new ArrayList<>();
        File dir = new File(pathDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File oGZip = new File(inputTxt.getText());
        try {
            FileInputStream fis = new FileInputStream(inputTxt.getText());
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry zipEntry = zis.getNextEntry();
            //buffer for read and write data to file
            byte[] buffer = new byte[1024];
            long startUnzipping = System.currentTimeMillis();
            long bytes = oGZip.length();
            System.out.println("Input Zip size: " + bytes + " bytes");
            System.out.println("Input Zip size: " + bytes / 1024 + " KB");
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
                    if (newFile.getName().equals("tileInfo.info")) {
                        tileInfo = newFile;
                    } else {
                        Image image = new Image(newFile.toURI().toString());
                        BufferedImage imatgeModi = SwingFXUtils.fromFXImage(image, null); // crea bufferedimage
                        this.imagesBuffered.add(imatgeModi);
                        this.images.add(newFile);
                    }
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
            if (Main.status == 3) {
                this.imagesBufferedNoDecode = this.imagesBuffered;
            }
            System.out.println("Final Unzipping");
            long endUnzipping = System.currentTimeMillis() - startUnzipping;
            System.out.println("Unzipping at: " + endUnzipping + "ms");
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
        File newZip = new File(Main.output + ".zip");
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
            if (Main.status > 0) {
                File aux = new File("titleInfo");
                FileWriter writer = new FileWriter(aux);
                for (ArrayList<Pair<Integer, Integer>> tile : listTilesInfo) {
                    writer.append("(");
                    for (Pair<Integer, Integer> p : tile) {
                        writer.append(String.valueOf(p.getValue0())).append(",").append(String.valueOf(p.getValue1()));
                    }
                    writer.append(")");
                    writer.append("\n");
                }
                writer.flush();
                writer.close();

                FileInputStream fis = new FileInputStream(aux);

                ZipEntry tileInfo = new ZipEntry("tileInfo.info");
                zipOutputStream.putNextEntry(tileInfo);

                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zipOutputStream.write(buffer, 0, len);
                }
            }
            zipOutputStream.finish();
            zipOutputStream.close();
            long endZip = System.currentTimeMillis() - startZip;
            System.out.println("All saved at " + endZip + "ms");
            long bytes = newZip.length();
            System.out.println("Zip saved size: " + bytes + " bytes");
            System.out.println("Zip saved size: " + bytes / 1024 + " KB");

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

                            if (Main.status == 3) {
                                //PONER LAS IMAGENES DEL DECODE!
                                imageContainer.setImage(SwingFXUtils.toFXImage(imagesBufferedNoDecode.get(finalI), null));
                                imageContainer2.setImage(SwingFXUtils.toFXImage(imagesBufferedDecode.get(finalI), null));
                            } else {
                                imageContainer.setImage(SwingFXUtils.toFXImage(imagesBuffered.get(finalI), null));
                            }
                        });
                        try {
                            int pepe = 1000 / Main.FPS;  // Muestra un frame cada pepe ms
                            Thread.sleep(pepe);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (i == imagesBuffered.size() - 1) {
                            i = 0;                          // Fa que no es pari de reproduir
                        }
                    }
                }
            }.start();
        } else {
            System.exit(0);
        }
    }

    public void changeFilterView() {
        encodeCheck.setSelected(Main.hasNegative); //SETEJA TOTES LES COMPONENTS DE LA DRETA
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
        nTilesXValue.setText(String.valueOf(Main.NTILESX));
        nTilesYValue.setText(String.valueOf(Main.NTILESY));
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
        System.out.println("Starting Encoding");
        long startEncode = System.currentTimeMillis();
        int aux = Main.GOP;
        ArrayList<
                ArrayList<
                        Pair<Integer, Integer>
                        >
                > tilesInfo = new ArrayList<>();
        for (int i = 0; i < imagesBuffered.size(); i++) {
            if (aux == Main.GOP) {
                aux = 1;
            } else {
                Pair<BufferedImage, ArrayList<ArrayList<Pair<Integer, Integer>>>> res = Encoder.encode(imagesBuffered.get(i), imagesBuffered.get(i - aux), 0, Main.NTILESX, Main.NTILESY, Main.QUALITY);
                imagesBuffered.set(i, res.getValue0());
                tilesInfo.addAll(res.getValue1());
                aux++;
            }
        }
        //for (int i = 0; i < imagesBuffered.size(); i++) {
        //    imagesBuffered.set(i, filtreAveraging(imagesBuffered.get(i), Main.avaragingValue));
        //}

        listTilesInfo = tilesInfo;
        long endEncode = System.currentTimeMillis() - startEncode;
        System.out.println("Encoded all at " + endEncode + " ms");
    }

    private ArrayList<ArrayList<Pair<Integer, Integer>>> getTileInfoByFile() {
        ArrayList<ArrayList<Pair<Integer, Integer>>> res = new ArrayList<>();
        BufferedReader reader;
        try {
            FileReader f = new FileReader(tileInfo);
            reader = new BufferedReader(f);
            String line = reader.readLine();
            while (line != null) {
                // read next line
                line = reader.readLine();
                if (line != null) {
                    line = line.substring(1, line.length() - 1);
                    String[] datas = line.split(",");
                    ArrayList<Pair<Integer, Integer>> aux = new ArrayList<>();
                    for (int i = 0; i < datas.length; i += 2) {
                        Pair<Integer, Integer> p = Pair.with(Integer.valueOf(datas[i]), Integer.valueOf(datas[i + 1]));
                        aux.add(p);
                    }
                    res.add(aux);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public void doDecode() {
        System.out.println("Starting Decode...");
        long startDecode = System.currentTimeMillis();

        if (!Main.hasEncode) {
            listTilesInfo = getTileInfoByFile();
        }
        int width = imagesBuffered.get(0).getWidth();
        int height = imagesBuffered.get(0).getHeight();
        if (Main.status == 3) {
            imagesBufferedDecode = new ArrayList<>();
            imagesBufferedDecode.add(imagesBuffered.get(0));
        }
        for (int i = 1; i < imagesBuffered.size(); i++) {
            if (i % Main.GOP != 0) {
                BufferedImage newImg = imagesBuffered.get(i);
                for (int k = 0; k < listTilesInfo.get(i / Main.GOP).size(); k++) {
                    for (Pair<Integer, Integer> p : listTilesInfo.get(i / Main.GOP)) {
                        int x = p.getValue0();
                        int y = p.getValue1();
                        int maxX = x + (width / Main.NTILESX);
                        if (maxX > width) {
                            maxX = width;
                        }
                        int maxY = y + (height / Main.NTILESY);
                        if (maxY > height) {
                            maxY = height;
                        }
                        for (int auxX = x; auxX < maxX; auxX++) {
                            for (int auxY = y; auxY < maxY; auxY++) {
                                newImg.setRGB(auxX, auxY, imagesBuffered.get(i).getRGB(auxX, auxY));
                            }
                        }
                    }
                }
                if (Main.status == 3) {
                    imagesBufferedDecode.add(imagesBuffered.get(i));
                } else {
                    imagesBuffered.set(i, newImg);
                }
            } else {
                if (Main.status == 3) {
                    imagesBufferedDecode.add(imagesBuffered.get(i));
                }
            }
        }
        long endDecode = System.currentTimeMillis() - startDecode;
        System.out.println("Decoded all at " + endDecode + " ms");
    }

    /**
     * Una vez selecionada una imagen le añadiremos los filtros dependiendo de cuales tenga.
     * Vamos sobreescribiendo las imagenes post-filtro asi podemos aplicar filtro encima de otros.
     *
     * @param a la imagen que sera modificada
     * @return la imagen ya modificada
     */
    public BufferedImage seleccioFiltres(BufferedImage a) {
        if (Main.hasSaturation) {
            a = filtreSaturacio(a, Main.SATVALUE);
        }
        if (Main.hasAveraging) {
            a = filtreAveraging(a, Main.avaragingValue);
        }
        if (Main.hasEdgeDetection) {
            a = filtreEdgeDetection(a, Main.EDGEDETECTH);
        }
        if (Main.hasBinarisation) {
            a = filtreBinarisation(a, Main.binarisationValue);
        }
        if (Main.hasNegative) {
            a = filtreNegatiu(a);
        }

        return a;
    }

    /**
     * Pasa una imagen por parametro para modificarla y aplicarle un filtro negativo
     *
     * @param imatge Imagen que sera modificada
     * @return imagen ya modificada
     */
    public BufferedImage filtreNegatiu(BufferedImage imatge) {
        for (int y = 0; y < imatge.getHeight(); y++) {
            for (int x = 0; x < imatge.getWidth(); x++) {
                // extreiem els valors alpha, RGB per posterior fer els inversos
                Color colorP = new Color(imatge.getRGB(x, y));
                int red = colorP.getRed();
                int green = colorP.getGreen();
                int blue = colorP.getBlue();
                int a = colorP.getAlpha();

                //li assignem el nou valor al pixel de la imatge
                imatge.setRGB(x, y, (a << 24) | (255 - red << 16) | (255 - green << 8) | 255 - blue);
            }
        }
        return imatge;
    }

    /**
     * Aplica el filtro de la media para hacer que se vea mas suave
     *
     * @param imatge la imagen que sera modificada
     * @param avNum  dimensio a fer la matriu avNum x avNum
     * @return la imagen modificada
     */
    public BufferedImage filtreAveraging(BufferedImage imatge, int avNum) {
        float[] filtmat = new float[avNum*avNum]; // Simularem a una llista una matriu de dimensio avNum x avNum
        Arrays.fill(filtmat, 1.f/(avNum*avNum)); // ompla la llista de 1/la dimensio de la matiu
        ConvolveOp smoth = new ConvolveOp(new Kernel(avNum,avNum, filtmat),ConvolveOp.EDGE_NO_OP,null);
        // creem una buffer image buida
        BufferedImage imatgeResult = new BufferedImage(imatge.getWidth(),imatge.getHeight(),BufferedImage.TYPE_INT_RGB);
        smoth.filter(imatge,imatgeResult); //farem la convolució a través de la matriu i retorna el
                                    // resultat a imatgeResult, que ja és el resultat de la multiplicació de la matriu
                                    // sobre la nostre imatge
        return imatgeResult;
    }


    /**
     * Dependiendo del parametro el pixel serà o negro o blanco
     *
     * @param imatge imagen que se modificara
     * @param binNum threshold
     * @return imagen modificada
     */
    public BufferedImage filtreBinarisation(BufferedImage imatge, int binNum) {
        // Aquesta variable sera la mitja de cada pixel per decidir si és blanc o negre
        double mitja = 0;

        for (int x = 0; x < imatge.getWidth(); x++) {
            for (int y = 0; y < imatge.getHeight(); y++) {
                Color colorP = new Color(imatge.getRGB(x, y)); //extreu el rgb del pixel en el que estem
                int red = colorP.getRed(); //R
                int green = colorP.getGreen(); //G
                int blue = colorP.getBlue(); //B
                mitja = (red + green + blue) / 3; // extreu la mitja dels tres canals del pixel
                if (mitja <= binNum) { // en cas que la mitja sigui menor o igual al threshold:
                    imatge.setRGB(x, y, ((0 << 24) | (0 << 16) | (0 << 8) | (0))); // seteja els pixels amb negre
                } else {
                    imatge.setRGB(x, y, (255 << 24) | (255 << 16) | (255 << 8) | (255));  // seteja els pixels amb blanc
                }
            }
        }
        // retorna la imatge modificada
        return imatge;
    }

    /**
     * Este filtro hace que se marquen los contornos del contenido de la imagen
     *
     * @param a         Imagen que ser modificara
     * @param threshold
     * @return imagen modificada
     */
    public BufferedImage filtreEdgeDetection(BufferedImage a, int threshold) {
        Color white = new Color(255, 255, 255);
        Color black = new Color(0, 0, 0);

        Color pixel1 = null;
        double media1;
        Color pixel2 = null;
        double media2;

        // pasamos pixel por pixel
        for (int y = 0; y < a.getHeight() - 1; y++) {
            for (int x = 0; x < a.getWidth(); x++) {

                // estas variables extraen el color con una altura de diferencia
                pixel1 = new Color(a.getRGB(x, y));
                pixel2 = new Color(a.getRGB(x, y + 1));

                // haremos la suma de los caneles de color i extraemos la media
                media1 = (pixel1.getRed() + pixel1.getGreen() + pixel1.getBlue()) / 3;
                media2 = (pixel2.getRed() + pixel2.getGreen() + pixel2.getBlue()) / 3;

                // Obtenemos la diferencia entre la media sacada anteriormente en valor absoluto y si es menor al
                // threshold el pixel sera blanco sino, sera negro
                if (Math.abs(media1 - media2) < threshold) {
                    a.setRGB(x, y, white.getRGB());
                } else {
                    a.setRGB(x, y, black.getRGB());
                }
            }
        }
        return a;
    }

    /**
     * Modifica la saturacion de la BufferedImage que pasamos por parametro segun el factor de saturacion
     *
     * @param imatge es la imagen que queremos modificar
     * @param s      es el porcentage de saturacion
     * @return la imagen modificada
     */
    public BufferedImage filtreSaturacio(BufferedImage imatge, double s) {
        WritableRaster raster = imatge.getRaster(); //mapa bits del buffer image
        int ancho = imatge.getWidth();
        int altura = imatge.getHeight();

        double[] r = new double[ancho * altura];
        double[] g = new double[ancho * altura];
        double[] b = new double[ancho * altura];

        // Distribuimos los porcentages optimos para el efecto de saturacion
        double rt = 0.3, rg = 0.6, rb = 0.1;

        // llena los r, g, b con el contenido 3 capas de raster rgb
        raster.getSamples(0, 0, ancho, altura, 0, r);
        raster.getSamples(0, 0, ancho, altura, 1, g);
        raster.getSamples(0, 0, ancho, altura, 2, b);

        // pasara por todoo el mapa de bits i calculara el nuevo valor de cada canal
        for (int x = 0; x < (ancho * altura); x++) {
            double rp = r[x];
            double gp = g[x];
            double bp = b[x];
            r[x] = ((1 - s) * rt + s) * rp + ((1 - s) * rg) * gp + ((1 - s) * rb) * bp;
            g[x] = ((1 - s) * rt) * rp + ((1 - s) * rg + s) * gp + ((1 - s) * rb) * bp;
            b[x] = ((1 - s) * rt) * rp + ((1 - s) * rg) * gp + (1 - s) * rb + s * bp;
        }

        // setea los nuevos valores en el mapa de bits
        raster.setSamples(0, 0, ancho, altura, 0, r);
        raster.setSamples(0, 0, ancho, altura, 1, g);
        raster.setSamples(0, 0, ancho, altura, 2, b);

        return imatge;
    }

    private void setFilters() {
        if (Main.hasEdgeDetection || Main.hasSaturation || Main.hasNegative || Main.hasAveraging || Main.hasBinarisation) {
            System.out.println("Starting Filtering...");
            long startFiltering = System.currentTimeMillis();
            for (int i = 0; i < imagesBuffered.size(); i++) {
                this.imagesBuffered.set(i, this.seleccioFiltres(imagesBuffered.get(i)));
            }
            System.out.println("End Filtering");
            long endFiltering = System.currentTimeMillis() - startFiltering;
            System.out.println("Filtering all at " + endFiltering + " ms");
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
            doDecode();
        } else if (Main.status == 3) {  //SI ENCODE SI DECODE
            setFilters();
            doEncode();
            doDecode();
        }
        changeFilterView();
        showImages();
        saveOnZip();
    }
}
