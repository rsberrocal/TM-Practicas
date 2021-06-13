import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class Main extends Application {

    public static String input;
    public static int binarisationValue = 0;
    public static int avaragingValue = 0;
    public List<String> params;
    private ArrayList<BufferedImage> raw;

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

    public static String output = "output.zip";

    public static int FPS = 24;

    final static int WIDTH = 600;
    final static int HEIGHT = 400;

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
        System.out.println("--nTiles {value}");
        System.out.println("Numero de teselas en la cual dividir la imagen.");
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

    public void setInput() throws Exception {
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
            // Miramos si es un zip
            if (isZip(data)) {
                input = params.get(dataIdx);
                hasInput = true;
            } else {
                printError("El parametro no es correcto");
            }
        } else {
            printError("El parametro no es correcto");
        }
    }

    private boolean isZip(String zip) {
        Pattern pattern = Pattern.compile(".*\\.zip$");
        return pattern.matcher(zip).matches();
    }

    public void printError(String error) {
        System.err.println(error);
        System.out.println("Use -h o --help para obtener mas informacion");
        System.exit(1);
    }

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

    // comentar
    public static ArrayList<BufferedImage> extractArrayBImages(File zipFile) throws Exception {
        ZipFile zip = null;
        ArrayList<BufferedImage> images = new ArrayList<>();
        try {
            zip = new ZipFile(zipFile);
        } catch (ZipException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Enumeration<? extends ZipEntry> zipComponent = zip.entries();
        while (zipComponent.hasMoreElements()) {
            ZipEntry next = zipComponent.nextElement();
            InputStream is = null;
            try {
                is = zip.getInputStream(next);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            ImageInputStream iis = null;
            try {
                iis = ImageIO.createImageInputStream(is);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            try {
                BufferedImage bufferedImg = ImageIO.read(iis);
                images.add(bufferedImg);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        zip.close();
        return images;
    }

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
                // check fps is int
                String frames = params.get(dataIdx);
                if (isNumeric(frames)) {
                    FPS = Integer.parseInt(frames);
                } else {
                    printError("Parametro erroneo");
                }
            }
        }
    }

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
        }

    }

    public void setNegative() {
        this.hasNegative = true;
    }

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
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // No implementamos JCommander debido a que JavaFx incorpora su parser propio de argumentos
        Parameters parameters = getParameters();
        if (parameters.getRaw().isEmpty()) {
            System.out.println("Faltan parametros, use -h o --help para saber que parametros usar");
            System.exit(0);
        }
        params = parameters.getRaw();
        if (params.contains("-h") || params.contains("--help")) {
            showHelp();
        }
        if (params.contains("-i") || params.contains("--input")) {
            setInput();
        }
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

        if (params.contains("--negative")) {
            setNegative();
        }

        if (params.contains("--averaging")) {
            setAveraging();
        }

        if (hasEncode && !hasDecode)
            status = 1;
        if (!hasEncode && hasDecode)
            status = 2;
        if (hasEncode && hasDecode)
            status = 3;

        // Faltaria poner el resto de parametros

        initApp(primaryStage);
    }

    public void initApp(Stage primaryStage) throws Exception {

        //Parent root = FXMLLoader.load(getClass().getResource("view/imageview.fxml"));
        // FXMLLoader loader = new FXMLLoader(getClass().getResource("view/main.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/imageview.fxml"));
        Parent root = loader.load();
        // GridPane pane = loader.load();
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("TITULO");
        primaryStage.show();
    }

    public static void main(String... args) {
        launch(args);
        // launch(args);
    }
}
