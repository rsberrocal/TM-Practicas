import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main extends Application {

    public static String input;
    public static int binarisationValue = 0;
    public static int avaragingValue = 0;
    public List<String> params;

    /*
        0 -> no encode, no decode
        1 -> encode, no decode
        2 -> no encode, decode
        3 -> encode, decode
     */
    public static int status = 0;
    public static boolean hasInput = false;
    public static boolean hasEncode = false;
    public static boolean hasDecode = false;
    public static boolean hasBinarisation = false;
    public static boolean hasNegative = false;
    public static boolean hasAveraging = false;
    public static boolean hasEdgeDetection = false;
    public static boolean hasSaturation = false;
    public static boolean hasBatch = false;
    public static String output = "output";

    public static int FPS = 24;
    public static int EDGEDETECTH = 0;
    public static double SATVALUE = 0;
    public static int NTILESX = 8;
    public static int NTILESY = 8;
    public static int SEEKRANGE = 0;
    public static int GOP = 2;
    public static double QUALITY = 0.4;

    final static int WIDTH = 600;
    final static int HEIGHT = 500;

    public void showHelp() {
        System.out.println("Lista de parametros a usar:");
        System.out.println("-i, --input {path}*");
        System.out.println("El zip a usar para generar el resultado. Es obligatorio incluir un input.");
        System.out.println("Es importante que sea un zip");
        System.out.println("-o, --output {path}");
        System.out.println("Nombre donde se va a guardar el archivo generado");
        System.out.println("--encode");
        System.out.println("Se aplicara la codificacion sobre el conjunto de imagenes obtenido como input");
        System.out.println("-d, --decode");
        System.out.println("Se aplicara la decodificacion sobre el conjunto de imagenes obtenido como input");
        System.out.println("--fps {value}");
        System.out.println("Numero de imagenes por segundos con los que se reproducira el video");
        System.out.println("--binarization {value}");
        System.out.println("Filtro de binarizacion utilizando como threshold el valor indicado");
        System.out.println("--negative");
        System.out.println("Se aplicara el filtro negativo sobre la imagen");
        System.out.println("--averaging {value}");
        System.out.println("Se aplica un filtro de mediana sobre zonas de value x value");
        System.out.println("--edgeDetection {value}");
        System.out.println("Se aplica un filtro de detección de esquinas con un threshold (value)");
        System.out.println("--saturacion {value}");
        System.out.println("Se aplica un filtro de saturación, value entre 0 i 1");
        System.out.println("--nTiles {value | valueX,valueY}");
        System.out.println("Tamaño de las teselas, si se pone solo un valor sera el mismo para X, Y");
        System.out.println("--seekRange {value}");
        System.out.println("Desplazamiento maximo en la busqueda de coincidencias");
        System.out.println("--GOP {value}");
        System.out.println("Numero de imagenes entre dos frames de referencia");
        System.out.println("--quality {value}");
        System.out.println("Factor de calidad que determina la coincidencia de dos teselas");
        System.out.println("-b --batch");
        System.out.println("No se mostrara la reproduccion del resultado.");
        System.exit(1);
    }

    public void setInput() {
        // Calculamos el indice del parametro para encontrarlo
        int index = params.indexOf("-i");
        // En caso de que no este, buscamos en su alternativa
        if (index == -1) {
            index = params.indexOf("--input");
        }
        // Si no esta, mandamos error
        if (index == -1) {
            printError("--input es un parametro necesario.");
        }
        // Buscamos el parametro
        int dataIdx = index + 1;
        if (dataIdx > params.size() - 1) {
            printError("No se ha encotrado el parametro");
        }
        // Miramos si el parametro es correcto
        if (!params.get(dataIdx).contains("-") || !params.get(dataIdx).contains("--")) {
            // Escogemos el valor
            String data = params.get(dataIdx);
            input = params.get(dataIdx);
            hasInput = true;
            convertFormat();
            input = "raw.zip";


        } else {
            printError("El parametro no es correcto");
        }
    }

    public static void convertFormat() {
        ArrayList<String> nombres = new ArrayList<>();
        //ImageIO.write(img, "jpeg", zipOutputStream);
        //CREAR ZIP CON JPEGS DENTRO PNG -> JPEG
        //abrir el zip input,
        ArrayList<BufferedImage> tmp = new ArrayList<>();
        File dir = new File("output/raw/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File oGZip = new File(input);
        try {
            FileInputStream fis = new FileInputStream(input);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry zipEntry = zis.getNextEntry();
            byte[] buffer = new byte[1024];
            while (zipEntry != null) {
                String name = zipEntry.getName();
                if (!zipEntry.isDirectory()) {
                    File newFile = new File("output/raw/" + File.separator + name);
                    new File(newFile.getParent()).mkdirs();
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    Image image = new Image(newFile.toURI().toString());
                    BufferedImage imatgeModi = SwingFXUtils.fromFXImage(image, null);
                    tmp.add(imatgeModi);
                    nombres.add(newFile.getName());

                }
                zis.closeEntry();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            fis.close();

            File newZip = new File("raw.zip");

            FileOutputStream fos = new FileOutputStream(newZip);
            ZipOutputStream zipOutputStream = new ZipOutputStream(fos);

            System.out.println("Saving on zip... raw.zip");
            for (int i = 0; i < tmp.size(); i++) {
                if (i == tmp.size() -1){
                    System.out.println("asda");
                }
                String name = getName(nombres.get(i)) + ".jpeg";
                ZipEntry entry = new ZipEntry(name);
                zipOutputStream.putNextEntry(entry);
                System.out.println(name + " img");
                ImageIO.write(tmp.get(i), "jpeg", zipOutputStream);
            }
            /* Se finaliza el stream y cierra el fichero zip */
            zipOutputStream.finish();
            zipOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String getName(String file) {
        String fileName = "";
        fileName = file.replaceFirst("[.][^.]+$", ""); // Remove the extension

        return fileName;
    }

    /**
     * Comprueba si es un zip
     *
     * @param zip archivo a mirar
     * @return true al ser zip, false al no serlo
     */
    private boolean isZip(String zip) {
        Pattern pattern = Pattern.compile(".*\\.zip$");
        return pattern.matcher(zip).matches();
    }

    /**
     * Imprime un error por consola y detiene la ejecucion
     *
     * @param error Mensaje de error
     */
    public void printError(String error) {
        System.err.println(error);
        System.out.println("Use -h o --help para obtener mas informacion");
        System.exit(1);
    }

    /**
     * Comprueba y pone el output del usuario
     */
    public void setOutput() {
        // Calculamos el indice del parametro para encontrarlo
        int index = params.indexOf("-o");
        // En caso de que no este, buscamos en su alternativa
        if (index == -1) {
            index = params.indexOf("--output");
        }
        // Buscamos el parametro
        int dataIdx = index + 1;
        if (dataIdx > params.size() - 1) {
            printError("No se ha encotrado el parametro");
        } else {
            output = params.get(dataIdx);
        }
    }

    /**
     * Comprueba y setea el mode Encode
     */
    public void setEncode() {
        // Calculamos el indice del parametro para encontrarlo
        int index = params.indexOf("-e");
        // En caso de que no este, buscamos en su alternativa
        if (index == -1) {
            index = params.indexOf("--encode");
        }

        if (index == -1) {
            printError("No se ha encotrado el parametro");
        } else {
            hasEncode = true;
        }
    }

    /**
     * Comprueba y setea el mode Decode
     */
    public void setDecode() {
        // Calculamos el indice del parametro para encontrarlo
        int index = params.indexOf("-d");
        // En caso de que no este, buscamos en su alternativa
        if (index == -1) {
            index = params.indexOf("--decode");
        }
        // Si no existe, damos error
        if (index == -1) {
            printError("No se ha encotrado el parametro");
        } else {
            hasDecode = true;
        }
    }

    /**
     * @param strNum Numero a comprobar
     * @return True si es numero, False sino
     */
    public boolean isNumeric(String strNum) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }

    /**
     * Comprueba y setea el numero de FPS
     */
    public void setFPS() {
        // Calculamos el indice del parametro para encontrarlo
        int index = params.indexOf("--fps");
        if (index == -1) {
            printError("No se ha encotrado el parametro");
        } else {
            // Buscamos el parametro
            int dataIdx = index + 1;
            if (dataIdx > params.size() - 1) {
                printError("No se ha encotrado el parametro");
            } else {
                // Miramos si es un int
                String frames = params.get(dataIdx);
                if (isNumeric(frames)) {
                    FPS = Integer.parseInt(frames);
                    System.out.println("Fps maximos: " + FPS);
                } else {
                    printError("Parametro erroneo");
                }
            }
        }
    }

    /**
     * Comprueba y setea el valor del filtro
     */
    public void setBinarization() {
        // --binarization {value}
        // Filtro de binarizacion utilizando como threshold el valor indicado
        int index = params.indexOf("--binarization");

        if (index == -1) {
            printError("--binarization es un parametro necesario.");
        }
        int dataIdx = index + 1;
        if (dataIdx > params.size() - 1) {
            printError("No se ha encotrado el parametro");
        }
        System.out.println();
        // Mirar si es un zip o no
        if (params.get(dataIdx).contains("-") || params.get(dataIdx).contains("--")) {
            printError("El parametro no es correcto");
        } else {
            binarisationValue = Integer.parseInt(params.get(dataIdx));
            hasBinarisation = true;
            System.out.println("Se esta aplicando el filtro Binarisation");
        }
    }

    /**
     * setesa si se aplica el filtro
     */
    public void setNegative() {
        this.hasNegative = true;
        System.out.println("Se esta aplicando el filtro Negativo");
    }

    /**
     * Comprueba y setea el valor del filtro
     */
    public void setAveraging() {
        // --averaging {value}
        // Se aplica un filtro de mediana sobre zonas de value x value
        int index = params.indexOf("--averaging");

        if (index == -1) {
            printError("--averaging es un parametro necesario.");
        }
        int dataIdx = index + 1;
        if (dataIdx > params.size() - 1) {
            printError("No se ha encotrado el parametro");
        }
        System.out.println();
        // Mirar si es un zip o no
        if (params.get(dataIdx).contains("-") || params.get(dataIdx).contains("--")) {
            printError("El parametro no es correcto");
        } else {
            avaragingValue = Integer.parseInt(params.get(dataIdx));
            hasAveraging = true;
            System.out.println("Se esta aplicando el filtro Averaging");
        }
    }

    /**
     * Comprueba y setea el valor del filtro
     */
    private void setEdgeDetection() {
        int index = params.indexOf("--edgeDetection");
        if (index == -1) {
            printError("No se ha encotrado el parametro");
        } else {
            // Buscamos el parametro
            int dataIdx = index + 1;
            if (dataIdx > params.size() - 1) {
                printError("No se ha encotrado el parametro");
            } else {
                String threshold = params.get(dataIdx);
                if (isNumeric(threshold)) {
                    EDGEDETECTH = Integer.parseInt(threshold);
                    hasEdgeDetection = true;
                    System.out.println("Se esta aplicando el filtro de detección de esquinas");
                } else {
                    printError("Parametro erroneo");

                }
            }
        }
    }

    /**
     * Comprueba y setea el valor del filtro
     */
    private void setSaturation() {
        int index = params.indexOf("--saturacion");
        if (index == -1) {
            printError("No se ha encotrado el parametro");
        } else {
            // Buscamos el parametro
            int dataIdx = index + 1;
            if (dataIdx > params.size() - 1) {
                printError("No se ha encotrado el parametro");
            } else {
                String threshold = params.get(dataIdx);
                if (isNumeric(threshold)) {
                    SATVALUE = Double.parseDouble(threshold);
                    hasSaturation = true;
                    System.out.println("Se esta aplicando el filtro de saturación");
                } else {
                    printError("Parametro erroneo");

                }
            }
        }
    }

    /**
     * Check del modo batch
     */
    private void setBatch() {
        hasBatch = true;
        System.out.println("No se mostrará ningun resultado (--batch/-b)");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // No implementamos JCommander debido a que JavaFx incorpora su parser propio de argumentos
        Parameters parameters = getParameters();
        if (parameters.getRaw().isEmpty()) {
            System.out.println("Faltan parametros, use -h o --help para saber que parametros usar");
            System.exit(0);
        }
        // Recogemos los parametros
        params = parameters.getRaw();

        // Comprobamos todos los parametros y ponemos los que nos han indicado
        if (params.contains("-h") || params.contains("--help")) {
            showHelp();
        }
        if (params.contains("-i") || params.contains("--input")) {
            setInput();
        }
        // Solo el input es necesario para trabajar
        if (!hasInput) {
            printError("--input es un parametro necesario.");
        }

        if (params.contains("-o") || params.contains("--output")) {
            setOutput();
        }

        if (params.contains("-e") || params.contains("--encode")) {
            setEncode();
        }

        if (params.contains("-d") || params.contains("--decode")) {
            setDecode();
        }

        if (params.contains("--fps")) {
            setFPS();
        }

        if (params.contains("--binarization")) {
            setBinarization();
        }

        if (params.contains("--edgeDetection")) {
            setEdgeDetection();
        }

        if (params.contains("--saturacion")) {
            setSaturation();
        }

        if (params.contains("--negative")) {
            setNegative();
        }

        if (params.contains("--averaging")) {
            setAveraging();
        }

        if (params.contains("--nTiles")) {
            setNTiles();
        }

        if (params.contains("--seekRange")) {
            setSeekRange();
        }

        if (params.contains("--GOP")) {
            setGop();
        }

        if (params.contains("--quality")) {
            setQuality();
        }

        if (params.contains("--batch") || params.contains("-b")) {
            setBatch();
        }

        // Ponemos el estado
        if (hasEncode && !hasDecode)
            status = 1;
        if (!hasEncode && hasDecode)
            status = 2;
        if (hasEncode && hasDecode)
            status = 3;

        // Iniciamos la app
        initApp(primaryStage);
    }

    /**
     * Pone el valor de los Tiles
     */
    private void setNTiles() {
        int index = params.indexOf("--nTiles");
        if (index == -1) {
            printError("No se ha encotrado el parametro");
        } else {
            // Buscamos el parametro
            int dataIdx = index + 1;
            if (dataIdx > params.size() - 1) {
                printError("No se ha encotrado el parametro");
            } else {
                String value = params.get(dataIdx);
                // Miramos si tiene dos valores
                String[] tileData = value.split(",");
                // Si tiene un solo valor, ponemos el mismo para las X y Y
                if (tileData.length == 1) {
                    if (isNumeric(value)) {
                        NTILESY = Integer.parseInt(value);
                        NTILESX = NTILESY;
                    } else {
                        printError("Parametro erroneo");
                    }
                } else if (tileData.length == 2) {
                    // En caso de ser dos valores, ponemos el primero en X y el siguiente en Y
                    if (isNumeric(tileData[0]) && isNumeric(tileData[1])) {
                        NTILESX = Integer.parseInt(tileData[0]);
                        NTILESY = Integer.parseInt(tileData[1]);
                    } else {
                        // Si no son ints, damos error
                        printError("Parametro erroneo");
                    }
                } else {
                    // si tiene mas de dos valores damos error
                    printError("Parametro erroneo");
                }
            }
        }
    }

    /**
     * Pone el valor de rango
     */
    private void setSeekRange() {
        int index = params.indexOf("--seekRange");
        if (index == -1) {
            printError("No se ha encotrado el parametro");
        } else {
            // Buscamos el parametro
            int dataIdx = index + 1;
            if (dataIdx > params.size() - 1) {
                printError("No se ha encotrado el parametro");
            } else {
                String value = params.get(dataIdx);
                if (isNumeric(value)) {
                    SEEKRANGE = Integer.parseInt(value);

                    System.out.println("seekRange: " + SEEKRANGE);
                } else {
                    printError("Parametro erroneo");

                }
            }
        }
    }

    /**
     * Pone el valor de GOP
     */
    private void setGop() {
        int index = params.indexOf("--GOP");
        if (index == -1) {
            printError("No se ha encotrado el parametro");
        } else {
            // Buscamos el parametro
            int dataIdx = index + 1;
            if (dataIdx > params.size() - 1) {
                printError("No se ha encotrado el parametro");
            } else {
                String value = params.get(dataIdx);
                if (isNumeric(value)) {
                    GOP = Integer.parseInt(value);
                    System.out.println("GOP: " + GOP);
                } else {
                    printError("Parametro erroneo");

                }
            }
        }
    }

    /**
     * Ajusta la calidad
     */
    private void setQuality() {
        int index = params.indexOf("--quality");
        if (index == -1) {
            printError("No se ha encotrado el parametro");
        } else {
            // Buscamos el parametro
            int dataIdx = index + 1;
            if (dataIdx > params.size() - 1) {
                printError("No se ha encotrado el parametro");
            } else {
                String value = params.get(dataIdx);
                if (isNumeric(value)) {
                    QUALITY = Double.parseDouble(value);
                    System.out.println("Quality: " + QUALITY);
                } else {
                    printError("Parametro erroneo");

                }
            }
        }
    }


    public void initApp(Stage primaryStage) throws Exception {
        // Cargamos la vista
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/imageview.fxml"));
        Parent root = loader.load();
        // Aplicamos la escena
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        //Al hacer click a la x se cierra el programa
        primaryStage.setOnCloseRequest(e -> {
            System.out.println("exit");
            System.exit(0);
        });
        // Ponemos titulo
        primaryStage.setTitle("(´-﹏-`；)");
        primaryStage.show();
    }

    public static void main(String... args) {
        launch(args);
        // launch(args);
    }
}
